package sai_cqt.sailib.net;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Created by Administrator on 2017/5/24.
 */

public interface ApiService {

    @Headers({"Content-type:application/json;charset=UTF-8"})
    @POST()
    Call<ResponseBody> post(@Url String url, @Body RequestBody body);
}
