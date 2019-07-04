package com.star.vbanner;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;


/**
 *
 * @作者 StarSpark
 * @描述
 * @创建日期 2019/6/10 19:08
 */
@SuppressWarnings("unused")
public class VerticalBannerView extends LinearLayout implements BaseBannerAdapter.OnDataChangedListener {
    /**
     * 指定banner条目的高度：当VerticalBannerView的height为wrap_content时，需要用自定义属性指定banner的高度
     * 如若不指定，则会默认取到25dp的值；如果指定VerticalBannerView的高度是确定值，则此值为无效
     */
    private float mDesignBannerHeight;
    /**
     * 当前banner条目的高度
     */
    private float mBannerHeight;
    private int mGap = 4000;
    private int mAnimDuration = 1000;

    private BaseBannerAdapter mAdapter;


    private View mFirstView;
    private View mSecondView;

    private int mPosition;

    private boolean isStarted;
    private Paint mDebugPaint;

    /**
     * <enum name="alpha_in_out" value="0" />//淡入淡出
     * <enum name="down_in_up_out" value="1" />//底部进入顶部退出
     */
    private int mAnimateType = 0;


    public VerticalBannerView(Context context) {
        this(context, null);
    }

    public VerticalBannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }


    /**
     * bannerHeight banner的高度
     * animDuration 每次切换动画时间
     * gap banner切换时间
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        setOrientation(VERTICAL);
        mDebugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.VerticalBannerView);
        mGap = array.getInteger(R.styleable.VerticalBannerView_gap, mGap);
        mAnimDuration = array.getInteger(R.styleable.VerticalBannerView_anim_duration,
                mAnimDuration);
        mDesignBannerHeight = array.getDimension(R.styleable.VerticalBannerView_banner_height,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25,
                        getResources().getDisplayMetrics()));
        mAnimateType = array.getInt(R.styleable.VerticalBannerView_banner_animate_type,
                mAnimateType);
        if (mGap <= mAnimDuration) {
            mGap = 4000;
            mAnimDuration = 1000;
        }
        array.recycle();
    }

    /**
     * 设置banner的数据
     */
    public void setAdapter(BaseBannerAdapter adapter) {
        if (adapter == null) {
            throw new RuntimeException("adapter must not be null");
        }
        if (mAdapter != null) {
            throw new RuntimeException("you have already set an Adapter");
        }
        this.mAdapter = adapter;
        mAdapter.setOnDataChangedListener(this);
        setupAdapter();
    }

    /**
     * 获取adapter 可判断是否setAdapter初始化
     */
    public BaseBannerAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * @return 是开始轮播
     */
    public boolean isStarted() {
        return isStarted;
    }

    public void start() {
        if (mAdapter == null) {
            throw new RuntimeException("you must call setAdapter() before start");
        }
        if (mAnimateType != 0) {
            post(() -> {
                if (!isStarted && mAdapter.getCount() > 1) {
                    isStarted = true;
                    postDelayed(mRunnable, mGap);
                } else {
                    isStarted = false;
                }
            });
        } else {
            post(() -> {
                if (!isStarted && mAdapter.getCount() >= 1) {
                    isStarted = true;
                    postDelayed(mRunnable, mGap);
                } else {
                    isStarted = false;
                }
            });
        }

    }

    public void stop() {
        isStarted = false;
        if (animatorSet != null && animatorSet.isRunning()) {
            animatorSet.end();
        }
        removeCallbacks(mRunnable);
    }

    private Drawable background;

    private void setupAdapter() {
        removeAllViews();
        if (LayoutParams.WRAP_CONTENT == getLayoutParams().height) {
            mBannerHeight = mDesignBannerHeight;
        } else {
            mBannerHeight = getHeight();
        }
        if (mAnimateType != 0) {
            if (mAdapter.getCount() < 1) {
                setBackgroundResource(android.R.color.transparent);
            } else if (mAdapter.getCount() == 1) {
                mFirstView = mAdapter.getView(this);
                addView(mFirstView);
                mFirstView.getLayoutParams().height = (int) mBannerHeight;
                mAdapter.setItem(mFirstView, mAdapter.getItem(0));
                setBackgroundDrawable(mFirstView.getBackground());
            } else {
                mFirstView = mAdapter.getView(this);
                mSecondView = mAdapter.getView(this);
                mFirstView.setTag("mFirstView");
                mSecondView.setTag("mSecondView");
                addView(mFirstView);
                addView(mSecondView);
                mFirstView.getLayoutParams().height = (int) mBannerHeight;
                mSecondView.getLayoutParams().height = (int) mBannerHeight;
                mAdapter.setItem(mFirstView, mAdapter.getItem(0));
                mAdapter.setItem(mSecondView, mAdapter.getItem(1));
                setBackgroundDrawable(mFirstView.getBackground());
                mPosition = 1;
                isStarted = false;
            }
        } else {
            post(() -> {
                if (mAdapter.getCount() < 1) {
                    setBackgroundResource(android.R.color.transparent);
                } else if (mAdapter.getCount() == 1) {
                    mFirstView = mAdapter.getView(VerticalBannerView.this);
                    if (mFirstView != null) {
                        background = mFirstView.getBackground();
                        mFirstView.setBackgroundResource(android.R.color.transparent);
                        addView(mFirstView);
                        mFirstView.getLayoutParams().height = (int) mBannerHeight;
                        mAdapter.setItem(mFirstView, mAdapter.getItem(0));
                        setBackground(background);
                    }

                } else {
                    mFirstView = mAdapter.getView(VerticalBannerView.this);
                    mSecondView = mAdapter.getView(VerticalBannerView.this);
                    if (mFirstView != null && mSecondView != null) {
                        background = mFirstView.getBackground();
                        mFirstView.setBackgroundResource(android.R.color.transparent);
                        mSecondView.setBackgroundResource(android.R.color.transparent);
                        addView(mFirstView);
                        addView(mSecondView);
                        mFirstView.getLayoutParams().height = (int) mBannerHeight;
                        mSecondView.getLayoutParams().height = (int) mBannerHeight;
                        mAdapter.setItem(mFirstView, mAdapter.getItem(0));
                        mAdapter.setItem(mSecondView, mAdapter.getItem(1));
                        setBackground(background);
                        mPosition = 1;
                        isStarted = false;
                    }
                }
            });
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (LayoutParams.WRAP_CONTENT == getLayoutParams().height) {
            getLayoutParams().height = (int) mDesignBannerHeight;
            mBannerHeight = mDesignBannerHeight;
        } else {
            mBannerHeight = getHeight();
        }
        if (isInEditMode()) {
            setBackgroundColor(Color.GRAY);
            return;
        }
        if (mFirstView != null) {
            mFirstView.getLayoutParams().height = (int) mBannerHeight;
        }
        if (mSecondView != null) {
            mSecondView.getLayoutParams().height = (int) mBannerHeight;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode()) {
            mDebugPaint.setColor(Color.WHITE);
            mDebugPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16,
                    getResources().getDisplayMetrics()));
            mDebugPaint.setStyle(Paint.Style.STROKE);
            canvas.drawText("banner is here", 20, getHeight() * 2 / 3, mDebugPaint);
        }
    }

    @Override
    public void onChanged() {
        stop();
        setupAdapter();
        start();
    }


    private void performSwitch() {
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(mFirstView, "translationY",
                mFirstView.getTranslationY() - mBannerHeight);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mSecondView, "translationY",
                mSecondView.getTranslationY() - mBannerHeight);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animator1, animator2);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mFirstView.setTranslationY(0);
                mSecondView.setTranslationY(0);
                View removedView = getChildAt(0);
                mPosition++;
                mAdapter.setItem(removedView, mAdapter.getItem(mPosition % mAdapter.getCount()));
                removeView(removedView);
                addView(removedView, 1);
            }

        });
        set.setDuration(mAnimDuration);
        set.start();
    }

    private AnimRunnable mRunnable = new AnimRunnable();
    ObjectAnimator animator3;
    ObjectAnimator animator4;
    AnimatorListenerAdapter animator3Listener;
    AnimatorListenerAdapter animator4Listener;
    AnimatorSet animatorSet;

    private class AnimRunnable implements Runnable {

        @Override
        public void run() {
            if (isStarted) {
                switch (mAnimateType) {
                    case 0://淡入淡出
                        if (animator3 == null) {
                            animator3 = ObjectAnimator.ofFloat(VerticalBannerView.this, "alpha"
                                    , 1f, 0f);
                            animator3.setDuration(mAnimDuration);
                            animator3Listener = new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    VerticalBannerView.this.setVisibility(INVISIBLE);
                                    if (!isStarted) {
                                        return;
                                    }
                                    if (mAdapter.getCount() < 1) {
                                        //不操作
                                    } else if (mAdapter.getCount() == 1) {

                                    } else {
                                        View removedView = getChildAt(0);
                                        mPosition++;
                                        mAdapter.setItem(removedView,
                                                mAdapter.getItem(mPosition % mAdapter.getCount()));
                                        removeView(removedView);
                                        if (getChildCount() >= 1) {
                                            addView(removedView, 1);
                                        }
                                    }

                                }

                            };
                            animator3.addListener(animator3Listener);
                        }
                        if (animator4 == null) {
                            animator4 = ObjectAnimator.ofFloat(VerticalBannerView.this, "alpha"
                                    , 0f, 1f);
                            animator4.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    VerticalBannerView.this.setVisibility(VISIBLE);
                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    if (!isStarted) {
                                        VerticalBannerView.this.setVisibility(INVISIBLE);
                                        return;
                                    }
                                    postDelayed(mRunnable, mGap);
                                }
                            });
                            animator4.setDuration(mAnimDuration);
                        }
                        if (animatorSet == null) {
                            animatorSet = new AnimatorSet();
                            animatorSet.play(animator4).after(animator3).after(6000);
                        }
                        if (isStarted) {
                            animatorSet.start();
                        } else {
                            VerticalBannerView.this.setVisibility(INVISIBLE);
                        }
                        break;
                    case 1://底部进入顶部退出
                        if (mAdapter.getCount() < 0) {
                            //不操作
                        } else if (mAdapter.getCount() == 1) {
                            if (getChildCount() >= 1) {
                                mAdapter.setItem(getChildAt(0),
                                        mAdapter.getItem(mPosition % mAdapter.getCount()));
                            }
                        } else {
                            animator3 = ObjectAnimator.ofFloat(mFirstView, "translationY",
                                    mFirstView.getTranslationY() - mBannerHeight);
                            animator4 = ObjectAnimator.ofFloat(mSecondView, "translationY",
                                    mSecondView.getTranslationY() - mBannerHeight);
                            animatorSet = new AnimatorSet();
                            animatorSet.playTogether(animator3, animator4);
                            animatorSet.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    if (!isStarted) {
                                        return;
                                    }
                                    mFirstView.setTranslationY(0);
                                    mSecondView.setTranslationY(0);
                                    View removedView = getChildAt(0);
                                    mPosition++;
                                    mAdapter.setItem(removedView,
                                            mAdapter.getItem(mPosition % mAdapter.getCount()));
                                    removeView(removedView);
                                    if (getChildCount() >= 1) {
                                        addView(removedView, 1);
                                    }
                                    postDelayed(AnimRunnable.this, mGap);

                                }

                            });
                            animatorSet.setDuration(mAnimDuration);
                            if (isStarted) {
                                animatorSet.start();
                            }

                        }
                        break;
                    default:
                        break;
                }
            } else {
                removeCallbacks(this);
            }


        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
