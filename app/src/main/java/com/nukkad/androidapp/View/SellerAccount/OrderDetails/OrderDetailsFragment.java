package com.nukkad.androidapp.View.SellerAccount.OrderDetails;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.nukkad.androidapp.Model.SellerAccountData.SellerOrderDetails;
import com.nukkad.androidapp.R;
import com.nukkad.androidapp.View.Common.UnrollListView;

/**
 * Created by vinayhuddar on 21/08/15.
 */
public class OrderDetailsFragment extends Fragment {
    SellerOrderDetails.OrderDetails mOrderDetails;

    int[] mProdTrackingNums;
    boolean mTrackingNumUpdates = false;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        final View rootView = inflater.inflate(
                R.layout.fragment_seller_order_details, container, false);

        // Invoice Number
        TextView invoiceNum = (TextView)getActivity().findViewById(R.id.InvoiceNum);
        int invNum = mOrderDetails.GetInvoiceNum();
        if (invNum > 0) {
            invoiceNum.setText(String.valueOf(invNum));
        }

        // Order Id
        TextView orderId = (TextView)rootView.findViewById(R.id.OrderId);
        orderId.setText(String.valueOf(mOrderDetails.GetOrderId()));

        // Date Added
        TextView date = (TextView)rootView.findViewById(R.id.DateAdded);
        date.setText(mOrderDetails.GetDate());

        // Pay method
        TextView payMethod = (TextView)rootView.findViewById(R.id.PaymentMethod);
        payMethod.setText(mOrderDetails.GetPaymentMethod());

        // Delivery Method
        TextView delMethod = (TextView)rootView.findViewById(R.id.DeliveryMethod);
        delMethod.setText(mOrderDetails.GetDeliveryMethod());

        // Delivery Address
        TextView delAddr = (TextView)rootView.findViewById(R.id.DeliveryAddress);
        delAddr.setText(mOrderDetails.GetDeliveryAddress());

        // Products
        ListView productsList = (ListView)rootView.findViewById(R.id.ProductList);
        ProductsListAdapter productsListAdapter = new ProductsListAdapter(mOrderDetails.GetProducts());
        productsList.setAdapter(productsListAdapter);
        UnrollListView.setListViewHeightBasedOnItems(productsList);

        // Total
        TextView total = (TextView)rootView.findViewById(R.id.TotalAllProducts);
        total.setText(String.valueOf(mOrderDetails.GetTotal()));

        // Submit
        TextView submitButton = (TextView)rootView.findViewById(R.id.SubmitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TBD: Post tracking numbers
            }
        });

        return rootView;
    }

    private class ProductsListAdapter extends BaseAdapter {
        SellerOrderDetails.Product[] mProducts;

        public ProductsListAdapter (SellerOrderDetails.Product[] products) {
            mProducts = products;
        }

        @Override
        public int getCount () {
            return (mProducts != null ? mProducts.length : 0);
        }

        @Override
        public Object getItem (int position) {
            return mProducts[position];
        }

        @Override
        public long getItemId (int position) {
            return position;
        }

        @Override
        public View getView (final int position, View convertView, ViewGroup parent) {
            SellerOrderDetails.Product currProdData = mProducts[position];

            View currView = null;
            LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null)
                currView = inflater.inflate(R.layout.order_details_product_list_item, null);
            else
                currView = convertView;

            TextView name = (TextView)currView.findViewById(R.id.ProductName);
            name.setText(currProdData.GetName());

            TextView qty = (TextView)currView.findViewById(R.id.Quantity);
            qty.setText(String.valueOf(currProdData.GetQuantity()));

            TextView price = (TextView)currView.findViewById(R.id.UnitPrice);
            price.setText(currProdData.GetPrice());

            TextView total = (TextView)currView.findViewById(R.id.Total);
            total.setText(currProdData.GetTotal());

            TextView transStatus = (TextView)currView.findViewById(R.id.TransactionStatus);
            transStatus.setText(currProdData.GetTransactionStatus());

            EditText trackNum = (EditText)currView.findViewById(R.id.TrackingNumber);
            if (currProdData.GetTrackingNumber() > 0) {
                trackNum.setText(String.valueOf(currProdData.GetTrackingNumber()));
                trackNum.setClickable(false);
            } else {
                trackNum.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        mProdTrackingNums[position] = Integer.getInteger(v.getText().toString());
                        mTrackingNumUpdates = true;
                        return false;
                    }
                });
            }

            return currView;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mOrderDetails = ((OrderDetailsActivity)activity).GetOrderInfo().GetOrderDetails();

        int numProds = mOrderDetails.GetProducts().length;
        mProdTrackingNums = new int[numProds];
        for (int i = 0; i < numProds; i++) {
            if (mOrderDetails.GetProduct(i).GetTrackingNumber() > 0)
                mProdTrackingNums[i] = mOrderDetails.GetProduct(i).GetTrackingNumber();
            else
                mProdTrackingNums[i] = 0;
        }

    }

}

