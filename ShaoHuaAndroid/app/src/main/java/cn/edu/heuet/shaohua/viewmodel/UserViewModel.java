package cn.edu.heuet.shaohua.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.vondear.rxtool.RxSPTool;

import cn.edu.heuet.shaohua.constant.ModelConstant;
import cn.edu.heuet.shaohua.dataobject.UserDO;

public class UserViewModel extends AndroidViewModel {
    private final MutableLiveData<UserDO> userDOLiveData = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<UserDO> getUser() {
        // 在 ViewModel 中获取数据
        RxSPTool sp = new RxSPTool(getApplication(), ModelConstant.NAME_LOGIN_INFO);
        UserDO userDO = (UserDO) sp.getObject(ModelConstant.KEY_LOGIN_USER, UserDO.class);
        userDOLiveData.setValue(userDO);
        return userDOLiveData;
    }


    void doAction() {
        // depending on the action, do necessary business logic calls and update the
        // userLiveData.
    }
}