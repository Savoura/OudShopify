package com.oud.oudshopify.data;

import com.google.gson.annotations.SerializedName;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ShopifyOrder {
    @SerializedName("id")
    private long id;
    @SerializedName("fulfillment_status")
    private String fulFillmentStatus;
    private String name;
    @SerializedName("total_price")
    private String totalPrice;
    private ShopifyCustomer customer;

    @SerializedName("line_items")
    private List<ShopifyItem> items;

    @SerializedName("phone")
    @Getter(AccessLevel.NONE)
    private String phoneNumber;

    @Getter(AccessLevel.NONE)
    private boolean confirmed;

    private String tags;

    @SerializedName("shipping_address")
    private ShopifyShippingAddress shippingAddress;

    @SerializedName("fulfillments")
    private List<ShopifyFulfillment> fulfillment;

    public boolean isConfirmed() {
        return tagsContainAny(new String[]{"confirmed", "Confirmed", "Confirm", "confirm"});
    }

    public String getPhoneNumber() {
        if (phoneNumber != null)
            return phoneNumber;
        else if (shippingAddress.getPhoneNumber() != null)
            return shippingAddress.getPhoneNumber();
        else if (getCustomer().getPhoneNumber() != null)
            return getCustomer().getPhoneNumber();
        else
            return getCustomer().getDefaultAddress().getPhoneNumber();
    }

    private List<String> getTagsFiltered() {
        String[] tags = getTags().split(",");
        return Arrays.stream(tags).map(String::trim)
                .collect(Collectors.toList());
    }

    public boolean tagsContainAny(String[] searchTags)
    {
        for(String str : searchTags) {
            if(getTagsFiltered().contains(str))
                return true;
        }
        return false;
    }
    public boolean tagsContain(String str) {
        return getTagsFiltered().contains(str);
    }
}
