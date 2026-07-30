package org.canopydb.repository;

import org.canopydb.models.CellValue;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Reads a ResultSet cell into a faithful {@link CellValue} for display / future edit.
 */
public final class ResultSetValueSerializer {

    private ResultSetValueSerializer() {
    }

    public static CellValue read(ResultSet rs, int columnIndex, int jdbcType) throws SQLException {
        return switch (jdbcType) {
            case Types.DATE, Types.TIME, Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE,
                 Types.TIME_WITH_TIMEZONE -> readTemporalAsString(rs, columnIndex);
            case Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT ->
                    readIntegral(rs, columnIndex, jdbcType);
            case Types.DECIMAL, Types.NUMERIC -> readDecimal(rs, columnIndex);
            case Types.REAL, Types.FLOAT, Types.DOUBLE -> readFloating(rs, columnIndex);
            case Types.BOOLEAN, Types.BIT -> readBooleanish(rs, columnIndex);
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB ->
                    readBinary(rs, columnIndex);
            default -> readAsString(rs, columnIndex);
        };
    }

    private static CellValue readTemporalAsString(ResultSet rs, int columnIndex) throws SQLException {
        // getString preserves MySQL textual form (including zero dates) better than getObject().
        String value = rs.getString(columnIndex);
        if (rs.wasNull() || value == null) {
            return CellValue.ofNull();
        }
        return CellValue.of(value);
    }

    private static CellValue readIntegral(ResultSet rs, int columnIndex, int jdbcType)
            throws SQLException {
        if (jdbcType == Types.BIGINT) {
            long value = rs.getLong(columnIndex);
            if (rs.wasNull()) return CellValue.ofNull();
            return CellValue.of(Long.toString(value));
        }
        int value = rs.getInt(columnIndex);
        if (rs.wasNull()) return CellValue.ofNull();
        return CellValue.of(Integer.toString(value));
    }

    private static CellValue readDecimal(ResultSet rs, int columnIndex) throws SQLException {
        BigDecimal value = rs.getBigDecimal(columnIndex);
        if (rs.wasNull() || value == null) return CellValue.ofNull();
        return CellValue.of(value.toPlainString());
    }

    private static CellValue readFloating(ResultSet rs, int columnIndex) throws SQLException {
        BigDecimal value = rs.getBigDecimal(columnIndex);
        if (rs.wasNull() || value == null) return CellValue.ofNull();
        return CellValue.of(value.toPlainString());
    }

    private static CellValue readBooleanish(ResultSet rs, int columnIndex) throws SQLException {
        boolean value = rs.getBoolean(columnIndex);
        if (rs.wasNull()) return CellValue.ofNull();
        return CellValue.of(value ? "1" : "0");
    }

    private static CellValue readBinary(ResultSet rs, int columnIndex) throws SQLException {
        byte[] value = rs.getBytes(columnIndex);
        if (rs.wasNull() || value == null) return CellValue.ofNull();
        return CellValue.of("<BINARY " + value.length + " bytes>");
    }

    private static CellValue readAsString(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        if (rs.wasNull() || value == null) return CellValue.ofNull();
        return CellValue.of(value);
    }
}
