package com.oud.oudshopify.data;

import com.google.gson.annotations.SerializedName;

public class RefundLineItem {
    @SerializedName("id")
    private long id;

    @SerializedName("line_item_id")
    private long lineItemId;
}
