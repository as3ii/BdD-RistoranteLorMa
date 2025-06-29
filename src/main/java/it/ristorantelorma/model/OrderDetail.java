package it.ristorantelorma.model;

import java.util.Objects;

public final class OrderDetail {
    private final int foodId;
    private final int orderId;
    private final int quantity;

    public OrderDetail(final int foodId, final int orderId, final int quantity) {
        this.foodId = foodId;
        this.orderId = orderId;
        this.quantity = quantity;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        } else if (other == null) {
            return false;
        } else if (other instanceof OrderDetail) {
            final OrderDetail d = (OrderDetail) other;
            return d.foodId == this.foodId
                && d.orderId == this.orderId;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.foodId, this.orderId);
    }

    //public String toString() { }

    public static final class DAO {  }
}
