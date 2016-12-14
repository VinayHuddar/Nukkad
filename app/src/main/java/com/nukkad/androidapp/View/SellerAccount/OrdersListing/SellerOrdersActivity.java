package com.nukkad.androidapp.View.SellerAccount.OrdersListing;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nukkad.androidapp.Common.ApplicationSettings;
import com.nukkad.androidapp.Common.CommonDefinitions;
import com.nukkad.androidapp.Common.NetworkProblemDialog;
import com.nukkad.androidapp.FazmartApplication;
import com.nukkad.androidapp.Model.DeliveryData.AccountData;
import com.nukkad.androidapp.Model.SellerAccountData.SellerOrderList;
import com.nukkad.androidapp.Model.UserAccountData;
import com.nukkad.androidapp.R;
import com.nukkad.androidapp.View.Common.BaseActivity;
import com.nukkad.androidapp.View.Common.NavDrawerItemList;
import com.nukkad.androidapp.View.Common.NavigationDrawerCallbacks;
import com.nukkad.androidapp.View.SellerAccount.OrderDetails.OrderDetailsActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SellerOrdersActivity extends BaseActivity implements NavigationDrawerCallbacks, OrdersFilterDrawerCallbacks {
    public static final String SELLER_ID = "seller_id";
    public static final String ORDER_ID = "order_id";
    public static final String ORDER_STATES = "order_states";

    private int mSellerId;

    FragmentActivity mActivity;
    NetworkProblemDialog mNetworkProblemDialog;

    OrdersFilterDrawerFragment mOrdersFilterDrawerFragment = null;

    String[] mOrderStates;
    String[] mSortFields;

    OrderListAdapter mOrderListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_orders);

        super.onCreateDrawer();
        mNavigationDrawerFragment.SetDefaultSelectedPosition(NavDrawerItemList.MY_SELLER_ACCOUNT_ITEM_ID);

        // Setup Filter
        mOrdersFilterDrawerFragment = (OrdersFilterDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_filter_drawer);

        /*mOrderStates = new String[4];
        mOrderStates[0] = "Pending";
        mOrderStates[1] = "Shipped";
        mOrderStates[2] = "Completed";
        mOrderStates[3] = "Canceled";*/

        mOrderStates = ApplicationSettings.GetInstance().GetOrderStates();
        mSortFields = ApplicationSettings.GetInstance().GetSellerOrderListSortFields();

        getSupportActionBar().setTitle("Orders");

        mActivity = this;

        AccountData accountData = UserAccountData.GetInstance().GetAccountData(this);
        mSellerId = accountData.GetSellerId();

        mNetworkProblemDialog = NetworkProblemDialog.newInstance();

        //FetchOrders(CommonDefinitions.ORDER_ALL);
        ShowOrders();
    }

    public class Order {
        int id;
        String cust_name;
        String order_status;
        String date;
        int total;

        public Order (int _id, String _cust_name, String _order_status, String _date, int _total) {
            id = _id;
            cust_name = _cust_name;
            order_status = _order_status;
            date = _date;
            total = _total;
        }
    }

    Order[] mOrderList;
    void ShowOrders () {
        mOrderList = new Order[20];
        String orderStatus = null;
        for (int i = 0; i < 20; i++) {
            switch (i%4) {
                case 0: orderStatus = "Pending"; break;
                case 1: orderStatus = "Complete"; break;
                case 2: orderStatus = "Shipped"; break;
                case 3: orderStatus = "Cancelled"; break;
            }
            mOrderList[i] = new Order(i, "", orderStatus, "17/08/2015", i*100);
        }

        //TextView title = (TextView)findViewById(R.id.OrdersLabel);
        //title.setText(String.format("All Orders (%d)", mOrderList.length));

        ListView ordersList = (ListView)findViewById(R.id.Orders);
        mOrderListAdapter = new OrderListAdapter(mOrderList);
        ordersList.setAdapter(mOrderListAdapter);

        /*FloatingActionButton filterFAB = (FloatingActionButton)findViewById(R.id.OrderFilterFAB);
        filterFAB.setSize(FloatingActionButton.SIZE_MINI);
        filterFAB.setVisibility(View.VISIBLE);
        filterFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOrdersFilterDrawerFragment.openDrawer();
            }
        });*/

        // Get Total Range
        int numOrders = mOrderList.length;
        double minTotal = 0xFFFFFFF, maxTotal = 0;
        for (int i = 0; i < numOrders; i++) {
            if (mOrderList[i].total < minTotal)
                minTotal = mOrderList[i].total;
            if (mOrderList[i].total > maxTotal)
                maxTotal = mOrderList[i].total;
        }

        mOrdersFilterDrawerFragment.setup(R.id.fragment_filter_drawer, (DrawerLayout) findViewById(R.id.drawer_layout),
                mSortFields, mOrderStates, minTotal, maxTotal);
    }

    public class OrderListAdapter extends BaseAdapter {
        Order[] mOrdersList;

        public OrderListAdapter(Order[] list) {
            mOrdersList = list;
        }

        public int getCount() {
            if (mOrdersList != null)
                return mOrdersList.length;
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
                listItem = inflater.inflate(R.layout.layout_seller_order, null);
            } else {
                listItem = convertView;
            }

            TextView orderIdTV = (TextView)listItem.findViewById(R.id.OrderId);
            orderIdTV.setText(String.valueOf(mOrdersList[position].id));

            TextView custNameTV = (TextView)listItem.findViewById(R.id.CustomerNameOrOrderStatus);
            custNameTV.setText(mOrdersList[position].order_status);

            TextView dateTV = (TextView)listItem.findViewById(R.id.OrderDate);
            dateTV.setText(mOrdersList[position].date);

            TextView orderTotalTV = (TextView)listItem.findViewById(R.id.OrderTotal);
            orderTotalTV.setText(String.valueOf(mOrdersList[position].total));

            listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mActivity, OrderDetailsActivity.class);
                    intent.putExtra(ORDER_ID, mOrdersList[position].id);
                    intent.putExtra(SELLER_ID, mSellerId);
                    startActivity(intent);
                }
            });

            return listItem;
        }

        //void SetOrderList (SellerOrderList.Order[] newOrderList) {
        void SetOrderList (Order[] newOrderList) {
            mOrdersList = newOrderList;
            notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sellers_orders, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.OrdersFilter) {
            mOrdersFilterDrawerFragment.openDrawer();
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
    public void onFilterSelectionChanged (OrdersFilterDrawerFragment.Filters filters) {
        Map<String, String> orderFilters = new HashMap<>();
        if (filters.sortFieldSelectPos != -1)
            orderFilters.put("sort_field", String.valueOf(filters.sortFieldSelectPos));
        if ((filters.orderStatusFiltersCheckedCnt > 0) &&
            (filters.orderStatusFiltersCheckedCnt < filters.orderStatusFilters.length)) {
            for (int i = 0; i < filters.orderStatusFilters.length; i++) {
                String val = (filters.orderStatusFilters[i] ? "true" : null);
                orderFilters.put("filter_".concat(mOrderStates[i]), val);
            }
        }
        if (filters.dateStart > 0)
            orderFilters.put("start_date", String.valueOf(filters.dateStart));
        if (filters.dateEnd > 0)
            orderFilters.put("end_date", String.valueOf(filters.dateEnd));
        if (filters.minTotal > 0)
            orderFilters.put("min_total", String.valueOf(filters.minTotal));
        if (filters.maxTotal > 0)
            orderFilters.put("max_total", String.valueOf(filters.maxTotal));

        //GetSellerOrders(orderFilters);

        if (toggleList) {
            Order[] orderList = new Order[4];
            for (int i = 0; i < 4; i++) {
                orderList[i] = new Order(4 - i, "", "Completed", "20/08/2015", i * 100);
            }
            mOrderListAdapter.SetOrderList(orderList);

            toggleList = !toggleList;
        } else {
            mOrderListAdapter.SetOrderList(mOrderList);
            toggleList = !toggleList;
        }
    }

    int mFetchSellerOrdersRetryCnt = 0;
    void GetSellerOrders (final Map<String, String> orderFilters) {
        ((FazmartApplication)getApplicationContext()).GetAPIService().GetSellerOrders(mSellerId,
                orderFilters, new Callback<SellerOrderList>() {
                    @Override
                    public void success(SellerOrderList sellerOrderList, Response response) {
                        //mOrderListAdapter.SetOrderList(sellerOrderList.GetOrders());
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (mFetchSellerOrdersRetryCnt < CommonDefinitions.RETRY_COUNT) {
                            mFetchSellerOrdersRetryCnt++;
                            GetSellerOrders(orderFilters);
                        } else {
                            if (mNetworkProblemDialog.isAdded() == false)
                                mNetworkProblemDialog.show(mActivity.getSupportFragmentManager(), "dialog");
                        }
                    }
                });
    }
}
