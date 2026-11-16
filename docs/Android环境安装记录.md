# Android 工具链安装记录

安装时间：2026-11-16 · 目标盘：**D:**（除 JDK，见下方说明）
安装完成后 `D:\Android` 合计 **3.9 GB**（SDK 405 MB + Gradle 142 MB + Android Studio 3.4 GB），D 盘剩余 320 GB 以上。

---

## 一、装在哪

| 组件 | 版本 | 路径 | 大小 | 干什么用 |
|---|---|---|---|---|
| Android SDK 根 | — | `D:\Android\Sdk` | — | `ANDROID_HOME` 指向这里 |
| cmdline-tools | 19.0 | `D:\Android\Sdk\cmdline-tools\latest` | 157 MB | `sdkmanager` / `avdmanager` |
| platform-tools | 37.0.1 | `D:\Android\Sdk\platform-tools` | 17 MB | `adb`（连平板、装 APK、看日志） |
| platforms;android-34 | API 34 | `D:\Android\Sdk\platforms\android-34` | 95 MB | 编译目标（Android 14） |
| build-tools | 34.0.0 | `D:\Android\Sdk\build-tools\34.0.0` | 137 MB | aapt2 / d8 / apksigner |
| Gradle | 8.7 | `D:\Android\gradle-8.7` | 142 MB | 构建工具 |
| Gradle 缓存 | 空 | `D:\Android\gradle-home` | 0 MB | 依赖缓存，**以后会长到 1–3 GB** |
| JDK | 17.0.20.1 (Microsoft) | `C:\Users\32853\.jdks\ms-17.0.20.1` | — | **原有的，没动** |

> **为什么 JDK 还在 C 盘**：这是你机器上原本就有的 JDK（IntelliJ 装的），Path、JAVA_HOME、IDE 都指着它。
> 挪走大概率会连带破坏 IntelliJ 配置，收益（约 300 MB）不值这个风险。如果你坚持要挪，我另开一轮做迁移 + 改 IDE 配置。

---

## 二、环境变量（已写入用户级，重开终端生效）

| 变量 | 值 |
|---|---|
| `ANDROID_HOME` | `D:\Android\Sdk` |
| `ANDROID_SDK_ROOT` | `D:\Android\Sdk` |
| `GRADLE_USER_HOME` | `D:\Android\gradle-home` |
| `Path` 追加 | `D:\Android\Sdk\platform-tools`<br>`D:\Android\Sdk\cmdline-tools\latest\bin`<br>`D:\Android\gradle-8.7\bin` |

> 设 `GRADLE_USER_HOME` 是关键一步：Gradle 默认把依赖缓存放 `C:\Users\你\.gradle`，
> 一个 Android 项目轻易几百 MB 到几 GB。指到 D 盘就一劳永逸。

---

## 三、验证结果

```
sdkmanager --list_installed
  build-tools;34.0.0     34.0.0    build-tools\34.0.0
  platform-tools         37.0.1    platform-tools
  platforms;android-34   3         platforms\android-34

adb version          → Android Debug Bridge version 1.0.41 / 37.0.1
gradle --version     → Gradle 8.7
javac -version       → javac 17.0.20.1
```

依赖源连通性（HEAD 请求耗时）：

| 源 | 状态 | 耗时 |
|---|---|---|
| Google Maven（AGP 主源） | 200 | 234 ms |
| Maven Central | 200 | 708 ms |
| 阿里云镜像 | 200 | 143 ms |

**结论：直连够快，暂不配镜像。** 以后构建如果卡在下载，再加 `D:\Android\gradle-home\init.gradle`：

```groovy
// 只在构建明显变慢时才需要
allprojects {
    repositories {
        maven { url 'https://maven.aliyun.com/repository/public' }
        maven { url 'https://maven.aliyun.com/repository/google' }
        google()
        mavenCentral()
    }
}
```

---

## 四、常用命令

```powershell
# 看装了哪些组件
sdkmanager --list_installed

# 再装一个 API 版本（比如 35）
sdkmanager "platforms;android-35" "build-tools;35.0.0"

# 平板连上 USB 后
adb devices                 # 看设备
adb install -r app-debug.apk
adb logcat -s DSH          # 只看自己的日志

# 无线调试（Android 11+，不用插线）
adb pair 192.168.x.x:PORT   # 平板「无线调试」里拿配对码
adb connect 192.168.x.x:PORT
```

---

## 五、还没做的（下一步）

- [ ] **没验证过真实 Android 构建**——工具链装好了，但还没有项目跑过一次 `assembleDebug`。
      等壳 App 骨架搭起来，第一次构建就是真正的验收（要下 AGP + 依赖，约 300–500 MB）。
- [ ] 没装模拟器（不需要：你有真平板，模拟器占 5–10 GB 且跑不出真实风控环境）。
- [ ] 平板还没连过 adb（需要你插线并允许 USB 调试）。

---

## 六、Android Studio（2026.2.2）

| 项 | 值 |
|---|---|
| 版本 | **Rabbit 2 · 2026.2.2 Patch 1** |
| build | `AI-262.10968.63.2622.16840690` |
| 路径 | `D:\Android\android-studio` |
| 大小 | 3.4 GB |
| 启动 | `D:\Android\android-studio\bin\studio64.exe`（桌面已建快捷方式） |
| 自带运行时 | JBR **25.0.4**（只给 IDE 自己用，不用于构建） |
| 完整性 | SHA-256 与官网一致 `6813b0ce…7a7b917` ✔ |

> Windows 版官方现在**只发 zip，不再有 .exe 安装程序**。所以这里是解压版，不写注册表、不进"添加删除程序"，删目录即卸载。

### 配置和缓存也搬到 D 盘了

改的是 `bin\idea.properties`，否则默认会写 `C:\Users\你\.AndroidStudio2026.2.2`（用久了几个 GB）：

```properties
idea.config.path  = D:/Android/studio-data/config
idea.system.path  = D:/Android/studio-data/system
idea.plugins.path = D:/Android/studio-data/plugins
idea.log.path     = D:/Android/studio-data/log
```

### 第一次启动会问你两件事

1. **Import settings** → 选 **Do not import settings**（全新装，没有旧配置可导）
2. **SDK 位置** → 指到 `D:\Android\Sdk`

> 我没替你启动过 IDE（避免弹出向导窗口卡住），所以第一次向导还得你自己走一遍。

### ⚠️ 一个必踩的坑：JDK 版本错配

Studio 2026.2.2 自带的 JBR 是 **JDK 25**，而我装的是 **Gradle 8.7——不支持 JDK 25**。
如果新项目默认拿 JBR 25 当 Gradle JDK，第一次构建会报：

```
Unsupported class file major version 69
```

**两条都对的做法（任选其一）**：

- **推荐**：用 Studio 生成项目时自带的 Gradle wrapper（会自动下新版 Gradle，缓存进 `D:\Android\gradle-home`），
  然后 `Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK` 选 **17**
  （即 `C:\Users\32853\.jdks\ms-17.0.20.1`）。
- 或者：把项目的 Gradle 升到 8.10+ / 9.x 再配 JBR 25。

> 我装的 Gradle 8.7 主要给**命令行构建**兜底；日常开发让 wrapper 管版本更省事。

---

## 七、构建验证（2026-11-16 通过 ✔）

这是"装好了"和"能用"的分界线——跑过一次真实构建：

```powershell
$env:JAVA_HOME = 'C:\Users\32853\.jdks\ms-17.0.20.1'   # JDK 17，不是 JBR 25
$env:ANDROID_HOME = 'D:\Android\Sdk'
$env:GRADLE_USER_HOME = 'D:\Android\gradle-home'
cd E:\DSH\DSH-10-19\jobpilot\app-shell
D:\Android\gradle-8.7\bin\gradle.bat :app:assembleDebug --console=plain
```

| 结果 | 值 |
|---|---|
| 输出 | `BUILD SUCCESSFUL in 2m 21s` |
| 任务 | 35 个全部执行 |
| 产物 | `app-debug.apk` **781 KB** |
| 包名 | `com.jobpilot.pad` v0.1.0 |
| SDK | minSdk 26 / targetSdk 34 / compileSdk 34 |
| 启动 Activity | `com.jobpilot.pad.MainActivity` |
| 签名 | Android Debug（`CN=Android Debug`）✔ |
| 依赖缓存 | 首次构建后 `D:\Android\gradle-home` 增到 **772 MB**（在 D 盘，没污染 C） |

> 说明：这一版**刻意零第三方依赖**（纯 `android.app.Activity` + `WebView` + Kotlin），
> 目的是把工具链问题和依赖问题分开。Compose、AndroidX、Room 等留到下一版再加。

### 还没验证的

- ❌ **没在真机上装过**——`adb devices` 是空的（平板还没插线）。
  插上 USB、平板开「开发者选项 → USB 调试」后：
  ```powershell
  adb devices                         # 认到设备
  adb install -r jobpilot\dist\JobPilot-0.1.0-envtest.apk
  ```

---

## 八、要卸载/搬家时

```powershell
# 变量
[Environment]::SetEnvironmentVariable('ANDROID_HOME',$null,'User')

# 目录：直接删 D:\Android 即可，不写注册表、不装服务
Remove-Item 'D:\Android' -Recurse -Force
```

全部是绿色目录，删掉即干净。
