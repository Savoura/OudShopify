package com.oud.oudshopify.backend.network;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.oud.oudshopify.backend.KeyValueStore;
import com.oud.oudshopify.data.ShopifyOrder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    private static final List<String> FIELDS_TO_FETCH = List.of("id", "name", "line_items", "current_total_price",
            "customer", "fulfillment_status", "phone", "tags", "confirmed", "shipping_address",
            "fulfillments", "financial_status", "refunds");
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
            baseUrl = String.format("https://%s/admin/api/2024-04/orders.json", KeyValueStore.getInstance()
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
        return String.format("https://%s.myshopify.com/admin/api/2024-04/orders.json", KeyValueStore.getInstance()
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
        JsonObject jsonObject = element.getAsJsonObject();
        JsonArray ordersArray = jsonObject.getAsJsonArray("orders");

        List<ShopifyOrder> orders = new ArrayList<>();

        for (JsonElement orderElement : ordersArray) {
            JsonObject orderObject = orderElement.getAsJsonObject();
            ShopifyOrder order = new Gson().fromJson(orderObject, ShopifyOrder.class);

            // Check if refunds are present
            if (orderObject.has("refunds")) {
                JsonArray refundsArray = orderObject.getAsJsonArray("refunds");
                for (JsonElement refundElement : refundsArray) {
                    JsonObject refundObject = refundElement.getAsJsonObject();
                    JsonArray refundLineItemsArray = refundObject.getAsJsonArray("refund_line_items");
                    for (JsonElement refundLineItemElement : refundLineItemsArray) {
                        JsonObject refundLineItemObject = refundLineItemElement.getAsJsonObject();
                        long lineItemId = refundLineItemObject.get("line_item_id")
                                .getAsLong();
                        // Remove refunded line item from order items
                        order.getItems()
                                .removeIf(item -> item.getId() == lineItemId);
                    }
                }
            }

            orders.add(order);
        }

        return orders;
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

    public String getPreviousTags(ShopifyOrder order) {
        return order.getTags();
    }

    public void updateOrderTags(ShopifyOrder order, String tags) {
        order.setTags(tags);
    }

    public boolean addTag(ShopifyOrder order, String tag) throws URISyntaxException, IOException, InterruptedException {
        String previousTags = getPreviousTags(order);
        if (!previousTags.isEmpty()) {
            tag = previousTags + ", " + tag;
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            tag = tag + ", " + date;
        }
        String editOrderUrl = String.format("https://%s/admin/api/2024-04/orders/%d.json",
                KeyValueStore.getInstance()
                        .getShopifyStoreUrl(), order.getId());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("order", Map.of("id", order.getId(), "tags", tag));
        String requestBodyJson = new Gson().toJson(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(editOrderUrl))
                .header("Content-Type", "application/json")
                .header("X-Shopify-Access-Token", KeyValueStore.getInstance()
                        .getShopifyApiKey())
                .PUT(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        HttpResponse<String> response = HttpExecutor.getInstance()
                .sendRequest(request);

        if (response.statusCode() == 200) {
            System.out.println("Order successfully updated");
            updateOrderTags(order, tag);
            return true;
        } else {
            System.out.println("Failed to update order. Response code: " + response.statusCode());
            return false;
        }
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

}
