package com.oud.oudshopify.backend.network;

import com.oud.oudshopify.backend.KeyValueStore;
import com.oud.oudshopify.backend.network.Shopify;
import org.htmlunit.BrowserVersion;
import org.htmlunit.Cache;
import org.htmlunit.WebClient;
import org.htmlunit.html.*;
import org.htmlunit.javascript.host.html.HTMLTableElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Swing {
    private static Swing swing;
    private static final String SWING_LOGIN = "http://dashboard.swing-eg.com/api/login.aspx";
    private static final String SWING_NEW_SHIPMENT = "http://dashboard.swing-eg.com/api/newshippment.aspx";

    private final WebClient webClient;
    private final Map<String, List<String>> cityToZones;

    public Swing() {
        cityToZones = new HashMap<>();
        webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        webClient.setCache(new Cache());
        webClient.getOptions().setCssEnabled(false);
    }

    public static Swing getInstance() {
        if (swing == null) {
            swing = new Swing();
        }
        return swing;
    }

    public List<String> getZones(String city) throws IOException {
        if (cityToZones.containsKey(city))
            return cityToZones.get(city);
        HtmlPage page = webClient.getPage(SWING_NEW_SHIPMENT);

        if (page.getUrl().toString().equals(SWING_LOGIN)) {
            fillLoginPage(page);
            page = webClient.getPage(SWING_NEW_SHIPMENT);
        }

        List<String> ret = fillCity(page, city);
        cityToZones.put(city, ret);
        return ret;
    }

    private HtmlOption findOptionByValue(List<HtmlOption> options, String value) {
        for (HtmlOption option : options) {
            if (option.getText().equals(value))
                return option;
        }
        throw new RuntimeException("Could not find city " + value);
    }

    private void chooseOptionByValue(HtmlSelect select, String value) {
        select.setSelectedAttribute(findOptionByValue(select.getOptions(), value), true);
    }

    public boolean uploadOrder(String orderRef, String customerName, String phone, String city, String zone,
                               String address, String pieces, String weight, String cost, String notes) throws IOException {
        webClient.getOptions().setDownloadImages(false);
        webClient.getOptions().setPopupBlockerEnabled(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setCssEnabled(false);

        HtmlPage newShipmentPage = webClient.getPage(SWING_NEW_SHIPMENT);
        System.out.println(newShipmentPage.getUrl());

        if (newShipmentPage.getUrl().toString().equals(SWING_LOGIN)) {
            fillLoginPage(newShipmentPage);
            newShipmentPage = webClient.getPage(SWING_NEW_SHIPMENT);
        }
        setTextBox(newShipmentPage, "MainContent_clientref", orderRef);
        setTextBox(newShipmentPage, "MainContent_recname", customerName);
        HtmlSelect select = (HtmlSelect) newShipmentPage.getElementById("MainContent_zone");
        System.out.println("Choosing City");
        chooseOptionByValue(select, city);
        System.out.println("Chosed city");
        webClient.waitForBackgroundJavaScript(10000);
        System.out.println("Javascript finished");
        newShipmentPage = (HtmlPage) webClient.getCurrentWindow()
                .getEnclosedPage();
        HtmlSelect zoneBox = (HtmlSelect) newShipmentPage.getElementById("MainContent_recarea");
        chooseOptionByValue(zoneBox, zone);
        HtmlSelect piecesBox = (HtmlSelect) newShipmentPage.getElementById("MainContent_pieces");
        chooseOptionByValue(piecesBox, pieces);
        HtmlSelect weightsBox = (HtmlSelect) newShipmentPage.getElementById("MainContent_weight");
        chooseOptionByValue(weightsBox, weight);
        setTextArea(newShipmentPage, "MainContent_rec_address", address);
        setTextBox(newShipmentPage, "MainContent_rec_mob", phone);
        setTextBox(newShipmentPage, "MainContent_custody", cost);
        setTextArea(newShipmentPage, "MainContent_info", notes);

        int oldRowCount = getTableRowCount(newShipmentPage, "MainContent_GridView1");

        clickButton(newShipmentPage, "MainContent_btnSave");
        webClient.waitForBackgroundJavaScript(5000);
        HtmlPage finalPage = (HtmlPage) webClient.getCurrentWindow()
                .getEnclosedPage();
        System.out.println(finalPage.asXml());

        int newRowCount = getTableRowCount(finalPage, "MainContent_GridView1");

        return oldRowCount != newRowCount;
    }

    private List<String> fillCity(HtmlPage page, String city) {
        HtmlSelect select = (HtmlSelect) page.getElementById("MainContent_zone");
        select.setSelectedAttribute(findOptionByValue(select.getOptions(), city), true);
        webClient.waitForBackgroundJavaScript(10000);
        HtmlSelect zones = (HtmlSelect) page.getElementById("MainContent_recarea");
        List<String> ret = new ArrayList<>();
        for (HtmlOption htmlOption : zones.getOptions()) {
            if (!htmlOption.getText().equals("اختار")) {
                ret.add(htmlOption.getText());
            }
        }
        return ret;
    }

    private void fillLoginPage(HtmlPage page) throws IOException {
        // Get the form that we are dealing with and within that form,
        // find the submit button and the field that we want to change.
        final HtmlForm form = page.getHtmlElementById("Form1");

        final HtmlSubmitInput button = form.getInputByName("btnLogIn");
        form.getInputByName("UserName").type(KeyValueStore.getInstance()
                .getSwingUsername());
        form.getInputByName("Password").type(KeyValueStore.getInstance()
                .getSwingPassword());


        // Now submit the form by clicking the button and get back the second page.
        button.click();
    }

    private void clickButton(HtmlPage page, String button) throws IOException {
        HtmlElement element = page.getHtmlElementById(button);
        element.click();
    }

    private void setTextBox(HtmlPage page, String id, String value) throws IOException {
        HtmlTextInput element = (HtmlTextInput) page.getElementById(id);
        element.setText(value);
    }

    private void setTextArea(HtmlPage page, String id, String value) throws IOException {
        HtmlTextArea element = (HtmlTextArea) page.getElementById(id);
        element.setText(value);
    }

    private int getTableRowCount(HtmlPage page, String id) throws IOException {
        try {
            HtmlTable element = page.getHtmlElementById(id);
            return element.getRowCount();
        }catch (Exception e){
            return 0;
        }
    }
}