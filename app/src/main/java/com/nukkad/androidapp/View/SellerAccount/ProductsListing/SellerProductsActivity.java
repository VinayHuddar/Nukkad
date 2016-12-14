package com.nukkad.androidapp.View.SellerAccount.ProductsListing;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nukkad.androidapp.Common.ApplicationSettings;
import com.nukkad.androidapp.Common.NetworkProblemDialog;
import com.nukkad.androidapp.Controller.Common.ImageDownloader;
import com.nukkad.androidapp.Model.DeliveryData.AccountData;
import com.nukkad.androidapp.Model.SellerAccountData.ProductFilterRange;
import com.nukkad.androidapp.Model.SellerAccountData.ProductStatus;
import com.nukkad.androidapp.Model.SellerAccountData.SellerAccountProductData;
import com.nukkad.androidapp.Model.UserAccountData;
import com.nukkad.androidapp.R;
import com.nukkad.androidapp.View.Common.BaseActivity;
import com.nukkad.androidapp.View.Common.NavDrawerItemList;
import com.nukkad.androidapp.View.Common.NavigationDrawerCallbacks;

import java.util.HashMap;
import java.util.Map;

public class SellerProductsActivity extends BaseActivity implements NavigationDrawerCallbacks, ProductsFilterDrawerCallbacks,
        SellerAccountProductData.Callback, ProductStatus.Callback, ProductFilterRange.Callback
{
    public static final String SELLER_ID = "seller_id";
    public static final String PRODUCT_ID = "product_id";
    public static final String PAGE_NUM = "page";

    private int mSellerId;

    FragmentActivity mActivity;
    NetworkProblemDialog mNetworkProblemDialog;

    SellerProductsFilterDrawerFragment mSellerProductsFilterDrawerFragment = null;

    String[] mSortFields;

    ProductListAdapter mProductListAdapter;
    ImageDownloader imageDownloader;

    SellerAccountProductData mProductData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_products);

        super.onCreateDrawer();
        mNavigationDrawerFragment.SetDefaultSelectedPosition(NavDrawerItemList.MY_SELLER_ACCOUNT_ITEM_ID);

        findViewById(R.id.loading_message).setVisibility(View.VISIBLE);
        imageDownloader = new ImageDownloader();

        // Setup Filter
        mSellerProductsFilterDrawerFragment = (SellerProductsFilterDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_filter_drawer);

        //mProductStates = ApplicationSettings.GetInstance().GetProductStates();
        mSortFields = ApplicationSettings.GetInstance().GetSellerProductListSortFields();

        getSupportActionBar().setTitle("Products");

        mActivity = this;

        AccountData accountData = UserAccountData.GetInstance().GetAccountData(this);
        mSellerId = accountData.GetSellerId();

        mNetworkProblemDialog = NetworkProblemDialog.newInstance();

        Map<String, String> productFilters = new HashMap<>();
        //productFilters.put(PAGE_NUM, "1");

        mProductData = new SellerAccountProductData (this);
        mProductData.FetchData(productFilters);

        ProductStatus statusData = new ProductStatus(this);
        statusData.FetchData();

        ProductFilterRange filterRange = new ProductFilterRange(this);
        filterRange.FetchData();
    }

    public void OnProductDataRecieved (SellerAccountProductData.Model prodList) {
        ListView productsList = (ListView)findViewById(R.id.Products);
        mProductListAdapter = new ProductListAdapter(prodList.GetProducts());
        productsList.setAdapter(mProductListAdapter);
    }

    ProductFilterRange.Model mFilterRanges = null;
    public void OnFilterRangeRecieved (ProductFilterRange.Model filterData) {
        mFilterRanges = filterData;
        SetupFilterDrawer ();
    }

    ProductStatus.Model mProductStates = null;
    public void OnStatusRecieved (ProductStatus.Model statusData) {
        mProductStates = statusData;
        SetupFilterDrawer ();
    }

    SellerAccountProductData.Model mProductList = null;
    void SetupFilterDrawer () {
        if ((mFilterRanges != null) && (mProductStates != null))
            mSellerProductsFilterDrawerFragment.setup(R.id.fragment_filter_drawer, (DrawerLayout) findViewById(R.id.drawer_layout),
                    mSortFields, mProductStates, mFilterRanges); //minQuantity, maxQuantity);
    }

    public class ProductListAdapter extends BaseAdapter {
        SellerAccountProductData.Model.Product[] mProductsList;

        public ProductListAdapter(SellerAccountProductData.Model.Product[] list) {
            mProductsList = list;
        }

        public int getCount() {
            if (mProductsList != null)
                return mProductsList.length;
            else
                return 0;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(final int position, View convertView, ViewGroup parent) {
            //Context mContext = mParentActivity;
            View listItem;
            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                listItem = inflater.inflate(R.layout.layout_seller_product_list_view, null);
            } else {
                listItem = convertView;
            }

            final SellerAccountProductData.Model.Product currProduct = mProductsList[position];

            ImageView imageView = (ImageView)listItem.findViewById(R.id.ProductImage);
            String imgURL = currProduct.GetImage();
            if (imgURL == null) // Needs investigation; dig into imageDownloader to understand how it handles null URLs
                imgURL = "null";
            imageDownloader.download(imgURL, imageView);

            TextView productName = (TextView)listItem.findViewById(R.id.ProductTitle);
            productName.setText(currProduct.GetName());

            // Set prices
            final TextView oldPriceTV = (TextView) listItem.findViewById(R.id.ProductOldPrice);
            final TextView currentPriceTV = (TextView) listItem.findViewById(R.id.ProductCurrentPrice);
            if (currProduct.GetSpecial() != 0) {
                oldPriceTV.setVisibility(View.VISIBLE);

                // Set Old Price
                oldPriceTV.setPaintFlags(oldPriceTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                oldPriceTV.setText(String.valueOf(currProduct.GetPrice()));

                // Set Special Price
                currentPriceTV.setText(String.valueOf(currProduct.GetSpecial()));
            } else {
                oldPriceTV.setVisibility(View.GONE);
                // Set current price
                currentPriceTV.setText(String.valueOf(currProduct.GetPrice()));
            }

            TextView dateAdded = (TextView)listItem.findViewById(R.id.DateAdded);
            dateAdded.setText(String.format("%s", currProduct.GetDateAdded()));

            TextView quantity = (TextView)listItem.findViewById(R.id.ProductQuantity);
            quantity.setText(String.valueOf(mQuantity));

            TextView status = (TextView)listItem.findViewById(R.id.ProductStatus);
            status.setText(currProduct.GetStatus());

            TextView soldQty = (TextView)listItem.findViewById(R.id.ProductQuantitySold);
            soldQty.setText(String.valueOf(currProduct.GetSales()));

            TextView earned = (TextView)listItem.findViewById(R.id.ProductEarned);
            earned.setText(String.valueOf(String.valueOf(currProduct.GetEarnings())));

            listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Intent intent = new Intent(mActivity, SellerProductDetailActivity.class);
                    intent.putExtra(PRODUCT_ID, mProductsList[position].GetProductId());
                    intent.putExtra(SELLER_ID, mSellerId);
                    startActivity(intent);*/
                }
            });

            return listItem;
        }

        int mQuantity = 100;
        void ChangeQuantity(boolean changeQty) {
            if (changeQty)
                mQuantity = 75;

            notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_seller_products, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.ProductsFilter) {
            mSellerProductsFilterDrawerFragment.openDrawer();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
    }

    @Override
    public int getDefaultItemSelectId() {
        return NavDrawerItemList.MY_SELLER_ACCOUNT_ITEM_ID;
    }

    boolean toggleList = true;
    @Override
    public void onFilterSelectionChanged (SellerProductsFilterDrawerFragment.Filters filters) {
        Map<String, String> orderFilters = new HashMap<>();
        if (filters.sortFieldName != null)
            orderFilters.put("sort_field", filters.sortFieldName);
        for (int i = 0; i < filters.productStatusFilters.length; i++) {
            String val = (filters.productStatusFilters[i] ? "true" : null);
            if (val != null) {
                orderFilters.put("filter_status_".concat(mProductStates.GetStatusName(i)), val);
            }
        }
        ProductFilterRange.Model filterRanges = filters.filterRanges;
        if (filterRanges.GetMinPrice() > mFilterRanges.GetMinPrice())
            orderFilters.put("filter_min_price", String.valueOf(filterRanges.GetMinPrice()));
        if (filterRanges.GetMaxPrice() < mFilterRanges.GetMaxPrice())
            orderFilters.put("filter_max_price", String.valueOf(filterRanges.GetMaxPrice()));
        if (filterRanges.GetMinSales() > mFilterRanges.GetMinSales())
            orderFilters.put("filter_min_sales", String.valueOf(filterRanges.GetMinSales()));
        if (filterRanges.GetMaxSales() < mFilterRanges.GetMaxSales())
            orderFilters.put("filter_max_sales", String.valueOf(filterRanges.GetMaxSales()));
        if (filterRanges.GetMinEarnings() > mFilterRanges.GetMinEarnings())
            orderFilters.put("filter_min_earnings", String.valueOf(filterRanges.GetMinEarnings()));
        if (filterRanges.GetMaxEarnings() < mFilterRanges.GetMaxEarnings())
            orderFilters.put("filter_max_earnings", String.valueOf(filterRanges.GetMaxEarnings()));
        /*if (filterRanges.GetMinQuantity() > mFilterRanges.GetMinQuantity())
            orderFilters.put("min_quantity", String.valueOf(filterRanges.GetMinQuantity()));
        if (filterRanges.GetMaxQuantity() < mFilterRanges.GetMaxQuantity())
            orderFilters.put("max_quantity", String.valueOf(filterRanges.GetMaxQuantity()));*/

        mProductData.FetchData(orderFilters);
    }
}
