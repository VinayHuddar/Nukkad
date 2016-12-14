package com.nukkad.androidapp.Model.SellerAccountData;

/**
 * Created by vinayhuddar on 23/08/15.
 */
public class SellerOrderSummary {
    int total_orders;
    double total_sales;
    String[] order_states;
    PendingOrder[] pend_orders;

    public int GetTotalOrders () {
        return total_orders;
    }

    public double GetTotalSales () {
        return total_sales;
    }

    public String[] GetOrderStates () {
        return order_states;
    }

    public PendingOrder[] GetPendingOrders () {
        return pend_orders;
    }

    public class PendingOrder {
        int id;
        String cust_name;
        String date;
        int total;

        public PendingOrder (int _id, String _cust_name, String _date, int _total) {
            id = _id;
            cust_name = _cust_name;
            date = _date;
            total = _total;
        }
    }
}
