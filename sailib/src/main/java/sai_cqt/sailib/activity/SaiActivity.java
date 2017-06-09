package sai_cqt.sailib.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sai_cqt.sailib.SaiManager;
import sai_cqt.sailib.MResource;
import sai_cqt.sailib.service.SaiService;
import sai_cqt.sailib.interf.SaiCallback;
import sai_cqt.sailib.net.RetrofitClient;
import sai_cqt.sailib.util.Utils;
import sai_cqt.sailib.util.JsonUtils;

/**
 * Created by huangjie on 2017/6/7.
 */

public class SaiActivity extends Activity {

    private static final int HAS_PERMISSION = 1;

    public static final int SAI_START = 0;
    public static final int SAI_WAIT = 1;
    public static final int SAI_SEND = 2;
    public static final int SAI_SUCCESS = 3;
    public static final int SAI_FAIL = 4;

    private EditText mEditPhone;
    private TextView mTxtState;
    private Button mBtnSai, mBtnDelete;
    private ImageView mImgState, mBtnBack;
    private TextView mTxtStart, mTxtWait, mTxtSend, mTxtSuccess;

    private BroadcastReceiver mReceiver = null;
    private static SaiCallback callback;
    private String messageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(MResource.getIdByName(getApplication(), "layout", "activity_sai"));
        initView();
        initListener();
        initData();
        initBroadcast();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    public static void initCallback(SaiCallback c) {
        callback = c;
    }

    private void initView() {
        mImgState = (ImageView) findViewById(MResource.getIdByName(getApplication(), "id", "iv_sai_state"));
        mEditPhone = (EditText) findViewById(MResource.getIdByName(getApplication(), "id", "edt_phone"));
        mTxtState = (TextView) findViewById(MResource.getIdByName(getApplication(), "id", "tv_sai_state"));
        mTxtStart = (TextView) findViewById(MResource.getIdByName(getApplication(), "id", "tv_sai_start"));
        mTxtWait = (TextView) findViewById(MResource.getIdByName(getApplication(), "id", "tv_sai_wait"));
        mTxtSend = (TextView) findViewById(MResource.getIdByName(getApplication(), "id", "tv_sai_send"));
        mTxtSuccess = (TextView) findViewById(MResource.getIdByName(getApplication(), "id", "tv_sai_success"));
        mBtnSai = (Button) findViewById(MResource.getIdByName(getApplication(), "id", "btn_sai"));
        mBtnDelete = (Button) findViewById(MResource.getIdByName(getApplication(), "id", "btn_delete"));
        mBtnBack = (ImageView) findViewById(MResource.getIdByName(getApplication(), "id", "btn_back"));
    }

    private void initData() {
        String phone = Utils.getPhone(this);
        if (!TextUtils.isEmpty(phone)) {
            mEditPhone.setText(phone);
            mEditPhone.setSelection(mEditPhone.getText().length());
        }
    }

    private void initListener() {
        mEditPhone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Utils.hideSoftInput(v);
                    checkPermission();
                    return true;
                }
                return false;
            }
        });
        mEditPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String phone = s.toString();
                mBtnDelete.setEnabled(!TextUtils.isEmpty(phone));
                mBtnSai.setEnabled(!TextUtils.isEmpty(phone));
            }
        });
        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditPhone.setText("");
            }
        });

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBtnSai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });
    }

    private void initBroadcast() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int type = intent.getIntExtra("type", SAI_FAIL);
                updateUI(type);
                stopService();

                if (type == SAI_SEND) {
                    stopService();
                    String phone = intent.getStringExtra("phone");
                    requestSai(phone);
                }
            }
        };
        registerReceiver(mReceiver, new IntentFilter("broadcast.sai"));
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestCall();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, HAS_PERMISSION);
            }
        } else {
            requestCall();
        }
    }

    private void requestCall() {
        String phone = mEditPhone.getText().toString();
        if (!isMobilePhone(phone)) {
            Toast.makeText(SaiActivity.this, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
            return;
        }
        updateUI(SAI_START);
        messageId = UUID.randomUUID().toString();
        HashMap<String, Object> headerMap = new HashMap<>();
        headerMap.put("serviceName", "callRequest");
        headerMap.put("messageId", messageId);
        headerMap.put("vccid", SaiManager.getInstance().getVccid());
        headerMap.put("token", SaiManager.getInstance().getToken());
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("calledNum", phone);
        final RequestBody requestBody = JsonUtils.toJson(headerMap, bodyMap);
        if (requestBody != null) {
            RetrofitClient.getInstance().post("dial.do", requestBody, new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    boolean requestCall = false;
                    try {
                        if (response.body() != null) {
                            Map<String, Object> map = JsonUtils.toBean(response.body().string());
                            if (map != null && !map.isEmpty()) {
                                String code = (String) map.get("stateCode");
                                if (!TextUtils.isEmpty(code) && "0000".equals(code)) {
                                    //成功，等待电话
                                    requestCall = true;
                                    updateUI(SAI_WAIT);
                                    startService();
                                } else {
                                    updateUI(SAI_FAIL);
                                }
                            } else {
                                updateUI(SAI_FAIL);
                            }
                        } else {
                            updateUI(SAI_FAIL);
                        }
                    } catch (Exception e) {
                        updateUI(SAI_FAIL);
                    }
                    if (callback != null && !requestCall) {
                        callback.result(false);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    updateUI(SAI_FAIL);
                    if (callback != null) {
                        callback.result(false);
                    }
                }
            });
        }
    }

    private void requestSai(String phone) {
        HashMap<String, Object> headerMap = new HashMap<>();
        headerMap.put("serviceName", "CIARequest");
        headerMap.put("messageId", messageId);
        headerMap.put("vccid", SaiManager.getInstance().getVccid());
        headerMap.put("token", SaiManager.getInstance().getToken());
        headerMap.put("verifiCode", Utils.verifiCode(phone, messageId));
        final RequestBody requestBody = JsonUtils.toJson(headerMap, null);
        if (requestBody != null) {
            RetrofitClient.getInstance().post("receiveMsg.do", requestBody, new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    boolean verifiState = false;
                    try {
                        if (response.body() != null) {
                            Map<String, Object> map = JsonUtils.toBean(response.body().string());
                            if (map != null && !map.isEmpty()) {
                                String code = (String) map.get("stateCode");
                                if (!TextUtils.isEmpty(code) && "0000".equals(code)) {
                                    updateUI(SAI_SUCCESS);
                                    verifiState = true;
                                } else {
                                    updateUI(SAI_FAIL);
                                }
                            } else {
                                updateUI(SAI_FAIL);
                            }
                        } else {
                            updateUI(SAI_FAIL);
                        }
                    } catch (Exception e) {
                        updateUI(SAI_FAIL);
                    }
                    if (callback != null) {
                        callback.result(verifiState);
                    }
                    if (verifiState) {
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    updateUI(SAI_FAIL);
                    if (callback != null) {
                        callback.result(false);
                    }
                }
            });
        }
    }

    private boolean isMobilePhone(String phone) {
        String telRegex = "^(0(10|2\\d|[3-9]\\d\\d)[- ]{0,3}\\d{7,8}|0?1[3584]\\d{9})$";
        if (TextUtils.isEmpty(phone)) return false;
        return phone.matches(telRegex);
    }

    private void updateUI(int type) {
        Drawable drawableGray = ContextCompat.getDrawable(this, MResource.getIdByName(getApplication(), "drawable", "shape_circle_gray"));
        drawableGray.setBounds(0, 0, drawableGray.getMinimumWidth(), drawableGray.getMinimumHeight());
        Drawable drawableGreen = ContextCompat.getDrawable(this, MResource.getIdByName(getApplication(), "drawable", "shape_circle_green"));
        drawableGreen.setBounds(0, 0, drawableGreen.getMinimumWidth(), drawableGreen.getMinimumHeight());

        switch (type) {
            case SAI_FAIL:
                mTxtState.setText("验证失败！");
                mTxtState.setVisibility(View.VISIBLE);
                mImgState.setVisibility(View.INVISIBLE);
                mTxtStart.setCompoundDrawables(null, drawableGray, null, null);
                mTxtWait.setCompoundDrawables(null, drawableGray, null, null);
                mTxtSend.setCompoundDrawables(null, drawableGray, null, null);
                mTxtSuccess.setCompoundDrawables(null, drawableGray, null, null);
                mBtnSai.setEnabled(true);
                mBtnDelete.setEnabled(true);
                mEditPhone.setEnabled(true);
                break;
            case SAI_START:
                mTxtState.setText("开始验证！");
                mTxtState.setVisibility(View.VISIBLE);
                mImgState.setVisibility(View.VISIBLE);
                mImgState.setImageDrawable(ContextCompat.getDrawable(this, MResource.getIdByName(getApplication(), "drawable", "ic_sai_start")));
                mTxtStart.setCompoundDrawables(null, drawableGreen, null, null);
                mTxtWait.setCompoundDrawables(null, drawableGray, null, null);
                mTxtSend.setCompoundDrawables(null, drawableGray, null, null);
                mTxtSuccess.setCompoundDrawables(null, drawableGray, null, null);
                mBtnSai.setEnabled(false);
                mBtnDelete.setEnabled(false);
                mEditPhone.setEnabled(false);
                break;
            case SAI_WAIT:
                mTxtState.setText("等待验证，请稍等...");
                mTxtState.setVisibility(View.VISIBLE);
                mImgState.setVisibility(View.VISIBLE);
                mImgState.setImageDrawable(ContextCompat.getDrawable(this, MResource.getIdByName(getApplication(), "drawable", "ic_sai_wait")));
                mTxtStart.setCompoundDrawables(null, drawableGray, null, null);
                mTxtWait.setCompoundDrawables(null, drawableGreen, null, null);
                mTxtSend.setCompoundDrawables(null, drawableGray, null, null);
                mTxtSuccess.setCompoundDrawables(null, drawableGray, null, null);
                mBtnSai.setEnabled(false);
                mBtnDelete.setEnabled(false);
                mEditPhone.setEnabled(false);
                break;
            case SAI_SEND:
                mTxtState.setText("验证码已送达，请稍等...");
                mTxtState.setVisibility(View.VISIBLE);
                mImgState.setVisibility(View.VISIBLE);
                mImgState.setImageDrawable(ContextCompat.getDrawable(this, MResource.getIdByName(getApplication(), "drawable", "ic_sai_send")));
                mTxtStart.setCompoundDrawables(null, drawableGray, null, null);
                mTxtWait.setCompoundDrawables(null, drawableGray, null, null);
                mTxtSend.setCompoundDrawables(null, drawableGreen, null, null);
                mTxtSuccess.setCompoundDrawables(null, drawableGray, null, null);
                mBtnSai.setEnabled(false);
                mBtnDelete.setEnabled(false);
                mEditPhone.setEnabled(false);
                break;
            case SAI_SUCCESS:
                mTxtState.setText("验证成功！");
                mTxtState.setVisibility(View.VISIBLE);
                mImgState.setVisibility(View.VISIBLE);
                mImgState.setImageDrawable(ContextCompat.getDrawable(this, MResource.getIdByName(getApplication(), "drawable", "ic_sai_success")));
                mTxtStart.setCompoundDrawables(null, drawableGray, null, null);
                mTxtWait.setCompoundDrawables(null, drawableGray, null, null);
                mTxtSend.setCompoundDrawables(null, drawableGray, null, null);
                mTxtSuccess.setCompoundDrawables(null, drawableGreen, null, null);
                mBtnSai.setEnabled(!TextUtils.isEmpty(mEditPhone.getText()));
                mBtnDelete.setEnabled(!TextUtils.isEmpty(mEditPhone.getText()));
                mEditPhone.setEnabled(true);
                break;
        }
    }

    private void startService() {
        Intent intent = new Intent(this, SaiService.class);
        startService(intent);
    }

    private void stopService() {
        Intent intent = new Intent(this, SaiService.class);
        stopService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case HAS_PERMISSION:
                {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        requestCall();
                    } else {
                        Toast.makeText(SaiActivity.this, "权限获取失败", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
}
