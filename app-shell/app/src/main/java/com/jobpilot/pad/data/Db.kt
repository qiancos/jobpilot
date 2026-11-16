package com.jobpilot.pad.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * 本地台账。
 *
 * v2 变更（对齐 PRD §10）：
 *   · 新增 score / greeting / application / company 四张表
 *   · job 增加 dedupe_key（跨采集源去重）与 source（web / app）
 *   · 冲突时合并字段：网页来的行没有薪资，App 来的行补上
 */
class Db private constructor(ctx: Context) :
    SQLiteOpenHelper(ctx.applicationContext, DB_NAME, null, VERSION) {

    companion object {
        private const val DB_NAME = "jobpilot.db"
        private const val VERSION = 2

        @Volatile
        private var inst: Db? = null

        fun get(ctx: Context): Db =
            inst ?: synchronized(this) {
                inst ?: Db(ctx).also { it.ensureDedupe() }
            }
    }

    // ---------------------------------------------------------------- 建表

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE job (
              id            TEXT PRIMARY KEY,
              title         TEXT NOT NULL,
              company       TEXT,
              salary        TEXT,
              area          TEXT,
              tags          TEXT,
              href          TEXT,
              keyword       TEXT,
              collected_at  INTEGER,
              selected      INTEGER DEFAULT 0,
              dedupe_key    TEXT,
              source        TEXT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_job_time ON job(collected_at DESC)")
        db.execSQL("CREATE INDEX idx_job_company ON job(company)")
        createV2Tables(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            runCatching { db.execSQL("ALTER TABLE job ADD COLUMN dedupe_key TEXT") }
            runCatching { db.execSQL("ALTER TABLE job ADD COLUMN source TEXT") }
            createV2Tables(db)
        }
    }

    private fun createV2Tables(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS score (
              job_id      TEXT PRIMARY KEY,
              total       INTEGER,
              d1          INTEGER, d2 INTEGER, d3 INTEGER, d4 INTEGER, d5 INTEGER,
              strengths   TEXT, gaps TEXT, advice TEXT,
              jd_hit      TEXT, jd_miss TEXT,
              model       TEXT, created_at INTEGER
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS greeting (
              id            INTEGER PRIMARY KEY AUTOINCREMENT,
              job_id        TEXT,
              variant       TEXT,
              text          TEXT,
              marker        TEXT,
              gate_pass     INTEGER,
              gate_fails    TEXT,
              quality_score INTEGER,
              created_at    INTEGER
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS application (
              id            INTEGER PRIMARY KEY AUTOINCREMENT,
              job_id        TEXT,
              company       TEXT,
              company_norm  TEXT,
              greeting_id   INTEGER,
              batch_id      TEXT,
              state         TEXT,
              plan_at       INTEGER,
              clicked_at    INTEGER,
              bound_at      INTEGER,
              delivered_at  INTEGER,
              verify_method TEXT,
              attempts      INTEGER DEFAULT 0,
              error_code    TEXT,
              error_detail  TEXT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_app_job ON application(job_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_app_state ON application(state)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS company (
              name_norm        TEXT PRIMARY KEY,
              raw_names        TEXT,
              first_contact_at INTEGER,
              last_state       TEXT,
              blacklisted      INTEGER DEFAULT 0,
              note             TEXT
            )
            """.trimIndent()
        )
    }

    // ---------------------------------------------------------------- 去重键回填

    /** 老库升级后补 dedupe_key、清理重复、建唯一索引。幂等。 */
    fun ensureDedupe() {
        val db = writableDatabase
        val missing = db.rawQuery(
            "SELECT COUNT(*) FROM job WHERE dedupe_key IS NULL OR dedupe_key = ''", null
        ).use { if (it.moveToFirst()) it.getInt(0) else 0 }

        if (missing > 0) {
            db.beginTransaction()
            try {
                val rows = ArrayList<Triple<String, String, String>>()
                db.rawQuery("SELECT id, IFNULL(company,''), IFNULL(title,'') FROM job", null).use { c ->
                    while (c.moveToNext()) rows.add(Triple(c.getString(0), c.getString(1), c.getString(2)))
                }
                for ((id, company, title) in rows) {
                    val cv = ContentValues().apply { put("dedupe_key", Keys.dedupe(company, title)) }
                    db.update("job", cv, "id = ?", arrayOf(id))
                }
                // 同一 dedupe_key 只留一条（优先保留字段更全的：有薪资 > 无薪资）
                db.execSQL(
                    """
                    DELETE FROM job WHERE rowid NOT IN (
                      SELECT rowid FROM (
                        SELECT rowid,
                               ROW_NUMBER() OVER (
                                 PARTITION BY dedupe_key
                                 ORDER BY CASE WHEN IFNULL(salary,'')='' THEN 1 ELSE 0 END,
                                          collected_at DESC
                               ) AS rn
                        FROM job
                      ) WHERE rn = 1
                    )
                    """.trimIndent()
                )
                db.setTransactionSuccessful()
            } catch (t: Throwable) {
                // 迁移失败不影响启动，保留原数据
            } finally {
                db.endTransaction()
            }
        }
        runCatching {
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_job_dedupe ON job(dedupe_key)")
        }
    }

    // ---------------------------------------------------------------- 岗位

    /** 返回真正新增的条数；已存在的只做字段补齐 */
    fun upsertJobs(jobs: List<Job>): Int {
        if (jobs.isEmpty()) return 0
        val db = writableDatabase
        var inserted = 0
        db.beginTransaction()
        try {
            for (j in jobs) {
                val key = Keys.dedupe(j.company, j.title)
                val cv = ContentValues().apply {
                    put("id", j.id)
                    put("title", j.title)
                    put("company", j.company)
                    put("salary", j.salary)
                    put("area", j.area)
                    put("tags", j.tags)
                    put("href", j.href)
                    put("keyword", j.keyword)
                    put("collected_at", j.collectedAt)
                    put("selected", 0)
                    put("dedupe_key", key)
                    put("source", j.source)
                }
                val row = db.insertWithOnConflict("job", null, cv, SQLiteDatabase.CONFLICT_IGNORE)
                if (row != -1L) {
                    inserted++
                } else {
                    // 冲突：把空字段补上（典型场景——网页抓到没薪资，App 抓到了补进来）
                    db.execSQL(
                        """
                        UPDATE job SET
                          salary = CASE WHEN IFNULL(salary,'')='' THEN ? ELSE salary END,
                          area   = CASE WHEN IFNULL(area,'')=''   THEN ? ELSE area   END,
                          tags   = CASE WHEN IFNULL(tags,'')=''   THEN ? ELSE tags   END,
                          href   = CASE WHEN IFNULL(href,'')=''   THEN ? ELSE href   END,
                          source = CASE WHEN IFNULL(source,'')='' THEN ? ELSE source END
                        WHERE dedupe_key = ?
                        """.trimIndent(),
                        arrayOf(j.salary, j.area, j.tags, j.href, j.source, key)
                    )
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return inserted
    }

    fun listJobs(onlySelected: Boolean = false): List<Job> {
        val where = if (onlySelected) "WHERE selected = 1" else ""
        val out = ArrayList<Job>()
        readableDatabase.rawQuery(
            "SELECT id,title,company,salary,area,tags,href,keyword,collected_at,selected,IFNULL(source,'') " +
                "FROM job $where ORDER BY collected_at DESC, id", null
        ).use { c ->
            while (c.moveToNext()) {
                out.add(
                    Job(
                        id = c.getString(0),
                        title = c.getString(1),
                        company = c.getString(2) ?: "",
                        salary = c.getString(3) ?: "",
                        area = c.getString(4) ?: "",
                        tags = c.getString(5) ?: "",
                        href = c.getString(6) ?: "",
                        keyword = c.getString(7) ?: "",
                        collectedAt = c.getLong(8),
                        selected = c.getInt(9) == 1,
                        source = c.getString(10) ?: "",
                    )
                )
            }
        }
        return out
    }

    fun setSelected(id: String, selected: Boolean) {
        val cv = ContentValues().apply { put("selected", if (selected) 1 else 0) }
        writableDatabase.update("job", cv, "id = ?", arrayOf(id))
    }

    fun selectAll(selected: Boolean) {
        val cv = ContentValues().apply { put("selected", if (selected) 1 else 0) }
        writableDatabase.update("job", cv, null, null)
    }

    fun count(): Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM job", null).use {
        if (it.moveToFirst()) it.getInt(0) else 0
    }

    fun countSelected(): Int =
        readableDatabase.rawQuery("SELECT COUNT(*) FROM job WHERE selected = 1", null).use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }

    /** 今天采集到的条数 */
    fun countCollectedToday(): Int {
        val start = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        return readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM job WHERE collected_at >= ?", arrayOf(start.toString())
        ).use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    fun clear() {
        writableDatabase.delete("job", null, null)
    }

    // ---------------------------------------------------------------- 投递台账

    fun countApplications(): Int =
        readableDatabase.rawQuery("SELECT COUNT(*) FROM application", null).use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }

    fun countDelivered(): Int =
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM application WHERE state = 'delivered' OR state = 'verified'", null
        ).use { if (it.moveToFirst()) it.getInt(0) else 0 }

    fun countReplied(): Int =
        readableDatabase.rawQuery("SELECT COUNT(*) FROM application WHERE state = 'replied'", null)
            .use { if (it.moveToFirst()) it.getInt(0) else 0 }

    /** 今日已送达条数（配额用） */
    fun countDeliveredToday(): Int {
        val start = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        return readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM application WHERE state = 'delivered' AND delivered_at >= ?",
            arrayOf(start.toString())
        ).use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    /** 记一条投递台账。level: none / clicked / bound / delivered */
    fun recordDelivery(job: Job, level: String, detail: String?): Long {
        val now = System.currentTimeMillis()
        val state = when (level) {
            "delivered" -> "delivered"
            "bound" -> "bound"
            "clicked" -> "clicked"
            else -> "failed"
        }
        val cv = ContentValues().apply {
            put("job_id", job.id)
            put("company", job.company)
            put("company_norm", Keys.normCompany(job.company))
            put("batch_id", java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.CHINA).format(java.util.Date()))
            put("state", state)
            put("plan_at", now)
            if (level != "none") put("clicked_at", now)
            if (level == "bound" || level == "delivered") put("bound_at", now)
            if (level == "delivered") put("delivered_at", now)
            put("verify_method", "text_fingerprint")
            if (state == "failed") put("error_code", level)
            put("error_detail", detail)
        }
        return writableDatabase.insert("application", null, cv)
    }
}
