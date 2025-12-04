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
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            Log.d(TAG, "Root node is null");
            return;
        }

        try {
            // 查找包含"开始学习"文本的节点
            List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(BUTTON_TEXT_START_LEARNING);
            
            if (nodes != null && !nodes.isEmpty()) {
                for (AccessibilityNodeInfo node : nodes) {
                    // 检查节点是否可点击
                    if (node.isClickable() || node.getParent() != null && node.getParent().isClickable()) {
                        AccessibilityNodeInfo clickableNode = node.isClickable() ? node : node.getParent();
                        
                        if (clickableNode != null) {
                            // 防止频繁点击
                            long currentTime = System.currentTimeMillis();
                            if (currentTime - lastClickTime < CLICK_DELAY_MS * 2) {
                                Log.d(TAG, "Click too frequent, skipping");
                                continue;
                            }
                            
                            Log.d(TAG, "Found '开始学习' button, attempting to click");
                            
                            // 尝试点击
                            boolean clicked = performClick(clickableNode);
                            
                            if (clicked) {
                                lastClickTime = currentTime;
                                Log.d(TAG, "Successfully clicked '开始学习' button");
                                
                                // 点击后延迟一段时间，等待下一个视频加载
                                handler.postDelayed(() -> {
                                    // 可以在这里添加切换到下一个视频的逻辑
                                    // 例如：查找下一个视频按钮并点击
                                    findAndClickNextVideo();
                                }, 1000);
                                
                                break;
                            }
                        }
                    }
                }
            } else {
                // 如果没找到文本，尝试通过其他方式查找按钮
                findButtonByClassName(rootNode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in checkAndClickStartLearningButton", e);
        } finally {
            rootNode.recycle();
        }
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

