/**
 * Auto.js脚本 - 自动刷学时
 * 
 * 使用方法：
 * 1. 在手机上安装Auto.js应用
 * 2. 将本脚本导入Auto.js
 * 3. 授予Auto.js无障碍权限
 * 4. 运行脚本
 * 
 * Auto.js下载：https://github.com/hyb1996/Auto.js
 * 或搜索"Auto.js"应用商店
 */

// 配置参数
const CONFIG = {
    BUTTON_TEXT: "开始学习",        // 按钮文本
    CHECK_INTERVAL: 500,            // 检查间隔（毫秒）
    CLICK_DELAY: 1000,              // 点击后延迟（毫秒）
    MAX_RETRY: 3                    // 最大重试次数
};

let isRunning = false;
let lastClickTime = 0;

// 主函数
function main() {
    console.log("自动刷学时脚本启动");
    console.log("按音量下键停止脚本");
    
    // 检查无障碍服务
    if (!auto.service) {
        toast("请先开启Auto.js的无障碍服务！");
        exit();
    }
    
    // 监听停止按键
    events.observeKey();
    events.onKeyDown("volume_down", function() {
        console.log("停止脚本");
        isRunning = false;
        exit();
    });
    
    isRunning = true;
    
    // 开始循环检查
    while (isRunning) {
        try {
            checkAndClick();
            sleep(CONFIG.CHECK_INTERVAL);
        } catch (e) {
            console.error("错误: " + e);
            sleep(CONFIG.CHECK_INTERVAL);
        }
    }
}

/**
 * 检查并点击"开始学习"按钮
 */
function checkAndClick() {
    // 查找包含"开始学习"文本的控件
    var button = textContains(CONFIG.BUTTON_TEXT).findOne(1000);
    
    if (button) {
        var currentTime = Date.now();
        
        // 防止频繁点击
        if (currentTime - lastClickTime < CONFIG.CLICK_DELAY * 2) {
            return;
        }
        
        console.log("找到'开始学习'按钮，准备点击");
        
        // 尝试点击
        var clicked = false;
        for (var i = 0; i < CONFIG.MAX_RETRY; i++) {
            try {
                if (button.click()) {
                    clicked = true;
                    break;
                }
            } catch (e) {
                console.log("点击失败，重试 " + (i + 1));
                sleep(200);
            }
        }
        
        if (clicked) {
            lastClickTime = currentTime;
            console.log("成功点击'开始学习'按钮");
            toast("已点击开始学习");
            
            // 延迟后尝试切换到下一个视频
            sleep(CONFIG.CLICK_DELAY);
            findAndClickNextVideo();
        } else {
            console.log("点击失败");
        }
    }
}

/**
 * 查找并点击下一个视频
 */
function findAndClickNextVideo() {
    console.log("尝试切换到下一个视频");
    
    // 方法1: 查找播放按钮
    var playButton = textContains("播放").findOne(2000);
    if (playButton && playButton.clickable()) {
        playButton.click();
        console.log("点击播放按钮");
        return;
    }
    
    // 方法2: 查找继续按钮
    var continueButton = textContains("继续").findOne(2000);
    if (continueButton && continueButton.clickable()) {
        continueButton.click();
        console.log("点击继续按钮");
        return;
    }
    
    // 方法3: 查找下一个视频的列表项
    // 这里可以根据实际UI结构调整
    var videoList = className("android.widget.ListView").findOne(2000);
    if (videoList) {
        var items = videoList.children();
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            // 查找未完成的视频项（可以根据实际UI调整判断条件）
            if (item.clickable() && !item.desc().includes("已完成")) {
                item.click();
                console.log("点击下一个视频项");
                return;
            }
        }
    }
    
    console.log("未找到下一个视频");
}

// 启动脚本
main();

