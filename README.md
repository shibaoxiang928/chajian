# 自动刷学时插件

一个用于自动刷科目一学时的安卓无障碍服务插件。当视频播放完成后，自动检测弹窗并点击"开始学习"按钮，切换到下一个视频。

## 功能特点

- ✅ 自动检测视频播放完成后的弹窗
- ✅ 自动点击"开始学习"按钮
- ✅ 自动切换到下一个视频
- ✅ 无需root权限，使用无障碍服务实现
- ✅ 简单易用的设置界面

## 使用方法

### 1. 安装应用

将项目编译为APK并安装到手机上。

### 2. 启用无障碍服务

1. 打开应用
2. 点击"打开无障碍设置"按钮
3. 在无障碍设置列表中找到"自动刷学时插件"
4. 开启服务开关

### 3. 开始使用

1. 打开科目一学习应用
2. 开始播放视频
3. 当视频播放完成后，插件会自动检测弹窗并点击"开始学习"按钮
4. 自动切换到下一个视频继续学习

## 技术实现

### 核心组件

- **AutoPlayService**: 无障碍服务，负责监听UI变化并执行自动点击
- **MainActivity**: 主界面，用于启用和配置服务

### 工作原理

1. 无障碍服务监听窗口内容变化事件
2. 检测包含"开始学习"文本的按钮
3. 自动执行点击操作
4. 点击后延迟一段时间，等待下一个视频加载

### 适配说明

如果目标应用的按钮文本或UI结构不同，可以修改以下常量：

```java
// AutoPlayService.java
private static final String BUTTON_TEXT_START_LEARNING = "开始学习";
```

也可以调整查找逻辑，例如：
- 通过View ID查找
- 通过类名查找
- 通过坐标点击

## 开发环境

- Android Studio
- Min SDK: 24
- Target SDK: 34
- Java 8+

## 编译和安装

### 方法一：使用Android Studio（推荐）

1. 打开Android Studio
2. 选择"Open an Existing Project"
3. 选择本项目目录
4. 等待Gradle同步完成
5. 点击"Build" -> "Build Bundle(s) / APK(s)" -> "Build APK(s)"
6. 编译完成后，APK在 `app/build/outputs/apk/debug/app-debug.apk`

### 方法二：使用命令行（无需Android Studio）

详见 [编译指南-命令行.md](编译指南-命令行.md)

```bash
# Windows
.\gradlew.bat assembleDebug

# Linux/Mac
./gradlew assembleDebug

# 安装到设备
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 方法三：使用GitHub Actions在线编译（最简单）

1. 将项目上传到GitHub
2. 在GitHub仓库页面，点击"Actions"
3. 选择"Build APK"工作流
4. 点击"Run workflow"
5. 构建完成后下载APK

### 方法四：使用Auto.js脚本（无需编译，推荐！）

**最简单的方法**：使用Auto.js脚本版本，无需编译APK！

详见 [Auto.js脚本版本/README.md](Auto.js脚本版本/README.md)

### 方法五：使用Frida脚本（需要root）

适合开发和调试，详见 [frida脚本版本/README.md](frida脚本版本/README.md)

## 注意事项

1. **权限要求**: 需要授予无障碍服务权限
2. **兼容性**: 不同版本的学习应用UI可能不同，可能需要调整代码
3. **使用规范**: 请遵守学习平台的使用规范，本插件仅供学习交流使用

## 常见问题

### Q: 服务无法启动？
A: 请确保在无障碍设置中正确启用了服务，并授予了所有必要的权限。

### Q: 无法检测到按钮？
A: 可能是目标应用的UI结构不同，需要根据实际情况调整查找逻辑。

### Q: 点击后没有反应？
A: 检查日志输出，可能需要调整点击延迟时间或使用坐标点击方式。

## 许可证

本项目仅供学习交流使用。

