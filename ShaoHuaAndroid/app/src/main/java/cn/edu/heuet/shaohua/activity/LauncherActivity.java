package cn.edu.heuet.shaohua.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.android.arouter.launcher.ARouter;
import com.jaredrummler.android.widget.AnimatedSvgView;
import com.vondear.rxtool.RxBarTool;
import com.vondear.rxtool.RxDeviceTool;
import com.vondear.rxtool.RxSPTool;
import com.xuexiang.xhttp2.XHttp;
import com.xuexiang.xhttp2.callback.SimpleCallBack;
import com.xuexiang.xhttp2.exception.ApiException;

import cn.edu.heuet.shaohua.R;
import cn.edu.heuet.shaohua.arouter.ARouterPath;
import cn.edu.heuet.shaohua.constant.ModelConstant;
import cn.edu.heuet.shaohua.constant.NetConstant;
import cn.edu.heuet.shaohua.dataobject.ModelSVG;
import cn.edu.heuet.shaohua.dataobject.UserDO;

/**
 * 启动类
 * <p>
 * 注意点:
 * 1、navigation() 要传入 context 否则转场动画不生效
 */
public class LauncherActivity extends AppCompatActivity {

    private static final String TAG = "LauncherActivity";

    private boolean isLogin = false;

    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBarTool.hideStatusBar(this);
        setContentView(R.layout.activity_launcher);

        context = this;

        RxDeviceTool.setPortrait(this);

        autoLogin();

        setSvg(ModelSVG.SHAOHUA1);

        toMain();
    }

    private void autoLogin() {
        RxSPTool sp = new RxSPTool(this, ModelConstant.NAME_LOGIN_INFO);
        UserDO userDO = (UserDO) sp.getObject(ModelConstant.KEY_LOGIN_USER, UserDO.class);
        if (userDO != null) {
            String telephoneInSP = userDO.getTelephone();
            String passwordInSP = userDO.getEncryptPassword();
            if (!TextUtils.isEmpty(telephoneInSP)
                    && !TextUtils.isEmpty(passwordInSP)) {
                // 异步登录
                // asyncValidate(telephoneInSP, passwordInSP);
                asyncValidateWithXHttp2(telephoneInSP, passwordInSP);
            }
        }
    }


    // 异步登录
    private void asyncValidateWithXHttp2(String account, String password) {
        XHttp.post(NetConstant.getLoginURL())
                .params("telephone", account)
                .params("password", password)
                .params("type", "autoLogin")
                .syncRequest(false)
                .execute(new SimpleCallBack<UserDO>() {
                    @Override
                    public void onSuccess(UserDO userDO) throws Throwable {
                        isLogin = true;
                        Log.d(TAG, "请求URL成功,自动登录成功");
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求URL异常,自动登录失败" + e.toString());
//                        showToastInThread(CountDownActivity.this, e.getMessage());
                    }
                });
    }

    private void setSvg(ModelSVG modelSvg) {
        AnimatedSvgView mSvgView = findViewById(R.id.animated_svg_view);
        mSvgView.setGlyphStrings(modelSvg.glyphs);
        mSvgView.setFillColors(modelSvg.colors);
        mSvgView.setViewportSize(modelSvg.width, modelSvg.height);
        mSvgView.setTraceResidueColor(0x32000000);
        mSvgView.setTraceColors(modelSvg.colors);
        mSvgView.rebuildGlyphData();
        mSvgView.start();
    }

    public void toMain() {
        // 开辟子线程跳转页面
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 倒计时 2.618 秒
                    Thread.sleep(2618);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isLogin) {
                    ARouter.getInstance()
                            .build(ARouterPath.ACTIVITY_MAIN)
                            // 转场动画, navigation() 要传入 当前页面context 否则动画不生效
                            // 注意 context 为 ApplicationContext 动画也不生效，只能是当前页面的 context
                            .withTransition(0, R.anim.fade_out)
//                        .navigation();
                            .navigation(context);
                } else {
                    ARouter.getInstance()
                            .build(ARouterPath.ACTIVITY_LOGIN)
                            // 转场动画, navigation() 要传入 当前页面context 否则动画不生效
                            // 注意 context 为 ApplicationContext 动画也不生效，只能是当前页面的 context
                            .withTransition(0, R.anim.fade_out)
//                        .navigation();
                            .navigation(context);
                }
                // 跳转后杀掉页面
                finish();
            }
        }).start();

    }
}
