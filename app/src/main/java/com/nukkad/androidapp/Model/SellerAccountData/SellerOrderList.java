package com.nukkad.androidapp.Model.SellerAccountData;

/**
 * Created by vinayhuddar on 18/08/15.
 */
public class SellerOrderList {
    int num_orders;
    String[] order_states;
    Order[] orders;

    public int GetNumOrders () { return num_orders; }
    public Order[] GetOrders () { return orders; }
    public String[] GetOrderStates () { return order_states; }

    public class Order {
        int id;
        String cust_name;
        String order_status;
        String date;
        int total;

        public Order (int _id, String _cust_name, String _order_status, String _date, int _total) {
            id = _id;
            cust_name = _cust_name;
            order_status = _order_status;
            date = _date;
            total = _total;
        }
    }
}
