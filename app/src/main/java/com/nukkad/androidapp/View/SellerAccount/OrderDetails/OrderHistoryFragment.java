package com.nukkad.androidapp.View.SellerAccount.OrderDetails;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.nukkad.androidapp.Common.ApplicationSettings;
import com.nukkad.androidapp.Common.CommonDefinitions;
import com.nukkad.androidapp.Common.NetworkProblemDialog;
import com.nukkad.androidapp.FazmartApplication;
import com.nukkad.androidapp.Model.SellerAccountData.SellerOrderDetails;
import com.nukkad.androidapp.R;
import com.nukkad.androidapp.View.Common.UnrollListView;
import com.nukkad.androidapp.View.SellerAccount.OrdersListing.SellerOrdersActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by vinayhuddar on 21/08/15.
 */
public class OrderHistoryFragment extends Fragment {
    SellerOrderDetails mOrderInfo;
    String[] mOrderStates;
    int mSellerId;

    NetworkProblemDialog mNetworkProblemDialog;

    View mFragmentView;
    OrderHistoryAdapter mHistoryAdapter;

    /*public OrderHistoryFragment(SellerOrderDetails orderInfo, String[] orderStates) {
        mOrderInfo = orderInfo;
        mOrderStates = orderStates;

        mNetworkProblemDialog = NetworkProblemDialog.newInstance();
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mSellerId = getArguments().getInt(SellerOrdersActivity.SELLER_ID);
        mOrderStates = ApplicationSettings.GetInstance().GetOrderStates();

        //mOrderInfo = (SellerOrderDetails)getArguments().getSerializable(OrderDetailsActivity.ORDER_DETAILS);

        mFragmentView = inflater.inflate(
                R.layout.fragment_seller_order_history, container, false);

        // Order Status
        final Spinner statusSpinner = (Spinner)mFragmentView.findViewById(R.id.OrderStatus);
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mOrderStates);
        statusSpinner.setAdapter(statusAdapter);
        for (int i = 0; i < mOrderStates.length; i++) {
            if (mOrderStates[i].compareTo(mOrderInfo.GetOrderDetails().GetStatus()) == 0)
                statusSpinner.setSelection(i);
        }

        final CheckedTextView notifyCust = (CheckedTextView)mFragmentView.findViewById(R.id.Notify_Customer);
        notifyCust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notifyCust.isChecked())
                    notifyCust.setChecked(false);
                else
                    notifyCust.setChecked(true);
            }
        });
        final CheckedTextView notifyAdmin = (CheckedTextView)mFragmentView.findViewById(R.id.Notify_Admin);
        notifyAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notifyAdmin.isChecked())
                    notifyAdmin.setChecked(false);
                else
                    notifyAdmin.setChecked(true);
            }
        });
        final EditText comments = (EditText)mFragmentView.findViewById((R.id.SellerComments));

        // Add History
        TextView addHistory = (TextView)mFragmentView.findViewById(R.id.AddHistoryButton);
        addHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> queries = new HashMap<>();
                queries.put("status", statusSpinner.getSelectedItem().toString());
                if (notifyCust.isChecked())
                    queries.put("notify_customer", "true");
                if (notifyAdmin.isChecked())
                    queries.put("notify_admin", "true");
                if (comments.getText().toString() != "")
                    queries.put("comments", comments.getText().toString());

                UpdateOrderState(queries);
            }
        });

        // Order History
        ListView orderHistory = (ListView)mFragmentView.findViewById(R.id.OrderHistory);
        mHistoryAdapter = new OrderHistoryAdapter(mOrderInfo.GetOrderHistory());
        orderHistory.setAdapter(mHistoryAdapter);
        UnrollListView.setListViewHeightBasedOnItems(orderHistory);
        orderHistory.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // How far the user has to scroll before it locks the parent vertical scrolling.
                ((ScrollView) getActivity().findViewById(R.id.SellerOrderHistoryScrollView)).requestDisallowInterceptTouchEvent(true);

                return false;
            }
        });


        return mFragmentView;
    }

    private class OrderHistoryAdapter extends BaseAdapter {
        SellerOrderDetails.OrderHistory[] mOrderHistory;

        public OrderHistoryAdapter (SellerOrderDetails.OrderHistory[] orderHistory) {
            mOrderHistory = orderHistory;
        }

        @Override
        public int getCount () {
            return (mOrderHistory != null ? mOrderHistory.length : 0);
        }

        @Override
        public Object getItem (int position) {
            return mOrderHistory[position];
        }

        @Override
        public long getItemId (int position) {
            return position;
        }

        @Override
        public View getView (final int position, View convertView, ViewGroup parent) {
            SellerOrderDetails.OrderHistory currOrderHistory = mOrderHistory[position];

            View currView = null;
            LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null)
                currView = inflater.inflate(R.layout.order_history_list_item, null);
            else
                currView = convertView;

            TextView date = (TextView)currView.findViewById(R.id.OrderHistoryDate);
            date.setText(currOrderHistory.GetDate());

            TextView status = (TextView)currView.findViewById(R.id.OrderHistoryStatus);
            status.setText(currOrderHistory.GetStatus());

            TextView commentTV = (TextView)currView.findViewById(R.id.OrderHistoryComment);
            String comment = currOrderHistory.GetComment();
            if (comment.compareTo("") != 0) {
                comment = "<font color=\"black\">Comments: </font>" + comment;
                commentTV.setText(Html.fromHtml(comment));
            } else {
                commentTV.setText(Html.fromHtml("<font color=\"#c8c8c8\">No Comments</font>"));
            }

            return currView;
        }

        public void UpdateHistoryData (SellerOrderDetails.OrderHistory[] newOrderHistory) {
            mOrderHistory = newOrderHistory;
        }
    }

    int mUpdateOderStateRetryCnt = 0;
    void UpdateOrderState (final Map<String, String> fields) {
        ((FazmartApplication)getActivity().getApplicationContext()).GetAPIService().UpdateOrderState(
                mOrderInfo.GetOrderDetails().GetOrderId(), mSellerId, fields,
                new Callback<SellerOrderDetails>() {
                    @Override
                    public void success(SellerOrderDetails orderDetails, Response response) {
                        // Update the status spinner
                        Spinner statusSpinner = (Spinner)mFragmentView.findViewById(R.id.OrderStatus);
                        for (int i = 0; i < mOrderStates.length; i++) {
                            if (mOrderStates[i].compareTo(orderDetails.GetOrderDetails().GetStatus()) == 0)
                                statusSpinner.setSelection(i);
                        }

                        // Update the history table
                        mHistoryAdapter.UpdateHistoryData(orderDetails.GetOrderHistory());
                        mHistoryAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (mUpdateOderStateRetryCnt < CommonDefinitions.RETRY_COUNT) {
                            mUpdateOderStateRetryCnt++;
                            UpdateOrderState(fields);
                        } else {
                            if (mNetworkProblemDialog.isAdded() == false)
                                mNetworkProblemDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                        }
                    }
                });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mOrderInfo = ((OrderDetailsActivity)activity).GetOrderInfo();
    }
}
