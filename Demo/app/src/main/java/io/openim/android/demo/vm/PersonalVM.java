package io.openim.android.demo.vm;

import androidx.lifecycle.MutableLiveData;

import com.alibaba.fastjson2.JSONObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.openim.android.demo.repository.OpenIMService;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.entity.ExtendUserInfo;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.UserInfo;

public class PersonalVM extends BaseViewModel {
    public WaitDialog waitDialog;
    public State<UserInfo> userInfo = new State<>();

    @Override
    protected void viewCreate() {
        super.viewCreate();
        waitDialog = new WaitDialog(getContext());
    }

    OnBase<String> callBack = new OnBase<String>() {
        @Override
        public void onError(int code, String error) {
            waitDialog.dismiss();
            getIView().toast(error + code);
        }

        @Override
        public void onSuccess(String data) {
            waitDialog.dismiss();
            userInfo.update();

            updateConfig(userInfo.val());
            Obs.newMessage(Constant.Event.USER_INFO_UPDATE);
        }
    };

    public void updateConfig(UserInfo userInfo) {
        LoginCertificate certificate = BaseApp.inst().loginCertificate;
        if (!userInfo.getUserID().equals(certificate.userID))return;

        certificate.nickname = userInfo.getNickname();
        certificate.faceURL = userInfo.getFaceURL();

        certificate.globalRecvMsgOpt = userInfo.getGlobalRecvMsgOpt();
        certificate.allowAddFriend = userInfo.getAllowAddFriend() == 1;
        certificate.allowBeep = userInfo.getAllowBeep() == 1;
        certificate.allowVibration = userInfo.getAllowVibration() == 1;

        BaseApp.inst().loginCertificate.cache(BaseApp.inst());
    }

    public void getSelfUserInfo() {
        waitDialog.show();
        getExtendUserInfo(BaseApp.inst().loginCertificate.userID);
    }

    private void getExtendUserInfo(String uid) {
        List<String> ids = new ArrayList<>();
        ids.add(uid);
        Parameter parameter = new Parameter().add("userIDs", ids);
        N.API(OpenIMService.class).getUsersFullInfo(parameter.buildJsonBody()).map(OpenIMService.turn(HashMap.class)).compose(N.IOMain()).subscribe(new NetObserver<HashMap>(getContext()) {
            @Override
            protected void onFailure(Throwable e) {
                getIView().toast(e.getMessage());
                waitDialog.dismiss();
            }

            @Override
            public void onSuccess(HashMap map) {
                waitDialog.dismiss();
                try {
                    ArrayList arrayList = (ArrayList) map.get("users");
                    if (null == arrayList || arrayList.isEmpty()) return;

                    UserInfo u = GsonHel.getGson().fromJson(arrayList.get(0).toString(),
                        UserInfo.class);
                    userInfo.setValue(u);

                    updateConfig(userInfo.val());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void getUserInfo(String id) {
        waitDialog.show();
       getExtendUserInfo(id);
    }

    public void setSelfInfo(Parameter param) {
        param.add("userID",BaseApp.inst().loginCertificate.userID);
        waitDialog.show();
        N.API(OpenIMService.class)
            .updateUserInfo(param.buildJsonBody())
            .compose(N.IOMain()).map(OpenIMService.turn(Object.class))

            .subscribe(new NetObserver<Object>(getContext()) {
                @Override
                protected void onFailure(Throwable e) {
                    callBack.onError(-1, e.getMessage());
                }

                @Override
                public void onSuccess(Object o) {
                    callBack.onSuccess("");
                }
            });
    }

    public void setNickname(String nickname) {
        userInfo.val().setNickname(nickname);
        setSelfInfo(new Parameter().add("nickname",nickname));
    }

    public void setFaceURL(String faceURL) {
        userInfo.val().setFaceURL(faceURL);
        setSelfInfo(new Parameter().add("faceURL",faceURL));
    }

    public void setGender(int gender) {
        userInfo.val().setGender(gender);
        setSelfInfo(new Parameter().add("gender",gender));
    }

    public void setBirthday(long birth) {
        userInfo.val().setBirth(birth);
        setSelfInfo(new Parameter().add("birth",birth));
    }

    public void setEmail(String email) {
        userInfo.val().setEmail(email);
        setSelfInfo(new Parameter().add("email",email));
    }

}
