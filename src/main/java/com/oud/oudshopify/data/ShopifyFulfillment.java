package com.oud.oudshopify.data;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Data
public class ShopifyFulfillment {
    @SerializedName("created_at")
    @Getter(AccessLevel.NONE)
    private String createdDate;
    @SerializedName("tracking_company")
    private String shoppingCompany;

    @SerializedName("updated_at")
    @Getter(AccessLevel.NONE)
    private String updatedDate;

    public LocalDate getCreatedDate() {
        return LocalDateTime.parse(createdDate, DateTimeFormatter.ISO_DATE_TIME)
                .toLocalDate();
    }

    public LocalDate getUpdatedDate() {
        return LocalDateTime.parse(updatedDate, DateTimeFormatter.ISO_DATE_TIME)
                .toLocalDate();
    }
}
