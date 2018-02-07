package com.sample.todaycoupon7.mapsearchautocomplete;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.sample.todaycoupon7.mapsearchautocomplete.dto.AutocompleteDto;
import com.sample.todaycoupon7.mapsearchautocomplete.network.DataManager;
import com.sample.todaycoupon7.mapsearchautocomplete.network.OnRequestAutocompleteListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText mEtInput;
    private RecyclerView mRvAutocomplete;
    private AutocompleteListAdapter mAutocompleteListAdapter;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private AutocompleteRunnable mAutocompleteRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEtInput = findViewById(R.id.etInput);
        mEtInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mRvAutocomplete.getVisibility() != View.VISIBLE) {
                    mRvAutocomplete.setVisibility(View.VISIBLE);
                }
                requestAutocomplete(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mRvAutocomplete = findViewById(R.id.rvAutocomplete);
        mRvAutocomplete.setHasFixedSize(true);
        mRvAutocomplete.setLayoutManager(new LinearLayoutManager(this));
        mAutocompleteListAdapter = new AutocompleteListAdapter();
        mRvAutocomplete.setAdapter(mAutocompleteListAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAutocompleteRunnable != null) {
            mAutocompleteRunnable.cancel();
            mHandler.removeCallbacks(mAutocompleteRunnable);
            mAutocompleteRunnable = null;
        }
    }

    /**
     * 자동완성 목록 요청
     * @param keyword
     */
    private void requestAutocomplete(String keyword) {
        // 이전 요청이 완료되기 전에 새로운 요청이 오는 경우, 이전 요청을 취소함
        if(mAutocompleteRunnable != null) {
            mAutocompleteRunnable.cancel();
            mHandler.removeCallbacks(mAutocompleteRunnable);
            mAutocompleteRunnable = null;
        }
        if(TextUtils.isEmpty(keyword)) {
            mAutocompleteListAdapter.clear();
        } else {    // 자동완성 요청 키워드가 있는 경우에만 요청함.
            mAutocompleteRunnable = new AutocompleteRunnable(keyword);
            mHandler.post(mAutocompleteRunnable);
        }
    }

    /**
     * 텍스트 입력에 따른 자동완성 요청 Runnable
     */
    private class AutocompleteRunnable implements Runnable {

        private String keyword;
        private String reqAutocomplete;

        public AutocompleteRunnable(String keyword) {
            this.keyword = keyword;
        }

        public void cancel() {
            if(!TextUtils.isEmpty(reqAutocomplete)) {
                DataManager.getInstance().cancelRequest(reqAutocomplete);
                reqAutocomplete = null;
            }
        }

        @Override
        public void run() {
            reqAutocomplete = DataManager.getInstance().requestAutocomplete(keyword,
                    new OnRequestAutocompleteListener() {
                        @Override
                        public void onCompleted(AutocompleteDto dto) {
                            reqAutocomplete = null;
                            if(dto != null) {
                                if(mEtInput.getText().toString().equals(dto.getQueryKeyword())) {
                                    mAutocompleteListAdapter.setKeyword(dto.results);
                                    mAutocompleteListAdapter.notifyDataSetChanged();
                                }
                            }
                        }

                        @Override
                        public void onError() {
                            reqAutocomplete = null;
                        }
                    });
        }
    }

    private class AutocompleteListAdapter extends RecyclerView.Adapter<AutocompleteListAdapter.ViewHolder> {

        private ArrayList<String> mKeywords;

        public void setKeyword(ArrayList<String> keywords) {
            mKeywords = keywords;
        }

        public void clear() {
            mKeywords = null;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_autocomplete, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.onBind(mKeywords.get(position));
        }

        @Override
        public int getItemCount() {
            return mKeywords == null ? 0 : mKeywords.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView tvKeyword;
            private String keyword;

            public ViewHolder(View itemView) {
                super(itemView);
                tvKeyword = itemView.findViewById(R.id.tvKeyword);
            }

            public void onBind(String keyword) {
                this.keyword = keyword;
                tvKeyword.setText(this.keyword);
            }

        }
    }

}
