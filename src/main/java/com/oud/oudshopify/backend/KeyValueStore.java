package com.oud.oudshopify.backend;

public class KeyValueStore {
    private final String SHOPIFY_API_KEY = "SHOPIFY_API_KEY";
    private final String SHOPIFY_STORE_NAME = "SHOPIFY_STORE_NAME_KEY";
    private final String SWING_USERNAME = "SWING_USERNAME";
    private final String SWING_PASSWORD = "SWING_PASSWORD";
    private final String MAIN_DOCUMENT = "API_KEYS";
    private static KeyValueStore keyValueStore;

    private KeyValueStore() {
    }

    public static synchronized KeyValueStore getInstance() {
        if (keyValueStore == null)
            keyValueStore = new KeyValueStore();
        return keyValueStore;
    }

    public String getSwingUsername() {
        return Database.getDatabase().getStringValue(MAIN_DOCUMENT, SWING_USERNAME);
    }

    public String getSwingPassword() {
        return Database.getDatabase().getStringValue(MAIN_DOCUMENT, SWING_PASSWORD);
    }

    public String getShopifyApiKey() {
        return Database.getDatabase().getStringValue(MAIN_DOCUMENT, SHOPIFY_API_KEY);
    }

    public String getShopifyStoreUrl() {
        return Database.getDatabase().getStringValue(MAIN_DOCUMENT, SHOPIFY_STORE_NAME);
    }

    public void setSwingUsername(String username) {
        updateStore(SWING_USERNAME, username);
    }

    public void setSwingPassword(String password) {
        updateStore(SWING_PASSWORD, password);
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
