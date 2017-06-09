package sai_cqt.sailib.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by huangjie on 2017/6/7.
 */

public class Utils {

    public static String getPhone(Context context) {
        String phone = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                phone = telephonyManager.getLine1Number();
                if (phone.indexOf("+86") != -1) {
                    phone = phone.substring(3);
                }
            }
        } catch (Exception e) {

        }
        return phone;
    }

    public static String verifiCode(String phone, String messageId) {
        String code = "";
        DES3 des3 = new DES3();
        try {
            String encode = "";
            encode = encode.concat(messageId).concat(phone);
            code = des3.encode(encode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    public static String decodeAES(String json) {
        String decode = "";
        try {
            String keyStr = "";
            byte[] key = Cryptos.hexStringToBytes("8445C55CB635FB10B667872001BFD9AE");
            decode = Cryptos.aesDecrypt(Cryptos.hexStringToBytes(json), key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decode;
    }

    public static String encodeAES(String json) {
        String encode = "";
        try {
            byte[] key = Cryptos.hexStringToBytes("8445C55CB635FB10B667872001BFD9AE");
            byte[] encryptResult = Cryptos.aesEncrypt(json.getBytes(), key);
            encode = Cryptos.bytesToHexString(encryptResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encode;
    }

    public static void hideSoftInput(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
    }
}
