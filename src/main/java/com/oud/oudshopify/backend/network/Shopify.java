package com.oud.oudshopify.backend.network;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.oud.oudshopify.backend.KeyValueStore;
import com.oud.oudshopify.data.ShopifyOrder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Shopify {
    private static final List<String> FIELDS_TO_FETCH = List.of("id", "name", "line_items", "total_price",
            "customer", "fulfillment_status", "phone", "tags", "confirmed", "shipping_address",
            "fulfillments");
    private static final String NEXT_LINK_REGEX = "<([^<>]+)>; rel=\"next\"";

    private List<ShopifyOrder> orders;

    public Shopify() {
        orders = new ArrayList<>();
    }

    private class ShopifyOrderRequestUriBuilder {
        private String baseUrl;
        private List<String> fieldsToFetch;
        private LocalDate createdAfter;

        public ShopifyOrderRequestUriBuilder() {
            baseUrl = String.format("https://%s/admin/api/2024-01/orders.json", KeyValueStore.getInstance()
                    .getShopifyStoreUrl());
        }

        public ShopifyOrderRequestUriBuilder setFieldsToFetch(List<String> fields) {
            fieldsToFetch = fields;
            return this;
        }

        public ShopifyOrderRequestUriBuilder onlyCreatedAfter(LocalDate date) {
            createdAfter = date;
            return this;
        }

        public String build() {
            List<String> params = new ArrayList<>();
            params.add("status=any");
            params.add("limit=200");
            if (fieldsToFetch != null && !fieldsToFetch.isEmpty()) {
                params.add(String.format("fields=%s", String.join(",", fieldsToFetch)));
            }
            if (createdAfter != null) {
                // Date has to be ISO 8601 compliant.
                Instant instant = createdAfter.atStartOfDay(ZoneId.systemDefault())
                        .toInstant();
                params.add(String.format("created_at_min=%s", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
                        .withZone(ZoneOffset.UTC)
                        .format(instant)));
            }
            if (!params.isEmpty()) {
                return String.format("%s?%s", baseUrl, String.join("&", params));
            }
            return baseUrl;
        }
    }

    private String getShopifyOrdersUri() {
        return String.format("https://%s.myshopify.com/admin/api/2024-01/orders.json", KeyValueStore.getInstance()
                .getShopifyStoreUrl());
    }

    private HttpRequest createGetAllOrders(Optional<String> uri, LocalDate filterDate) throws URISyntaxException {
        HttpRequest.Builder builder = HttpRequest.newBuilder().GET();
        if (uri.isEmpty()) {
            builder.uri(new URI(new ShopifyOrderRequestUriBuilder().setFieldsToFetch(FIELDS_TO_FETCH)
                    .onlyCreatedAfter(filterDate)
                    .build()));
        } else {
            builder.uri(new URI(uri.get()));
        }
        builder.setHeader("X-Shopify-Access-Token", KeyValueStore.getInstance()
                .getShopifyApiKey());
        return builder.build();
    }

    private List<ShopifyOrder> parseOrderResponse(HttpResponse<String> response) {
        JsonElement element = JsonParser.parseString(response.body());
        return new Gson().fromJson(element.getAsJsonObject()
                        .get("orders"),
                new TypeToken<ArrayList<ShopifyOrder>>() {
                }.getType());
    }

    private HttpResponse<String> getNextResponse(HttpResponse<String> response) throws URISyntaxException, IOException, InterruptedException {
        Map<String, List<String>> headers = response.headers().map();
        if (headers.containsKey("link")) {
            Pattern pattern = Pattern.compile(NEXT_LINK_REGEX, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(headers.get("link").get(0));
            boolean matchFound = matcher.find();
            if (matchFound) {
                return HttpExecutor.getInstance()
                        .sendRequest(createGetAllOrders(Optional.of(matcher.group(1)), null));

            }
        }
        return null;
    }

    public void refreshOrders(LocalDate filterDate) throws URISyntaxException, IOException, InterruptedException {
        orders.clear();
        HttpResponse<String> response = HttpExecutor.getInstance()
                .sendRequest(createGetAllOrders(Optional.empty(), filterDate));
        if (response.statusCode() == 200) {
            orders.addAll(parseOrderResponse(response));
            orders.addAll(getNextOrders(response));
        }
    }

    public List<ShopifyOrder> getOrders() {
        return orders;
    }

    public List<ShopifyOrder> getNextOrders(HttpResponse<String> response) throws IOException, InterruptedException, URISyntaxException {
        HttpResponse<String> nextResponse = getNextResponse(response);
        if (nextResponse == null || nextResponse.statusCode() != 200)
            return List.of();
        List<ShopifyOrder> orders = parseOrderResponse(nextResponse);
        orders.addAll(getNextOrders(nextResponse));
        return orders;
    }
}
