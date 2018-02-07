package com.sample.todaycoupon7.mapsearchautocomplete.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

/**
 * Created by chaesooyang on 2018. 2. 7..
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class AutocompleteDto {
    public ArrayList<String> query;
    public ArrayList<String> results;

    public void setItems(ArrayList<ArrayList<ArrayList<String>>> items) {
        results = new ArrayList<>();
        for(ArrayList<ArrayList<String>> item1 : items) {
            for(ArrayList<String> item2 : item1) {
                results.addAll(item2);
            }
        }
    }

    public String getQueryKeyword() {
        if(query != null &&
                query.size() > 0) {
            return query.get(0);
        }
        return null;
    }
}
