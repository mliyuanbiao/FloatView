package cn.liyuanbiao.floatview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.graphics.Point;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class FloatView {
    public static final int LOC_TYPE_DECOR_VIEW = 0;
    public static final int LOC_TYPE_SCREEN = 1;
    private static final Map<String, FloatView> mFloatViewMap = new HashMap<>();

    @NonNull
    private Application application;
    @NonNull
    private final ViewFactory viewFactory;
    private boolean isActive;
    private boolean isShow;
    private int locType;
    private float locXScale, locYScale;

    private FloatView(
            @NonNull Application application,
            @NonNull ViewFactory viewFactory) {
        this.application = application;
        this.viewFactory = viewFactory;
    }

    @NonNull
    public static FloatView with(
            @NonNull Application application,
            @NonNull String tag,
            @NonNull ViewFactory viewFactory) {
        if (TextUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("tag must not empty");
        }
        FloatView floatView = mFloatViewMap.get(tag);
        if (floatView != null) {
            throw new IllegalStateException("tag: " + tag + " , 已经被创建");
        } else {
            floatView = new FloatView(application, viewFactory);
            mFloatViewMap.put(tag, floatView);
        }
        return floatView;
    }

    @Nullable
    public static FloatView get(@NonNull String tag) {
        return mFloatViewMap.get(tag);
    }

    /**
     * 设置是否可以移动
     *
     * @param active 是否可移动
     * @return self
     */
    public FloatView setActive(boolean active) {
        isActive = active;
        return this;
    }

    /**
     * 设置初始化的位置
     *
     * @param x       x方向的比例
     * @param y       y方向的比例
     * @param locType 比例类型 {@link FloatView#LOC_TYPE_DECOR_VIEW} or {@link FloatView#LOC_TYPE_SCREEN}
     * @return self
     */
    public FloatView setLocation(float x, float y, int locType) {
        this.locXScale = x;
        this.locYScale = y;
        this.locType = locType;
        return this;
    }

    public boolean isShow() {
        return isShow;
    }

    public void show() {
        isShow = true;
        for (Map.Entry<Activity, View> entry :
                activityViewWeakHashMap.entrySet()) {
            View view = entry.getValue();
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
        }
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    public void hide() {
        isShow = false;
        application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
        for (Map.Entry<Activity, View> entry :
                activityViewWeakHashMap.entrySet()) {
            View view = entry.getValue();
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
    }

    public boolean isActive() {
        return isActive;
    }

    private final WeakHashMap<Activity, View> activityViewWeakHashMap = new WeakHashMap<>();

    private final BaseActivityLifecycleCallbacks activityLifecycleCallbacks
            = new BaseActivityLifecycleCallbacks() {
        private float x = -1;
        private float y = -1;

        private void setViewLocation(View view, float width, float height) {
            view.setX(x == -1 ? (int) (width * locXScale) : (int) x);
            view.setY(y == -1 ? (int) (height * locYScale) : (int) y);
        }

        @Override
        public void onActivityResumed(Activity activity) {
            super.onActivityResumed(activity);

            if (activityViewWeakHashMap.get(activity) != null) {
                return;
            }

            final View view = viewFactory.makeView(activity);
            activityViewWeakHashMap.put(activity, view);

            view.setId(R.id.floatview_float_view);
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);

            final View decorView = activity.getWindow().getDecorView();
            ((ViewGroup) decorView).addView(view);

            if (locType == LOC_TYPE_SCREEN) {
                Point outSize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(outSize);
                float width = outSize.x;
                float height = outSize.y;
                setViewLocation(view, width, height);
            } else {
                decorView.post(new Runnable() {
                    @Override
                    public void run() {
                        float width = decorView.getWidth();
                        float height = decorView.getHeight();
                        setViewLocation(view, width, height);
                    }
                });
            }

            if (isActive) {
                view.setOnTouchListener(new View.OnTouchListener() {
                    private float lastX;
                    private float lastY;
                    private float downX;
                    private float downY;

                    @Override
                    @SuppressLint("ClickableViewAccessibility")
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                downX = lastX = event.getRawX();
                                downY = lastY = event.getRawY();
                                break;
                            case MotionEvent.ACTION_MOVE: {
                                float currentX = event.getRawX();
                                float currentY = event.getRawY();

                                float offsetX = currentX - lastX;
                                float offsetY = currentY - lastY;

                                float mX = v.getX() + offsetX;
                                float mY = v.getY() + offsetY;

                                lastX = currentX;
                                lastY = currentY;

                                ViewGroup parent = (ViewGroup) v.getParent();
                                int parentWidth = parent.getWidth();
                                int parentHeight = parent.getHeight();

                                int vWidth = v.getWidth();
                                int vHeight = v.getHeight();

                                if (mX < 0) {
                                    mX = 0;
                                } else {
                                    if (mX > parentWidth - vWidth) {
                                        mX = parentWidth - vWidth;
                                    }
                                }
                                if (mY < 0) {
                                    mY = 0;
                                } else {
                                    if (mY > parentHeight - vHeight) {
                                        mY = parentHeight - vHeight;
                                    }
                                }
                                v.setX(mX);
                                v.setY(mY);

                                x = v.getX();
                                y = v.getY();
                                break;
                            }
                            case MotionEvent.ACTION_UP: {
                                float currentX = event.getRawX();
                                float currentY = event.getRawY();
                                x = v.getX();
                                y = v.getY();
                                float absX = Math.abs(currentX - downX);
                                float absY = Math.abs(currentY - downY);
                                int touchSlop = ViewConfiguration.get(v.getContext())
                                        .getScaledTouchSlop();
                                if (Math.sqrt(Math.pow(absX, 2) +
                                        Math.pow(absY, 2)) > touchSlop) {
                                    return true;
                                } else if (v.performClick()) {
                                    return true;
                                }
                                break;
                            }
                        }
                        return false;
                    }
                });
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
            super.onActivityPaused(activity);
            View view = activityViewWeakHashMap.remove(activity);
            if (view != null) {
                view.setOnClickListener(null);
                ViewGroup viewGroup = (ViewGroup) view.getParent();
                if (viewGroup != null) {
                    viewGroup.removeView(view);
                }
            }
        }
    };

    public interface ViewFactory {
        View makeView(Activity activity);
    }

}
