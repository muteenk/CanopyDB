package org.canopydb.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lesson: "connected" tests — TableSession owns TableQuery + pagination rules.
 * We don't mock TableQuery; we let the real objects collaborate and assert behavior.
 */
class TableSessionTest {

    @Test
    void getTablePath_usesSharedFormatter() {
        TableSession session = new TableSession("users", "app");
        assertEquals("app : users", session.getTablePath());
    }

    @Test
    void hasNext_trueWhenMoreRowsRemain() {
        TableSession session = new TableSession("users", "app");
        session.setTotalRowCount(900); // 3 pages of 300

        assertTrue(session.hasNext());
        assertFalse(session.hasPrevious());
    }

    @Test
    void setNextOffset_advancesByLimit() {
        TableSession session = new TableSession("users", "app");
        session.setTotalRowCount(900);

        assertTrue(session.setNextOffset());
        assertEquals(300, session.getTableQuery().getOffset());
        assertTrue(session.hasPrevious());
    }

    @Test
    void setNextOffset_falseAtLastPage() {
        TableSession session = new TableSession("users", "app");
        session.setTotalRowCount(300);

        assertFalse(session.setNextOffset());
        assertEquals(0, session.getTableQuery().getOffset());
    }

    @Test
    void setPreviousOffset_movesBackAndStopsAtZero() {
        TableSession session = new TableSession("users", "app");
        session.setTotalRowCount(900);
        session.setNextOffset();
        session.setNextOffset(); // offset 600

        assertTrue(session.setPreviousOffset());
        assertEquals(300, session.getTableQuery().getOffset());

        assertTrue(session.setPreviousOffset());
        assertEquals(0, session.getTableQuery().getOffset());

        assertFalse(session.setPreviousOffset());
    }

    @Test
    void setQueryOrder_togglesDirectionOnSameColumn() {
        TableSession session = new TableSession("users", "app");

        session.setQueryOrder("id");
        assertEquals("id", session.getTableQuery().getOrder().getColumn());
        assertEquals(
                org.canopydb.queries.Order.OrderDirection.ASC,
                session.getTableQuery().getOrder().getDirection()
        );

        session.setQueryOrder("id");
        assertEquals(
                org.canopydb.queries.Order.OrderDirection.DESC,
                session.getTableQuery().getOrder().getDirection()
        );
    }

    @Test
    void setQueryOrder_newColumnStartsAscending() {
        TableSession session = new TableSession("users", "app");
        session.setQueryOrder("id");
        session.setQueryOrder("id"); // now DESC

        session.setQueryOrder("name");

        assertEquals("name", session.getTableQuery().getOrder().getColumn());
        assertEquals(
                org.canopydb.queries.Order.OrderDirection.ASC,
                session.getTableQuery().getOrder().getDirection()
        );
    }

    @Test
    void addQueryFilter_appearsInEmittedSqlAndResetsPage() {
        TableSession session = new TableSession("users", "app");
        session.setTotalRowCount(900);
        session.setNextOffset();

        session.addQueryFilter("f1", "active = 1");

        assertEquals(0, session.getTableQuery().getOffset());
        assertTrue(session.emitQuery().contains("( active = 1 )"));
        assertTrue(session.emitCountQuery().contains("( active = 1 )"));
    }

    @Test
    void getPaginationData_reflectsLimitOffsetAndTotal() {
        TableSession session = new TableSession("users", "app");
        session.setTotalRowCount(1250);
        session.setNextOffset();

        TablePagination page = session.getPaginationData();

        assertEquals(300, page.limit());
        assertEquals(300, page.offset());
        assertEquals(1250, page.totalRows());
    }
}
