package com.oud.oudshopify.backend;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import java.io.File;

public class Database {
    private static final String DB_FILE_NAME = "oud.db";
    private static final DB databaseImpl;

    private Database() {
    }

    static {
        File dbFile = new File(DB_FILE_NAME);
        if (!dbFile.exists()) {
            createNewDatabaseFile();
        }
        databaseImpl = DBMaker.fileDB(DB_FILE_NAME).fileMmapEnable().closeOnJvmShutdown().make();
    }

    private static void createNewDatabaseFile() {
        // Create a new database file
        DB newDB = DBMaker.fileDB(DB_FILE_NAME).fileMmapEnable().closeOnJvmShutdown().make();
        newDB.close();
    }

    private static Database database;

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

    public void close() {
        databaseImpl.close();
    }

}
