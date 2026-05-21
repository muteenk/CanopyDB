package org.canopydb.queries;

public class Order {
    private String col = "";
    private int direction = 0;

    public void setOrder(String col, int direction) {
        this.col = col;
        this.direction = direction;
    }

    public String getColumn() {return this.col;}

    public int getDirection() {return this.direction;}
}
