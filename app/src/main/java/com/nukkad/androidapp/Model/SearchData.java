package com.nukkad.androidapp.Model;

import com.nukkad.androidapp.Model.ProductData.ProductData;

/**
 * Created by Vinay on 08-06-2015.
 */
public class SearchData {
    int count;
    ProductData[] products;

    public int GetCount () { return count; }
    public ProductData[] GetProductsData () {
        return products;
    }
}
