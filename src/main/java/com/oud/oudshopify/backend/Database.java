package com.oud.oudshopify.backend;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

public class Database {
    private static final DB databaseImpl = DBMaker.fileDB("oud.db").fileMmapEnable().closeOnJvmShutdown().make();
    private static Database database;

    private Database() {
    }

    public static synchronized Database getDatabase() {
        if (database == null)
            database = new Database();
        return database;
    }

    public String getStringValue(String documentKey, String key) {
        HTreeMap<String, String> document = (HTreeMap<String, String>) databaseImpl.hashMap(documentKey).createOrOpen();
        return document.get(key);
    }

    public void updateStringValue(String documentKey, String key, String value) {
        HTreeMap<String, String> document = (HTreeMap<String, String>) databaseImpl.hashMap(documentKey).createOrOpen();
        document.put(key, value);
    }
}
