package org.canopydb.models;

public final class ColumnMeta {

    private final String name;
    private final int jdbcType;
    private final String typeName;

    public ColumnMeta(String name, int jdbcType, String typeName) {
        this.name = name;
        this.jdbcType = jdbcType;
        this.typeName = typeName;
    }

    public String getName() {
        return name;
    }

    public int getJdbcType() {
        return jdbcType;
    }

    public String getTypeName() {
        return typeName;
    }
}
