package org.canopydb.queries;

public class Order {
    private String col = "";
    private OrderDirection direction = OrderDirection.ASC;

    public enum OrderDirection {
        ASC,
        DESC
    }

    public void setOrder(String col, OrderDirection direction) {
        this.col = col;
        this.direction = direction;
    }

    public String getColumn() {return this.col;}

    public OrderDirection getDirection() {return this.direction;}
}
