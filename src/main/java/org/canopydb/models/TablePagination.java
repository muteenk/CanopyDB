package org.canopydb.models;

public record TablePagination(int limit, int offset, int totalRows) {}
