package com.nukkad.androidapp.View.SellerAccount;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nukkad.androidapp.Common.ApplicationSettings;
import com.nukkad.androidapp.Common.CommonDefinitions;
import com.nukkad.androidapp.Common.NetworkProblemDialog;
import com.nukkad.androidapp.FazmartApplication;
import com.nukkad.androidapp.Model.DeliveryData.AccountData;
import com.nukkad.androidapp.Model.SellerAccountData.SellerOrderSummary;
import com.nukkad.androidapp.Model.UserAccountData;
import com.nukkad.androidapp.R;
import com.nukkad.androidapp.View.Common.BaseActivity;
import com.nukkad.androidapp.View.Common.NavDrawerItemList;
import com.nukkad.androidapp.View.Common.NavigationDrawerCallbacks;
import com.nukkad.androidapp.View.SellerAccount.OrderDetails.OrderDetailsActivity;
import com.nukkad.androidapp.View.SellerAccount.OrdersListing.SellerOrdersActivity;
import com.nukkad.androidapp.View.SellerAccount.ProductsListing.SellerProductsActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SellerAccountActivity extends BaseActivity implements NavigationDrawerCallbacks {
    private static final String SELLER_ID = "seller_id";

    private static final int POSITION_ORDERS = 0;
    private static final int POSITION_TRANSACTIONS = 1;
    private static final int POSITION_PRODUCT_LIST = 2;
    private static final int POSITION_PROFILE = 3;

    private int mSellerId;

    FragmentActivity mActivity;
    NetworkProblemDialog mNetworkProblemDialog;

    private String[] mOrderStates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_account);

        super.onCreateDrawer();
        mNavigationDrawerFragment.SetDefaultSelectedPosition(NavDrawerItemList.MY_SELLER_ACCOUNT_ITEM_ID);

        getSupportActionBar().setTitle("Dashboard");

        mActivity = this;
        mOrderStates = null;

        AccountData accountData = UserAccountData.GetInstance().GetAccountData(this);
        mSellerId = accountData.GetSellerId();

        mNetworkProblemDialog = NetworkProblemDialog.newInstance();

        //FetchPendingOrders();
        ShowPendingOrders();
        ShowActionButtons();
    }

    public class Order {
        int id;
        String cust_name;
        String date;
        int total;

        public Order (int _id, String _cust_name, String _date, int _total) {
            id = _id;
            cust_name = _cust_name;
            date = _date;
            total = _total;
        }
    }

    void ShowPendingOrders () {
        // Show the big numbers
        TextView totalOrders = (TextView)findViewById(R.id.SellerTotalOrders);
        totalOrders.setText(String.valueOf(2034));

        TextView totalSales = (TextView)findViewById(R.id.SellerTotalSales);
        totalSales.setText("\u20b9 203400");

        mOrderStates = ApplicationSettings.GetInstance().GetOrderStates();

        Order[] orderList = new Order[20];
        for (int i = 0; i < 20; i++) {
            orderList[i] = new Order(i, "Vinay Huddar", "17/08/2015", i*100);
        }

        TextView pendOrdersHdg = (TextView)findViewById(R.id.PendingOrdersLabel);
        String hdg = pendOrdersHdg.getText().toString();
        pendOrdersHdg.setText(String.format("%s ( %d )", hdg, orderList.length));

        ListView pendingOrdersList = (ListView)findViewById(R.id.PendingOrders);
        pendingOrdersList.setAdapter(new PendingOrdersAdapter(orderList));
    }

    public class PendingOrdersAdapter extends BaseAdapter {
        public static final String ORDER_ID = "order_id";
        Order[] mOrdersList;
        public PendingOrdersAdapter(Order[] list) {
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
                listItem = (View) convertView;
            }

            TextView orderIdTV = (TextView)listItem.findViewById(R.id.OrderId);
            orderIdTV.setText(String.valueOf(mOrdersList[position].id));

            TextView custNameTV = (TextView)listItem.findViewById(R.id.CustomerNameOrOrderStatus);
            custNameTV.setText(mOrdersList[position].cust_name);

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
    }

    void ShowActionButtons() {
        ImageView ordersButton = (ImageView)findViewById(R.id.SellerOrders);
        ordersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, SellerOrdersActivity.class);
                startActivity(intent);
            }
        });

        ImageView productsButton = (ImageView)findViewById(R.id.SellerProducts);
        productsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, SellerProductsActivity.class);
                startActivity(intent);
            }
        });

        ImageView addProductButton = (ImageView)findViewById(R.id.AddProduct);
        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, SellersAddProductActivity.class);
                startActivity(intent);
            }
        });

        ImageView transactionsButton = (ImageView)findViewById(R.id.SellerTransactions);
        transactionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, SellersTransactionsActivity.class);
                startActivity(intent);
            }
        });

        ImageView profileButton = (ImageView)findViewById(R.id.SellerProfile);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, SellersMyProfileActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.loading_message).setVisibility(View.INVISIBLE);
    }

    int mFetchSummaryDataRetryCnt = 0;
    void FetchDashboardData() {
        Map<String, String> queries = new HashMap<> ();
        queries.put("filter_pending", "true");
        ((FazmartApplication) getApplicationContext()).GetAPIService().GetSellerOrderSummary(mSellerId,
                new Callback<SellerOrderSummary>() {
                    @Override
                    public void success(final SellerOrderSummary orderSummary, Response response) {
                        /*mFragmentView.findViewById(R.id.loading_message).setVisibility(View.INVISIBLE);

                        TextView totalOrders = (TextView)findViewById(R.id.seller_total_orders);
                        totalOrders.setText(String.valueOf(orderSummary.GetTotalOrders()));

                        TextView totalSales = (TextView)findViewById(R.id.seller_total_sales);
                        totalSales.setText(String.valueOf(orderSummary.GetTotalSales()));

                        ListView pendingOrdersList = (ListView)findViewById(R.id.PendingOrders);
                        pendingOrdersList.setAdapter(new PendingOrdersAdapter(orderSummary.GetPendingOrders()));

                        mOrderStates = orderSummary.GetOrderStates();*/
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (mFetchSummaryDataRetryCnt < CommonDefinitions.RETRY_COUNT) {
                            mFetchSummaryDataRetryCnt++;
                            FetchDashboardData();
                        } else {
                            if (mNetworkProblemDialog.isAdded() == false)
                                mNetworkProblemDialog.show(mActivity.getSupportFragmentManager(), "dialog");
                        }

                    }
                });
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
    }

    @Override
    public int getDefaultItemSelectId() {
        return NavDrawerItemList.MY_SELLER_ACCOUNT_ITEM_ID;
    }

    public String[] GetOrderStates () {
        return mOrderStates;
    }
}

