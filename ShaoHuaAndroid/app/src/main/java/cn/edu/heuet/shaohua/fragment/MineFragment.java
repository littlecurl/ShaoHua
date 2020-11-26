package cn.edu.heuet.shaohua.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.vondear.rxtool.RxPhotoTool;
import com.vondear.rxtool.RxSPTool;
import com.vondear.rxui.view.dialog.RxDialogChooseImage;
import com.vondear.rxui.view.dialog.RxDialogScaleView;
import com.vondear.rxui.view.dialog.RxDialogSureCancel;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.edu.heuet.shaohua.R;
import cn.edu.heuet.shaohua.arouter.ARouterPath;
import cn.edu.heuet.shaohua.constant.ModelConstant;
import cn.edu.heuet.shaohua.databinding.FragmentMineBinding;
import cn.edu.heuet.shaohua.dataobject.UserDO;
import cn.edu.heuet.shaohua.viewmodel.UserViewModel;

import static android.app.Activity.RESULT_OK;
import static com.vondear.rxui.view.dialog.RxDialogChooseImage.LayoutType.TITLE;

/**
 * 我的个人资料页面
 * 注意点：
 * 1、裁切头像的 Activity 需要在 AndroidManifest.xml 中注册，否则无法使用
 * 2、new RxDialogChooseImage() 调用 Dialog 的构造方法要切换成 Fragment 的，否则无法调起 Dialog
 * 3、initUCrop() 最后裁切后需要切换成 Fragment 的用法，否则无法返回图片
 */
public class MineFragment extends Fragment implements View.OnClickListener {

    private Uri resultUri;
    private FragmentMineBinding mBinding;
    private UserViewModel userViewModel;

    public MineFragment() {
        // Required empty public constructor
    }

    public void onStart() {
        super.onStart();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_mine, container, false);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_mine, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // ViewModel 初始化方式
//        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // AndroidViewModel 初始化方式
        userViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication()))
                .get(UserViewModel.class);
        initView();
    }


    private void initUI(View view) {
        mBinding.btnExit.setOnClickListener(this);
        mBinding.btnLogin.setOnClickListener(this);
    }

    private void initView() {
        Resources r = getActivity().getResources();
        resultUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + r.getResourcePackageName(R.drawable.ic_fab) + "/"
                + r.getResourceTypeName(R.drawable.ic_fab) + "/"
                + r.getResourceEntryName(R.drawable.ic_fab));

        mBinding.rxTitle.setLeftFinish(getActivity());

        // 单击头像响应事件
        mBinding.ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initDialogChooseImage();
            }
        });
        // 长按头像响应事件
        mBinding.ivAvatar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
//                RxImageTool.showBigImageView(mContext, resultUri);
                RxDialogScaleView rxDialogScaleView = new RxDialogScaleView(getActivity());
                rxDialogScaleView.setImage(resultUri);
                rxDialogScaleView.show();
                return false;
            }
        });


        // 观察者动态更新 UI
        userViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<UserDO>() {
            @Override
            public void onChanged(UserDO userDO) {
                // 登录成功
                if (userDO != null) {
                    mBinding.btnExit.setVisibility(View.VISIBLE);
                    mBinding.btnLogin.setVisibility(View.GONE);
                    mBinding.tvName.setText(userDO.getName());
                }
                // 未登录
                else {
                    mBinding.btnExit.setVisibility(View.GONE);
                    mBinding.btnLogin.setVisibility(View.VISIBLE);
                    mBinding.tvName.setText("");
                }

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        userViewModel.getUser();
    }

    private void initDialogChooseImage() {
        /*
        这里使用 this 传递 Fragment 进去，而不是 getActivity() 传递 Activity 进去
        因为 Fragment 和 Activity 调用 startActivityForResult() 方法的效果不一样
         */
//        RxDialogChooseImage dialogChooseImage = new RxDialogChooseImage(getActivity(), TITLE);
        RxDialogChooseImage dialogChooseImage = new RxDialogChooseImage(this, TITLE);
        dialogChooseImage.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RxPhotoTool.GET_IMAGE_FROM_PHONE://选择相册之后的处理
                if (resultCode == RESULT_OK) {
//                    RxPhotoTool.cropImage(ActivityUser.this, );// 裁剪图片
                    initUCrop(data.getData());
                }

                break;
            case RxPhotoTool.GET_IMAGE_BY_CAMERA://选择照相机之后的处理
                if (resultCode == RESULT_OK) {
                    /* data.getExtras().get("data");*/
//                    RxPhotoTool.cropImage(ActivityUser.this, RxPhotoTool.imageUriFromCamera);// 裁剪图片
                    initUCrop(RxPhotoTool.imageUriFromCamera);
                }

                break;
            case RxPhotoTool.CROP_IMAGE://普通裁剪后的处理
                RequestOptions options = new RequestOptions()
                        .placeholder(R.drawable.ic_fab)
                        //异常占位图(当加载异常的时候出现的图片)
                        .error(R.drawable.ic_fab)
                        //禁止Glide硬盘缓存缓存
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

                Glide.with(getActivity()).
                        load(RxPhotoTool.cropImageUri).
                        apply(options).
                        thumbnail(0.5f).
                        into(mBinding.ivAvatar);
//                RequestUpdateAvatar(new File(RxPhotoTool.getRealFilePath(mContext, RxPhotoTool.cropImageUri)));
                break;

            case UCrop.REQUEST_CROP://UCrop裁剪之后的处理
                if (resultCode == RESULT_OK) {
                    resultUri = UCrop.getOutput(data);
                    roadImageView(resultUri, mBinding.ivAvatar);
                    RxSPTool.putString(getContext(), "AVATAR", resultUri.toString());
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    final Throwable cropError = UCrop.getError(data);
                }
                break;
            case UCrop.RESULT_ERROR://UCrop裁剪错误之后的处理
                final Throwable cropError = UCrop.getError(data);
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //从Uri中加载图片 并将其转化成File文件返回
    private File roadImageView(Uri uri, ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_fab)
                //异常占位图(当加载异常的时候出现的图片)
                .error(R.drawable.ic_fab)
                .transform(new CircleCrop())
                //禁止Glide硬盘缓存缓存
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

        Glide.with(getActivity()).
                load(uri).
                apply(options).
                thumbnail(0.5f).
                into(imageView);

        return (new File(RxPhotoTool.getImageAbsolutePath(getActivity(), uri)));
    }

    private void initUCrop(Uri uri) {
        SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        long time = System.currentTimeMillis();
        String imageName = timeFormatter.format(new Date(time));

        Uri destinationUri = Uri.fromFile(new File(getActivity().getCacheDir(), imageName + ".jpeg"));

        UCrop.Options options = new UCrop.Options();
        //设置裁剪图片可操作的手势
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        //设置隐藏底部容器，默认显示
        //options.setHideBottomControls(true);
        //设置toolbar颜色
        options.setToolbarColor(ActivityCompat.getColor(getActivity(), R.color.colorPrimary));
        //设置状态栏颜色
        options.setStatusBarColor(ActivityCompat.getColor(getActivity(), R.color.colorPrimaryDark));

        //开始设置
        //设置最大缩放比例
        options.setMaxScaleMultiplier(5);
        //设置图片在切换比例时的动画
        options.setImageToCropBoundsAnimDuration(666);
        //设置裁剪窗口是否为椭圆
        //options.setCircleDimmedLayer(true);
        //设置是否展示矩形裁剪框
        // options.setShowCropFrame(false);
        //设置裁剪框横竖线的宽度
        //options.setCropGridStrokeWidth(20);
        //设置裁剪框横竖线的颜色
        //options.setCropGridColor(Color.GREEN);
        //设置竖线的数量
        //options.setCropGridColumnCount(2);
        //设置横线的数量
        //options.setCropGridRowCount(1);

        UCrop.of(uri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(1000, 1000)
                .withOptions(options)
                // 注意这里要切换成 Fragment 的用法，否则无法返回图片
//                .start(getActivity())
                .start(getContext(), this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                ARouter.getInstance()
                        .build(ARouterPath.ACTIVITY_LOGIN)
                        .navigation();
                break;

            // 退出登录
            case R.id.btn_exit:
                final RxDialogSureCancel rxDialogSureCancel = new RxDialogSureCancel(getActivity());
                rxDialogSureCancel.getCancelView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rxDialogSureCancel.cancel();
                    }
                });
                rxDialogSureCancel.getSureView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 清空本地缓存信息
                        RxSPTool sp = new RxSPTool(getContext(), ModelConstant.NAME_LOGIN_INFO);
                        sp.putObject(ModelConstant.KEY_LOGIN_USER, null);

                        // 动态更新UI
                        userViewModel.getUser().setValue(null);

                        rxDialogSureCancel.cancel();
                    }
                });
                rxDialogSureCancel.show();
                break;


        }
    }
}
