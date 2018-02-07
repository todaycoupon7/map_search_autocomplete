package com.sample.todaycoupon7.mapsearchautocomplete.network;

import com.sample.todaycoupon7.mapsearchautocomplete.dto.AutocompleteDto;

/**
 * Created by chaesooyang on 2018. 2. 7..
 */

public interface OnRequestAutocompleteListener extends OnBaseRequestListener {
    void onCompleted(AutocompleteDto dto);
}