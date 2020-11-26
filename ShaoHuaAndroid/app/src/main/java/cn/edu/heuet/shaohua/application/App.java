package cn.edu.heuet.shaohua.application;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;
import com.xuexiang.xhttp2.XHttpSDK;

import cn.edu.heuet.shaohua.constant.NetConstant;


/**
 * @ClassName App
 * @Author littlecurl
 * @Date 2020/11/8 13:08
 * @Version 1.0.0
 */
public class App extends Application {

    private boolean isDebug = true;

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化 ARouter
        initARouter();

        //初始化网络请求框架
        XHttpSDK.init(this);
        //设置网络请求的基础地址
        XHttpSDK.setBaseUrl(NetConstant.baseService);
    }

    private void initARouter() {
        if (isDebug) {
            ARouter.openLog();
            ARouter.openDebug();
        }
        ARouter.init(this);
    }
}
