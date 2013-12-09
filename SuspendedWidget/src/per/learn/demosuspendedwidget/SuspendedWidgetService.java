package per.learn.demosuspendedwidget;

import per.learn.demosuspendedwidget.util.LogUtil;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SuspendedWidgetService extends Service {
    public static final String KEY_CMD_SUSPENDED_WIDGET = "key_cmd_start_widget";
    public static final int CMD_ILLEGAL = 0;
    public static final int CMD_START_SMALL_SUSPENDED_WIDGET = 1;
    public static final int CMD_REMOVE_SMALL_SUSPENDED_WIDGET = 2;
    public static final int CMD_START_BIG_SUSPENDED_WIDGET = 3;
    public static final int CMD_REMOVE_BIG_SUSPENDED_WIDGET = 4;

    private static boolean isSmallWidgetShowing = false;
    private static boolean isBigWidgetShowing = false;

    private static SuspendedWidgetWindowManager mSwwMgr;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && intent.getIntExtra(KEY_CMD_SUSPENDED_WIDGET, CMD_ILLEGAL)
                == CMD_START_SMALL_SUSPENDED_WIDGET) {
            mSwwMgr.createSmallWidget(this);

            isSmallWidgetShowing = mSwwMgr.isSmallWidgetShowing();

            LogUtil.Log("SuspendedWidgetService.onStartCommand(), start small widget"
                    + ", isSmallWidgetShowing = " + isSmallWidgetShowing);
        } else if(intent != null && intent.getIntExtra(KEY_CMD_SUSPENDED_WIDGET, CMD_ILLEGAL)
                == CMD_REMOVE_SMALL_SUSPENDED_WIDGET) {
            mSwwMgr.removeSmallWidget(this);

            isSmallWidgetShowing = mSwwMgr.isSmallWidgetShowing();

            LogUtil.Log("SuspendedWidgetService.onStartCommand(), remove small widget"
                    + ", isSmallWidgetShowing = " + isSmallWidgetShowing);
        } else if(intent != null && intent.getIntExtra(KEY_CMD_SUSPENDED_WIDGET, CMD_ILLEGAL)
                == CMD_START_BIG_SUSPENDED_WIDGET) {
            mSwwMgr.createBigWidget(this);

            isBigWidgetShowing = mSwwMgr.isBigWidgetShowing();

            LogUtil.Log("SuspendedWidgetService.onStartCommand(), start big widget"
                    + ", isBigWidgetShowing = " + isBigWidgetShowing);
        } else if(intent != null && intent.getIntExtra(KEY_CMD_SUSPENDED_WIDGET, CMD_ILLEGAL)
                ==CMD_REMOVE_BIG_SUSPENDED_WIDGET) {
            mSwwMgr.removeBigWidget(this);

            isBigWidgetShowing = mSwwMgr.isBigWidgetShowing();

            LogUtil.Log("SuspendedWidgetService.onStartCommand(), remove big widget"
                    + ", isBigWidgetShowing = " + isBigWidgetShowing);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mSwwMgr = SuspendedWidgetWindowManager.getInstance();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static boolean isSmallWidgetShowing() {
        return mSwwMgr.isSmallWidgetShowing();
    }

    public static boolean isBigWidgetShowing() {
        return mSwwMgr.isBigWidgetShowing();
    }
}
