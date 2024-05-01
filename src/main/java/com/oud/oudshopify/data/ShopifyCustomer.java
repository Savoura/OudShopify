package com.oud.oudshopify.data;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class ShopifyCustomer {
    private String email;
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("last_name")
    private String lastName;
    @SerializedName("phone")
    private String phoneNumber;
    @SerializedName("default_address")
    private ShopifyShippingAddress defaultAddress;
}
