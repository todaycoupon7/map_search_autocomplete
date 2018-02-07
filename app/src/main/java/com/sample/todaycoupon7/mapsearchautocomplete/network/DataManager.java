package com.sample.todaycoupon7.mapsearchautocomplete.network;

import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by chaesooyang on 2018. 2. 7..
 */

public class DataManager {

    private static DataManager mInstance;

    public static synchronized DataManager getInstance() {
        if(mInstance == null) {
            mInstance = new DataManager();
        }
        return mInstance;
    }


    private final HashMap<String, CancelableRequest> mRequestContainer = new HashMap<>();
    private int mTagCount = 1000;	// default 1001부터 시작함.
    private Object mObject = new Object();

    private final Retrofit mRetrofit;

    public DataManager() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        // add logging as last interceptor
        httpClient.addInterceptor(logging);  // <-- this is the important line!

        mRetrofit = new Retrofit.Builder()
                .baseUrl("https://map.naver.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .client(httpClient.build()) // 수발신 데이터 로그를 보기위해 추가함.
                .build();
    }

    public Retrofit getRetrofit() {
        return mRetrofit;
    }

    /**
     * 요청키를 생성하여 반환함.
     * @return
     */
    public String generateKey() {
        StringBuilder tagBuilder = new StringBuilder();
        tagBuilder.append((char)(65 + (25 * Math.random())));
        tagBuilder.append((char) (97 + (25 * Math.random())));
        synchronized (mObject) {
            tagBuilder.append(++mTagCount);
        }
        return tagBuilder.toString();
    }

    /**
     * 요청을 취소함
     * @param tag
     */
    public void cancelRequest(String tag) {
        final CancelableRequest request = popRequest(tag);
        if(request != null) {
            request.cancel();
        }
    }

    public String pushRequest(CancelableRequest request) {
        synchronized (mRequestContainer) {
            final String requestKey = generateKey();
            mRequestContainer.put(requestKey, request);
            return requestKey;
        }
    }

    public CancelableRequest popRequest(String tag) {
        synchronized (mRequestContainer) {
            return mRequestContainer.remove(tag);
        }
    }

    /**
     * 자동완성 목록을 요청함.
     * @param keyword
     * @param listener
     * @return
     */
    public String requestAutocomplete(String keyword, OnRequestAutocompleteListener listener) {
        return new RequestAutocomplete(this).get(keyword, listener);
    }

}
