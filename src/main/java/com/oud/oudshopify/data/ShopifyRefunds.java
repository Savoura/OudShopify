package com.oud.oudshopify.data;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

public class ShopifyRefunds {
    private long id;

    @SerializedName("refund_line_items")
    private List<RefundLineItem> refundLineItems;
}
