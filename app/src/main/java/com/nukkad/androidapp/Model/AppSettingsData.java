package com.nukkad.androidapp.Model;

/**
 * Created by vinayhuddar on 23/08/15.
 */
public class AppSettingsData {
    int crc;
    AppSettings app_settings;

    public class AppSettings {
        String[] order_states;
        String[] product_states;
        String[] seller_order_list_sort_fields;
        String[] seller_product_list_sort_fields;

        public AppSettings (String[] _order_states, String[] _seller_order_list_sort_fields,
                            String[] _product_states, String[] _seller_product_list_sort_fields) {
            order_states = _order_states;
            seller_order_list_sort_fields = _seller_order_list_sort_fields;
            product_states = _product_states;
            seller_product_list_sort_fields = _seller_product_list_sort_fields;
        }
    }

    public int GetCRC () { return crc; }
    public AppSettings GetAppSettings () { return app_settings; }

    public void SetCRC (int _crc) { crc = _crc; }
    public void SetAppSettings (String[] orderStates, String[] sellerOrdersListSortFields,
                                String[] productStates, String[] sellerProductsListSortFields) {
        app_settings = new AppSettings(orderStates, sellerOrdersListSortFields, productStates, sellerProductsListSortFields);
    }

    public String[] GetOrderStates () { return app_settings.order_states; }
    public String[] GetSellerOrdersListSortFields () { return app_settings.seller_order_list_sort_fields; }
    public String[] GetProductStates () { return app_settings.product_states; }
    public String[] GetSellerProductsListSortFields () { return app_settings.seller_product_list_sort_fields; }
}
