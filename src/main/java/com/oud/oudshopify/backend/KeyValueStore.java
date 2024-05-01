package com.oud.oudshopify.backend;

public class KeyValueStore {
    private final String SHIP_BLU_API = "SHIP_BLU_API_KEY";
    private final String SHOPIFY_API_KEY = "SHOPIFY_API_KEY";
    private final String BOSTA_API_KEY = "BOSTA_API_KEY";
    private final String SHOPIFY_STORE_NAME = "SHOPIFY_STORE_NAME_KEY";
    private final String MAIN_DOCUMENT = "API_KEYS";
    private static KeyValueStore keyValueStore;

    private KeyValueStore() {
    }

    public static synchronized KeyValueStore getInstance() {
        if (keyValueStore == null)
            keyValueStore = new KeyValueStore();
        return keyValueStore;
    }

    public String getBostaApiKey() {
        return Database.getDatabase().getStringValue(MAIN_DOCUMENT, BOSTA_API_KEY);
    }

    public String getShipBluApiKey() {
        return Database.getDatabase().getStringValue(MAIN_DOCUMENT, SHIP_BLU_API);
    }

    public String getShopifyApiKey() {
        return Database.getDatabase().getStringValue(MAIN_DOCUMENT, SHOPIFY_API_KEY);
    }

    public String getShopifyStoreUrl() {
        return Database.getDatabase().getStringValue(MAIN_DOCUMENT, SHOPIFY_STORE_NAME);
    }

    public void setBostaApiKey(String bostaApiKey) {
        updateStore(BOSTA_API_KEY, bostaApiKey);
    }

    public void setShipBluApiKey(String shipBluApiKey) {
        updateStore(SHIP_BLU_API, shipBluApiKey);
    }

    public void setShopifyApiKey(String shopifyApiKey) {
        updateStore(SHOPIFY_API_KEY, shopifyApiKey);
    }

    public void setShopifyStoreName(String storeName) {
        updateStore(SHOPIFY_STORE_NAME, storeName);
    }

    private void updateStore(String key, String value) {
        Database.getDatabase().updateStringValue(MAIN_DOCUMENT, key, value);
    }
}
