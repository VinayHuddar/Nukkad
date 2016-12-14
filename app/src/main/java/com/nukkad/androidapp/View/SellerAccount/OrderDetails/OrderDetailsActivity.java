package com.nukkad.androidapp.View.SellerAccount.OrderDetails;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.nukkad.androidapp.Common.ApplicationSettings;
import com.nukkad.androidapp.Model.SellerAccountData.SellerOrderDetails;
import com.nukkad.androidapp.R;
import com.nukkad.androidapp.View.Common.SlidingTabLayout;
import com.nukkad.androidapp.View.SellerAccount.OrdersListing.SellerOrdersActivity;

public class OrderDetailsActivity extends AppCompatActivity {
    int mOrderId;
    int mSellerId;
    String[] mOrderStates;

    FragmentActivity mActivity;

    SellerOrderDetails mOrderInfo;
    SellerOrderDetails.OrderDetails mOrderDetails;
    SellerOrderDetails.OrderHistory[] mOrderHistory;

    OrderDetailsPagerAdapter mOrderDetailsPagerAdapter;
    ViewPager mViewPager;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setTitle("Order Information");

        mActivity = this;
        mSellerId = getIntent().getIntExtra(SellerOrdersActivity.SELLER_ID, -1);
        mOrderId = getIntent().getIntExtra(SellerOrdersActivity.ORDER_ID, -1);
        mOrderStates = ApplicationSettings.GetInstance().GetOrderStates();

        GetSellerOrderDetails();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_order_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    int mFetchOrderDetailsRetryCnt = 0;
    void GetSellerOrderDetails () {
        /*((FazmartApplication)getApplicationContext()).GetAPIService().GetSellerOrderDetails(mOrderId,
                mSellerId, new Callback<SellerOrderDetails>() {
                    @Override
                    public void success(SellerOrderDetails orderDetails, Response response) {
                        mOrderDetails = orderDetails.GetOrderDetails();
                        mOrderHistory = orderDetails.GetOrderHistory();

                        int numProds = mOrderDetails.GetProducts().length;
                        mProdTrackingNums = new int[numProds];
                        for (int i = 0; i < numProds; i++) {
                            if (mOrderDetails.GetProduct(i).GetTrackingNumber() > 0)
                                mProdTrackingNums[i] = mOrderDetails.GetProduct(i).GetTrackingNumber();
                            else
                                mProdTrackingNums[i] = 0;
                        }

                        mActivity.findViewById(R.id.loading_message).setVisibility(View.INVISIBLE);
                        ShowOrderDetails ();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (mFetchOrderDetailsRetryCnt < CommonDefinitions.RETRY_COUNT) {
                            mFetchOrderDetailsRetryCnt++;
                            GetSellerOrderDetails();
                        } else {
                            if (mNetworkProblemDialog.isAdded() == false)
                                mNetworkProblemDialog.show(mActivity.getSupportFragmentManager(), "dialog");
                        }
                    }
                });*/

        SellerOrderDetails orderInfo = new SellerOrderDetails(123, 345, "pending", "20/08/2015 13:01",
                "Cash On Delivery", "Door Delivery", "C802, Salarpuria Sattva Melody \nNayandahalli, Mysore Road\nBangalore - 560039",
                100);
        orderInfo.AddProducts(4);
        orderInfo.AddProduct(0, 109, "Speaking Tee 1", 20, "Not Paid", "\u20b9100", "\u20b92000", 0);
        orderInfo.AddProduct(1, 116, "Legging1-Kids", 300, "Not Paid", "\u20b9100", "\u20b930000", 123);
        orderInfo.AddProduct(2, 121, "Frock1", 1, "Not Paid", "\u20b9250", "\u20b9250", 0);
        orderInfo.AddProduct(3, 126, "Top1", 2, "Not Paid", "\u20b9100", "\u20b9200", 0);

        orderInfo.InitHistory(3);
        orderInfo.AddHistory(0, "20/08/2015 13:01", "Pending", "sdfsdf");
        orderInfo.AddHistory(1, "21/08/2015 13:01", "Shipping", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        orderInfo.AddHistory(2, "22/08/2015 13:01", "Completed", "sdfsdf");

        mOrderInfo = orderInfo;
        mOrderDetails = orderInfo.GetOrderDetails();
        mOrderHistory = orderInfo.GetOrderHistory();

        mActivity.findViewById(R.id.loading_message).setVisibility(View.INVISIBLE);

        // Setup the Pager
        mOrderDetailsPagerAdapter = new OrderDetailsPagerAdapter(getSupportFragmentManager(), this);
        mViewPager = (ViewPager)findViewById(R.id.OrderDetailsPager);
        mViewPager.setAdapter(mOrderDetailsPagerAdapter);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // How far the user has to scroll before it locks the parent vertical scrolling.
                final int margin = 10;
                final int fragmentOffset = v.getScrollX() % v.getWidth();

                // Disallow the touch request for parent scroll on touch of child view
                if (fragmentOffset > margin && fragmentOffset < v.getWidth() - margin) {
                    mViewPager.getParent().requestDisallowInterceptTouchEvent(true);
                }

                //((ScrollView)findViewById(R.id.SellerProfileScrollView)).requestDisallowInterceptTouchEvent(true);
                //mViewPager.getParent().requestDisallowInterceptTouchEvent(true);

                return false;
            }
        });

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.SlidingTabs);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);

        //ShowOrderDetails ();
    }

    public class OrderDetailsPagerAdapter extends FragmentPagerAdapter {
        Context mContext;

        public OrderDetailsPagerAdapter(FragmentManager fm, Context context) {
            super(fm);

            mContext = context;
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;
            Bundle args;

            switch(i) {
                case 0:
                    fragment = new OrderDetailsFragment();

                    args = new Bundle();
                    args.putInt(SellerOrdersActivity.SELLER_ID, mSellerId);
                    fragment.setArguments(args);

                    break;

                case 1:
                    fragment = new OrderHistoryFragment();

                    args = new Bundle();
                    args.putInt(SellerOrdersActivity.SELLER_ID, mSellerId);
                    fragment.setArguments(args);

                    break;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return (position == 0 ? "Order Details" : "Order History");
        }
    }

    public SellerOrderDetails GetOrderInfo () {
        return mOrderInfo;
    }
}
