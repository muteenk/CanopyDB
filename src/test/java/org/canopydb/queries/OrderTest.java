package org.canopydb.queries;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderTest {

    @Test
    void setOrder_storesColumnAndDirection() {
        Order order = new Order();

        order.setOrder("created_at", Order.OrderDirection.DESC);

        assertEquals("created_at", order.getColumn());
        assertEquals(Order.OrderDirection.DESC, order.getDirection());
    }

    @Test
    void defaultOrder_isEmptyColumnAscending() {
        Order order = new Order();

        assertEquals("", order.getColumn());
        assertEquals(Order.OrderDirection.ASC, order.getDirection());
    }
}
