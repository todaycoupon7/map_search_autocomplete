package com.sample.todaycoupon7.mapsearchautocomplete.network;

import com.sample.todaycoupon7.mapsearchautocomplete.dto.AutocompleteDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by chaesooyang on 2018. 2. 7..
 */

public class RequestAutocomplete extends CancelableRequest {

    private final DataManager mDataMgr;

    private OnRequestAutocompleteListener mListener;

    private String mRequestKey;
    private Call<AutocompleteDto> mCall;

    private interface AutocompleteService {
        @GET("/ac.map/ac")
        Call<AutocompleteDto> get(
                @Query("q") String q,
                @Query("st") int st,
                @Query("r_lt") int r_lt,
                @Query("r_format") String r_format,
                @Query("t_koreng") int t_koreng,
                @Query("q_enc") String q_enc,
                @Query("r_enc") String r_enc,
                @Query("r_unicode") int r_unicode);
    }

    public RequestAutocomplete(DataManager dm) {
        mDataMgr = dm;
    }

    @Override
    public void cancel() {
        // listener
        mListener = null;
        // request
        if(mCall != null &&
                !mCall.isCanceled()) {
            mCall.cancel();
        }
    }

    public String get(String q, OnRequestAutocompleteListener listener) {
        mListener = listener;
        mRequestKey = mDataMgr.pushRequest(this);
        mCall = mDataMgr.getRetrofit().create(AutocompleteService.class).get(
                q,
                10,
                10,
                "json",
                1,
                "UTF-8",
                "UTF-8",
                0);
        mCall.enqueue(new Callback<AutocompleteDto>() {
            @Override
            public void onResponse(Call<AutocompleteDto> call, Response<AutocompleteDto> response) {
                if(mListener != null) {
                    if(response != null &&
                            response.isSuccessful()) {
                        mListener.onCompleted(response.body());
                    } else {
                        mListener.onError();
                    }
                }
                mDataMgr.popRequest(mRequestKey);
            }

            @Override
            public void onFailure(Call<AutocompleteDto> call, Throwable t) {
                if(mListener != null) {
                    mListener.onError();
                }
                mDataMgr.popRequest(mRequestKey);
            }
        });
        return mRequestKey;
    }
}
