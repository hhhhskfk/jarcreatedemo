package sai_cqt.sailib;

import android.content.Context;
import android.content.Intent;

import sai_cqt.sailib.activity.SaiActivity;
import sai_cqt.sailib.interf.SaiCallback;
import sai_cqt.sailib.net.RetrofitClient;

/**
 * Created by huangjie on 2017/6/7.
 */

public class SaiManager {
    private static SaiManager _singleton = new SaiManager();

    private String vccid;
    private String token;

    private SaiManager() {

    }

    public static SaiManager getInstance() {
        return _singleton;
    }

    public void init(String vccid, String token) {
        RetrofitClient.getInstance().init();
        this.vccid = vccid;
        this.token = token;
    }

    public void requestSai(Context context, SaiCallback callback) {
        Intent intent = new Intent(context, SaiActivity.class);
        context.startActivity(intent);
        SaiActivity.initCallback(callback);
    }

    public String getVccid() {
        return vccid;
    }

    public void setVccid(String vccid) {
        this.vccid = vccid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
