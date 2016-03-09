package perseverance.li.gradient.color.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import perseverance.li.gradient.color.R;

/**
 * ---------------------------------------------------------------
 * Author: LiYi
 * Create: 16-3-8 19:43
 * ---------------------------------------------------------------
 * Describe:
 * 渐变色自定义View
 * ---------------------------------------------------------------
 * Changes:
 * ---------------------------------------------------------------
 * 16-3-8 19 : Create by LiYi
 * ---------------------------------------------------------------
 */
public class GradientView extends View {

    private static final String TAG = "GradientView";
    /**
     * 梯度背景
     */
    public static final int[] GRADIENT_COLOR_BG = new int[]{
            R.drawable.main_bg_excellent,
            R.drawable.main_bg_well,
            R.drawable.main_bg_medium,
            R.drawable.main_bg_poor
    };

    /**
     * 梯度值索引位置
     */
    private int mGradientColorLevel = 0;
    /**
     * drawable 软引用缓存
     */
    private HashMap<Integer, SoftReference<Drawable>> drawableCacheMap = new HashMap<>();
    /**
     * 蒙版
     */
    private Bitmap mBitmapCover;
    /**
     * 蒙版高度
     */
    private int mCoverHeight;
    /**
     * 蒙版Paint
     */
    private Paint mCoverPaint;
    /**
     * 当前梯度值对应的bitmap
     */
    private Bitmap mCurrentGradientBitmap;
    /**
     * 左侧padding
     */
    private float mPaddingLeft;
    /**
     * 右侧padding
     */
    private float mPaddingRight;
    /**
     * 顶部padding
     */
    private float mPaddingTop;
    /**
     * 底部padding
     */
    private float mPaddingBottom;
    /**
     * 实际view宽度
     */
    private float mViewWidth;
    /**
     * 实际view高度
     */
    private float mViewHeight;
    /**
     * 实际view的矩形
     */
    private Rect mViewRect;
    /**
     * view执行动画时，动态top值
     */
    private int mAnimTop = 0;
    /**
     * 两个相交图像，图层的混合模式
     * http://blog.csdn.net/edisonlg/article/details/7084977
     */
    private PorterDuffXfermode mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    /**
     * 动画开始
     */
    private boolean mStartAnim;
    /**
     * 计算色值百分比，来确定动画状态
     */
    private float mPercent = 0.0f;
    /**
     * 动画执行Task
     */
    private AnimTask mAnimTask;
    /**
     * 延迟动画Task,主要是在开始动画时如果动画在执行时，需要等上个动画执行完后，在执行
     */
    private Runnable mPendingAnimTask;

    public GradientView(Context context) {
        super(context);
    }

    public GradientView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GradientView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 初始化view数据
     */
    private void initCover() {
        //初始化蒙版bitmap
        mBitmapCover = BitmapFactory.decodeResource(getResources(), R.drawable.main_bg_cover);
        mCoverHeight = mBitmapCover.getHeight();
        mCoverPaint = new Paint();
        mCoverPaint.setAntiAlias(true);
        mCoverPaint.setFilterBitmap(false);
    }

    /**
     * 数据重新计算
     */
    private void reset() {
        mPaddingTop = getPaddingTop();
        mPaddingBottom = getPaddingBottom();
        mPaddingLeft = getPaddingLeft();
        mPaddingRight = getPaddingRight();

        mViewWidth = getWidth() - mPaddingLeft - mPaddingRight;
        mViewHeight = getHeight() - mPaddingTop - mPaddingBottom;
        mViewRect = new Rect(0, 0, (int) mViewWidth, (int) mViewHeight);

        //初始化梯度背景bitmap
        if (mCurrentGradientBitmap == null) {
            mCurrentGradientBitmap = BitmapFactory.decodeResource(getResources(), GRADIENT_COLOR_BG[mGradientColorLevel]);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmapCover == null) {
            initCover();
        }

        if (mBitmapCover != null && mCurrentGradientBitmap != null) {
            //保存view层级
            int sc = canvas.saveLayer(0, 0, mViewWidth, mViewHeight, null,
                    Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
                            | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                            | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
                            | Canvas.CLIP_TO_LAYER_SAVE_FLAG);

            canvas.drawBitmap(mBitmapCover, 0, mAnimTop, mCoverPaint);
            mCoverPaint.setXfermode(mPorterDuffXfermode);
            canvas.drawBitmap(mCurrentGradientBitmap, mViewRect, mViewRect, mCoverPaint);
            mCoverPaint.setXfermode(null);
            canvas.restoreToCount(sc);
        }
        if (mStartAnim) {
            reset();
            if (mAnimTask == null) {
                mAnimTask = new AnimTask();
            }
            mAnimTask.startTask();
            mStartAnim = false;
        }
        super.onDraw(canvas);
    }

    /**
     * 开始执行梯度变化动画
     */
    public void startGradientAnim(final int level) {
        //过滤掉无用的level值
        if (level == mGradientColorLevel || level < 0 || level > GRADIENT_COLOR_BG.length - 1) {
            return;
        }

        if (isAnimRunning()) {
            mPendingAnimTask = new Runnable() {
                @Override
                public void run() {
                    mCurrentGradientBitmap = null;
                    mGradientColorLevel = level;
                    mStartAnim = true;
                    invalidate();
                }
            };
            return;
        }
        mCurrentGradientBitmap = null;
        mGradientColorLevel = level;
        mStartAnim = true;
        invalidate();
    }

    private boolean isAnimRunning() {
        return mPercent != 0.0f && mPercent != 1.0f;
    }

    /**
     * 获取背景图的Drawable
     *
     * @param index
     * @return
     */
    private Drawable getCacheDrawable(int index) {
        SoftReference<Drawable> softRef = drawableCacheMap.get(index);
        Drawable drawable;
        if (softRef == null) {
            drawable = getResources().getDrawable(GRADIENT_COLOR_BG[index]);
            softRef = new SoftReference<>(drawable);
            drawableCacheMap.put(index, softRef);
        } else {
            drawable = softRef.get();
        }
        return drawable;
    }

    private class AnimTask implements Runnable {

        /**
         * 动画开始时间
         */
        private long mAnimStartTime;
        /**
         * 动画执行最长时间
         */
        private long ANIM_MAX_TIME = 2500;

        public void startTask() {
            mAnimStartTime = System.currentTimeMillis();
            mPercent = 0.0f;
            onAnimStart();
            post(this);
        }

        @Override
        public void run() {
            long lastTime = System.currentTimeMillis() - mAnimStartTime;
            //计算蒙版top值
            mAnimTop = (int) (mViewHeight - (mCoverHeight * mPercent));
            //计算动画执行百分比
            mPercent = lastTime * 1.0f / ANIM_MAX_TIME;
            invalidate();
            if (lastTime > ANIM_MAX_TIME) {
                mPercent = 1.0f;
                mAnimTop = (int) mViewHeight - mCoverHeight;
                invalidate();
                Drawable bgDrawable = getCacheDrawable(mGradientColorLevel);
                setBackground(bgDrawable);
                onAnimEnd();
                return;
            }
            post(this);
        }
    }

    private void onAnimStart() {
    }

    private void onAnimEnd() {
        if (mPendingAnimTask != null) {
            post(mPendingAnimTask);
            mPendingAnimTask = null;
        }
    }
}
