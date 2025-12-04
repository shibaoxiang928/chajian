# Frida脚本版本使用说明

## 优点

- ✅ 无需编译APK
- ✅ 无需安装应用
- ✅ 代码修改后立即生效
- ✅ 适合开发和调试

## 缺点

- ❌ 需要root权限或安装frida-server
- ❌ 需要电脑连接手机
- ❌ 每次使用都需要运行脚本

## 安装要求

### 1. 安装Frida

**Windows:**
```bash
pip install frida-tools
```

**Linux/Mac:**
```bash
pip3 install frida-tools
```

### 2. 在手机上安装frida-server

**有root权限：**
```bash
# 下载对应架构的frida-server
# https://github.com/frida/frida/releases
# 例如：frida-server-16.0.0-android-arm64

adb push frida-server /data/local/tmp/
adb shell "chmod 755 /data/local/tmp/frida-server"
adb shell "su -c '/data/local/tmp/frida-server &'"
```

**无root权限（需要rootless frida）：**
- 使用Magisk模块或Xposed模块
- 或使用frida-gadget（需要修改目标应用）

### 3. 修改脚本中的包名

编辑 `autoplay.js`，修改：
```javascript
const TARGET_PACKAGE = "com.example.xuexi"; // 改为实际的学习应用包名
```

## 使用方法

### 方法一：附加到运行中的应用

```bash
# 1. 先启动学习应用
# 2. 运行脚本
frida -U <应用包名> -l autoplay.js
```

### 方法二：启动应用并注入脚本

```bash
frida -U -f <应用包名> -l autoplay.js --no-pause
```

### 方法三：使用frida-trace监控

```bash
frida-trace -U <应用包名> -i "android.view.accessibility.*"
```

## 调试

查看日志：
```bash
frida -U <应用包名> -l autoplay.js --no-pause | tee log.txt
```

## 注意事项

1. 确保手机和电脑在同一网络，或使用USB连接
2. 首次使用需要信任frida-server
3. 如果应用有反调试，可能需要绕过检测

