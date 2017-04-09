package com.example.wcp.cutoutavatar.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.example.wcp.cutoutavatar.R;
import com.example.wcp.cutoutavatar.util.DensityUtil;
import com.example.wcp.cutoutavatar.util.LogUtil;


/**
 * Created by wcp on 2016/8/11.
 */
public class MasklayerCircleView extends View {
    private String TAG = MasklayerCircleView.class.getSimpleName();

    private int CIRCLE_TYPE = 0;
    private int RECT_TYPE = 1;
    private int MIN_CIRCLE_RADIUS = 80;

    private int INIT_TYPE = 0;
    private int TOP_SCALE_TYPE = 1;  //顶部扩大
    private int BOTTOM_SCALE_TYPE = 2;  //底部扩大
    private int LEFT_SCALE_TYPE = 3;    //左边扩大
    private int RIGHT_SCALE_TYPE = 4;   //右边扩大
    private int SCALE_TYPE = 5;   //任意方向扩大
    private int SHIFT_TYPE = 6;   //移动
    private int mScaleType;  //缩放类型 0 任一点缩放 1边框四点缩放
    private boolean mEntityCircleOnLine;  //4个实体圆在边上 true 显现 false 隐藏

    private int OVERALL_SHIFT_TYPE = 5;
    private int mChangeType = INIT_TYPE;
    private float mCenterX;
    private float mCenterY;
    private float mRadius;
    private int mBigCircleColor;
    private int mSmallCircleColor;
    private int mDottedLineColor;
    private float smallCircleRadius;
    private int mOutPutShapeType;  //输出形状 0 圆形 1矩形
    private boolean mDashLineRect;  //正方形虚线  默认false是没有 true 有

    //改变前的圆心坐标
    private float mLastCenterX, mLastCenterY, mLastRadius;
    private float mBeforeChangeRadius;  //处理事件之前半径
    private Paint mBigCirclePaint, mSmallCirclePaint, mLinePaint;
    private int smallTouchRangeRadius = 40;   //上下左右边触摸范围
    private Rect topRect, bottomRect, leftRect, rightRect;
    private Context mContext;
    private float lastX;
    private float lastY;
    private float offsetX = 0;
    private float offsetY = 0;
    private float mMinRadius;
    private float mMaxRadius;
    private Paint mPaint = new Paint();
    //画重叠遮罩
    private Paint masklayerPaint;
    //根据图片的宽高来，设置截图圆移动范围
    private float mShiftBorderTop;
    private float mShiftBorderLeft;
    private float mShiftBorderRight;
    private float mShiftBorderBottom;
    private Canvas topCircleCanvas = new Canvas();
    private Canvas bottomRectCanvas = new Canvas();
    private float mTouchCenterX;
    private float mTouchCenterY;
    private float mLastTouchCenterX;
    private float mLastTouchCenterY;
    private float mTouchX, mTouchY;   //触碰点的坐标
    private boolean isZooming;
    private boolean isTracting;
    private ZoomImage zoomImage;
    private float screenW, screenH;
    private boolean touchSidelineUpOrBottom;
    private boolean touchSidelineLeftOrRight;

    /**
     * 触碰点临时cos值
     */
    private float mTempCos;

    public MasklayerCircleView(Context context) {
        this(context, null);
    }

    public MasklayerCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MasklayerCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.Masklayer);

        mCenterX = mTypedArray.getFloat(R.styleable.Masklayer_centerX, 0);
        mCenterY = mTypedArray.getFloat(R.styleable.Masklayer_centerY, 0);
        mRadius = mTypedArray.getFloat(R.styleable.Masklayer_bigCircleRadius, 0);
        smallCircleRadius = mTypedArray.getFloat(R.styleable.Masklayer_smallCircleRadius, 17);
        mMinRadius = mTypedArray.getFloat(R.styleable.Masklayer_bigCircleMinRadius, MIN_CIRCLE_RADIUS);
        mMaxRadius = mTypedArray.getFloat(R.styleable.Masklayer_bigCircleMaxRadius, (DensityUtil.getScreenSize((Activity) mContext).widthPixels / 2));
        mOutPutShapeType = mTypedArray.getInt(R.styleable.Masklayer_outPutShape, CIRCLE_TYPE);
        mDashLineRect = mTypedArray.getBoolean(R.styleable.Masklayer_dashLineRect, false);
        mScaleType = mTypedArray.getInteger(R.styleable.Masklayer_scaleType, 0);
        mEntityCircleOnLine = mTypedArray.getBoolean(R.styleable.Masklayer_entityCircleOnLine, false);

        mBigCircleColor = mTypedArray.getColor(R.styleable.Masklayer_bigCircleColor, getResources().getColor(R.color.circle_color));
        mSmallCircleColor = mTypedArray.getColor(R.styleable.Masklayer_smallCircleColor, getResources().getColor(R.color.circle_color));
        mDottedLineColor = mTypedArray.getColor(R.styleable.Masklayer_dottedLineColor, getResources().getColor(R.color.circle_color));
        init();
    }

    public void setZoomImage(ZoomImage zoomImage) {
        this.zoomImage = zoomImage;
    }

    private void init() {
        screenW = DensityUtil.getScreenSize((Activity) mContext).widthPixels;
        screenH = DensityUtil.getScreenSize((Activity) mContext).heightPixels;

        mPaint.setAntiAlias(true);//设置消除锯齿

        /**俩图片相交模式*/
        masklayerPaint = new Paint();
        masklayerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        /**大圆*/
        mBigCirclePaint = new Paint();
        mBigCirclePaint.setStyle(Paint.Style.STROKE);
        mBigCirclePaint.setAntiAlias(true);
        mBigCirclePaint.setColor(mBigCircleColor);
        mBigCirclePaint.setStrokeWidth(4);

        /**四个小圆*/
        mSmallCirclePaint = new Paint();
        mSmallCirclePaint.setAntiAlias(true);
        mSmallCirclePaint.setColor(mSmallCircleColor);
        mSmallCirclePaint.setShadowLayer(10.0f, 0.0f, 2.0f, 0xFF000000);

        /**虚线*/
        mLinePaint = new Paint();
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(3);
        mLinePaint.setColor(mDottedLineColor);
        PathEffect effects = new DashPathEffect(new float[]{5, 5, 5, 5}, 1);
        mLinePaint.setPathEffect(effects);

        topRect = new Rect();
        bottomRect = new Rect();
        leftRect = new Rect();
        rightRect = new Rect();

        updateRect();
    }

    /**
     * 获取截图蒙板
     *
     * @return
     */
    private Bitmap getMaskLayer() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ALPHA_8);
        bottomRectCanvas.setBitmap(bitmap);
        mPaint.setColor(mContext.getResources().getColor(R.color.transparent_black));
        bottomRectCanvas.drawRect(0, 0, DensityUtil.getScreenSize((Activity) mContext).widthPixels
                , DensityUtil.getScreenSize((Activity) mContext).heightPixels, mPaint);

        Bitmap bitmap1 = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ALPHA_8);
        topCircleCanvas.setBitmap(bitmap1);
        mPaint.reset();
        if (mOutPutShapeType == CIRCLE_TYPE) {
            topCircleCanvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
        } else {
            topCircleCanvas.drawRect(mCenterX - mRadius, mCenterY - mRadius, mCenterX + mRadius, mCenterY + mRadius, mPaint);
        }

        bottomRectCanvas.drawBitmap(bitmap1, 0, 0, masklayerPaint);
        bitmap1.recycle();
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Bitmap bitmap = getMaskLayer();
        mPaint.reset();
        canvas.drawBitmap(bitmap, 0, 0, mPaint);

        if (mOutPutShapeType == CIRCLE_TYPE) {
            canvas.drawCircle(mCenterX, mCenterY, mRadius, mBigCirclePaint);
        }

        if (isZooming && mOutPutShapeType == CIRCLE_TYPE) {
            canvas.drawCircle(mTouchCenterX, mTouchCenterY, smallCircleRadius, mSmallCirclePaint);
        }

        if (mDashLineRect) {
            canvas.drawRect((int) mCenterX - mRadius, (int) mCenterY - mRadius, (int) mCenterX + mRadius, (int) mCenterY + mRadius, mLinePaint);
        }

        if ((mScaleType == 1 || mEntityCircleOnLine) && (isZooming || isTracting)) {
            canvas.drawCircle(mCenterX, mCenterY - mRadius, smallCircleRadius, mSmallCirclePaint);
            canvas.drawCircle(mCenterX, mCenterY + mRadius, smallCircleRadius, mSmallCirclePaint);
            canvas.drawCircle(mCenterX - mRadius, mCenterY, smallCircleRadius, mSmallCirclePaint);
            canvas.drawCircle(mCenterX + mRadius, mCenterY, smallCircleRadius, mSmallCirclePaint);
        }

        updateRect();
        bitmap.recycle();
    }

    /**
     * 根据触碰点坐标，求出圆上相同角度的坐标
     * 设置触碰点坐标，以便在圆上画圆
     */
    private void setTouchCoordinate(boolean isInit, float x, float y) {
        if (isInit) {
            mTouchX = (x - mCenterX);
            mTouchY = (mCenterY - y);
            mTempCos = (float) (mTouchX / Math.sqrt(Math.pow(mTouchX, 2) + Math.pow(mTouchY, 2)));
            Log.i(TAG, " mTouchX=" + mTouchX + "||mTouchY=" + mTouchY + "||x=" + x + "||y=" + y);
        }

        mTouchCenterX = mRadius * mTempCos;
        mTouchCenterY = (float) Math.sqrt((Math.pow(mRadius, 2) - Math.pow(mTouchCenterX, 2)));

        mTouchCenterX += mCenterX;
        if (mTouchY > 0) {
            mTouchCenterY = mCenterY - mTouchCenterY;
        } else {
            mTouchCenterY = mCenterY + mTouchCenterY;
        }
    }

    private void initData() {
        touchSidelineUpOrBottom = false;
        touchSidelineLeftOrRight = false;
        isTracting = false;
        isZooming = false;
        mChangeType = INIT_TYPE;
        lastX = 0;
        lastY = 0;
    }

    /**
     * 是否可以拖动   触碰点在圆内
     *
     * @param x
     * @param y
     * @return
     */
    private boolean isCanDrag(float x, float y) {
        if (mOutPutShapeType == CIRCLE_TYPE) {
            float var1 = Math.abs(x - mCenterX);
            float var2 = Math.abs(y - mCenterY);
            return (float) Math.hypot(var1, var2) < mRadius - smallTouchRangeRadius / 2;
        } else if (mOutPutShapeType == RECT_TYPE) {
//            Rect rect = new Rect()
            if (mCenterX - mRadius + smallTouchRangeRadius / 2 < x && mCenterX + mRadius - smallTouchRangeRadius / 2 > x) {
                if (mCenterY - mRadius + smallTouchRangeRadius / 2 < y && mCenterY + mRadius - smallTouchRangeRadius / 2 > y) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 触碰圆的边界
     *
     * @return
     */
    private boolean isTouchSideline(float x, float y) {
        if (mOutPutShapeType == CIRCLE_TYPE) {
            float tempR = (float) Math.sqrt(Math.pow((x - mCenterX), 2) + Math.pow((mCenterY - y), 2));
            if (Math.abs(mRadius - tempR) < smallTouchRangeRadius / 2) {
                return true;
            }
        } else if (mOutPutShapeType == RECT_TYPE) {
            Rect leftRect = new Rect((int) (mCenterX - mRadius - smallTouchRangeRadius / 2), (int) (mCenterY - mRadius - smallTouchRangeRadius / 2),
                    (int) (mCenterX - mRadius + smallTouchRangeRadius / 2), (int) (mCenterY + mRadius + smallTouchRangeRadius / 2));
            Rect topRect = new Rect((int) (mCenterX - mRadius - smallTouchRangeRadius / 2), (int) (mCenterY - mRadius - smallTouchRangeRadius / 2),
                    (int) (mCenterX + mRadius + smallTouchRangeRadius / 2), (int) (mCenterY - mRadius + smallTouchRangeRadius / 2));
            Rect rightRect = new Rect((int) (mCenterX + mRadius - smallTouchRangeRadius / 2), (int) (mCenterY - mRadius - smallTouchRangeRadius / 2),
                    (int) (mCenterX + mRadius + smallTouchRangeRadius / 2), (int) (mCenterY + mRadius + smallTouchRangeRadius / 2));
            Rect bottomRect = new Rect((int) (mCenterX - mRadius - smallTouchRangeRadius / 2), (int) (mCenterY + mRadius - smallTouchRangeRadius / 2),
                    (int) (mCenterX + mRadius + smallTouchRangeRadius / 2), (int) (mCenterY + mRadius + smallTouchRangeRadius / 2));
            if (topRect.contains((int) x, (int) y) || bottomRect.contains((int) x, (int) y)) {
                touchSidelineUpOrBottom = true;
                return true;
            }
            if (leftRect.contains((int) x, (int) y) || rightRect.contains((int) x, (int) y)) {
                touchSidelineLeftOrRight = true;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mScaleType == 0) {
                    lastX = x;
                    lastY = y;
                    isZooming = isTouchSideline(x, y);
                    if (isZooming) {
                        mBeforeChangeRadius = mRadius;
                        setTouchCoordinate(true, x, y);
                        mChangeType = SCALE_TYPE;
                        invalidate();
                    }

                    isTracting = isCanDrag(x, y);
                    if (isTracting) {
                        mChangeType = SHIFT_TYPE;
                    }
                    Log.i(TAG, "onTouchEvent: isZooming=" + isZooming + "||isTracting=" + isTracting);

                    return isZooming || isTracting;
                } else {
                    if (topRect.contains((int) x, (int) y)) {
                        mChangeType = TOP_SCALE_TYPE;
                    } else if (bottomRect.contains((int) x, (int) y)) {
                        mChangeType = BOTTOM_SCALE_TYPE;
                    } else if (leftRect.contains((int) x, (int) y)) {
                        mChangeType = LEFT_SCALE_TYPE;
                    } else if (rightRect.contains((int) x, (int) y)) {
                        mChangeType = RIGHT_SCALE_TYPE;
                    } else {
                        mChangeType = OVERALL_SHIFT_TYPE;
                    }
                    break;
                }
            case MotionEvent.ACTION_MOVE:
                offsetX = x - lastX;
                offsetY = y - lastY;
                mLastCenterX = mCenterX;
                mLastCenterY = mCenterY;
                mLastTouchCenterX = mTouchCenterX;
                mLastTouchCenterY = mTouchCenterY;

                mLastRadius = mRadius;
                changeSizeAndPositionAccordingType(mChangeType, x, y);
                checkRaius();
                checkCircleBounds();
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (isZooming) {
                    //背景图片扩大
                    scaleZoomImage();
                    float OUTPUT_LENGTH = 150;
                    changeTheCircleRadiusAuto(!zoomImage.isScaleMax() ? OUTPUT_LENGTH : mRadius);
                }
                initData();
                break;
        }
        invalidate();
        return true;
    }

    /**
     * 缩放图片
     */
    private void scaleZoomImage() {
        float scale = 0;   //缩放比例
        float factor = 0;
        if (mBeforeChangeRadius > mRadius) {
            if (zoomImage.getScale() != ZoomImage.SCALE_MAX) {
                factor = (ZoomImage.SCALE_MAX - zoomImage.getScale()) / (mBeforeChangeRadius - mMinRadius);
                scale = factor * Math.abs(mRadius - mBeforeChangeRadius);
                zoomImage.setScaleMatrix(zoomImage.getScale() + scale, mCenterX, mCenterY);
            }
        } else if (mBeforeChangeRadius < mRadius) {
            if (zoomImage.getScale() != ZoomImage.SCALE_MIN) {
                factor = (zoomImage.getScale() - ZoomImage.SCALE_MIN) / (mMaxRadius - mBeforeChangeRadius);
                scale = factor * Math.abs(mRadius - mBeforeChangeRadius);
                zoomImage.setScaleMatrix(zoomImage.getScale() - scale, mCenterX, mCenterY);
            }
        }
        LogUtil.showlog("&&&mScale=" + scale + "||factor=" + factor);
    }

    /**
     * 移动背景图
     */
    private void shiftImageAccordingCirclePosition() {
        float factorX = 0;
        float factorY = 0;
        RectF rectF = zoomImage.getMatrixRectF();

        int SHIFT_IMAGE_THRESHOLD = 70; //开始移动背景图，临界值（圆到屏幕的距离）
        if (mCenterX + mRadius > screenW - SHIFT_IMAGE_THRESHOLD) {
            if (rectF.right > screenW) {
                factorX = (rectF.right - screenW) / (screenW - (mCenterX + mRadius));
            }
        } else if (mCenterX - mRadius < SHIFT_IMAGE_THRESHOLD) {
            if (rectF.left < 0) {
                factorX = -rectF.left / (mCenterX - mRadius);
            }
        }
        if (mCenterY - mRadius < SHIFT_IMAGE_THRESHOLD) {
            if (rectF.top < 0) {
                factorY = -rectF.top / (mCenterY - mRadius);
            }
        } else if (mCenterY + mRadius > screenH - SHIFT_IMAGE_THRESHOLD) {
            if (rectF.bottom > screenH) {
                factorY = (rectF.bottom - screenH) / (screenH - (mCenterY + mRadius));
            }
        }
        zoomImage.setTranslate(offsetX * factorX, offsetY * factorY);
    }

    /**
     * 改变圆的y轴
     *
     * @param dstCenterY
     */
    private void changeTheCirclemCenterY(float dstCenterY) {
        ValueAnimator mAnimator = valueAnimatorFactory(mCenterY, dstCenterY, 400);

        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                mCenterY = animatedValue;
                invalidate();
            }
        });
        mAnimator.start();
    }

    /**
     * 改变圆的x轴
     *
     * @param dstCenterX
     */
    private void changeTheCirclemCenterX(float dstCenterX) {
        ValueAnimator mAnimator = valueAnimatorFactory(mCenterX, dstCenterX, 200);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                mCenterX = animatedValue;
                invalidate();
            }
        });
        mAnimator.start();
    }

    public ValueAnimator valueAnimatorFactory(float src, float dst, long duration) {
        ValueAnimator mAnimator = ValueAnimator.ofFloat(src, dst);
        mAnimator.setDuration(duration);
        mAnimator.setInterpolator(new DecelerateInterpolator());
        return mAnimator;
    }

    /**
     * 将圆改变到指定半径
     *
     * @param dstRadius
     */
    private void changeTheCircleRadiusAuto(float dstRadius) {
        ValueAnimator mAnimator = valueAnimatorFactory(mRadius, dstRadius, 400);

        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                mRadius = animatedValue;

                invalidate();
            }
        });
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                scaleCircleWithinImage();
                translateCircleWithinImage();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }

    /**
     * 缩小圆的半径以便在圆内
     */
    private void scaleCircleWithinImage() {
        RectF rectf = zoomImage.getMatrixRectF();

        if (rectf.height() < mRadius * 2) {
            changeTheCircleRadiusAuto(rectf.height() / 2);
        }
    }

    /**
     * 当圆位于图片外时，将其平移回图片内
     */
    private void translateCircleWithinImage() {
        RectF rectf = zoomImage.getMatrixRectF();
        if (rectf.top > mCenterY - mRadius) {
            changeTheCirclemCenterY(mCenterY + (rectf.top - (mCenterY - mRadius)));
        } else if (rectf.bottom < mCenterY + mRadius) {
            changeTheCirclemCenterY(mCenterY - (mCenterY + mRadius - rectf.bottom));
        }
        if (mCenterX - mRadius < 0) {
            changeTheCirclemCenterX(mRadius);
        } else if (mCenterX + mRadius > screenW) {
            changeTheCirclemCenterX(screenW - mRadius);
        }
    }

    /**
     * 根据不同类型对圆形状和位置进行改变
     *
     * @param x
     * @param y
     */
    private void changeSizeAndPositionAccordingType(int changeType, float x, float y) {
        if (changeType == TOP_SCALE_TYPE) {
/* 一边不动，改变圆心和半径 以下注释都是
                    float var = offsetY / 2;
                    mCenterY += var;
                    mRadius -= var;*/
            mRadius -= offsetY;
        } else if (changeType == BOTTOM_SCALE_TYPE) {
//                    float var = -(offsetY / 2);
//                    mCenterY -= var;
//                    mRadius -= var;
            mRadius += offsetY;
        } else if (changeType == RIGHT_SCALE_TYPE) {
//                    float var = -(offsetX / 2);
//                    mCenterX -= var;
//                    mRadius -= var;
            mRadius += offsetX;
        } else if (changeType == LEFT_SCALE_TYPE) {
//                    float var = offsetX / 2;
//                    mCenterX += var;
//                    mRadius -= var;
            mRadius -= offsetX;
        } else if (changeType == SCALE_TYPE) {
            changeCircleRaiusAccordingSlide(x, y);
            setTouchCoordinate(false, x, y);
        } else if (changeType == SHIFT_TYPE) {
            mCenterX += offsetX;
            mCenterY += offsetY;
            mTouchCenterX += offsetX;
            mTouchCenterY += offsetY;
            shiftImageAccordingCirclePosition();
            checkCircleWithinTheScreen();
        }
    }

    /**
     * 改变圆的大小
     *
     * @param x
     * @param y
     */
    private void changeCircleRaiusAccordingSlide(float x, float y) {
        float distance = (float) Math.sqrt(Math.pow(offsetX, 2) + Math.pow(offsetY, 2));

        if (mOutPutShapeType == CIRCLE_TYPE) {
            accordingTangent(x, y, distance);
            Log.i(TAG, " distance=" + distance + "||offsetX=" + offsetX + "||offsetY=" + offsetY);
        } else if (mOutPutShapeType == RECT_TYPE) {
            accordingSideline(x, y, distance);
        }
    }

    /**
     * 根据四边的方向
     */
    private void accordingSideline(float x, float y, float distance) {
        Log.i(TAG, "accordingSideline: x=" + x + "||y=" + y + "||distance=" + distance + "\n" +
                "mCenterY=" + mCenterY + "||mRadius=" + mRadius + "||mCenterY - mRadius=" + (mCenterY - mRadius));
        if (touchSidelineLeftOrRight) {
            if (x < mCenterX) {
                if (x < mCenterX - mRadius - smallTouchRangeRadius / 2) {
                    mRadius += distance;
                } else {
                    mRadius -= distance;
                }
            } else {
                if (x > mCenterX + mRadius + smallTouchRangeRadius / 2) {
                    mRadius += distance;
                } else {
                    mRadius -= distance;
                }
            }
        }
        if (touchSidelineUpOrBottom) {
            if (y < mCenterY) {
                if (y < mCenterY - mRadius) {
                    mRadius += distance;
                } else {
                    mRadius -= distance;
                }
            } else {
                if (y > mCenterY + mRadius + smallTouchRangeRadius / 2) {
                    mRadius += distance;
                } else {
                    mRadius -= distance;
                }
            }
        }
    }

    /**
     * 根据圆的切线，来判断圆是要扩大还是缩小
     *
     * @param x
     * @param y
     * @param distance
     */
    private void accordingTangent(float x, float y, float distance) {
        float k2 = -(mTouchCenterX - mCenterX) / (mTouchCenterY - mCenterY);
        float b = mTouchCenterY - k2 * mTouchCenterX;
        float var = x * k2 + b;
//        Log.i(TAG, "accordingSlope: k2="+k2);
//        Log.i(TAG, "accordingSlope: b=" + b);
//        Log.i(TAG, "accordingSlope: varY=" + var);

        if (y < mCenterY) {
            if (var > y) {
                mRadius += distance;
            } else {
                mRadius -= distance;
            }
        } else if (y > mCenterY) {
            if (var > y) {
                mRadius -= distance;
            } else {
                mRadius += distance;
            }
        }
    }

    public float getmCenterX() {
        return mCenterX;
    }

    public float getmCenterY() {
        return mCenterY;
    }

    public float getmRadius() {
        return mRadius;
    }

    /**
     * 图像大小缩变了之后，设置移动范围
     *
     * @param rect
     */
    public void setShiftRange(RectF rect) {
        mShiftBorderLeft = rect.left;
        mShiftBorderTop = rect.top;
        mShiftBorderRight = rect.right;
        mShiftBorderBottom = rect.bottom;
    }

    /**
     * 设置移动范围和初始话圆的位置和大小
     */
    public void setShiftRange(float photoWidth, float photoHigh) {
        mShiftBorderTop = (DensityUtil.getScreenSize((Activity) mContext).heightPixels - photoHigh) / 2;
        mShiftBorderBottom = mShiftBorderTop + photoHigh;
        mShiftBorderLeft = 0;
        mShiftBorderRight = photoWidth;
        if (photoWidth > photoHigh) {
            mRadius = photoHigh / 2;
            mCenterX = photoWidth / 2;
            mCenterY = mShiftBorderTop + mRadius;
        }
        invalidate();
    }

    /**
     * 更新上下左右拖拽点的范围
     */
    private void updateRect() {
        topRect.set((int) mCenterX - smallTouchRangeRadius, (int) (mCenterY - mRadius - smallTouchRangeRadius), (int) mCenterX + smallTouchRangeRadius, (int) (mCenterY - mRadius + smallTouchRangeRadius));
        bottomRect.set((int) mCenterX - smallTouchRangeRadius, (int) (mCenterY + mRadius - smallTouchRangeRadius), (int) mCenterX + smallTouchRangeRadius, (int) (mCenterY + mRadius + smallTouchRangeRadius));
        leftRect.set((int) (mCenterX - mRadius - smallTouchRangeRadius), (int) mCenterY - smallTouchRangeRadius, (int) (mCenterX - mRadius + smallTouchRangeRadius), (int) mCenterY + smallTouchRangeRadius);
        rightRect.set((int) (mCenterX + mRadius - smallTouchRangeRadius), (int) mCenterY - smallTouchRangeRadius, (int) (mCenterX + mRadius + smallTouchRangeRadius), (int) mCenterY + smallTouchRangeRadius);
    }

    /**
     * 设置圆的半径范围
     */
    private void checkRaius() {
        if (mRadius < mMinRadius) {
            mRadius = mMinRadius;
            mCenterX = mLastCenterX;
            mCenterY = mLastCenterY;
        } else if (mRadius > mMaxRadius) {
            mRadius = mMaxRadius;
            mCenterX = mLastCenterX;
            mCenterY = mLastCenterY;
        }
    }

    /**
     * @return
     */
    public int getmOutPutShapeType() {
        return mOutPutShapeType;
    }

    /**
     * 圆位于要截取的图片内
     */
    private void checkCircleBounds() {
        if (mCenterY - mRadius < mShiftBorderTop || mCenterY + mRadius > mShiftBorderBottom) {
            mCenterY = mLastCenterY;
            mRadius = mLastRadius;
            mTouchCenterY = mLastTouchCenterY;
        }
        if (mCenterX - mRadius < mShiftBorderLeft || mCenterX + mRadius > mShiftBorderRight) {
            mCenterX = mLastCenterX;
            mRadius = mLastRadius;
            mTouchCenterX = mLastTouchCenterX;
        }
    }

    /**
     * 圆位于要屏幕内,由于移动圆到临界值的时候，背景图会跟着移动，从而使圆在平面内
     */
    private void checkCircleWithinTheScreen() {
        if (mCenterY - mRadius < 0 || mCenterY + mRadius > screenH) {
            mCenterY = mLastCenterY;
            mRadius = mLastRadius;
            mTouchCenterY = mLastTouchCenterY;
        }
        if (mCenterX - mRadius < 0 || mCenterX + mRadius > screenW) {
            mCenterX = mLastCenterX;
            mRadius = mLastRadius;
            mTouchCenterX = mLastTouchCenterX;
        }
    }
}
