package com.nukkad.androidapp.View.SellerAccount;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nukkad.androidapp.Common.NetworkProblemDialog;
import com.nukkad.androidapp.Model.DeliveryData.AccountData;
//import com.nukkad.androidapp.Model.SellerAccountData.SellerAccountSummary;
import com.nukkad.androidapp.Model.UserAccountData;
import com.nukkad.androidapp.R;
import com.nukkad.androidapp.View.Common.BaseActivity;
import com.nukkad.androidapp.View.Common.NavDrawerItemList;
import com.nukkad.androidapp.View.Common.NavigationDrawerCallbacks;
import com.nukkad.androidapp.View.SellerAccount.OrdersListing.SellerOrdersActivity;

public class SellerAccountActivityOld extends BaseActivity implements NavigationDrawerCallbacks {
    private static final String SELLER_ID = "seller_id";

    private static final int POSITION_ORDERS = 0;
    private static final int POSITION_TRANSACTIONS = 1;
    private static final int POSITION_PRODUCT_LIST = 2;
    private static final int POSITION_PROFILE = 3;

    private int mSellerId;

    FragmentActivity mActivity;
    NetworkProblemDialog mNetworkProblemDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_account);

        super.onCreateDrawer();
        mNavigationDrawerFragment.SetDefaultSelectedPosition(NavDrawerItemList.MY_SELLER_ACCOUNT_ITEM_ID);

        getSupportActionBar().setTitle("My Seller Account");

        mActivity = this;
        //imageDownloader = new ImageDownloader();

        AccountData accountData = UserAccountData.GetInstance().GetAccountData(this);
        mSellerId = accountData.GetSellerId();

        mNetworkProblemDialog = NetworkProblemDialog.newInstance();

        //FetchSummaryData();
        RenderSummaryData();

    }

    public class Field {
        public Field (String label, int value) {
            field_value = value;
            field_label = label;
        }

        String field_label;
        int field_value;

        public String GetLabel () { return field_label; }
        public int GetValue () { return field_value; }
    }

    void RenderSummaryData () {
        final Field[] fields = new Field[4];

        fields[0] = new Field ("Orders", 10);
        fields[1] = new Field ("Transactions", 10000);
        fields[2] = new Field ("Products", 15);
        fields[3] = new Field ("Profile", -1);

        GridView sellerAccountGrid = (GridView) findViewById(R.id.SellerAccountFields);
        sellerAccountGrid.setAdapter(new SellerAccountFieldsAdapter(fields));
        sellerAccountGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, final View v,
                                    int position, long id) {
                Intent intent = null;
                switch (position) {
                    case POSITION_ORDERS:
                        intent = new Intent(mActivity, SellerOrdersActivity.class);
                        break;
                    case POSITION_TRANSACTIONS:
                        intent = new Intent(mActivity, SellersTransactionsActivity.class);
                        break;
                    case POSITION_PRODUCT_LIST:
                        intent = new Intent(mActivity, SellersProductListActivity.class);
                        break;
                    case POSITION_PROFILE:
                        intent = new Intent(mActivity, SellersMyProfileActivity.class);
                        break;
                }
                mActivity.startActivity(intent);
            }
        });

        findViewById(R.id.loading_message).setVisibility(View.INVISIBLE);
    }

    int mFetchSummaryDataRetryCnt = 0;
    void FetchSummaryData () {
        /*((FazmartApplication) getApplicationContext()).GetAPIService().GetSellerAccountSummary(mSellerId,
                new Callback<SellerAccountSummary>() {
                    @Override
                    public void success(final SellerAccountSummary summaryData, Response response) {
                        mFragmentView.findViewById(R.id.loading_message).setVisibility(View.INVISIBLE);

                        final SellerAccountSummary.Field[] fields = summaryData.GetSummaryData();
                        GridView sellerAccountGrid = (GridView) getActivity().findViewById(R.id.SellerAccountFields);
                        sellerAccountGrid.setAdapter(new SellerAccountFieldsAdapter(fields));
                        sellerAccountGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> parent, final View v,
                                                    int position, long id) {
                                Intent intent = null;
                                switch (position) {
                                    case POSITION_ORDERS:
                                        intent = new Intent(mParentActivity, SellerProductsActivity.class);
                                        break;
                                    case POSITION_TRANSACTIONS:
                                        intent = new Intent(mParentActivity, SellersTransactionsActivity.class);
                                        break;
                                    case POSITION_PRODUCT_LIST:
                                        intent = new Intent (mParentActivity, SellersProductListActivity.class);
                                        break;
                                    case POSITION_PROFILE:
                                        intent = new Intent (mParentActivity, SellersMyProfileActivity.class);
                                        break;
                                }
                                mParentActivity.startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (mFetchSummaryDataRetryCnt < CommonDefinitions.RETRY_COUNT) {
                            mFetchSummaryDataRetryCnt++;
                            FetchSummaryData();
                        } else {
                            if (mNetworkProblemDialog.isAdded() == false)
                                mNetworkProblemDialog.show(mActivity.getSupportFragmentManager(), "dialog");
                        }

                    }
                });*/
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
    }

    @Override
    public int getDefaultItemSelectId() {
        return NavDrawerItemList.BROWSE_ITEM_ID;
    }

    public class SellerAccountFieldsAdapter extends BaseAdapter {
        /*SellerAccountSummary.Field[] mFieldsData;
        public SellerAccountFieldsAdapter(SellerAccountSummary.Field[] subCategories) {
            mFieldsData = subCategories;
        }*/

        Field[] mFieldsData;
        public SellerAccountFieldsAdapter(Field[] fields) {
            mFieldsData = fields;
        }

        public int getCount() {
            if (mFieldsData != null)
                return mFieldsData.length;
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
            View gridItem;
            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                gridItem = inflater.inflate(R.layout.layout_seller_account_field, null);
            } else {
                gridItem = (View) convertView;
            }

            TextView fieldNameTV = (TextView) gridItem.findViewById(R.id.FieldTitle);
            String title = mFieldsData[position].GetLabel();
            int value = mFieldsData[position].GetValue();
            if (value != -1 )
                title = String.format("%s - %d", title, value);

            fieldNameTV.setText(title);

            ImageView imageView = (ImageView) gridItem.findViewById(R.id.FieldImage);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            switch (position) {
                case POSITION_ORDERS:
                    imageView.setImageResource(R.drawable.white_cart_64);
                    break;
                case POSITION_TRANSACTIONS:
                    imageView.setImageResource(R.drawable.transactions_64);
                    break;
                case POSITION_PRODUCT_LIST:
                    imageView.setImageResource(R.drawable.products_64);
                    break;
                case POSITION_PROFILE:
                    imageView.setImageResource(R.drawable.profile_64);
                    break;
            }

            gridItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = null;
                    switch (position) {
                        case POSITION_ORDERS:
                            intent = new Intent(mActivity, SellerOrdersActivity.class);
                            mActivity.startActivity(intent);
                            break;
                        case POSITION_TRANSACTIONS:
                            intent = new Intent(mActivity, SellersTransactionsActivity.class);
                            mActivity.startActivity(intent);
                            break;
                        case POSITION_PRODUCT_LIST:
                            intent = new Intent(mActivity, SellersProductListActivity.class);
                            mActivity.startActivity(intent);
                            break;
                        case POSITION_PROFILE:
                            intent = new Intent(mActivity, SellersMyProfileActivity.class);
                            mActivity.startActivity(intent);
                            break;
                    }
                }
            });

            return gridItem;
        }
    }
}

