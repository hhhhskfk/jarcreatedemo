package sai_cqt.sailib.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

import sai_cqt.sailib.activity.SaiActivity;

/**
 * Created by huangjie on 2017/6/8.
 */

public class SaiService extends Service {
    private TelephonyManager tm;
    private MyPhoneStateListener listener;
    private NotificationManager nm;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        listener = new MyPhoneStateListener();
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
    }


    private final class MyPhoneStateListener extends PhoneStateListener {
        //private long startTime = 0;
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    if (!TextUtils.isEmpty(incomingNumber) && incomingNumber.startsWith("95078")) {
                        // 挂断电话
                        endCall();
                        // 删除通话记录
                        Uri uri = Uri.parse("content://call_log/calls");
                        getContentResolver().registerContentObserver(uri, true, new MyContentObserver(new Handler(), incomingNumber));

                        // 验证码送达
                        Intent intent = new Intent();
                        intent.putExtra("type", SaiActivity.SAI_SEND);
                        intent.putExtra("phone", incomingNumber);
                        intent.setAction("broadcast.sai");
                        sendBroadcast(intent);
                    } else {
                        // 验证失败
                        Intent intent = new Intent();
                        intent.putExtra("type", SaiActivity.SAI_FAIL);
                        intent.setAction("broadcast.sai");
                        sendBroadcast(intent);
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    break;

                default:
                    break;
            }
        }

    }

    private class MyContentObserver extends ContentObserver {
        String incomingNumber;

        /**
         * Creates a content observer.
         *
         * @param handler        The handler to run {@link #onChange} on, or null if none.
         * @param incomingNumber
         */
        public MyContentObserver(Handler handler, String incomingNumber) {
            super(handler);
            this.incomingNumber = incomingNumber;
        }

        //当通话记录改变的时候调用的方法
        @Override
        public void onChange(boolean selfChange) {
            getContentResolver().unregisterContentObserver(this);
            //删除电话号码
            deleteCallLog(incomingNumber);
            super.onChange(selfChange);
        }
    }

    //删掉电话号码
    private void deleteCallLog(String incomingNumber) {
        Uri uri = Uri.parse("content://call_log/calls");
        //删除表中的电话号码
        Cursor cursor = null;
        try {
            String[] projection = {"_id"};
            String[] selectionArgs = {incomingNumber};
            cursor = getContentResolver().query(uri, projection, "number=?", selectionArgs, null);
            if (cursor.moveToFirst()) {
                String id = cursor.getString(0);
                getContentResolver().delete(uri, "_id=?", new String[]{id});
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {

            }
        }
    }


    //挂断电话
    private void endCall() {
        try {
            Class<?> clazz = Class.forName("android.os.ServiceManager");
            Method method = clazz.getMethod("getService", String.class);
            IBinder ibinder = (IBinder) method.invoke(null, Context.TELEPHONY_SERVICE);
            ITelephony iTelephony = ITelephony.Stub.asInterface(ibinder);
            iTelephony.endCall();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tm != null && listener != null) {
            tm.listen(listener, PhoneStateListener.LISTEN_NONE);
        }
    }
}
