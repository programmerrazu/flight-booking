package eagles.com.helpers;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface GoogleAPI {
    @GET
    Call<String> getPath(@Url String url);
}