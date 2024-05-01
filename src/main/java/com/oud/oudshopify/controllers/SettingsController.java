package com.oud.oudshopify.controllers;

import atlantafx.base.controls.CustomTextField;
import com.oud.oudshopify.backend.KeyValueStore;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SettingsController {
    @FXML
    private CustomTextField shopifyApiTextField;
    @FXML
    private CustomTextField shopifyStoreUrlTextField;

    public void initialize() {
        shopifyApiTextField.setText(KeyValueStore.getInstance()
                .getShopifyApiKey());
        shopifyStoreUrlTextField.setText(KeyValueStore.getInstance()
                .getShopifyStoreUrl());

    }

    @FXML
    protected void onSaveButtonClicked() {
        KeyValueStore.getInstance()
                .setShopifyApiKey(shopifyApiTextField.getText());
        KeyValueStore.getInstance()
                .setShopifyStoreName(shopifyStoreUrlTextField.getText());
        Stage stage = (Stage) shopifyApiTextField.getScene().getWindow();
        stage.close();
    }
}
