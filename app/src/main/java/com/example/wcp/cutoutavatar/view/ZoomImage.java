package com.example.wcp.cutoutavatar.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Created by wcp on 2016/8/26.
 */
public class ZoomImage extends ImageView implements ViewTreeObserver.OnGlobalLayoutListener {
    String TAG = ZoomImage.class.getSimpleName();

    private Matrix mScaleMatrix = new Matrix();
    private boolean once = true;
    public static final float SCALE_MAX = 2.0f;
    public static final float SCALE_MIN = 1.0f;
    private MasklayerCircleView masklayerCircleView;
    private float mTargetScale = SCALE_MIN;

    /**
     * 用于存放矩阵的9个值
     */
    private final float[] matrixValues = new float[9];

    public ZoomImage(Context context) {
        this(context, null);
    }

    public ZoomImage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setScaleType(ScaleType.MATRIX);
    }

    public boolean isScaleMaxOrInit() {
        return (mTargetScale > SCALE_MIN - 0.01 && mTargetScale < SCALE_MIN + 0.01);
                //||(mTargetScale < SCALE_MAX + 0.1 && mTargetScale > SCALE_MAX - 0.1) ;
    }

    /**
     * 缩放最大比例
     * @return
     */
    public boolean isScaleMax() {
        return (mTargetScale > SCALE_MIN - 0.01 && mTargetScale < SCALE_MIN + 0.01);
    }

    public boolean isScaleMin() {
        return  mTargetScale == SCALE_MIN;
    }

    public void setMasklayer(MasklayerCircleView masklayerCircleView) {
        this.masklayerCircleView = masklayerCircleView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void setScaleMatrix(float scale, float x, float y) {
        mScaleMatrix.set(super.getImageMatrix());
        Log.i(TAG, "setScaleMatrix: scale="+scale+"||getScale()="+getScale());

        postDelayed(new AutoScaleRunnable(scale, x, y), 16);
    }

    public void setTranslate(float dx,float dy) {
        mScaleMatrix.postTranslate(-dx, -dy);
        checkBorderAndCenterWhenScale();
        setImageMatrix(mScaleMatrix);
        masklayerCircleView.setShiftRange(getMatrixRectF());
    }

    /**
     * 自动缩放的任务
     * @author zhy
     */
    private class AutoScaleRunnable implements Runnable {
        static final float BIGGER = 1.07f;
        static final float SMALLER = 0.93f;
        private float tmpScale;

        /**
         * 缩放的中心
         */
        private float x;
        private float y;

        /**
         * 传入目标缩放值，根据目标值与当前值，判断应该放大还是缩小
         *
         * @param targetScale
         */
        public AutoScaleRunnable(float targetScale, float x, float y) {
            if (targetScale >= SCALE_MAX) {
                targetScale = SCALE_MAX;
            } else if (targetScale <= SCALE_MIN) {
                targetScale = SCALE_MIN;
            }
            mTargetScale = targetScale;
            this.x = x;
            this.y = y;
            if (getScale() < mTargetScale) {
                tmpScale = BIGGER;
            } else {
                tmpScale = SMALLER;
            }
        }

        @Override
        public void run() {
            // 进行缩放
            mScaleMatrix.postScale(tmpScale, tmpScale, x, y);
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);

            final float currentScale = getScale();
//            Log.i(TAG, "setScaleMatrix: tmpScale="+tmpScale+"||getScale()="+currentScale);

            // 如果值在合法范围内，继续缩放
            if (((tmpScale > 1f) && (currentScale < mTargetScale))
                    || ((tmpScale < 1f) && (mTargetScale < currentScale))) {
                ZoomImage.this.postDelayed(this, 16);
            } else
            // 设置为目标的缩放比例
            {
                final float deltaScale = mTargetScale / currentScale;
                mScaleMatrix.postScale(deltaScale, deltaScale, x, y);

                checkBorderAndCenterWhenScale();
                setImageMatrix(mScaleMatrix);
                Log.i(TAG, "setScaleMatrix: setLast"+"getScale()="+getScale());
                masklayerCircleView.setShiftRange(getMatrixRectF());
            }
        }
    }
    /**
     * 在缩放时，进行图片显示范围的控制
     */
    private void checkBorderAndCenterWhenScale() {
        RectF rect = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();

        // 如果宽或高大于屏幕，则控制范围
        if (rect.width() >= width) {
            if (rect.left > 0) {
                deltaX = -rect.left;
            }
            if (rect.right < width) {
                deltaX = width - rect.right;
            }
        }
        if (rect.height() >= height) {
            if (rect.top > 0) {
                deltaY = -rect.top;
            }
            if (rect.bottom < height) {
                deltaY = height - rect.bottom;
            }
        }
        // 如果宽或高小于屏幕，则让其居中
        if (rect.width() < width) {
            deltaX = width * 0.5f - rect.right + 0.5f * rect.width();
        }
        if (rect.height() < height) {
            deltaY = height * 0.5f - rect.bottom + 0.5f * rect.height();
        }
        Log.e(TAG, "deltaX = " + deltaX + " , deltaY = " + deltaY);

        mScaleMatrix.postTranslate(deltaX, deltaY);
    }


    /**
     * 根据当前图片的Matrix获得图片的范围
     *
     * @return
     */
    public RectF getMatrixRectF() {
        Matrix matrix = mScaleMatrix;
        RectF rect = new RectF();
        Drawable d = getDrawable();
        if (null != d) {
            rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rect);
        }
        return rect;
    }

    /**
     * 获得当前的缩放比例
     *
     * @return
     */
    public final float getScale() {
        mScaleMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    public void translateToCenter() {
        Drawable d = getDrawable();
        if (d == null)
            return;

        Log.e(TAG, d.getIntrinsicWidth() + " , " + d.getIntrinsicHeight() + ",getWidth()=" + getWidth() + ",getH()" + getHeight());
        int width = getWidth();
        int height = getHeight();
        // 拿到图片的宽和高
        int dw = d.getIntrinsicWidth();
        int dh = d.getIntrinsicHeight();

        mScaleMatrix.postTranslate((width - dw) / 2, (height - dh) / 2);

        setImageMatrix(mScaleMatrix);
    }

    @Override
    public void onGlobalLayout() {
        if (once) {
            Log.i(TAG, "onGlobalLayout: ");
            translateToCenter();
            once = false;
        }
    }
}
