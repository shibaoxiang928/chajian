package com.xuexi.autoplay;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class AutoPlayService extends AccessibilityService {

    private static final String TAG = "AutoPlayService";
    private static final String BUTTON_TEXT_START_LEARNING = "开始学习";
    private static final long CLICK_DELAY_MS = 500; // 点击延迟，避免过快操作
    
    private Handler handler = new Handler(Looper.getMainLooper());
    private long lastClickTime = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) {
            return;
        }

        int eventType = event.getEventType();
        Log.d(TAG, "Event type: " + eventType);

        // 监听窗口内容变化和窗口状态变化
        if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            
            // 延迟执行，等待UI完全加载
            handler.postDelayed(() -> {
                checkAndClickStartLearningButton();
            }, 300);
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Accessibility service connected");
    }

    /**
     * 检查并点击"开始学习"按钮
     */
    private void checkAndClickStartLearningButton() {
        // 增加等待时间，确保弹窗完全加载
        handler.postDelayed(() -> {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode == null) {
                Log.d(TAG, "Root node is null");
                return;
            }

            try {
                Log.d(TAG, "开始查找'开始学习'按钮");
                
                // 策略1: 直接查找包含"开始学习"文本的节点（支持弹窗）
                List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(BUTTON_TEXT_START_LEARNING);
                
                if (nodes != null && !nodes.isEmpty()) {
                    Log.d(TAG, "找到包含'开始学习'文本的节点数量: " + nodes.size());
                    for (AccessibilityNodeInfo node : nodes) {
                        // 获取节点的详细信息用于调试
                        Log.d(TAG, "节点文本: " + node.getText() + ", 可点击: " + node.isClickable() + ", 父节点可点击: " + (node.getParent() != null && node.getParent().isClickable()));
                        
                        // 检查节点是否可点击
                        if (node.isClickable() || node.getParent() != null && node.getParent().isClickable()) {
                            AccessibilityNodeInfo clickableNode = node.isClickable() ? node : node.getParent();
                            
                            if (clickableNode != null) {
                                // 防止频繁点击
                                long currentTime = System.currentTimeMillis();
                                if (currentTime - lastClickTime < CLICK_DELAY_MS * 2) {
                                    Log.d(TAG, "点击过于频繁，跳过");
                                    continue;
                                }
                                
                                Log.d(TAG, "找到可点击的'开始学习'按钮，尝试点击");
                                
                                // 尝试点击
                                boolean clicked = performClick(clickableNode);
                                
                                if (clicked) {
                                    lastClickTime = currentTime;
                                    Log.d(TAG, "成功点击'开始学习'按钮");
                                    
                                    // 点击后延迟一段时间，等待下一个视频加载
                                    handler.postDelayed(() -> {
                                        findAndClickNextVideo();
                                    }, 1000);
                                    
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "未找到包含'开始学习'文本的节点，尝试其他查找策略");
                    
                    // 策略2: 查找弹窗容器，然后在弹窗内查找按钮
                    findInDialog(rootNode);
                    
                    // 策略3: 通过类名查找按钮
                    findButtonByClassName(rootNode);
                    
                    // 策略4: 递归查找所有可点击节点
                    findClickableNodes(rootNode);
                }
            } catch (Exception e) {
                Log.e(TAG, "checkAndClickStartLearningButton 方法出错", e);
            } finally {
                rootNode.recycle();
            }
        }, 500); // 增加延迟，确保弹窗完全显示
    }
    
    /**
     * 在弹窗中查找并点击"开始学习"按钮
     */
    private void findInDialog(AccessibilityNodeInfo rootNode) {
        try {
            // 查找可能是弹窗的容器（通常是Dialog或PopupWindow）
            List<AccessibilityNodeInfo> dialogNodes = new ArrayList<>();
            
            // 遍历所有节点，寻找弹窗特征
            traverseNodes(rootNode, node -> {
                // 弹窗通常有较高的层级和特定的类名
                String className = node.getClassName().toString();
                if (className.contains("Dialog") || className.contains("PopupWindow") || 
                    className.contains("FrameLayout") || className.contains("LinearLayout")) {
                    dialogNodes.add(node);
                }
            });
            
            Log.d(TAG, "找到可能是弹窗的节点数量: " + dialogNodes.size());
            
            // 在每个弹窗中查找"开始学习"按钮
            for (AccessibilityNodeInfo dialog : dialogNodes) {
                List<AccessibilityNodeInfo> buttonsInDialog = dialog.findAccessibilityNodeInfosByText(BUTTON_TEXT_START_LEARNING);
                if (buttonsInDialog != null && !buttonsInDialog.isEmpty()) {
                    Log.d(TAG, "在弹窗中找到'开始学习'按钮");
                    for (AccessibilityNodeInfo button : buttonsInDialog) {
                        performClick(button);
                        break;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "findInDialog 方法出错", e);
        }
    }
    
    /**
     * 遍历所有节点并执行回调
     */
    private void traverseNodes(AccessibilityNodeInfo node, NodeCallback callback) {
        if (node == null) {
            return;
        }
        
        try {
            callback.onNode(node);
            
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    traverseNodes(child, callback);
                    child.recycle();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "遍历节点出错", e);
        }
    }
    
    /**
     * 节点回调接口
     */
    private interface NodeCallback {
        void onNode(AccessibilityNodeInfo node);
    }

    /**
     * 通过类名查找按钮（备用方法）
     */
    private void findButtonByClassName(AccessibilityNodeInfo rootNode) {
        try {
            // 查找所有可点击的按钮
            List<AccessibilityNodeInfo> buttons = rootNode.findAccessibilityNodeInfosByViewId("android:id/button1");
            
            if (buttons == null || buttons.isEmpty()) {
                // 尝试递归查找所有可点击节点
                findClickableNodes(rootNode);
            } else {
                for (AccessibilityNodeInfo button : buttons) {
                    if (button.getText() != null && button.getText().toString().contains("开始")) {
                        performClick(button);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in findButtonByClassName", e);
        }
    }

    /**
     * 递归查找可点击的节点
     */
    private void findClickableNodes(AccessibilityNodeInfo node) {
        if (node == null) {
            return;
        }

        try {
            if (node.isClickable()) {
                CharSequence text = node.getText();
                CharSequence contentDescription = node.getContentDescription();
                
                String textStr = text != null ? text.toString() : "";
                String descStr = contentDescription != null ? contentDescription.toString() : "";
                
                if (textStr.contains("开始学习") || descStr.contains("开始学习") ||
                    textStr.contains("开始") || descStr.contains("开始")) {
                    Log.d(TAG, "Found clickable node with text: " + textStr);
                    performClick(node);
                    return;
                }
            }

            // 递归查找子节点
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    findClickableNodes(child);
                    child.recycle();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in findClickableNodes", e);
        }
    }

    /**
     * 执行点击操作
     */
    private boolean performClick(AccessibilityNodeInfo node) {
        if (node == null) {
            return false;
        }

        try {
            // 方法1: 直接点击
            if (node.isClickable()) {
                return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

            // 方法2: 如果节点不可点击，尝试点击父节点
            AccessibilityNodeInfo parent = node.getParent();
            if (parent != null && parent.isClickable()) {
                boolean result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                parent.recycle();
                return result;
            }

            // 方法3: 使用手势点击（坐标点击）
            android.graphics.Rect bounds = new android.graphics.Rect();
            node.getBoundsInScreen(bounds);
            int centerX = bounds.centerX();
            int centerY = bounds.centerY();
            
            return performGestureClick(centerX, centerY);
        } catch (Exception e) {
            Log.e(TAG, "Error performing click", e);
            return false;
        }
    }

    /**
     * 使用手势在指定坐标执行点击
     */
    private boolean performGestureClick(int x, int y) {
        try {
            Path path = new Path();
            path.moveTo(x, y);
            
            GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(
                path, 0, 100);
            
            GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(stroke)
                .build();
            
            return dispatchGesture(gesture, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    Log.d(TAG, "Gesture click completed at (" + x + ", " + y + ")");
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    Log.d(TAG, "Gesture click cancelled");
                }
            }, null);
        } catch (Exception e) {
            Log.e(TAG, "Error in performGestureClick", e);
            return false;
        }
    }

    /**
     * 查找并点击下一个视频
     */
    private void findAndClickNextVideo() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            return;
        }

        try {
            // 查找视频播放相关的控件
            // 这里可以根据实际应用调整查找逻辑
            // 例如：查找播放按钮、下一个视频的列表项等
            
            // 示例：查找包含"播放"或"继续"的按钮
            List<AccessibilityNodeInfo> playNodes = rootNode.findAccessibilityNodeInfosByText("播放");
            if (playNodes != null && !playNodes.isEmpty()) {
                for (AccessibilityNodeInfo node : playNodes) {
                    if (node.isClickable()) {
                        performClick(node);
                        Log.d(TAG, "Clicked next video play button");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in findAndClickNextVideo", e);
        } finally {
            if (rootNode != null) {
                rootNode.recycle();
            }
        }
    }
}

