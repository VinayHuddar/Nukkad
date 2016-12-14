package com.nukkad.androidapp.Model.SellerData;

import com.nukkad.androidapp.Model.ProductData.ProductData;

/**
 * Created by Vinay on 08-06-2015.
 */
public class SellerProductListData {
    int total_products;
    ProductData[] products;

    public ProductData[] GetProductsData () {
        return products;
    }
    public int GetProductCount () {
        return total_products;
    }
}
