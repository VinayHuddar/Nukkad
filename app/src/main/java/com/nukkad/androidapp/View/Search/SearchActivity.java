package com.nukkad.androidapp.View.Search;

import android.app.Activity;
import android.app.SearchManager;
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
import com.nukkad.androidapp.Controller.ProductList.ProductListScrollListener;
import com.nukkad.androidapp.Controller.ProductList.ProductListScrollListenerCallback;
import com.nukkad.androidapp.Controller.ProductList.ProductListViewHelper;
import com.nukkad.androidapp.FazmartApplication;
import com.nukkad.androidapp.Model.SearchData;
import com.nukkad.androidapp.R;
import com.nukkad.androidapp.View.CartActivity;
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

public class SearchActivity extends BaseActivity implements BreadcrumbAdapterCallbacks, FilterDrawerCallbacks,
        NavigationDrawerCallbacks, ProductListScrollListenerCallback {
    // Used to store the last screen title. For use in {@link #restoreActionBar()}.
    private CharSequence mTitle;

    ProductListManager mProductListManager;
    ProductListViewHelper mProductListViewHelper;

    // The following flag is used in onResume() to decide whether or not to sync with the cart.
    // List view creation includes syncing with cart, hence, syncing in resume is unnecessary.
    boolean mComingFromOnCreate = false;

    ProductListScrollListener scrollListener = null;
    boolean mNewSearch = true;

    ProductFilterDrawerFragment mProductFilterDrawerFragment = null;
    List<String> mSearchMessage;
    BreadCrumbAdapter mSearchMessageAdapter;

    Activity mActivity;
    String mSearchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        super.onCreateDrawer();

        mActivity = this;

        getSupportActionBar().setTitle("Search");

        mProductFilterDrawerFragment = (ProductFilterDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_filter_drawer);

        // Setup Filter
        ImageView productFilter = (ImageView)findViewById(R.id.product_filter);
        productFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProductFilterDrawerFragment.openDrawer();
            }
        });

        RecyclerView breadcrumdRV = (RecyclerView)findViewById(R.id.breadcrumb);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        breadcrumdRV.setLayoutManager(layoutManager);

        mSearchMessage = new ArrayList<String>();
        mSearchMessage.add("Search in progress...");
        mSearchMessageAdapter = new BreadCrumbAdapter(mSearchMessage, this, true);
        mSearchMessageAdapter.setBreadcrumbAdapterCallbacks(this);
        breadcrumdRV.setAdapter(mSearchMessageAdapter);

        // Setup the ScrollListener
        mProductListViewHelper = new ProductListViewHelper((ListView) findViewById(R.id.ListViewOfProducts),
                this, this, CommonDefinitions.SEARCH_CATEGORY_ID);
        mProductListViewHelper.ResetDetailViewInitiated();

        mAPIService = ((FazmartApplication)getApplication()).GetAPIService();

        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) { // Defensive programming - in case this activity receives other broadcast actions in the future
            mSearchQuery = getIntent().getStringExtra(SearchManager.QUERY);

            scrollListener = new ProductListScrollListener(mProductListViewHelper, mSearchQuery);

            ListView lv = (ListView) findViewById(R.id.ListViewOfProducts);
            lv.setOnScrollListener(scrollListener);

            GetSearchResults(mSearchQuery, 1, "ASC");
        }

        mProductListManager = ProductListManager.GetInstance();

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.AddToCartFAB);
        fab.setSize(FloatingActionButton.SIZE_MINI);
        findViewById(R.id.AddToCartFAB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, CartActivity.class);
                startActivity(intent);
            }
        });

        mComingFromOnCreate = true;
    }

    int retryCntGetSearchProds = 0;
    public void GetSearchResults (final String query, final int pageNum, final String order) {
        // Fetch Product Data from the server
        FazmartApplication.GetAPIService().GetProductsSearched(query, pageNum, order,
                new Callback<SearchData>() {
                    @Override
                    public void success(SearchData searchData, Response response) {
                        findViewById(R.id.loading_message).setVisibility(View.GONE);

                        RelativeLayout contentView = (RelativeLayout) findViewById(R.id.ProductList);
                        contentView.setVisibility(View.VISIBLE);

                        mProductListViewHelper.DisplayList(pageNum, searchData.GetProductsData(), searchData.GetCount());
                        SetupAttributeSpinner();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        retryCntGetSearchProds++;
                        if (retryCntGetSearchProds < CommonDefinitions.RETRY_COUNT)
                            GetSearchResults(query, pageNum, order);
                        else
                            retryCntGetSearchProds = 0;
                    }
                }
        );
    }

    void SetupAttributeSpinner () {
        final String[] attributeList = mProductListViewHelper.GetAttributeList(CommonDefinitions.SEARCH_CATEGORY_ID);

        ProductFilterDrawerFragment productFilterDrawerFragment = (ProductFilterDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_filter_drawer);
        productFilterDrawerFragment.setup(R.id.fragment_filter_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), attributeList);
    }

    public void UpdateSearchMessage (String msg) {
        RecyclerView breadcrumdRV = (RecyclerView)findViewById(R.id.breadcrumb);
        mSearchMessage.set(0, msg);
        mSearchMessageAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {// Defensive programming - in case this activity receives other broadcast actions in the future
            mNewSearch = true;
            String query = intent.getStringExtra(SearchManager.QUERY);

            scrollListener.NewSearch(query);
            //mProductListViewHelper.EventBusRegister();

            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            GetSearchResults(query, 1, "ASC");
        }
    }

    public void OnScrolledToEnd (int nextPageNum) {
        GetSearchResults(mSearchQuery, nextPageNum, "ASC");;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mNewSearch)
            mNewSearch = false;
        else {
            //mProductListViewHelper.EventBusRegister();
            if (mProductListManager.SyncWithCartAndWishList(CommonDefinitions.SEARCH_CATEGORY_ID))
                mProductListViewHelper.GetProductListAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mProductListViewHelper.ResetDetailViewInitiated();
        //mProductListViewHelper.EventBusUnRegister();
    }

    @Override
    public void onBreadcrumbItemSelected(int position) {
        // Nothing to be done for search
    }

    @Override
    public void onFilterDrawerItemSelected(int position) {
        mProductListViewHelper.onFilterDrawerItemSelected(position, CommonDefinitions.SEARCH_CATEGORY_ID);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
    }

    @Override
    public int getDefaultItemSelectId() {
        return NavDrawerItemList.SEARCH_ITEM_ID;
    }
}
