package com.nukkad.androidapp.Controller.ProductList;

import android.widget.AbsListView;

/**
 * Created by Vinay on 10-06-2015.
 */
public class ProductListScrollListener implements AbsListView.OnScrollListener {
    final int SCROLL_LISTENER_THRESHOLD = 8;

    // how many entries earlier to start loading next page
    private int currentPage = 1;
    private int previousTotal = 0;
    private boolean loading = true;
    private int mTotalProductCount = 0;

    private String mSearchQuery = null;
    private int mSellerId = 0;
    ProductListViewHelper mProductListViewHelper = null;

    ProductListScrollListenerCallback mCallback;

    public ProductListScrollListener(ProductListViewHelper listViewHelper, ProductListScrollListenerCallback callback) {
        mProductListViewHelper = listViewHelper;

        mCallback = callback;
    }

    public ProductListScrollListener(ProductListViewHelper listViewHelper, int sellerId) {
        mProductListViewHelper = listViewHelper;
        mSellerId = sellerId;

    }

    public ProductListScrollListener(ProductListViewHelper listViewHelper, String searchQuery) {
        mProductListViewHelper = listViewHelper;
        mSearchQuery = searchQuery;

    }

    // User by search activity
    public void NewSearch (String query) {
        mSearchQuery = query;

        // Initialize State
        currentPage = 1;
        previousTotal = 0;
        loading = true;
        mTotalProductCount = 0;
    }

    public void SetTotalProductCount (int cnt) {
        mTotalProductCount = cnt;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (mProductListViewHelper.IsAttribSelectionApplied() == false) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    currentPage++;
                }
            }
            //if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + SCROLL_LISTENER_THRESHOLD)) {
            if (!loading && (firstVisibleItem  >= (totalItemCount - SCROLL_LISTENER_THRESHOLD)) && (totalItemCount < mTotalProductCount)) {
                loading = true;

                mCallback.OnScrolledToEnd(currentPage);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {}
}
