package org.canopydb.models;

/**
 * One table cell. Keeps SQL NULL distinct from empty / literal text for future edits.
 */
public final class CellValue {

    private final boolean isNull;
    private final String text;

    private CellValue(boolean isNull, String text) {
        this.isNull = isNull;
        this.text = text;
    }

    public static CellValue ofNull() {
        return new CellValue(true, "");
    }

    public static CellValue of(String text) {
        return new CellValue(false, text == null ? "" : text);
    }

    public boolean isNull() {
        return isNull;
    }

    public String getText() {
        return text;
    }

    /** Text shown in the grid (nulls rendered as NULL). */
    public String toDisplayString() {
        return isNull ? "NULL" : text;
    }

    @Override
    public String toString() {
        return toDisplayString();
    }
}
