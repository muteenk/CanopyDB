package org.canopydb.utils;

public class TableUtilities {
    public static String tablePath(String databaseName, String tableName){
        return databaseName+" : "+tableName;
    }
}
