package com.nukkad.androidapp.Controller;

import com.nukkad.androidapp.Common.CommonDefinitions;
import com.nukkad.androidapp.Controller.Cart.CartController;
import com.nukkad.androidapp.Controller.Common.CartUpdatesHandler;
import com.nukkad.androidapp.Controller.ProductList.ProductListManager;
import com.nukkad.androidapp.FazmartApplication;
import com.nukkad.androidapp.Model.ProductData.ProductData;
import com.nukkad.androidapp.Model.SellerData.SellerProductListData;
import com.nukkad.androidapp.Model.SellerAccountData.SellerInfo;
import com.nukkad.androidapp.View.ProductDetailViewCallbacks;

import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Admin on 08-05-2015.
 */
public class ProductDetailDataController {
    // Implement Singleton
    private static ProductDetailDataController instance = null;
    public static ProductDetailDataController GetInstance ()
    {
        if(instance == null) {
            instance = new ProductDetailDataController();
        }
        return instance;
    }

    int mCategoryId;
    ProductListManager.ProductDataForListView mRefToProdInListView;

    ProductListManager mProductListManager;
    CartController mCartController;
    WishListManager mWishListManager;

    CartUpdatesHandler mAPIHandler;

    ProductDetailViewCallbacks mActivityCallback;
    public void RegisterCallback (ProductDetailViewCallbacks cb) {
        mActivityCallback = cb;
    }

    ProductDetailData mCurrentProductData = null;
    public ProductDetailData GetCurrentProductData () {
        return mCurrentProductData;
    }

    SellerInfo mCurrentProductSellerInfo = null;
    public SellerInfo GetCurrentProductSellerInfo () { return mCurrentProductSellerInfo; }

    ProductData[] mSellerProducts = null;
    public ProductData[] GetSellerProducts () { return mSellerProducts; }

    int mSellerProdCount = 0;
    public int GetSellerProductCount () { return mSellerProdCount; }

    public ProductDetailDataController() {
        mCategoryId = 0;
        mRefToProdInListView = null;

        mProductListManager = ProductListManager.GetInstance();
        mCartController = CartController.GetInstance();
        mWishListManager = WishListManager.GetInstance();

        mAPIHandler = new CartUpdatesHandler();
    }

    int mFetchProductDetailDataRetryCnt = 0;
    //public void FetchProductDetailData(int cat_Id, int prod_id, final int opt_idx) {
    public void FetchProductDetailData(int prod_id, final int opt_idx) {
        //final int catId = cat_Id;
        final int prodId = prod_id;
        final int optIdx = opt_idx;
        FazmartApplication.GetAPIService().GetProductDetails(prodId, new Callback<com.nukkad.androidapp.Model.ProductData.ProductDetailData>() {
            @Override
            public void success(com.nukkad.androidapp.Model.ProductData.ProductDetailData productDetailData, Response response) {
                mCurrentProductData = new ProductDetailData(productDetailData); //(catId, productDetailData);
                mCurrentProductData.SetOptionSelection(optIdx);

                mActivityCallback.OnDetailDataFetched();

                mFetchProductDetailDataRetryCnt = 0;
            }

            @Override
            public void failure(RetrofitError error) {
                mFetchProductDetailDataRetryCnt++;
                if (mFetchProductDetailDataRetryCnt < 5)
                    //FetchProductDetailData(catId, prodId, optIdx);
                    FetchProductDetailData(prodId, optIdx);
                else
                    mFetchProductDetailDataRetryCnt = 0;
            }
        });
    }

    int mFetchSellerInfoRetryCnt = 0;
    public void FetchSellerInfo(final int sellerId) {
        FazmartApplication.GetAPIService().GetSellerInfo(sellerId, new Callback<SellerInfo>() {
            @Override
            public void success(SellerInfo sellerInfo, Response response) {
                mCurrentProductSellerInfo = sellerInfo;
                mActivityCallback.OnSellerInfoFetched(sellerInfo);

                mFetchSellerInfoRetryCnt = 0;
            }

            @Override
            public void failure(RetrofitError error) {
                mFetchSellerInfoRetryCnt++;
                if (mFetchSellerInfoRetryCnt < 5)
                    FetchSellerInfo(sellerId);
                else
                    mFetchSellerInfoRetryCnt = 0;
            }
        });
    }

    int retryCntGetSellerProds = 0;
    public void FetchSellerProducts (final int sellerId, final int pageNum, final Map<String, String> queries) {
        // Fetch Product Data from the server
        FazmartApplication.GetAPIService().GetSellerProducts(sellerId, pageNum, queries,
                new Callback<SellerProductListData>() {
                    @Override
                    public void success(SellerProductListData products, Response response) {
                        mSellerProducts = products.GetProductsData();
                        mSellerProdCount = products.GetProductCount();

                        mActivityCallback.OnSellerProductsFetched(products.GetProductsData());

                        retryCntGetSellerProds = 0;
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        retryCntGetSellerProds++;
                        if (retryCntGetSellerProds < CommonDefinitions.RETRY_COUNT)
                            FetchSellerProducts(sellerId, pageNum, queries);
                        else
                            retryCntGetSellerProds = 0;
                    }
                });
    }


    public void AddToCart(int optionIdx) {
        com.nukkad.androidapp.Model.ProductData.ProductDetailData.Product prodDetailData =
                mCurrentProductData.GetProductDetailData();

        if (mCartController.IsProductPresentInCart (prodDetailData.GetProductId(), prodDetailData.GetPrimaryOptionValueId(optionIdx))) {
            mAPIHandler.PutProduct(prodDetailData.GetProductId(), prodDetailData.GetPrimaryOptionValueId(optionIdx), 1);
        } else {
            mAPIHandler.PostProduct(prodDetailData.GetProductId(), prodDetailData.GetPrimaryOptionId(),
                    prodDetailData.GetPrimaryOptionValueId(optionIdx), 1);
        }
    }

    public void RemoveFromCart(int optionIdx) {
        com.nukkad.androidapp.Model.ProductData.ProductDetailData.Product prodDetailData =
                mCurrentProductData.GetProductDetailData();
        mAPIHandler.PutProduct(prodDetailData.GetProductId(), prodDetailData.GetPrimaryOptionValueId(optionIdx), -1);
    };

    // An object of this class is created for each product that is viewed in product detail screen.
    // The objects are then buffered for later use
    public class ProductDetailData {
        com.nukkad.androidapp.Model.ProductData.ProductDetailData.Product mProductDetailData;
        public com.nukkad.androidapp.Model.ProductData.ProductDetailData.Product GetProductDetailData () { return mProductDetailData; }

        SellerInfo mProductSellerData;
        public SellerInfo GetProductSellerData () { return mProductSellerData; }

        //public ProductDetailData (int catId, com.nukkad.androidapp.Model.ProductData.ProductDetailData prodData) {
        public ProductDetailData (com.nukkad.androidapp.Model.ProductData.ProductDetailData prodData) {
            mProductDetailData = prodData.GetProduct();

            int prodId = prodData.GetProduct().GetProductId();
            //if (catId == -1) {
                // Get quantity-in-cart
                int numOptions = mProductDetailData.GetPrimaryOptionsCount();
                mQuantityInCart = new int[numOptions];
                for (int i = 0; i < numOptions; i++) {
                    mQuantityInCart[i] = mCartController.GetItemQuantity(mProductDetailData.GetProductId(),
                            mProductDetailData.GetPrimaryOptionValueId(i));
                }

                mAddedToWishList = mWishListManager.IsProductPresentInWishList(prodId);
            /*}
            else {
                ProductListManager.ProductDataForListView dataFromProductList = mProductListManager.GetProduct(catId, prodId);

                // Read options from pre-fetched product data
                int numOptions = mProductDetailData.GetPrimaryOptionsCount();
                mQuantityInCart = new int[numOptions];
                for (int i = 0; i < numOptions; i++) {
                    mQuantityInCart[i] = dataFromProductList.GetQuantityInCart(i);
                }

                mAddedToWishList = dataFromProductList.IsAddedToWishList();
            }*/
        }

        // This one doesn't come from the server; it is maintained locally
        int[] mQuantityInCart;
        public void UpdateQuantityInCart(int prodOptValId, int newQty) {
            for (int i = 0; i < mQuantityInCart.length; i++) {
                if (mProductDetailData.GetPrimaryOptionValueId(i) == prodOptValId) {
                    mQuantityInCart[i] = newQty;
                    break;
                }
            }
        };
        public int GetQuantityInCart(int option) { return mQuantityInCart[option]; };
        public String GetTitle () { return mProductDetailData.GetTitle(); }
        public boolean IsPresentInCart(int option) { return (mQuantityInCart[option] != 0); };

        // Status-in-wish-list doesn't come from the server. It is maintained locally
        boolean mAddedToWishList;
        public boolean IsAddedToWishList () { return mAddedToWishList; };
        public void AddToWishList () {
            mAddedToWishList = true;
            mWishListManager.AddToWishList(mProductDetailData.GetProductId());
        };

        public void RemoveFromWishList () {
            mAddedToWishList = false;
            mWishListManager.RemoveFromWishList(mProductDetailData.GetProductId());
        };

        // This function is invoked from WishListManager when the list is initially created and
        // synced with the wishlist. Note the absence of a call back to mWishListManager.AddToWishList(mProductId);
        public void InitWishListStatus () {
            mAddedToWishList = true;
        }

        int mOptionSpinnerPosition;
        public void SetOptionSelection (int option) { mOptionSpinnerPosition = option; }
        public int GetOptionSelection () { return mOptionSpinnerPosition; }
    }
}
