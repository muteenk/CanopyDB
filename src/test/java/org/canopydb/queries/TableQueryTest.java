package org.canopydb.queries;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lesson: when a class builds strings (SQL), assert the important fragments —
 * not every character — unless order is guaranteed.
 */
class TableQueryTest {

    @Test
    void getQuery_includesQuotedTableAndDefaultLimit() {
        TableQuery query = new TableQuery("app", "users");

        String sql = query.getQuery();

        assertTrue(sql.contains("FROM `app`.`users`"));
        assertTrue(sql.contains("LIMIT 300 OFFSET 0"));
        assertTrue(sql.endsWith(";"));
    }

    @Test
    void addFilter_wrapsSnippetAndResetsOffset() {
        TableQuery query = new TableQuery("app", "users");
        query.setOffset(600);

        query.addFilter("f1", "status = 'active'");

        assertEquals(0, query.getOffset());
        String sql = query.getQuery();
        assertTrue(sql.contains("WHERE"));
        assertTrue(sql.contains("( status = 'active' )"));
    }

    @Test
    void removeFilter_dropsClause() {
        TableQuery query = new TableQuery("app", "users");
        query.addFilter("f1", "id > 10");
        query.removeFilter("f1");

        assertFalse(query.getQuery().contains("WHERE"));
        assertFalse(query.getRowCountQuery().contains("WHERE"));
    }

    @Test
    void setOrderColumn_appendsOrderBy() {
        TableQuery query = new TableQuery("app", "users");
        query.setOrderColumn("id", Order.OrderDirection.DESC);

        String sql = query.getQuery();
        assertTrue(sql.contains("ORDER BY id DESC"));
    }

    @Test
    void countQuery_ignoresLimitButKeepsFilters() {
        TableQuery query = new TableQuery("app", "users");
        query.addFilter("f1", "age >= 18");

        String countSql = query.getRowCountQuery();

        assertTrue(countSql.contains("COUNT(*) AS row_count"));
        assertTrue(countSql.contains("( age >= 18 )"));
        assertFalse(countSql.contains("LIMIT"));
        assertFalse(countSql.contains("ORDER BY"));
    }
}
