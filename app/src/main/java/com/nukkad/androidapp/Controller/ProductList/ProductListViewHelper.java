package com.nukkad.androidapp.Controller.ProductList;

import android.app.Activity;
import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import com.nukkad.androidapp.Common.APIService;
import com.nukkad.androidapp.Common.CommonDefinitions;
import com.nukkad.androidapp.FazmartApplication;
import com.nukkad.androidapp.Controller.CategoryDataController;
import com.nukkad.androidapp.Model.ProductData.ProductData;
import com.nukkad.androidapp.View.Common.ProductFilterDrawerFragment;
import com.nukkad.androidapp.R;
import com.nukkad.androidapp.Model.SearchData;
import com.nukkad.androidapp.View.Search.SearchActivity;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Vinay on 19-05-2015.
 */
public class ProductListViewHelper {
    Context mContext;

    Activity mParentActivity;
    ProductListManager mProductListManager;
    CategoryDataController mCategorData;
    ArrayList<ProductListManager.ProductDataForListView> mProductList;
    ProductListAdapter mProductListAdapter;

    AttributeMap mAttributeMap;
    int mCategoryId;
    View mFooterView = null;
    boolean mDetailViewInitiated = false;

    ListView mListView;
    ProductListScrollListener mProductListScrollListener = null;

    public ProductListViewHelper (ListView listView, ProductListScrollListenerCallback callback, Activity parentActivity, int catId) {
        mListView = listView;
        mParentActivity = parentActivity;
        mContext = parentActivity;
        mCategoryId = catId;

        mAttributeMap = null;

        mProductListManager = ProductListManager.GetInstance();
        mCategorData = CategoryDataController.GetInstance();

        mFooterView = ((LayoutInflater)mParentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_list_view_footer, null, false);

        mProductListScrollListener = new ProductListScrollListener(this, callback);
    }

    public void ResetDetailViewInitiated() {
        mDetailViewInitiated = false;
    }

    public ProductListAdapter GetProductListAdapter () {
        return mProductListAdapter;
    }

    boolean mAttribSelectionApplied = false;
    public boolean IsAttribSelectionApplied () { return mAttribSelectionApplied; }

    int mTotalProductCount;
    public void DisplayList (int pageNum, ProductData[] productListData, int totalProductCount) {
        boolean appendData = pageNum > 1;
        if (!appendData) {
            mTotalProductCount = totalProductCount;
            mProductListManager.SetTotalProductCount(totalProductCount); //mCategorData.GetProductCount(mCategoryId));
            mProductListScrollListener.SetTotalProductCount(totalProductCount);//mCategorData.GetProductCount(mCategoryId));
        }

        mProductListManager.ProcessRetrofitData(productListData, mCategoryId, appendData);
        PopulateViews(mCategoryId, appendData);
    }

    public void PopulateViews (final int parentCategoryId, boolean append)
    {
        mProductListManager.SyncWithCartAndWishList(parentCategoryId);
        mProductList = mProductListManager.GetProductsListForCategory(parentCategoryId);

        int currItemCount = mProductListManager.GetProductsListForCategory(parentCategoryId).size();

        if (!append) {
            mListView.addFooterView(mFooterView);
            mListView.getId();

            final ProductListAdapter productListAdapter =
                    new ProductListAdapter(mParentActivity, mListView.getId(), mProductList, mCategoryId);

            mProductListAdapter = productListAdapter;   // Save a reference for use in refreshing the view when required
            mListView.setAdapter(mProductListAdapter);
        } else {
            mProductListAdapter.UpdateProductCount(currItemCount);
            mProductListAdapter.notifyDataSetChanged();
        }
        if (currItemCount == mTotalProductCount)
            mListView.removeFooterView(mFooterView);
    }

    public void onFilterDrawerItemSelected (int position, int parentCategoryId) {
        int prodCnt = 0;
        int[] prodIds = null;

        ListView mProductListLV = (ListView) mParentActivity.findViewById(R.id.ListViewOfProducts);
        if (position == 0) {
            mProductListAdapter.ShowAllItems();

            int totalProdCnt = (parentCategoryId == CommonDefinitions.SEARCH_CATEGORY_ID) ? mTotalProductCount  //mProductListManager.GetTotalProductCount()
                    : mCategorData.GetProductCount(parentCategoryId);
            if (mProductListLV.getFooterViewsCount() > 0) {
                if (mProductListAdapter.getCount() == totalProdCnt)
                    mProductListLV.removeFooterView(mFooterView);
            } else if (mProductListAdapter.getCount() < totalProdCnt) {
                mProductListLV.addFooterView(mFooterView);
            }

            mAttribSelectionApplied = false;
        } else {
            prodCnt = mAttributeMap.GetProductsOfAttribute(position - 1).length;
            prodIds = mAttributeMap.GetProductsOfAttribute(position - 1).clone();
            mProductListAdapter.FilterProducts(prodCnt, prodIds);

            if (mProductListLV.getFooterViewsCount() > 0) {
                mProductListLV.removeFooterView(mFooterView);
            }
            mAttribSelectionApplied = true;
        }

        mProductListAdapter.notifyDataSetChanged();
    }

    public String[] GetAttributeList (int parentCategoryId) {
        mAttributeMap = mProductListManager.GetAttributeMapForCategory(parentCategoryId);
        String[] attribNames = mAttributeMap.GetAttributeList();
        int numAttribs = mAttributeMap.GetAttributeList().length;
        final String[] attributeList = new String[numAttribs + 1];

        int currItemCount = mProductListManager.GetProductsListForCategory(parentCategoryId).size();

        if (parentCategoryId == CommonDefinitions.SEARCH_CATEGORY_ID) {
            attributeList[0] = (mTotalProductCount == 0) ? "Search returned 0 results" :
                    String.format("All %d of %d hits", currItemCount, mTotalProductCount);

            ((SearchActivity)mParentActivity).UpdateSearchMessage(String.format("Showing %d of %d hits",
                    currItemCount, mTotalProductCount));
        }
        else
            attributeList[0] = String.format("All Items - (%d of %d)", currItemCount, mTotalProductCount);

        for (int i = 1; i <= numAttribs; i++)
            attributeList[i] = attribNames[i - 1];

        return (attributeList);
    }
}
