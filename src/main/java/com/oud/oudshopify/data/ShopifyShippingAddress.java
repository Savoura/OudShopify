package com.oud.oudshopify.data;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class ShopifyShippingAddress {
    @SerializedName("address1")
    private String firstAddress;
    @SerializedName("address2")
    private String secondAddress;
    @SerializedName("phone")
    private String phoneNumber;
    private String city;
    private String province;
    private String country;
}
