package cn.edu.heuet.shaohua.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import cn.edu.heuet.shaohua.databinding.ActivityRegisterBinding;
import cn.edu.heuet.shaohua.dataobject.OtpCode;
import cn.edu.heuet.shaohua.dataobject.UserDO;
import cn.edu.heuet.shaohua.util.ValidUtils;

@Route(path = ARouterPath.ACTIVITY_REGISTER)
public class RegisterActivity extends BaseActivity
        implements View.OnClickListener {

    // Log打印的通用Tag
    private final String TAG = "RegisterActivity";


    String account = "";
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private ActivityRegisterBinding registerBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_register);
        registerBinding = DataBindingUtil.setContentView(this, R.layout.activity_register);

        // 接收用户在登录界面输入的数据，简化用户操作（如果输入过了就不用再输入了）
        // 注意接收上一个页面 Intent 的信息，需要 getIntent() 即可，而非重新 new 一个 Intent
        Intent intent = getIntent();
        account = intent.getStringExtra("account");
        // 把对应的 account 设置到 telephone 输入框
        registerBinding.etTelephone.setText(account);


        // 为点击事件设置监听器
        setOnClickListener();

         /*
            设置当输入框焦点失去时提示错误信息
            第一个参数：输入框对象
            第二个参数：输入数据类型
            第三个参数：输入不合法时提示信息
         */
        setOnFocusChangeErrMsg(registerBinding.etTelephone, "phone", "手机号格式不正确");
        setOnFocusChangeErrMsg(registerBinding.etPassword, "password", "密码必须不少于6位");
        setOnFocusChangeErrMsg(registerBinding.etGender, "gender", "性别只能填1或2");
    }

    // 为点击事件的UI对象设置监听器
    private void setOnClickListener() {
        registerBinding.btGetOtp.setOnClickListener(this);
        registerBinding.btSubmitRegister.setOnClickListener(this);
    }

    // 因为 implements View.OnClickListener 所以OnClick方法可以写到onCreate方法外
    @Override
    public void onClick(View v) {
        String telephone = registerBinding.etTelephone.getText().toString();
        String otpCode = registerBinding.etOtpCode.getText().toString();
        String username = registerBinding.etUsername.getText().toString();
        String gender = registerBinding.etGender.getText().toString();
        String age = registerBinding.etAge.getText().toString();
        String password1 = registerBinding.etPassword.getText().toString();
        String password2 = registerBinding.etPassword2.getText().toString();

        switch (v.getId()) {
            case R.id.bt_get_otp:
                // 获取验证码
                if (TextUtils.isEmpty(telephone)) {
                    Toast.makeText(RegisterActivity.this, "手机号不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    if (ValidUtils.isPhoneValid(telephone)) {
                        hideToast();
                        asyncGetOtpCodeWithXHttp2(telephone);
                    } else {
                        Toast.makeText(RegisterActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.bt_submit_register:
                // 提交注册
                asyncRegisterWithXHttp2(telephone, otpCode, username, gender, age, password1, password2);
                break;
        }
    }

    private void asyncGetOtpCodeWithXHttp2(String telephone) {
        XHttp.post(NetConstant.getGetOtpCodeURL())
                .params("telephone", telephone)
                .syncRequest(false)
                .execute(new SimpleCallBack<OtpCode>() {
                    @Override
                    public void onSuccess(OtpCode data) throws Throwable {
                        Log.d(TAG, "请求URL成功： " + data);
                        if (data != null) {
                            String otpCode = data.getOtpCode();
                            // 自动填充验证码
                            setTextInThread(registerBinding.etOtpCode, otpCode);
                            // 在子线程中显示Toast
                            showToastInThread(RegisterActivity.this, "验证码：" + otpCode);
                            Log.d(TAG, "telephone: " + telephone + " otpCode: " + otpCode);
                        }
                        Log.d(TAG, "验证码已发送，注意查收！");
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求URL异常： " + e.toString());
                        showToastInThread(RegisterActivity.this, e.getMessage());
                    }
                });
    }

    /* 在子线程中更新UI ，实现自动填充验证码 */
    private void setTextInThread(EditText editText, String otpCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editText.setText(otpCode);
            }
        });
    }

    private void asyncRegisterWithXHttp2(final String telephone,
                                         final String otpCode,
                                         final String username,
                                         final String gender,
                                         final String age,
                                         final String password1,
                                         final String password2) {
        // 非空校验
        if (TextUtils.isEmpty(telephone) || TextUtils.isEmpty(otpCode) || TextUtils.isEmpty(username)
                || TextUtils.isEmpty(gender) || TextUtils.isEmpty(age)
                || TextUtils.isEmpty(password1) || TextUtils.isEmpty(password2)) {
            Toast.makeText(RegisterActivity.this, "存在输入为空，注册失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // 密码一致校验
        if (!TextUtils.equals(password1, password2)) {
            Toast.makeText(RegisterActivity.this, "两次密码不一致，注册失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // 注册
        XHttp.post(NetConstant.getRegisterURL())
                .params("telephone", telephone)
                .params("otpCode", otpCode)
                .params("name", username)
                .params("gender", gender)
                .params("age", age)
                .params("password", password1)
                .syncRequest(false)
                .execute(new SimpleCallBack<UserDO>() {
                    @Override
                    public void onSuccess(UserDO userDO) throws Throwable {
                        // 注册成功，保存用户信息到本地
                        RxSPTool sp = new RxSPTool(RegisterActivity.this, ModelConstant.NAME_LOGIN_INFO);
                        sp.putObject(ModelConstant.KEY_LOGIN_USER, userDO);

                        Intent intentToMain = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intentToMain);

                        // 注册成功后，注册界面就没必要占据资源了
                        finish();
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求URL异常： " + e.toString());
                        showToastInThread(RegisterActivity.this, e.getMessage());
                    }
                });
    }

}



