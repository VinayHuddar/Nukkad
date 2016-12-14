package com.nukkad.androidapp.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.nukkad.androidapp.Common.CommonDefinitions;
import com.nukkad.androidapp.Controller.ProductList.ProductListManager;
import com.nukkad.androidapp.Controller.ProductList.ProductListScrollListenerCallback;
import com.nukkad.androidapp.Controller.ProductList.ProductListViewHelper;
import com.nukkad.androidapp.FazmartApplication;
import com.nukkad.androidapp.Controller.CategoryDataController;
import com.nukkad.androidapp.Model.ProductData.ProductListData;
import com.nukkad.androidapp.R;
import com.nukkad.androidapp.View.Common.BaseActivity;
import com.nukkad.androidapp.View.Common.BreadCrumbAdapter;
import com.nukkad.androidapp.View.Common.BreadcrumbAdapterCallbacks;
import com.nukkad.androidapp.View.Common.FilterDrawerCallbacks;
import com.nukkad.androidapp.View.Common.ProductFilterDrawerFragment;
import com.nukkad.androidapp.View.Common.NavDrawerItemList;
import com.nukkad.androidapp.View.Common.NavigationDrawerCallbacks;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Vinay on 13-06-2015.
 */
public class ProductListActivity extends BaseActivity implements BreadcrumbAdapterCallbacks, FilterDrawerCallbacks,
        NavigationDrawerCallbacks, ProductListScrollListenerCallback {

    CategoryDataController mCategoryDataController;
    ProductListManager mProductListManager;
    ProductListViewHelper mProductListViewHelper;
    int mCategoryId;

    // The following flag is used in onResume() to decide whether or not to sync with the cart.
    // List view creation includes syncing with cart, hence, syncing in resume is unnecessary.
    boolean mComingFrom_onCreate = true;

    ProductFilterDrawerFragment mProductFilterDrawerFragment = null;
    int[] mLineage = null;

    Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list_view);
        super.onCreateDrawer();

        mActivity = this;

        mProductFilterDrawerFragment = (ProductFilterDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_filter_drawer);

        RelativeLayout contentView = (RelativeLayout) findViewById(R.id.ProductList);
        contentView.setVisibility(View.INVISIBLE);

        mLineage = getIntent().getIntArrayExtra(CategoryActivity.LINEAGE);
        mCategoryId = mLineage[mLineage.length - 1];

        mProductListViewHelper = new ProductListViewHelper((ListView) findViewById(R.id.ListViewOfProducts), this, this, mCategoryId);
        mProductListViewHelper.ResetDetailViewInitiated();

        mProductListManager = ProductListManager.GetInstance();
        mCategoryDataController = CategoryDataController.GetInstance();

        getSupportActionBar().setTitle(mCategoryDataController.GetCategoryName(mLineage[mLineage.length - 1])); //mCategoryController.GetCategoryName(mLineage[mLineage.length - 1]));

        // Show breadcrumb
        final int hrchyLevel = mLineage.length;
        RecyclerView breadcrumdRV = (RecyclerView)findViewById(R.id.breadcrumb);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        breadcrumdRV.setLayoutManager(layoutManager);

        final List<String> catNameList = new ArrayList<String>();
        final String rootCatName = "Main Sections";
        catNameList.add(rootCatName);

        String catName;
        for (int i = 1; i < hrchyLevel; i++) {
            catName = String.format(" > %s", mCategoryDataController.GetCategoryName(mLineage[i])); //mCategoryController.GetCategoryName(mLineage[i]));
            catNameList.add(catName);
        }
        BreadCrumbAdapter adapter = new BreadCrumbAdapter(catNameList, this, false);
        adapter.setBreadcrumbAdapterCallbacks(this);
        breadcrumdRV.setAdapter(adapter);

        layoutManager.scrollToPositionWithOffset(hrchyLevel - 1, 20);

        // Setup Filter
        ImageView productFilter = (ImageView)findViewById(R.id.product_filter);
        productFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProductFilterDrawerFragment.openDrawer();
            }
        });

        findViewById(R.id.loading_message).setVisibility(View.VISIBLE);

        //if (mCategoryController.WasCategoryViewed(mCategoryId) == false) {
        if (mCategoryDataController.WasCategoryViewed(mCategoryId) == false) {
            GetProducts(1);

            /*ProductListScrollListener scrollListener = new ProductListScrollListener(mProductListViewHelper, this);

            ListView lv = (ListView) findViewById(R.id.ListViewOfProducts);
            lv.setOnScrollListener(scrollListener);*/
        } else {
            mProductListViewHelper.PopulateViews(mCategoryId, false);
            contentView.setVisibility(View.VISIBLE);
        }

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.AddToCartFAB);
        fab.setSize(FloatingActionButton.SIZE_MINI);
        findViewById(R.id.AddToCartFAB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, CartActivity.class);
                startActivity(intent);
            }
        });
    }

    int retryCntGetProds = 0;
    public void GetProducts (final int pageNum) {
        // Fetch Product Data from the server
        FazmartApplication.GetAPIService().GetCategoryProducts(mCategoryId, 16, pageNum,
                new Callback<ProductListData>() {
                    @Override
                    public void success(ProductListData productListData, Response response) {
                        findViewById(R.id.loading_message).setVisibility(View.GONE);

                        RelativeLayout contentView = (RelativeLayout) findViewById(R.id.ProductList);
                        contentView.setVisibility(View.VISIBLE);

                        mProductListViewHelper.DisplayList(pageNum, productListData.GetProductsData(), mCategoryDataController.GetProductCount(mCategoryId));
                        SetupAttributeSpinner();


                        retryCntGetProds = 0;
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        retryCntGetProds++;
                        if (retryCntGetProds < CommonDefinitions.RETRY_COUNT)
                            GetProducts(pageNum);
                        else
                            retryCntGetProds = 0;
                    }
                }
        );
    }

    void SetupAttributeSpinner () {
        final String[] attributeList = mProductListViewHelper.GetAttributeList(mCategoryId);

        ProductFilterDrawerFragment productFilterDrawerFragment = (ProductFilterDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_filter_drawer);
        productFilterDrawerFragment.setup(R.id.fragment_filter_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), attributeList);
    }

    @Override
    public void onFilterDrawerItemSelected(int position) {
        mProductListViewHelper.onFilterDrawerItemSelected(position, mCategoryId);
    }

    @Override
    public void onBreadcrumbItemSelected(int position) {
        if (position < mLineage.length-1) {
            Intent intent = new Intent();
            intent.putExtra(CategoryActivity.HIERARCHY_BACK_TRACK_CNT, (mLineage.length - 1) - position);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    public void OnScrolledToEnd (int nextPageNum) {
        GetProducts(nextPageNum);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mComingFrom_onCreate)
            mComingFrom_onCreate = false;
        else {
            if (mProductListManager.SyncWithCartAndWishList(mCategoryId))
                mProductListViewHelper.GetProductListAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mProductListViewHelper.ResetDetailViewInitiated();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
    }

    @Override
    public int getDefaultItemSelectId() {
        return NavDrawerItemList.BROWSE_ITEM_ID;
    }
}
