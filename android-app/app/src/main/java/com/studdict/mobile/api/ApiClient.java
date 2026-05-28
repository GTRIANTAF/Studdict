package com.studdict.mobile.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    public static final String BASE_URL = "http://192.168.1.110:8080/";

    private static StuddictApi api;

    private ApiClient() {
    }

    public static StuddictApi getApi() {
        if (api == null) {
            api = getRetrofitInstance().create(StuddictApi.class);
        }

        return api;
    }

    public static Retrofit getRetrofitInstance() {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
