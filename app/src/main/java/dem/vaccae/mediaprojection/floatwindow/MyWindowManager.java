package dem.vaccae.mediaprojection.floatwindow;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;

/**
 * 作者: Vaccae
 * 邮箱: 3657447@qq.com
 * 作用:
 */
public class MyWindowManager {

    private static FloatWindowSmallView smallWindow;
    private static FloatWindowBigView bigWindow;
    private static WindowManager.LayoutParams smallWindowParams;
    private static WindowManager.LayoutParams bigWindowParams;
    private static WindowManager mWindowManager;
    private static ActivityManager mActivityManager;

    /**
     * 创建一个小悬浮窗。初始位置为屏幕的右部中间位置。
     *
     * @param context
     *            必须为应用程序的Context.
     */
    public static void createSmallWindow(Context context) {
        WindowManager windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if (smallWindow == null) {
            smallWindow = new FloatWindowSmallView(context);
            if (smallWindowParams == null) {
                smallWindowParams = new WindowManager.LayoutParams();
                //设置类型  android8.0后有变化此处进行修改
//                if (Build.VERSION.SDK_INT >= 26) {
//                    smallWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//                } else {
//                    smallWindowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
//                }
                if (Build.VERSION.SDK_INT < 19) {
                    smallWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    smallWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else {
                    smallWindowParams.type = WindowManager.LayoutParams.TYPE_TOAST;
                }

                smallWindowParams.format = PixelFormat.RGBA_8888;
                smallWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//                        | WindowManager.LayoutParams.FLAG_SECURE;
                smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
//                smallWindowParams.width = FloatWindowSmallView.viewWidth;
//                smallWindowParams.height = FloatWindowSmallView.viewHeight;

                smallWindowParams.width = screenWidth / 11;
                smallWindowParams.height = smallWindowParams.width;
                smallWindowParams.x = 0;
                smallWindowParams.y = 0;
            }
            smallWindow.setParams(smallWindowParams);
            windowManager.addView(smallWindow, smallWindowParams);
        }
    }

    //重新设置小图标的位置
    public static void setSmallWindowParamsPosition(WindowManager.LayoutParams params) {
        if (smallWindowParams != null) {
            smallWindowParams.x = params.x;
            smallWindowParams.y = params.y;
        }
        if (bigWindowParams != null) {
            bigWindowParams.x = params.x;
            bigWindowParams.y = params.y;
        }
    }


    /**
     * 将小悬浮窗从屏幕上移除。
     *
     * @param context
     *            必须为应用程序的Context.
     */
    public static void removeSmallWindow(Context context) {
        if (smallWindow != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(smallWindow);
            smallWindow = null;
        }
    }

    /**
     * 创建一个大悬浮窗。位置为屏幕正中间。
     *
     * @param context
     *            必须为应用程序的Context.
     */
    public static void createBigWindow(Context context) {
        WindowManager windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if (bigWindow == null) {
            bigWindow = new FloatWindowBigView(context);
            if (bigWindowParams == null) {
                bigWindowParams = new WindowManager.LayoutParams();
                bigWindowParams.x = smallWindowParams.x;
                bigWindowParams.y = smallWindowParams.y;
//                bigWindowParams.x = screenWidth / 2 - FloatWindowBigView.viewWidth / 2;
//                bigWindowParams.y = screenHeight / 2 - FloatWindowBigView.viewHeight / 2;
                bigWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                //设置类型  android8.0后有变化此处进行修改
//                if (Build.VERSION.SDK_INT >= 26) {
//                    bigWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//                } else {
//                    bigWindowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
//                }

                if (Build.VERSION.SDK_INT < 19) {
                    bigWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    bigWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else {
                    bigWindowParams.type = WindowManager.LayoutParams.TYPE_TOAST;
                }

                bigWindowParams.format = PixelFormat.RGBA_8888;
                bigWindowParams.gravity = Gravity.LEFT | Gravity.TOP;

                bigWindowParams.width = screenWidth / 11;
                bigWindowParams.height = bigWindowParams.width * 3 + 10;


//                bigWindowParams.width = FloatWindowBigView.viewWidth;
//                bigWindowParams.height = FloatWindowBigView.viewHeight;
            }
            bigWindow.setViewWidth(bigWindowParams.width);
            bigWindow.setViewHeight(bigWindowParams.height);
            windowManager.addView(bigWindow, bigWindowParams);
        }
    }

    /**
     * 将大悬浮窗从屏幕上移除。
     *
     * @param context
     *            必须为应用程序的Context.
     */
    public static void removeBigWindow(Context context) {
        if (bigWindow != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(bigWindow);
            bigWindow = null;
        }
    }

    /**
     * 更新悬浮窗。
     *
     * @param context
     *            可传入应用程序上下文。
     */
    public static void updateUsedPercent(Context context) {
        updateUsedPercent(context, false);
    }

    /**
     * 更新悬浮窗。
     *
     * @param context
     *            可传入应用程序上下文。
     */
    public static void updateUsedPercent(Context context, boolean readd) {
        WindowManager windowManager = getWindowManager(context);

        if (readd) {
            if (smallWindow != null) {
                windowManager.removeViewImmediate(smallWindow);
                windowManager.addView(smallWindow, smallWindowParams);
            } else if (bigWindow != null) {
                windowManager.removeViewImmediate(bigWindow);
                windowManager.addView(bigWindow, bigWindowParams);
            }
        } else {
            if (smallWindow != null) {
                windowManager.updateViewLayout(smallWindow, smallWindowParams);
            } else if (bigWindow != null) {
                windowManager.updateViewLayout(bigWindow, bigWindowParams);
            }
        }



    }

    /**
     * 是否有悬浮窗(包括小悬浮窗和大悬浮窗)显示在屏幕上。
     *
     * @return 有悬浮窗显示在桌面上返回true，没有的话返回false。
     */
    public static boolean isWindowShowing() {
        return smallWindow != null || bigWindow != null;
    }

    /**
     * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
     *
     * @param context
     *            必须为应用程序的Context.
     * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
     */
    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }


}
