package sai_cqt.sailib.net;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Retrofit;

/**
 * Created by Administrator on 2017/5/24.
 */

public class RetrofitClient {
    private static ApiService mApiService;
    private static RetrofitClient mInstance;
    private static final int DEFAULT_TIMEOUT = 10;
    private static String baseUrl = "http://153.3.49.41:8078/ciaproject/cia/";

    public static RetrofitClient getInstance() {
        if (mInstance == null) {
            synchronized (RetrofitClient.class) {
                if (mInstance == null) {
                    synchronized (RetrofitClient.class) {
                        mInstance = new RetrofitClient();
                    }
                }
            }
        }
        return mInstance;
    }

    public void init() {
        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .build();
        mApiService = retrofit.create(ApiService.class);
    }

    public void post(String action, RequestBody body, Callback<ResponseBody> callback) {
        mApiService.post(action, body).enqueue(callback);
    }
}
