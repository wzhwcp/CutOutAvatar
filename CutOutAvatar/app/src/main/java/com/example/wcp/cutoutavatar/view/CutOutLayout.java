package com.example.wcp.cutoutavatar.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.wcp.cutoutavatar.R;
import com.example.wcp.cutoutavatar.util.BitmapUtil;
import com.example.wcp.cutoutavatar.util.DensityUtil;


/**
 * Created by wcp on 2016/8/23.
 */
public class CutOutLayout extends RelativeLayout {
    private ZoomImage showPic;
    private MasklayerCircleView masklayerCircleView;
    private Context mContext;

    public CutOutLayout(Context context) {
        this(context,null);
    }

    public CutOutLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CutOutLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        masklayerCircleView = (MasklayerCircleView)findViewById(R.id.mask_layer);
        showPic = (ZoomImage) findViewById(R.id.show_image);
        showPic.setMasklayer(masklayerCircleView);
        masklayerCircleView.setZoomImage(showPic);
        super.onFinishInflate();
    }

    /**
     * 设置将要被截取的图片
     * @param bitmap
     */
    public void setShowPic(Bitmap bitmap) {
        setVisibility(VISIBLE);
        masklayerCircleView.setVisibility(VISIBLE);
        Bitmap tempBitmap = BitmapUtil.compressBitmap(bitmap,(Activity) mContext);
        showPic.setImageBitmap(tempBitmap);
        masklayerCircleView.setShiftRange(showPic.getDrawable().getIntrinsicWidth(),showPic.getDrawable().getIntrinsicHeight());
    }

    public void initState() {
        showPic.setScaleMatrix(ZoomImage.SCALE_MIN, DensityUtil.getScreenSize((Activity)mContext).widthPixels/2,
                DensityUtil.getScreenSize((Activity)mContext).heightPixels/2);
    }

    /**
     * 获取指定位置的bitmap
     * @return
     */
    public Bitmap getCutOutBitmap() {
        masklayerCircleView.setVisibility(View.GONE);
       return BitmapUtil.toShapeBitmap(BitmapUtil.cutOutScreen((Activity) mContext),masklayerCircleView.getmCenterX()
                ,masklayerCircleView.getmCenterY(),masklayerCircleView.getmRadius(),masklayerCircleView.getmOutPutShapeType());
    }

    public void setVisible() {
        masklayerCircleView.setVisibility(View.VISIBLE);
    }


}
