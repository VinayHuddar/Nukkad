package com.nukkad.androidapp.Model.SellerAccountData;

/**
 * Created by vinayhuddar on 20/08/15.
 */
public class SellerOrderDetails {
    OrderDetails order;
    OrderHistory[] history;

    public OrderDetails GetOrderDetails () { return order; }
    public OrderHistory[] GetOrderHistory () { return history; }

    public class OrderDetails {
        int invoice_no;
        int id;
        String status;
        String date;
        String pay_method;
        String del_method;
        String del_addr;

        Product[] products;
        double total;

        public int GetInvoiceNum () { return invoice_no; }
        public int GetOrderId () { return id; }
        public String GetStatus () { return status; }
        public String GetDate () { return date; }
        public String GetPaymentMethod () { return pay_method; }
        public String GetDeliveryMethod () { return del_method; }
        public String GetDeliveryAddress () { return del_addr; }
        public Product[] GetProducts () { return products; }
        public Product GetProduct (int pos) { return products[pos]; }
        public double GetTotal () { return total; }
    }

    public class Product {
        int id;
        String name;
        int qty;
        String trans_status;
        String price;
        String total;
        int track_no;

        public String GetName () { return name; }
        public int GetQuantity () { return qty; }
        public String GetTransactionStatus () { return trans_status; }
        public String GetPrice () { return price; }
        public String GetTotal () { return total; }
        public int GetTrackingNumber () { return track_no; }

        public Product (
                int _id,
                String _name,
                int _qty,
                String _trans_status,
                String _price,
                String _total,
                int _track_no) {
            id = _id;
            name = _name;
            qty = _qty;
            trans_status = _trans_status;
            price = _price;
            total = _total;
            track_no = _track_no;
        }
    }

    public class OrderHistory {
        String date;
        String status;
        String comment;

        public String GetDate () { return date; }
        public String GetStatus () { return status; }
        public String GetComment () { return comment; }

        public OrderHistory (String _date,
                String _status,
                String _comment) {
            date = _date;
            status = _status;
            comment = _comment;
        }
    }


    public SellerOrderDetails (int invoice_no,
            int id,
            String status,
            String date,
            String pay_method,
            String del_method,
            String del_addr,
            int total) {
        order = new OrderDetails();

        order.id = id;
        order.status = status;
        order.date = date;
        order.pay_method = pay_method;
        order.del_method = del_method;
        order.del_addr = del_addr;
        order.total = total;
    }

    public void AddProducts (int numProds) {
        order.products = new Product[numProds];
    }
    public void AddProduct (int prodNum,
            int prodId,
            String name,
            int qty,
            String trans_status,
            String price,
            String total,
            int track_no) {
        order.products[prodNum] = new Product(prodId, name, qty, trans_status, price, total, track_no);
    }

    public void InitHistory (int numHist) {
        history = new OrderHistory[numHist];
    }
    public void AddHistory (int histNum, String date,
                            String status,
                            String comment) {
        history[histNum] = new OrderHistory(date, status, comment);
    }
}
