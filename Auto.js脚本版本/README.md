# Auto.js脚本版本使用说明

## 优点

- ✅ **最简单**：无需root，无需电脑
- ✅ **直接在手机上运行**
- ✅ **无需编译**
- ✅ **易于修改和调试**

## 安装步骤

### 1. 安装Auto.js应用

**方法一：从GitHub下载**
- 访问：https://github.com/hyb1996/Auto.js
- 下载最新版本的APK
- 或搜索"Auto.js"在应用商店下载

**方法二：使用Auto.js Pro（付费版）**
- 功能更强大，支持更多API

### 2. 导入脚本

1. 打开Auto.js应用
2. 点击"+"创建新脚本
3. 将 `autoplay.js` 的内容复制粘贴进去
4. 保存脚本

### 3. 授予权限

1. 打开Auto.js设置
2. 开启"无障碍服务"
3. 在系统无障碍设置中找到"Auto.js"
4. 开启服务

### 4. 运行脚本

1. 在Auto.js中打开脚本
2. 点击"运行"按钮
3. 打开学习应用
4. 脚本会自动工作

## 使用方法

### 启动脚本

1. 打开Auto.js应用
2. 选择"自动刷学时"脚本
3. 点击运行按钮（▶️）

### 停止脚本

- 按**音量下键**停止脚本
- 或在Auto.js中点击停止按钮

## 自定义配置

编辑脚本中的 `CONFIG` 对象：

```javascript
const CONFIG = {
    BUTTON_TEXT: "开始学习",        // 修改按钮文本
    CHECK_INTERVAL: 500,            // 检查间隔（毫秒）
    CLICK_DELAY: 1000,              // 点击后延迟
    MAX_RETRY: 3                    // 最大重试次数
};
```

## 调试技巧

### 查看日志

1. 在Auto.js中运行脚本
2. 点击"日志"标签查看输出
3. 脚本会输出详细的执行信息

### 测试按钮查找

可以在脚本中添加测试代码：

```javascript
// 测试：打印所有可见文本
var allTexts = textMatches(".*").find();
allTexts.forEach(function(t) {
    console.log("文本: " + t.text());
});
```

### 手动定位按钮

如果按钮文本不同，可以：

1. 使用Auto.js的"布局分析"功能
2. 查看按钮的实际文本或ID
3. 修改脚本中的查找条件

## 常见问题

### Q: 脚本无法找到按钮？

A: 
1. 检查按钮文本是否正确
2. 使用布局分析查看实际UI结构
3. 调整查找条件（可以使用 `desc()`、`id()` 等）

### Q: 点击没有反应？

A:
1. 检查无障碍服务是否正常
2. 尝试增加延迟时间
3. 使用坐标点击（需要先获取按钮坐标）

### Q: 如何获取按钮坐标？

A: 在脚本中添加：

```javascript
var button = textContains("开始学习").findOne();
if (button) {
    var bounds = button.bounds();
    console.log("按钮坐标: " + bounds);
    // 使用坐标点击
    click(bounds.centerX(), bounds.centerY());
}
```

## 高级功能

### 添加通知提醒

```javascript
// 点击成功后发送通知
if (clicked) {
    engines.myEngine().notify("已点击开始学习", "继续学习中...");
}
```

### 记录日志到文件

```javascript
var logFile = "/sdcard/autoplay.log";
files.append(logFile, "点击时间: " + new Date() + "\n");
```

### 定时运行

使用Auto.js的定时任务功能，设置每天固定时间运行脚本。

## 注意事项

1. **保持屏幕常亮**：建议在系统设置中关闭自动锁屏
2. **保持应用在前台**：确保学习应用不被后台杀死
3. **网络连接**：确保网络稳定，避免视频加载失败
4. **遵守规则**：请遵守学习平台的使用规范

## 推荐方案

**Auto.js版本是最推荐的方案**，因为：
- 最简单易用
- 无需电脑
- 无需root
- 易于调试和修改

