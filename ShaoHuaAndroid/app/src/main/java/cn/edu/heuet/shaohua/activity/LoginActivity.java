package cn.edu.heuet.shaohua.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.vondear.rxtool.RxSPTool;
import com.xuexiang.xhttp2.XHttp;
import com.xuexiang.xhttp2.callback.SimpleCallBack;
import com.xuexiang.xhttp2.exception.ApiException;

import cn.edu.heuet.shaohua.MainActivity;
import cn.edu.heuet.shaohua.R;
import cn.edu.heuet.shaohua.arouter.ARouterPath;
import cn.edu.heuet.shaohua.constant.ModelConstant;
import cn.edu.heuet.shaohua.constant.NetConstant;
import cn.edu.heuet.shaohua.databinding.ActivityLoginBinding;
import cn.edu.heuet.shaohua.dataobject.UserDO;
import cn.edu.heuet.shaohua.util.ValidUtils;

@Route(path = ARouterPath.ACTIVITY_LOGIN)
public class LoginActivity extends BaseActivity
        implements View.OnClickListener {

    // Log打印的通用Tag
    private final String TAG = "LoginActivity";

    private ActivityLoginBinding loginBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullScreenConfig();
//        setContentView(R.layout.activity_login);
        loginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        // 为点击事件设置监听器
        setOnClickListener();

        /*
            当输入框焦点失去时,检验输入数据，提示错误信息
            第一个参数：输入框对象
            第二个参数：输入数据类型
            第三个参数：输入不合法时提示信息
         */
        setOnFocusChangeErrMsg(loginBinding.etAccount, "phone", "手机号格式不正确");
        setOnFocusChangeErrMsg(loginBinding.etPassword, "password", "密码必须不少于6位");
    }

    // 为点击事件的UI对象设置监听器
    private void setOnClickListener() {
        loginBinding.btLogin.setOnClickListener(this); // 登录按钮
        loginBinding.tvToRegister.setOnClickListener(this); // 注册文字
        loginBinding.tvForgetPassword.setOnClickListener(this); // 忘记密码文字
        loginBinding.tvServiceAgreement.setOnClickListener(this); // 同意协议文字
        loginBinding.ivThirdMethod1.setOnClickListener(this); // 第三方登录方式1
        loginBinding.ivThirdMethod2.setOnClickListener(this); // 第三方登录方式2
        loginBinding.ivThirdMethod3.setOnClickListener(this); // 第三方登录方式3
    }


    // 因为 implements View.OnClickListener 加上已经 setOnClickListener()
    // 所以OnClick方法可以写到onCreate方法外
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        // 获取用户输入的账号和密码以进行验证
        String account = loginBinding.etAccount.getText().toString();
        String password = loginBinding.etPassword.getText().toString();

        switch (v.getId()) {
            // 登录按钮 响应事件
            case R.id.bt_login:
                // 让密码输入框失去焦点,触发setOnFocusChangeErrMsg方法
                loginBinding.etPassword.clearFocus();
                // 发送URL请求之前,先进行校验
                if (!(ValidUtils.isPhoneValid(account) && ValidUtils.isPasswordValid(password))) {
                    Toast.makeText(this, "账号或密码错误", Toast.LENGTH_SHORT).show();
                    break;
                }
                /*
                   因为验证是耗时操作，所以独立成方法
                   在方法中开辟子线程，避免在当前UI线程进行耗时操作
                   否则会造成 ANR（application not responding）
                */
                asyncLoginWithXHttp2(account, password);
                break;
            // 注册用户 响应事件
            case R.id.tv_to_register:
                /*
                  关于这里传参说明：给用户一个良好的体验，
                  如果在登录界面填写过的，就不需要再填了
                  所以Intent把填写过的数据传递给注册界面
                 */
                Intent intentToRegister = new Intent(this, RegisterActivity.class);
                intentToRegister.putExtra("account", account);
                startActivity(intentToRegister);
                break;

            // 以下功能目前都没有实现
            case R.id.tv_forget_password:
                // 跳转到修改密码界面

                break;
            case R.id.tv_service_agreement:
                // 跳转到服务协议界面

                break;
            case R.id.iv_third_method1:
                // 跳转第三方登录方式1

                break;
            case R.id.iv_third_method2:
                // 跳转第三方登录方式2

                break;
            case R.id.iv_third_method3:
                // 跳转第三方登录方式3

                break;
        }
    }


    private void asyncLoginWithXHttp2(String telephone, String password) {
        XHttp.post(NetConstant.getLoginURL())
                .params("telephone", telephone)
                .params("password", password)
                .params("type", "login")
                .syncRequest(false)
                .execute(new SimpleCallBack<UserDO>() {
                    @Override
                    public void onSuccess(UserDO userDO) throws Throwable {
                        Log.d(TAG, "请求URL成功,登录成功");
                        // 更新本地用户信息，用于下次打开的自动登录
                        RxSPTool sp = new RxSPTool(LoginActivity.this, ModelConstant.NAME_LOGIN_INFO);
                        sp.putObject(ModelConstant.KEY_LOGIN_USER, userDO);

                        Intent it_login_to_main = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(it_login_to_main);

                        // 登录成功后，登录界面就没必要占据资源了
                        finish();
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求URL失败： " + e.getMessage());
                        showToastInThread(LoginActivity.this, e.getMessage());
                    }
                });
    }
}
