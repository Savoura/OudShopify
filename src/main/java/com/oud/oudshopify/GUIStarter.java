package com.oud.oudshopify;

import com.oud.oudshopify.backend.Database;

public class GUIStarter {

    public static void main(final String[] args) {
        Application.main(args);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Database.getDatabase().close();
        }));
    }

}