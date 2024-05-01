package com.oud.oudshopify.controllers;

import atlantafx.base.theme.Styles;
import com.oud.oudshopify.Application;
import com.oud.oudshopify.backend.network.Shopify;
import com.oud.oudshopify.controllers.filterable.*;
import com.oud.oudshopify.data.ShopifyItem;
import com.oud.oudshopify.data.ShopifyOrder;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController {
    @FXML
    private FilteredTableViewV2<ShopifyOrder> ordersTableView;

    @FXML
    private DatePicker ordersDate;

    private CheckBox selectAll;

    @FXML
    private AnchorPane pane;
    private FilteredList<ShopifyOrder> filteredList;
    private final Shopify shopify;
    private FilterableStringTableColumnV2<ShopifyOrder, String> orderNameCol, tagsCol, phoneNumberCol;
    private FilterableDateTableColumnV2<ShopifyOrder, LocalDate> fulfillmentDateCol;
    private TableColumn<ShopifyOrder, String> nameCol;
    private TableColumn<ShopifyOrder, CheckBox> checkboxCol;
    private static final HashMap<String, Boolean> pickedOrdersMap = new HashMap<>();

    public MainController() {
        shopify = new Shopify();
    }

    private void createColumns() {
        selectAll = new CheckBox();
        checkboxCol = getOrderCheckBoxTableColumn(selectAll);
        orderNameCol = new FilterableStringTableColumnV2<>("Order Name");
        nameCol = new TableColumn<>("Customer Name");
        phoneNumberCol = new FilterableStringTableColumnV2<>("Phone Number");
        fulfillmentDateCol = new FilterableDateTableColumnV2<>("Fulfillment Date");
        tagsCol = new FilterableStringTableColumnV2<>("Tags");
    }

    private void createHooks() {
        selectAll.setOnAction(evt -> {
            ordersTableView.getItems().forEach(
                    item -> pickedOrdersMap.put(item.getName(), selectAll.isSelected())
            );
            ordersTableView.refresh();
            evt.consume();
        });
        ordersDate.valueProperty()
                .addListener((observableValue, localDate, t1) -> refreshOrders());
        ordersTableView.addEventHandler(ColumnFilterEventV2.FILTER_CHANGED_EVENT, columnFilterEventV2 -> updatePredicate());
    }

    private void styleColumns() {
        orderNameCol.setPrefWidth(150);
        nameCol.setPrefWidth(250);
        phoneNumberCol.setPrefWidth(250);
        fulfillmentDateCol.setPrefWidth(200);
        tagsCol.setPrefWidth(500);
        tagsCol.setCellFactory(tv -> new TableTagsCell());
        Styles.toggleStyleClass(ordersTableView, Styles.STRIPED);
        Styles.toggleStyleClass(ordersTableView, Styles.BORDERED);
    }

    private void createValueFactories() {
        orderNameCol.setCellValueFactory(orderCellData -> new SimpleStringProperty(orderCellData.getValue()
                .getName()));
        nameCol.setCellValueFactory(orderCellData -> {
            String firstName = orderCellData.getValue().getCustomer()
                    .getFirstName();
            String lastName = orderCellData.getValue().getCustomer()
                    .getLastName();
            return new SimpleStringProperty(String.format("%s %s", firstName, lastName));
        });
        phoneNumberCol.setCellValueFactory(shopifyOrderStringCellDataFeatures -> new SimpleStringProperty(shopifyOrderStringCellDataFeatures.getValue()
                .getPhoneNumber()));
        fulfillmentDateCol.setCellValueFactory(shopifyOrder -> {
            if (shopifyOrder.getValue().getFulfillment().isEmpty())
                return null;
            return new SimpleObjectProperty<>(shopifyOrder.getValue()
                    .getFulfillment().get(0)
                    .getUpdatedDate());
        });
        tagsCol.setCellValueFactory(orderCellData -> new SimpleStringProperty(orderCellData.getValue()
                .getTags()));
        ordersTableView.setRowFactory(shopifyOrderTableView -> {
            TableRow<ShopifyOrder> tableRow = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem sendToBosta = new MenuItem("Send to Swing");
            sendToBosta.setOnAction(actionEvent -> {
                try {
                    showSwingUpload(ordersTableView.getSelectionModel()
                            .getSelectedItem());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            contextMenu.getItems().add(sendToBosta);
            tableRow.setContextMenu(contextMenu);
            return tableRow;
        });
    }

    private void showSwingUpload(ShopifyOrder order) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("swing_form.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("Swing Upload Form");
        SwingController controller = fxmlLoader.getController();
        controller.setShopifyOrder(order);
        controller.setOnUploadSuccededCallback(new Runnable() {
            @Override
            public void run() {
                try {
                    if (shopify.addTag(order, "سوينج")) {
                        ordersTableView.refresh();
                    }
                } catch (URISyntaxException | IOException |
                         InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        controller.fillFields();
        stage.setScene(scene);
        stage.sizeToScene();
        stage.showAndWait();
    }

    public void initialize() {
        createColumns();
        styleColumns();
        createHooks();
        createValueFactories();
        ordersTableView.getColumns()
                .addAll(checkboxCol, orderNameCol, nameCol, phoneNumberCol, fulfillmentDateCol, tagsCol);
        ordersTableView.getSelectionModel()
                .setSelectionMode(SelectionMode.SINGLE);
        ordersDate.setValue(LocalDate.now().minusWeeks(1));
    }

    private static TableColumn<ShopifyOrder, CheckBox> getOrderCheckBoxTableColumn(CheckBox selectAll) {
        TableColumn<ShopifyOrder, CheckBox> checkboxCol = new TableColumn<>();
        checkboxCol.setGraphic(selectAll);
        checkboxCol.setSortable(false);
        checkboxCol.setCellValueFactory(arg0 -> {
            ShopifyOrder order = arg0.getValue();
            CheckBox checkBox = new CheckBox();
            checkBox.selectedProperty()
                    .setValue(pickedOrdersMap.getOrDefault(order.getName(), false));
            checkBox.selectedProperty()
                    .addListener((ov, old_val, new_val) -> pickedOrdersMap.put(order.getName(), new_val));
            return new SimpleObjectProperty<>(checkBox);
        });
        checkboxCol.setEditable(true);
        return checkboxCol;
    }

    @FXML
    protected void onSettingsClicked() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("settings.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("Settings");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.showAndWait();
    }

    private void refreshOrders() {
        try {
            shopify.refreshOrders(ordersDate.getValue());
        } catch (IOException | InterruptedException | URISyntaxException e) {
            System.out.println(e);
            return;
        }
        ObservableList<ShopifyOrder> list = FXCollections.observableList(shopify.getOrders());
        filteredList = new FilteredList<>(list);
        ordersTableView.setItems(filteredList);

        updatePredicate();
    }

    private void updatePredicate() {
        filteredList.setPredicate(shopifyOrder -> {
            boolean show = true;
            for (StringOperatorV2 operator : tagsCol.getFilters()) {
                if (operator.getType().equals(IFilterOperatorV2.Type.EQUALS)) {
                    show &= shopifyOrder.tagsContain(operator.getValue());
                } else if (operator.getType()
                        .equals(IFilterOperatorV2.Type.CONTAINS)) {
                    show &= shopifyOrder.tagsContainAny(operator.getValue()
                            .split(","));
                }
            }
            for (StringOperatorV2 operator : orderNameCol.getFilters()) {
                if (operator.getType().equals(IFilterOperatorV2.Type.EQUALS)) {
                    show &= shopifyOrder.getName()
                            .equals(String.format("#%s", operator.getValue())) | shopifyOrder.getName()
                            .equals(operator.getValue());
                }
            }

            for (StringOperatorV2 operator : phoneNumberCol.getFilters()) {
                if (operator.getType()
                        .equals(IFilterOperatorV2.Type.EQUALS) || operator.getType()
                        .equals(IFilterOperatorV2.Type.CONTAINS)) {
                    show &= shopifyOrder.getPhoneNumber()
                            .equals(String.format("#%s", operator.getValue())) | shopifyOrder.getPhoneNumber()
                            .equals(operator.getValue());
                }
            }

            Boolean fulfillmentDateColFilter = applyFulfillmentDateColFilter(shopifyOrder);
            if (fulfillmentDateColFilter != null)
                show &= fulfillmentDateColFilter;
            return show;
        });
    }

    private Boolean applyFulfillmentDateColFilter(ShopifyOrder shopifyOrder) {
        if (!fulfillmentDateCol.isFiltered())
            return null;

        DateOperatorV2 operator = fulfillmentDateCol.getFilters().get(0);
        if (shopifyOrder.getFulfillment().isEmpty())
            return operator.getType().equals(IFilterOperatorV2.Type.SHOW_EMPTY);
        if (operator.getType()
                .equals(IFilterOperatorV2.Type.EQUALS)) {
            return shopifyOrder.getFulfillment().get(0)
                    .getUpdatedDate().isEqual(operator.getValue());
        } else {
            return false;
        }
    }

    @FXML
    protected void onRefreshOrders() {
        refreshOrders();
    }

    @FXML
    protected void onCopyInventoryForPicked() {
        Map<String, Integer> countMap = new TreeMap<>();
        int unConfirmedOrders = 0;
        int totalPicked = 0;
        for (ShopifyOrder order : ordersTableView.getItems()) {
            if (pickedOrdersMap.getOrDefault(order.getName(), false)) {
                for (ShopifyItem item : order.getItems()) {
                    countMap.put(item.getName(), countMap.getOrDefault(item.getName(), 0) + item.getQuantity());
                }
                if (!order.isConfirmed()) {
                    unConfirmedOrders += 1;
                }
                totalPicked++;
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Total Picked: ").append(totalPicked).append("\n");
        stringBuilder.append("Unconfirmed orders: ").append(unConfirmedOrders)
                .append("\n");

        stringBuilder.append(buildOutput(countMap));

        StringSelection selection = new StringSelection(stringBuilder.toString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText("Info");
        alert.setContentText("Inventory Copied Successfully!");
        alert.initOwner(pane.getScene().getWindow());
        alert.show();
    }


    StringBuilder buildOutput(Map<String, Integer> countMap) {
        StringBuilder stringBuilder = new StringBuilder();
        int elements = countMap.size(), idx = 0;
        String[][] output = new String[elements][4];
        int maxPerfumeNameLength = 12;
        int maxTypeLength = 4;
        int maxBoxLength = 3;

        // Build output array and find max lengths
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            output[idx] = buildStr(entry.getKey());
            maxPerfumeNameLength = Math.max(maxPerfumeNameLength, output[idx][0].length());
            maxTypeLength = Math.max(maxTypeLength, output[idx][1].length());
            maxBoxLength = Math.max(maxBoxLength, output[idx][2].length());
            output[idx][3] = String.valueOf(entry.getValue());
            idx++;
        }

        // Append hyphens line
        int totalLength = maxPerfumeNameLength + maxTypeLength + maxBoxLength + 26; // 26 = Length of static parts and separators
        for (int i = 0; i < totalLength - 10; i++) {
            stringBuilder.append("-");
        }
        stringBuilder.append("\n");

        // Append header
        stringBuilder.append(String.format("| %-" + maxPerfumeNameLength + "s | %-" + maxTypeLength + "s | %-" + maxBoxLength + "s | %-3s |\n", "Perfume Name", "Type", "Box", "Qty"));

        // Append data
        for (String[] item : output) {
            stringBuilder.append(String.format("| %-" + maxPerfumeNameLength + "s | %-" + maxTypeLength + "s | %-" + maxBoxLength + "s | %-3s |\n", item[0], item[1], item[2], item[3]));
            // Append hyphens line after each data line
            for (int i = 0; i < totalLength - 10; i++) {
                stringBuilder.append("-");
            }
            stringBuilder.append("\n");
        }

        return stringBuilder;
    }

    String[] buildStr(String inputStr) {
        Pattern sizePattern = Pattern.compile("-?\\s?\\b\\d+\\s*(ml|ML|Ml)\\b");
        inputStr = inputStr.replaceAll(sizePattern.pattern(), "");

        // Replace "Eau de Parfum" with "EDP" and "Eau de Toilette" with "EDT"
        inputStr = inputStr.replaceAll("\\b(Eau\\sde\\sParfum)\\b", "EDP");
        inputStr = inputStr.replaceAll("\\b(Eau\\sDe\\sToilette)\\b", "EDT");

        Pattern boxPattern = Pattern.compile("\\b(Origin(al)?|Outlet(\\sMaster\\sBox)?|Without\\sbox|Tester|Teaster|Without(\\sa\\sbox)?)\\b", Pattern.CASE_INSENSITIVE);
        Pattern typePattern = Pattern.compile("\\b(EDT|EDP|Parfum|Le\\sParfum|Eau\\sFraiche|Deodorant|Intense)\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = boxPattern.matcher(inputStr);
        String[] output = new String[4];

        if (matcher.find())
            output[2] = matcher.group();
        else
            output[2] = " - ";
        inputStr = inputStr.replaceAll(boxPattern.pattern(), "");
        matcher = typePattern.matcher(inputStr);
        if (matcher.find())
            output[1] = matcher.group();
        else
            output[1] = " - ";

        inputStr = inputStr.replaceAll(typePattern.pattern(), "");

        output[0] = inputStr.split(" - ")[0];
        return output;
    }

    private class TableTagsCell extends TableCell<ShopifyOrder, String> {
        private final HBox hBox;

        public TableTagsCell() {
            super();
            hBox = new HBox(5);
            setGraphic(hBox);
        }

        @Override
        protected void updateItem(String tags, boolean empty) {
            super.updateItem(tags, empty);
            hBox.getChildren().clear();
            if (!empty && tags != null) {
                for (String tag : tags.split(",")) {
                    Label label = new Label(tag);
                    CornerRadii corn = new CornerRadii(10);
                    Background background = new Background(new BackgroundFill(javafx.scene.paint.Color.rgb(80, 50, 50), corn, Insets.EMPTY));
                    label.setBackground(background);
                    hBox.getChildren().add(label);
                }
            }
        }
    }

}