package com.oud.oudshopify.controllers;

import atlantafx.base.controls.CustomTextField;
import com.oud.oudshopify.backend.KeyValueStore;
import javafx.fxml.FXML;
import javafx.stage.Stage;

public class SettingsController {
    @FXML
    private CustomTextField shopifyApiTextField;
    @FXML
    private CustomTextField shopifyStoreUrlTextField;
    @FXML
    private CustomTextField swingUsernameTextField;
    @FXML
    private CustomTextField swingPasswordTextField;

    public void initialize() {
        shopifyApiTextField.setText(KeyValueStore.getInstance()
                .getShopifyApiKey());
        shopifyStoreUrlTextField.setText(KeyValueStore.getInstance()
                .getShopifyStoreUrl());

        swingUsernameTextField.setText(KeyValueStore.getInstance().getSwingUsername());
        swingPasswordTextField.setText(KeyValueStore.getInstance()
                .getSwingPassword());

    }

    @FXML
    protected void onSaveButtonClicked() {
        KeyValueStore.getInstance()
                .setShopifyApiKey(shopifyApiTextField.getText());
        KeyValueStore.getInstance()
                .setShopifyStoreName(shopifyStoreUrlTextField.getText());

        KeyValueStore.getInstance().setSwingUsername(swingUsernameTextField.getText());
        KeyValueStore.getInstance()
                .setSwingPassword(swingPasswordTextField.getText());

        Stage stage = (Stage) shopifyApiTextField.getScene().getWindow();
        stage.close();
    }
}
