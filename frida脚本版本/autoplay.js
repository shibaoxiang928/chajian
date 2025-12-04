/**
 * Frida脚本 - 自动刷学时
 * 使用方法：
 * 1. 手机需要root或安装frida-server
 * 2. 在电脑上运行：frida -U -f <包名> -l autoplay.js --no-pause
 * 
 * 或者使用frida-tools：
 * frida -U <包名> -l autoplay.js
 */

// 目标应用的包名（需要根据实际应用修改）
const TARGET_PACKAGE = "com.example.xuexi"; // 修改为实际的学习应用包名

// 按钮文本
const BUTTON_TEXT = "开始学习";

// 延迟时间（毫秒）
const CLICK_DELAY = 500;

let lastClickTime = 0;

// Hook AccessibilityService
Java.perform(function() {
    console.log("[*] 开始Hook无障碍服务...");
    
    try {
        // Hook AccessibilityNodeInfo的performAction方法
        var AccessibilityNodeInfo = Java.use("android.view.accessibility.AccessibilityNodeInfo");
        
        AccessibilityNodeInfo.performAction.implementation = function(action) {
            var result = this.performAction(action);
            
            // 如果是点击操作
            if (action === 16) { // ACTION_CLICK = 16
                var text = this.getText();
                var contentDesc = this.getContentDescription();
                
                if (text && text.toString().includes(BUTTON_TEXT)) {
                    console.log("[+] 检测到'开始学习'按钮点击");
                    lastClickTime = Date.now();
                }
            }
            
            return result;
        };
        
        // Hook AccessibilityService的onAccessibilityEvent
        var AccessibilityService = Java.use("android.accessibilityservice.AccessibilityService");
        
        AccessibilityService.onAccessibilityEvent.implementation = function(event) {
            this.onAccessibilityEvent(event);
            
            if (event) {
                var eventType = event.getEventType();
                
                // 监听窗口内容变化
                if (eventType === 2048 || eventType === 32) { // TYPE_WINDOW_CONTENT_CHANGED = 2048, TYPE_WINDOW_STATE_CHANGED = 32
                    setTimeout(function() {
                        clickStartLearningButton();
                    }, 300);
                }
            }
        };
        
        console.log("[+] Hook成功！");
    } catch (e) {
        console.log("[-] Hook失败: " + e);
    }
});

// 查找并点击"开始学习"按钮
function clickStartLearningButton() {
    Java.perform(function() {
        try {
            var AccessibilityService = Java.use("android.accessibilityservice.AccessibilityService");
            var service = AccessibilityService.$new();
            
            // 获取根节点
            var rootNode = service.getRootInActiveWindow();
            if (!rootNode) {
                return;
            }
            
            // 查找包含"开始学习"的节点
            var nodes = rootNode.findAccessibilityNodeInfosByText(BUTTON_TEXT);
            
            if (nodes && nodes.size() > 0) {
                var iterator = nodes.iterator();
                while (iterator.hasNext()) {
                    var node = iterator.next();
                    
                    if (node.isClickable()) {
                        var currentTime = Date.now();
                        if (currentTime - lastClickTime < CLICK_DELAY * 2) {
                            continue;
                        }
                        
                        console.log("[+] 找到'开始学习'按钮，执行点击");
                        node.performAction(16); // ACTION_CLICK
                        lastClickTime = currentTime;
                        
                        // 延迟后切换到下一个视频
                        setTimeout(function() {
                            findAndClickNextVideo();
                        }, 1000);
                        
                        break;
                    }
                }
            }
            
            rootNode.recycle();
        } catch (e) {
            console.log("[-] 点击失败: " + e);
        }
    });
}

// 查找并点击下一个视频
function findAndClickNextVideo() {
    Java.perform(function() {
        try {
            var AccessibilityService = Java.use("android.accessibilityservice.AccessibilityService");
            var service = AccessibilityService.$new();
            var rootNode = service.getRootInActiveWindow();
            
            if (!rootNode) {
                return;
            }
            
            // 查找播放按钮
            var playNodes = rootNode.findAccessibilityNodeInfosByText("播放");
            if (playNodes && playNodes.size() > 0) {
                var iterator = playNodes.iterator();
                while (iterator.hasNext()) {
                    var node = iterator.next();
                    if (node.isClickable()) {
                        console.log("[+] 点击下一个视频");
                        node.performAction(16);
                        break;
                    }
                }
            }
            
            rootNode.recycle();
        } catch (e) {
            console.log("[-] 切换视频失败: " + e);
        }
    });
}

console.log("[*] Frida脚本加载完成！");
console.log("[*] 等待目标应用启动...");

