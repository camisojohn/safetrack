package com.example.safetrack.api;

import com.example.safetrack.utils.Constants;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static SupabaseApiService getService() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request  = original.newBuilder()
                                .header("apikey",
                                        Constants.SUPABASE_KEY)
                                .header("Authorization",
                                        "Bearer " + Constants.SUPABASE_KEY)
                                .header("Content-Type",
                                        "application/json")
                                .header("Prefer",
                                        "return=representation")
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.SUPABASE_URL + "/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(SupabaseApiService.class);
    }
}