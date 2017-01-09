package com.example.wcp.cutoutavatar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.wcp.cutoutavatar.util.FileUtil;
import com.example.wcp.cutoutavatar.util.LogUtil;
import com.example.wcp.cutoutavatar.view.CutOutLayout;
import com.example.wcp.cutoutavatar.view.MasklayerCircleView;
import com.example.wcp.cutoutavatar.view.ZoomImage;

import java.io.File;
import java.io.IOException;
import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends Activity implements View.OnClickListener {
    // 请求
    private static final int CAMERA_TAKE = 1;

    @Bind(R.id.show_image)
    ZoomImage showImage;
    @Bind(R.id.mask_layer)
    MasklayerCircleView maskLayer;
    @Bind(R.id.cutout_layout)
    CutOutLayout cutoutLayout;
    @Bind(R.id.bindUserAvatar)
    ImageView bindUserAvatar;
    @Bind(R.id.re_take_piture)
    TextView reTakePiture;
    @Bind(R.id.idGetUserInfoLayout)
    RelativeLayout idGetUserInfoLayout;
    @Bind(R.id.cut_out_tip)
    TextView cutOutTip;
    @Bind(R.id.btn_cutout_finish)
    ImageButton btnCutoutFinish;
    @Bind(R.id.root)
    RelativeLayout root;

    private StateController mStateController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initData();
        initView();

        takePhoto();
    }

    private void initData() {
        mStateController = new StateController();

        idGetUserInfoLayout.setVisibility(View.GONE);
        btnCutoutFinish.setOnClickListener(this);
        reTakePiture.setOnClickListener(this);
    }

    private void initView() {
        setTakePictureUi();
    }

    private void setTakePictureUi() {
        mStateController.setState(new TakePictureState());
        mStateController.showCurrentState();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.re_take_piture:
                setTakePictureUi();
                takePhoto();
                break;
            case R.id.btn_cutout_finish:
                btnCutoutFinish.setVisibility(View.GONE);
                cutOutTip.setVisibility(View.GONE);
                Bitmap bitmap = cutoutLayout.getCutOutBitmap();
                setUploadUi();
                bindUserAvatar.setImageBitmap(bitmap);
                FileUtil.saveMyBitmap(bitmap, Global.PATH + "/" + Global.TOP_IMG);
                cutoutLayout.initState();
                break;
        }
    }

    /**
     * 调用系统相机
     */
    public void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 调用系统相机
        File file = new File(Global.PATH, Global.TOP_IMG_NAME);
        if (!file.exists()) {
            LogUtil.showlog("file.exists");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Uri imageUri = Uri.fromFile(file);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_TAKE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.showlog("requestCode=" + requestCode + "||resultCode=" + resultCode);
        if (resultCode == RESULT_OK && requestCode == CAMERA_TAKE) {
            Bitmap bitmap = BitmapFactory.decodeFile(Global.PATH + "/" + Global.TOP_IMG_NAME);
            LogUtil.showlog("bitmap="+(bitmap==null));
            if (bitmap != null) {

                cutoutLayout.setShowPic(bitmap);
                setCutOutStateUi();
                File tempFile = new File(Global.PATH, Global.TOP_IMG_NAME);
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        } else {
            finish();
        }
    }

    private void setUploadUi() {
        mStateController.setState(new UploadState());
        mStateController.showCurrentState();
    }

    private void setCutOutStateUi() {
        mStateController.setState(new CutOutState());
        mStateController.showCurrentState();
    }

    public class StateController {
        State mState;

        public void setState(State state) {
            mState = state;
        }

        public void showCurrentState() {
            mState.showCurrentState();
        }
    }

    interface State {
        void showCurrentState();
    }

    /**
     * 拍照时显示的view
     */
    public class TakePictureState implements State {

        @Override
        public void showCurrentState() {
            cutoutLayout.setVisibility(View.GONE);
            idGetUserInfoLayout.setVisibility(View.GONE);
            cutOutTip.setVisibility(View.GONE);
            btnCutoutFinish.setVisibility(View.GONE);
        }
    }

    /**
     * 截取图片view的状态
     */
    public class CutOutState implements State {

        @Override
        public void showCurrentState() {
            cutoutLayout.setVisibility(View.VISIBLE);
            idGetUserInfoLayout.setVisibility(View.GONE);
            cutOutTip.setVisibility(View.VISIBLE);
            btnCutoutFinish.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 上传图片时的状态
     */
    public class UploadState implements State {
        @Override
        public void showCurrentState() {
            cutoutLayout.setVisibility(View.GONE);
            idGetUserInfoLayout.setVisibility(View.VISIBLE);
            cutOutTip.setVisibility(View.GONE);
            btnCutoutFinish.setVisibility(View.GONE);
        }
    }
}
