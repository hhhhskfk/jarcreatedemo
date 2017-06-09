package sai_cqt.sailib.util;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.http.POST;

/**
 * Created by Administrator on 2017/5/8.
 */

public class JsonUtils {

    public static RequestBody toJson(Map<String, Object> header, Map<String, Object> body) {
        RequestBody requestBody = null;
        try {
            JSONObject root = new JSONObject();
            if (header != null) {
                JSONObject headerObject = new JSONObject();
                for (String key : header.keySet()) {
                    headerObject.put(key, header.get(key));
                }
                root.put("header", headerObject);
            }
            if (body != null) {
                JSONObject bodyObject = new JSONObject();
                for (String key : body.keySet()) {
                    bodyObject.put(key, body.get(key));
                }
                root.put("body", bodyObject);
            }
            String json = Utils.encodeAES(root.toString());
            requestBody = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), json);
        } catch (Exception e) {

        }
        return requestBody;
    }

    public static Map<String, Object> toBean(String body) {
        HashMap<String, Object> map = new HashMap<>();
        try {
            String json = Utils.decodeAES(body);
            JSONObject jsonObject = new JSONObject(json);
            jsonObject = jsonObject.getJSONObject("header");
            if (jsonObject != null) {
                Iterator<String> iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next().toString();
                    Object value = jsonObject.opt(key);
                    map.put(key, value);
                }
            }
        } catch (Exception e) {

        }
        return map;
    }
}
