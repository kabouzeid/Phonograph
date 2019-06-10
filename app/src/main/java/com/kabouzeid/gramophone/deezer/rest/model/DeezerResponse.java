package com.kabouzeid.gramophone.deezer.rest.model;

import java.util.ArrayList;
import java.util.List;

public class DeezerResponse<T> {

    List<T> data = new ArrayList<>();

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
