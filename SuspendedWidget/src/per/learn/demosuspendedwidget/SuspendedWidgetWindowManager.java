package per.learn.demosuspendedwidget;

import java.lang.reflect.Field;

import per.learn.demosuspendedwidget.util.LogUtil;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class SuspendedWidgetWindowManager {

    private static SuspendedWidgetWindowManager mSwwMgr;

    private static View mSmallWidget;
    private static View mBigWidget;
    private static WindowManager mWindowMgr;
    private static WindowManager.LayoutParams mSmallWidgetParams;
    private static WindowManager.LayoutParams mBigWidgetParams;

    private static View mTempWidget;
    private static WindowManager.LayoutParams mTempWidgetParams;

    private static Button mCollectBtn;
    private static Button mBuyBtn;
    private static Button mSelectBtn;
    private static Button mEditBtn;
    private static Button mListBtn;
    private static Button mSmallWidgetBtn;

    private static AnimatorSet mAnimSet;
    private static AnimatorSet mRotateAnimSet;

    public static final int DEFAULT_STATUSBAR_HEIGHT = 38;
    private static int mStatusBarHeight = 38;
    public static final int TOUCH_SLOP = ViewConfiguration.getTouchSlop();

    private static int mLastWidgetX = -1;
    private static int mLastWidgetY = -1;
    private static boolean mIsWidgetDragging = false;

    public static final int LOCATION_LEFT_TOP = 0;
    public static final int LOCATION_RIGHT_TOP = 1;
    public static final int LOCATION_LEFT_BOTTOM = 2;
    public static final int LOCATION_RIGHT_BOTTOM = 3;
    private static int mWidgetCurrentLocation = LOCATION_RIGHT_BOTTOM;

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
            final int widgetWidth = context.getResources().getDimensionPixelSize(
                    R.dimen.small_widget_width);
            final int widgetHeight = context.getResources().getDimensionPixelSize(
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

            //let small widget can been dragged everywhere
            mSmallWidget.findViewById(R.id.small_widget_btn).setOnTouchListener(
                    new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int curX = (int)event.getRawX();
                    int curY = (int)event.getRawY();
                    int screenWidth = mWindowMgr.getDefaultDisplay().getWidth();
                    int screenHeight = mWindowMgr.getDefaultDisplay().getHeight();

                    switch(event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            if(mIsWidgetDragging)
                                return true;
                        }break;

                        case MotionEvent.ACTION_MOVE: {
                            if(Math.sqrt(Math.pow(curX - mLastWidgetX, 2) +
                                    Math.pow(curY - mLastWidgetY, 2)) >= TOUCH_SLOP) {
                                updateSmallWidgetLocation(context, curX, curY);

                                mIsWidgetDragging = true;
                            }
                        }break;

                        case MotionEvent.ACTION_UP: {
                            if(mSmallWidget == null || !mIsWidgetDragging)
                                break;

                            int factY = curY - getStatusBarHeight(context);
                            mSmallWidgetParams = getWindowManagerParams();
                            mSmallWidgetParams.gravity = Gravity.LEFT | Gravity.TOP;
                            mSmallWidgetParams.flags |= WindowManager.LayoutParams
                                    .FLAG_FORCE_NOT_FULLSCREEN;

                            mTempWidget = LayoutInflater.from(context).inflate(
                                    R.layout.layout_temp_widget, null);
                            mTempWidgetParams = getWindowManagerParams();
                            mTempWidgetParams.x = 0;
                            mTempWidgetParams.y = 0;
                            mTempWidgetParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                            mTempWidgetParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                            mTempWidgetParams.format = PixelFormat.RGBA_8888;
                            mTempWidgetParams.gravity = Gravity.LEFT | Gravity.TOP;
                            mTempWidgetParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN |
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

                            //use AnimationSet to complete the translate animation
                            mAnimSet = new AnimatorSet();

                            if(curX <= screenWidth / 2 && factY <= screenHeight / 2) {
                                //the target position we want
                                mSmallWidgetParams.x = 0;
                                mSmallWidgetParams.y = getStatusBarHeight(context);

                                //update widget's current location
                                mWidgetCurrentLocation = LOCATION_LEFT_TOP;

                                //mWindowMgr.updateViewLayout(mSmallWidget, mSmallWidgetParams);
                            } else if(curX <= screenWidth / 2 && factY > screenHeight / 2) {
                                //the target position we want
                                mSmallWidgetParams.x = 0;
                                mSmallWidgetParams.y = screenHeight - getStatusBarHeight(context)
                                        - widgetHeight;

                                //update widget's current location
                                mWidgetCurrentLocation = LOCATION_LEFT_BOTTOM;

                                //mWindowMgr.updateViewLayout(mSmallWidget, mSmallWidgetParams);
                            } else if(curX > screenWidth / 2 && factY <= screenHeight / 2) {
                                mSmallWidgetParams.x = screenWidth - widgetWidth;
                                mSmallWidgetParams.y = getStatusBarHeight(context);

                                //update widget's current location
                                mWidgetCurrentLocation = LOCATION_RIGHT_TOP;

                                //mWindowMgr.updateViewLayout(mSmallWidget, mSmallWidgetParams);
                            } else if(curX > screenWidth / 2 && factY > screenHeight / 2) {
                                mSmallWidgetParams.x = screenWidth - widgetWidth;
                                mSmallWidgetParams.y = screenHeight - getStatusBarHeight(context)
                                        - widgetHeight;

                                //update widget's current location
                                mWidgetCurrentLocation = LOCATION_RIGHT_BOTTOM;

                                //mWindowMgr.updateViewLayout(mSmallWidget, mSmallWidgetParams);
                            }


                            //play translate animation
                            mWindowMgr.addView(mTempWidget, mTempWidgetParams);
                            mWindowMgr.removeView(mSmallWidget);
                            View widget = mTempWidget.findViewById(R.id.temp_widget_btn);
                            mAnimSet.playTogether(
                                    ObjectAnimator.ofFloat(widget, "translationX",
                                            curX, mSmallWidgetParams.x),
                                    ObjectAnimator.ofFloat(widget, "translationY",
                                            factY, mSmallWidgetParams.y));
                            mAnimSet.setInterpolator(new AccelerateInterpolator());
                            mAnimSet.addListener(new AnimatorListener() {

                                @Override
                                public void onAnimationStart(Animator arg0) {
                                }

                                @Override
                                public void onAnimationRepeat(Animator arg0) {
                                }

                                @Override
                                public void onAnimationEnd(Animator arg0) {
                                    mIsWidgetDragging = false;

                                    //after the translate animation, we need update
                                    //the paramaters of the view
                                    mWindowMgr.addView(mSmallWidget, mSmallWidgetParams);
                                    mWindowMgr.removeView(mTempWidget);
                                }

                                @Override
                                public void onAnimationCancel(Animator arg0) {
                                }
                            });
                            mAnimSet.setDuration(300);
                            mAnimSet.start();

                            mLastWidgetX = mSmallWidgetParams.x;
                            mLastWidgetY = mSmallWidgetParams.y;

                            return mIsWidgetDragging;
                        }
                    }

                    return false;
                }
            });

            mSmallWidgetParams = getWindowManagerParams();
            mSmallWidgetParams.gravity = Gravity.LEFT | Gravity.TOP;
            mSmallWidgetParams.flags |= WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
            mSmallWidgetParams.x = 0;
            mSmallWidgetParams.y = getStatusBarHeight(context);
            if(mWidgetCurrentLocation == LOCATION_LEFT_BOTTOM) {
                mSmallWidgetParams.x = 0;
                mSmallWidgetParams.y = screenHeight - getStatusBarHeight(context)
                        - widgetHeight;
            } else if(mWidgetCurrentLocation == LOCATION_RIGHT_TOP) {
                mSmallWidgetParams.x = screenWidth - widgetWidth;
                mSmallWidgetParams.y = getStatusBarHeight(context);
            } else if(mWidgetCurrentLocation == LOCATION_RIGHT_BOTTOM) {
                mSmallWidgetParams.x = screenWidth - widgetWidth;
                mSmallWidgetParams.y = screenHeight - getStatusBarHeight(context)
                        - widgetHeight;
            }

            mLastWidgetX = mSmallWidgetParams.x;
            mLastWidgetY = mSmallWidgetParams.y;

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
            int curLayout = R.layout.layout_big_widget_left_top;
            if(mWidgetCurrentLocation == LOCATION_LEFT_BOTTOM)
                curLayout = R.layout.layout_big_widget_left_bottom;
            else if(mWidgetCurrentLocation == LOCATION_RIGHT_TOP)
                curLayout = R.layout.layout_big_widget_right_top;
            else if(mWidgetCurrentLocation == LOCATION_RIGHT_BOTTOM)
                curLayout = R.layout.layout_big_widget_right_bottom;
            mBigWidget = (RelativeLayout) LayoutInflater.from(context).inflate(
                    curLayout, null);
            mBigWidget.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(isNeedPlayReverseAnim()) {
                        //reverse translate animation for five child button
                        mAnimSet = getReverseAnimSetByLocation(context,
                                mWidgetCurrentLocation);
                        mAnimSet.setInterpolator(new OvershootInterpolator());
                        mAnimSet.setDuration(350).start();
                        //reverse rotation animation for the main button
                        mRotateAnimSet = new AnimatorSet();
                        mRotateAnimSet.playTogether(ObjectAnimator.ofFloat(mSmallWidgetBtn,
                                "rotation", 540, 0));
                        mRotateAnimSet.setInterpolator(new BounceInterpolator());
                        mRotateAnimSet.addListener(new AnimatorListener() {

                            @Override
                            public void onAnimationStart(Animator arg0) {
                            }
                            
                            @Override
                            public void onAnimationRepeat(Animator arg0) {
                            }
                            
                            @Override
                            public void onAnimationEnd(Animator arg0) {
                                removeBigWidget(context);
                                createSmallWidget(context);
                            }

                            @Override
                            public void onAnimationCancel(Animator arg0) {
                            }
                        });
                        mRotateAnimSet.setDuration(350).start();
                    } else {
                        removeBigWidget(context);
                        createSmallWidget(context);
                    }
                }
            });
            mCollectBtn = (Button)mBigWidget.findViewById(R.id.collect_btn);
            mCollectBtn.setOnClickListener(
                    new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "click collect button", Toast.LENGTH_SHORT).show();
                }
            });
            mBuyBtn = (Button)mBigWidget.findViewById(R.id.buy_btn);
            mBuyBtn.setOnClickListener(
                    new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "click buy button", Toast.LENGTH_SHORT).show();
                }
            });
            mSelectBtn = (Button)mBigWidget.findViewById(R.id.select_btn);
            mSelectBtn.setOnClickListener(
                    new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "click select button", Toast.LENGTH_SHORT).show();
                }
            });
            mEditBtn = (Button)mBigWidget.findViewById(R.id.edit_btn);
            mEditBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "click edit button", Toast.LENGTH_SHORT).show();
                }
            });
            mListBtn = (Button)mBigWidget.findViewById(R.id.list_btn);
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
                        //reverse translate animation for five child button
                        mAnimSet = getReverseAnimSetByLocation(context,
                                mWidgetCurrentLocation);
                        mAnimSet.setInterpolator(new OvershootInterpolator());
                        mAnimSet.setDuration(350).start();
                        //reverse rotation animation for the main button
                        mRotateAnimSet = new AnimatorSet();
                        mRotateAnimSet.playTogether(ObjectAnimator.ofFloat(mSmallWidgetBtn,
                                "rotation", 540, 0));
                        mRotateAnimSet.setInterpolator(new BounceInterpolator());
                        mRotateAnimSet.addListener(new AnimatorListener() {

                            @Override
                            public void onAnimationStart(Animator arg0) {
                            }
                            
                            @Override
                            public void onAnimationRepeat(Animator arg0) {
                            }
                            
                            @Override
                            public void onAnimationEnd(Animator arg0) {
                                removeBigWidget(context);
                                createSmallWidget(context);
                            }

                            @Override
                            public void onAnimationCancel(Animator arg0) {
                            }
                        });
                        mRotateAnimSet.setDuration(350).start();
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
            //translation animation for five child button
            mAnimSet = getAnimSetByLocation(context, mWidgetCurrentLocation);
            mAnimSet.start();
            //rotation animation for the main button
            mRotateAnimSet = new AnimatorSet();
            mRotateAnimSet.playTogether(
                    ObjectAnimator.ofFloat(mSmallWidgetBtn, "rotation", 0, 540));
            mRotateAnimSet.setInterpolator(new BounceInterpolator());
            mRotateAnimSet.setDuration(450).start();
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

    public static void updateSmallWidgetLocation(Context context, int x, int y) {
        if(mSmallWidget == null)
            return;

        mSmallWidgetParams = getWindowManagerParams();
        mSmallWidgetParams.gravity = Gravity.LEFT | Gravity.TOP;
        mSmallWidgetParams.flags |= WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
        mSmallWidgetParams.x = x;
        mSmallWidgetParams.y = y - getStatusBarHeight(context);

        mWindowMgr.updateViewLayout(mSmallWidget, mSmallWidgetParams);

        mLastWidgetX = mSmallWidgetParams.x;
        mLastWidgetY = mSmallWidgetParams.y;
    }

    private static AnimatorSet getAnimSetByLocation(Context context, int loc) {
        Resources res = context.getResources();
        AnimatorSet anim1, anim2, anim3, anim4, anim5;

        //animation for button 1
        anim1 = new AnimatorSet();
        anim1.playTogether(
                ObjectAnimator.ofFloat(mBuyBtn, "translationX",
                loc == LOCATION_RIGHT_BOTTOM || loc == LOCATION_RIGHT_TOP
                        ? res.getDimension(R.dimen.circle_menu_radius)
                                : -res.getDimension(R.dimen.circle_menu_radius),
                0),
                ObjectAnimator.ofFloat(mBuyBtn, "scaleX", 0, 1),
                ObjectAnimator.ofFloat(mBuyBtn, "scaleY", 0, 1)
                );
        anim1.setStartDelay(0);

        //animation for button 2
        anim2 = new AnimatorSet();
        anim2.playTogether(
                ObjectAnimator.ofFloat(mSelectBtn, "translationX",
                        loc == LOCATION_RIGHT_BOTTOM || loc == LOCATION_RIGHT_TOP
                                ? res.getDimension(R.dimen.circle_menu_button_1_x)
                                        : -res.getDimension(R.dimen.circle_menu_button_1_x),
                        0),
                ObjectAnimator.ofFloat(mSelectBtn, "translationY",
                        loc == LOCATION_LEFT_BOTTOM || loc == LOCATION_RIGHT_BOTTOM
                                ? res.getDimension(R.dimen.circle_menu_button_1_y)
                                        : -res.getDimension(R.dimen.circle_menu_button_1_y),
                        0),
                ObjectAnimator.ofFloat(mSelectBtn, "scaleX", 0, 1),
                ObjectAnimator.ofFloat(mSelectBtn, "scaleY", 0, 1));
        anim2.setStartDelay(30);

        //animation for button 3
        anim3 = new AnimatorSet();
        anim3.playTogether(
                ObjectAnimator.ofFloat(mListBtn, "translationX",
                        loc == LOCATION_RIGHT_BOTTOM || loc == LOCATION_RIGHT_TOP
                                ? res.getDimension(R.dimen.circle_menu_button_2_x)
                                        : -res.getDimension(R.dimen.circle_menu_button_2_x),
                        0),
                ObjectAnimator.ofFloat(mListBtn, "translationY",
                        loc == LOCATION_LEFT_BOTTOM || loc == LOCATION_RIGHT_BOTTOM
                                ? res.getDimension(R.dimen.circle_menu_button_2_y)
                                        : -res.getDimension(R.dimen.circle_menu_button_2_y),
                        0),
                ObjectAnimator.ofFloat(mListBtn, "scaleX", 0, 1),
                ObjectAnimator.ofFloat(mListBtn, "scaleY", 0, 1));
        anim3.setStartDelay(60);

        //animation for button 4
        anim4 = new AnimatorSet();
        anim4.playTogether(
                ObjectAnimator.ofFloat(mEditBtn, "translationX",
                        loc == LOCATION_RIGHT_BOTTOM || loc == LOCATION_RIGHT_TOP
                                ? res.getDimension(R.dimen.circle_menu_button_3_x)
                                        : -res.getDimension(R.dimen.circle_menu_button_3_x),
                        0),
                ObjectAnimator.ofFloat(mEditBtn, "translationY",
                        loc == LOCATION_LEFT_BOTTOM || loc == LOCATION_RIGHT_BOTTOM
                                ? res.getDimension(R.dimen.circle_menu_button_3_y)
                                        : -res.getDimension(R.dimen.circle_menu_button_3_y),
                        0),
                ObjectAnimator.ofFloat(mEditBtn, "scaleX", 0, 1),
                ObjectAnimator.ofFloat(mEditBtn, "scaleY", 0, 1));
        anim4.setStartDelay(90);

        //animation for button 5
        anim5 = new AnimatorSet();
        anim5.playTogether(
                ObjectAnimator.ofFloat(mCollectBtn, "translationY",
                        loc == LOCATION_LEFT_BOTTOM || loc == LOCATION_RIGHT_BOTTOM
                                ? res.getDimension(R.dimen.circle_menu_radius)
                                        : -res.getDimension(R.dimen.circle_menu_radius),
                        0),
                ObjectAnimator.ofFloat(mCollectBtn, "scaleX", 0, 1),
                ObjectAnimator.ofFloat(mCollectBtn, "scaleY", 0, 1)
                );
        anim5.setStartDelay(120);

        //play all animations together
        AnimatorSet set = new AnimatorSet();
        set.playTogether(anim1, anim2, anim3, anim4, anim5);
        set.setDuration(300);
        set.setInterpolator(new OvershootInterpolator(3.0f));

        return set;
    }

    private static AnimatorSet getReverseAnimSetByLocation(Context context, int loc) {
        Resources res = context.getResources();

        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(mBuyBtn, "translationX",
                        0,
                        loc == LOCATION_RIGHT_BOTTOM || loc == LOCATION_RIGHT_TOP
                                ? res.getDimension(R.dimen.circle_menu_radius)
                                        : -res.getDimension(R.dimen.circle_menu_radius)
                        ),
                ObjectAnimator.ofFloat(mBuyBtn, "scaleX", 1, 0),
                ObjectAnimator.ofFloat(mBuyBtn, "scaleY", 1, 0),
                ObjectAnimator.ofFloat(mSelectBtn, "translationX",
                        0,
                        loc == LOCATION_RIGHT_BOTTOM || loc == LOCATION_RIGHT_TOP
                                ? res.getDimension(R.dimen.circle_menu_button_1_x)
                                        : -res.getDimension(R.dimen.circle_menu_button_1_x)
                        ),
                ObjectAnimator.ofFloat(mSelectBtn, "translationY",
                        0,
                        loc == LOCATION_LEFT_BOTTOM || loc == LOCATION_RIGHT_BOTTOM
                                ? res.getDimension(R.dimen.circle_menu_button_1_y)
                                        : -res.getDimension(R.dimen.circle_menu_button_1_y)
                        ),
                ObjectAnimator.ofFloat(mSelectBtn, "scaleX", 1, 0),
                ObjectAnimator.ofFloat(mSelectBtn, "scaleY", 1, 0),
                ObjectAnimator.ofFloat(mListBtn, "translationX",
                        0,
                        loc == LOCATION_RIGHT_BOTTOM || loc == LOCATION_RIGHT_TOP
                                ? res.getDimension(R.dimen.circle_menu_button_2_x)
                                        : -res.getDimension(R.dimen.circle_menu_button_2_x)
                        ),
                ObjectAnimator.ofFloat(mListBtn, "translationY",
                        0,
                        loc == LOCATION_LEFT_BOTTOM || loc == LOCATION_RIGHT_BOTTOM
                                ? res.getDimension(R.dimen.circle_menu_button_2_y)
                                        : -res.getDimension(R.dimen.circle_menu_button_2_y)
                        ),
                ObjectAnimator.ofFloat(mListBtn, "scaleX", 1, 0),
                ObjectAnimator.ofFloat(mListBtn, "scaleY", 1, 0),
                ObjectAnimator.ofFloat(mEditBtn, "translationX",
                        0,
                        loc == LOCATION_RIGHT_BOTTOM || loc == LOCATION_RIGHT_TOP
                                ? res.getDimension(R.dimen.circle_menu_button_3_x)
                                        : -res.getDimension(R.dimen.circle_menu_button_3_x)
                        ),
                ObjectAnimator.ofFloat(mEditBtn, "translationY",
                        0,
                        loc == LOCATION_LEFT_BOTTOM || loc == LOCATION_RIGHT_BOTTOM
                                ? res.getDimension(R.dimen.circle_menu_button_3_y)
                                        : -res.getDimension(R.dimen.circle_menu_button_3_y)
                        ),
                ObjectAnimator.ofFloat(mEditBtn, "scaleX", 1, 0),
                ObjectAnimator.ofFloat(mEditBtn, "scaleY", 1, 0),
                ObjectAnimator.ofFloat(mCollectBtn, "translationY",
                        0,
                        loc == LOCATION_LEFT_BOTTOM || loc == LOCATION_RIGHT_BOTTOM
                                ? res.getDimension(R.dimen.circle_menu_radius)
                                        : -res.getDimension(R.dimen.circle_menu_radius)
                        ),
                ObjectAnimator.ofFloat(mCollectBtn, "scaleX", 1, 0),
                ObjectAnimator.ofFloat(mCollectBtn, "scaleY", 1, 0)
                );

        return set;
    }
}
