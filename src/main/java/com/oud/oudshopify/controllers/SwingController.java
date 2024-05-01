package com.oud.oudshopify.controllers;

import atlantafx.base.theme.Styles;
import com.oud.oudshopify.Application;
import com.oud.oudshopify.backend.network.Shopify;
import com.oud.oudshopify.backend.network.Swing;
import com.oud.oudshopify.data.ShopifyItem;
import com.oud.oudshopify.data.ShopifyOrder;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class SwingController {
    private final Swing swing;

    @FXML
    private TextField orderRefText;
    @FXML
    private TextField costText;
    @FXML
    private TextField customerNameText;
    @FXML
    private TextField phoneText;
    @FXML
    private TextArea addressText;
    @FXML
    private TextArea notesText;
    @FXML
    private ComboBox<String> cityComboBox;
    @FXML
    private ComboBox<String> zoneComboBox;
    @FXML
    private ComboBox<Integer> piecesComboBox;
    @FXML
    private ComboBox<Integer> weightComboBox;

    ObservableList<Integer> CHOICES = FXCollections.observableList(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    ObservableList<String> CITIES = FXCollections.observableList(List.of(
            "Choose",
            "الاسكندرية",
            "العجمي",
            "برج العرب",
            "القاهرة",
            "الجيزة",
            "محافظات الدلتا",
            "ضواحي القاهره و الجيزه",
            "مدن القناه",
            "مدن الصعيد",
            "الاقصر و اسوان",
            "مطروح",
            "الغردقة"));
    private ShopifyOrder order;
    private Runnable uploadCallback;

    public SwingController() {
        swing = Swing.getInstance();
    }

    public void setShopifyOrder(ShopifyOrder order) {
        this.order = order;
    }

    public void setOnUploadSuccededCallback(Runnable runnable) {
        this.uploadCallback = runnable;
    }

    private void setAddressField() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(order.getShippingAddress().getFirstAddress())
                .append("\n");
        stringBuilder.append(order.getShippingAddress().getCity()).append("\n");
        if (order.getShippingAddress().getProvince() != null) {
            stringBuilder.append(order.getShippingAddress().getProvince())
                    .append("\n");
        }
        stringBuilder.append(order.getShippingAddress().getCountry())
                .append("\n");
        addressText.setText(stringBuilder.toString());
    }

    public void fillFields() {
        orderRefText.setText(order.getName());
        customerNameText.setText(order.getCustomer()
                .getFirstName() + " " + order.getCustomer().getLastName());
        phoneText.setText(order.getPhoneNumber().replace("+2", ""));
        setAddressField();
        piecesComboBox.setValue(Math.min(10, order.getItems().size()));
        weightComboBox.setValue(Math.min(10, Math.max(order.getItems()
                .size() / 2, 1)));
        costText.setText(order.isPaid() ? "1" : order.getTotalPrice());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Order No: ").append(order.getName()).append("\n");
        for (ShopifyItem item : order.getItems()) {
            stringBuilder.append(item.getName()).append(": ")
                    .append(item.getQuantity()).append("\n");
        }
        notesText.setText(stringBuilder.toString());
    }

    public void initialize() {
        piecesComboBox.setItems(CHOICES);
        weightComboBox.setItems(CHOICES);
        cityComboBox.setItems(CITIES);
        cityComboBox.getSelectionModel().select(0);
        zoneComboBox.setDisable(true);
        cityComboBox.valueProperty().addListener((observableValue, s, t1) -> {
            if (cityComboBox.getValue().equals("Choose")) {
                zoneComboBox.setDisable(false);
                zoneComboBox.setValue("");
                return;
            }
            try {
                zoneComboBox.setItems(FXCollections.observableList(swing.getZones(cityComboBox.getValue())));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            zoneComboBox.setDisable(false);
        });
    }

    @FXML
    protected void onUploadClicked() throws IOException {
        if (swing.uploadOrder(orderRefText.getText(), customerNameText.getText(), phoneText.getText(), cityComboBox.getValue(),
                zoneComboBox.getValue(), addressText.getText(), String.valueOf(piecesComboBox.getValue())
                , String.valueOf(weightComboBox.getValue()), costText.getText(), notesText.getText())) {
            uploadCallback.run();
            onCancelClicked();
        } else {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Failed to upload order to Swing");
            alert.setContentText("Failed to upload order to Swing");
            alert.initOwner(weightComboBox.getScene().getWindow());
            alert.show();
        }
    }

    @FXML
    protected void onCancelClicked() {
        Stage stage = (Stage) orderRefText.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
}