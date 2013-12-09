package per.learn.demosuspendedwidget;

import java.lang.reflect.Field;

import per.learn.demosuspendedwidget.util.LogUtil;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class SuspendedWidgetWindowManager {

    private static SuspendedWidgetWindowManager mSwwMgr;

    private static View mSmallWidget;
    private static View mBigWidget;
    private static WindowManager mWindowMgr;
    private static WindowManager.LayoutParams mSmallWidgetParams;
    private static WindowManager.LayoutParams mBigWidgetParams;

    private static Button mCollectBtn;
    private static LinearLayout mCollectBtnLl;
    private static Button mBuyBtn;
    private static LinearLayout mBuyBtnLl;
    private static Button mSelectBtn;
    private static LinearLayout mSelectBtnLl;
    private static Button mEditBtn;
    private static LinearLayout mEditBtnLl;
    private static Button mListBtn;
    private static LinearLayout mListBtnLl;
    private static Button mSmallWidgetBtn;

    private static Animation mTranlateAnim;
    private static Animation mReverseAnim;
    private static Animation mRotateAnim;
    private static Animation mReverseRotateAnim;

    public static final int DEFAULT_STATUSBAR_HEIGHT = 38;
    private static int mStatusBarHeight = 38;

    private SuspendedWidgetWindowManager() {}

    public static SuspendedWidgetWindowManager getInstance() {
        if(mSwwMgr == null) {
            synchronized (SuspendedWidgetWindowManager.class) {
                if(mSwwMgr == null)
                    mSwwMgr = new SuspendedWidgetWindowManager();
            }
        }

        return mSwwMgr;
    }

    /**
     * create the small suspended widget on screen
     * @return true create successfully, false not
     * */
    public boolean createSmallWidget(final Context context) {
        if(mWindowMgr == null)
            mWindowMgr = getWindowManager(context);

        int screenWidth = mWindowMgr.getDefaultDisplay().getWidth();
        int screenHeight = mWindowMgr.getDefaultDisplay().getHeight();

        if(mSmallWidget == null) {
            mSmallWidget = LayoutInflater.from(context).inflate(
                    R.layout.layout_small_widget, null);
            int widgetWidth = context.getResources().getDimensionPixelSize(
                    R.dimen.small_widget_width);
            int widgetHeight = context.getResources().getDimensionPixelSize(
                    R.dimen.small_widget_height);

            mSmallWidget.findViewById(R.id.small_widget_btn).setOnClickListener(
                    new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //Toast.makeText(context, "click small widget", Toast.LENGTH_SHORT).show();
                    removeSmallWidget(context);
                    createBigWidget(context);
                }
            });

            mSmallWidgetParams = getWindowManagerParams();
            mSmallWidgetParams.gravity = Gravity.LEFT | Gravity.TOP;
            mSmallWidgetParams.flags |= WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
            mSmallWidgetParams.x = screenWidth - widgetWidth;
            mSmallWidgetParams.y = screenHeight - getStatusBarHeight(context)
                    - widgetHeight;

            try {
                mWindowMgr.addView(mSmallWidget, mSmallWidgetParams);
            } catch(Exception e) {
                e.printStackTrace();

                LogUtil.Log("SuspendedWidgetWindowManager.createSmallWindow(), exception!!!");

                return false;
            }

            return true;
        }

        return false;
    }

    /**
     * remove the small suspended widget
     * @return true remove successfully, false not
     * */
    public boolean removeSmallWidget(Context context) {
        if(mSmallWidget == null)
            return true;

        if(mWindowMgr == null) {
            mWindowMgr = getWindowManager(context);
        }

        try {
            mWindowMgr.removeView(mSmallWidget);
        } catch(Exception e) {
            e.printStackTrace();

            LogUtil.Log("removeSmallWindow(), exception!!!");
        } finally {
            mSmallWidget = null;
        }

        return true;
    }

    /**
     * create big suspended widget on the screen
     * @return true create successfully, false not
     * */
    public boolean createBigWidget(final Context context) {
        if(mWindowMgr == null)
            mWindowMgr = getWindowManager(context);

        if(mBigWidget == null) {
            mBigWidget = (RelativeLayout) LayoutInflater.from(context).inflate(
                    R.layout.layout_big_widget, null);
            mBigWidget.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(isNeedPlayReverseAnim()) {
                        mReverseAnim = AnimationUtils.loadAnimation(context, R.anim.anim_item_pull_back);
                        mCollectBtnLl.startAnimation(mReverseAnim);
                        mBuyBtnLl.startAnimation(mReverseAnim);
                        mSelectBtnLl.startAnimation(mReverseAnim);
                        mEditBtnLl.startAnimation(mReverseAnim);
                        mListBtnLl.startAnimation(mReverseAnim);

                        mReverseRotateAnim = AnimationUtils.loadAnimation(context,
                                R.anim.anim_reverse_rotate);
                        mReverseRotateAnim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }
                            
                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                            
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                removeBigWidget(context);
                                createSmallWidget(context);
                            }
                        });
                        mSmallWidgetBtn.startAnimation(mReverseRotateAnim);
                    } else {
                        removeBigWidget(context);
                        createSmallWidget(context);
                    }
                }
            });
            mCollectBtn = (Button)mBigWidget.findViewById(R.id.collect_btn);
            mCollectBtnLl = (LinearLayout)mBigWidget.findViewById(R.id.collect_btn_ll);
            mCollectBtn.setOnClickListener(
                    new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "click collect button", Toast.LENGTH_SHORT).show();
                }
            });
            mBuyBtn = (Button)mBigWidget.findViewById(R.id.buy_btn);
            mBuyBtnLl = (LinearLayout)mBigWidget.findViewById(R.id.buy_btn_ll);
            mBuyBtn.setOnClickListener(
                    new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "click buy button", Toast.LENGTH_SHORT).show();
                }
            });
            mSelectBtn = (Button)mBigWidget.findViewById(R.id.select_btn);
            mSelectBtnLl = (LinearLayout)mBigWidget.findViewById(R.id.select_btn_ll);
            mSelectBtn.setOnClickListener(
                    new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "click select button", Toast.LENGTH_SHORT).show();
                }
            });
            mEditBtn = (Button)mBigWidget.findViewById(R.id.edit_btn);
            mEditBtnLl = (LinearLayout)mBigWidget.findViewById(R.id.edit_btn_ll);
            mEditBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "click edit button", Toast.LENGTH_SHORT).show();
                }
            });
            mListBtn = (Button)mBigWidget.findViewById(R.id.list_btn);
            mListBtnLl = (LinearLayout)mBigWidget.findViewById(R.id.list_btn_ll);
            mListBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "click list button", Toast.LENGTH_SHORT).show();
                }
            });
            mSmallWidgetBtn = (Button)mBigWidget.findViewById(R.id.small_widget_btn);
            mSmallWidgetBtn.setOnClickListener(
                    new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(isNeedPlayReverseAnim()) {
                        mReverseAnim = AnimationUtils.loadAnimation(context, R.anim.anim_item_pull_back);
                        mCollectBtnLl.startAnimation(mReverseAnim);
                        mBuyBtnLl.startAnimation(mReverseAnim);
                        mSelectBtnLl.startAnimation(mReverseAnim);
                        mEditBtnLl.startAnimation(mReverseAnim);
                        mListBtnLl.startAnimation(mReverseAnim);

                        mReverseRotateAnim = AnimationUtils.loadAnimation(context,
                                R.anim.anim_reverse_rotate);
                        mReverseRotateAnim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }
                            
                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                            
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                removeBigWidget(context);
                                createSmallWidget(context);
                            }
                        });
                        mSmallWidgetBtn.startAnimation(mReverseRotateAnim);
                    } else {
                        removeBigWidget(context);
                        createSmallWidget(context);
                    }
                }
            });

            mBigWidgetParams = getWindowManagerParams();
            mBigWidgetParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            mBigWidgetParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            mBigWidgetParams.format = PixelFormat.RGBA_8888;
            mBigWidgetParams.gravity = Gravity.LEFT | Gravity.TOP;
            mBigWidgetParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;

            try {
                mWindowMgr.addView(mBigWidget, mBigWidgetParams);
            } catch(Exception e) {
                e.printStackTrace();

                LogUtil.Log("SuspendedWidgetWindowManager.createBigWidget(), exception!!!");

                return false;
            }

            //play animation
            mTranlateAnim = AnimationUtils.loadAnimation(context, R.anim.anim_item_push_out);
            mCollectBtnLl.startAnimation(mTranlateAnim);
            mBuyBtnLl.startAnimation(mTranlateAnim);
            mSelectBtnLl.startAnimation(mTranlateAnim);
            mEditBtnLl.startAnimation(mTranlateAnim);
            mListBtnLl.startAnimation(mTranlateAnim);
            mRotateAnim = AnimationUtils.loadAnimation(context, R.anim.anim_normal_rotate);
            mSmallWidgetBtn.startAnimation(mRotateAnim);
        }

        return true;
    }

    /**
     * remove the big suspended widget
     * @return true remove successfully, false not
     * */
    public boolean removeBigWidget(final Context context) {
        if(mBigWidget == null)
            return true;

        if(mWindowMgr == null) {
            mWindowMgr = getWindowManager(context);
        }

        try {
            mWindowMgr.removeView(mBigWidget);
        } catch(Exception e) {
            e.printStackTrace();
            LogUtil.Log("SuspendedWidgetWindowManager.removeBigWidget(), exception!!!");
        } finally {
            mBigWidget = null;
        }

        return true;
    }

    private boolean isNeedPlayReverseAnim() {
        return mBigWidget != null;
    }

    /**
     * return if the Small suspended widget is showing
     * @return true the widget is showing, false not
     * */
    public boolean isSmallWidgetShowing() {
        if(mSmallWidget != null)
            return true;

        return false;
    }

    /**
     * return if the big suspended widget is showing
     * @return true the widget is showing, false not
     * */
    public boolean isBigWidgetShowing() {
        if(mBigWidget != null)
            return true;

        return false;
    }

    private static WindowManager getWindowManager(Context context) {
        return (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    }

    private static WindowManager.LayoutParams getWindowManagerParams() {
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                0, 0,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.RGBA_8888);
    }

    private static int getStatusBarHeight(Context context) {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object o = c.newInstance();
            Field field = c.getField("status_bar_height");
            int resourceId = (Integer)field.get(o);
            mStatusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        } catch(Exception e) {
            mStatusBarHeight = DEFAULT_STATUSBAR_HEIGHT;
        }

        return mStatusBarHeight;
    }
}
