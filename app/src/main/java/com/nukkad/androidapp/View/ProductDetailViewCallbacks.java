package com.nukkad.androidapp.View;

import com.nukkad.androidapp.Model.ProductData.ProductData;
import com.nukkad.androidapp.Model.SellerAccountData.SellerInfo;

/**
 * Created by vinayhuddar on 26/08/15.
 */
public interface ProductDetailViewCallbacks {
    public void OnDetailDataFetched ();
    public void OnSellerInfoFetched (SellerInfo sellerInfo);
    public void OnSellerProductsFetched (ProductData[] productData);
}
