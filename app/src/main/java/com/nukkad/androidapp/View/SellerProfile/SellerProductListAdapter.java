package com.nukkad.androidapp.View.SellerProfile;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nukkad.androidapp.Controller.Common.ImageDownloader;
import com.nukkad.androidapp.Model.ProductData.ProductData;
import com.nukkad.androidapp.R;

public class SellerProductListAdapter extends RecyclerView.Adapter<SellerProductListAdapter.ViewHolder> {

    private ProductData[] mData;
    private SellerProductListAdapterCallbacks mSellerProductListAdapterCallbacks;
    private int mSelectedPosition;
    private int mTouchedPosition = -1;

    Activity mParentActivity;

    public SellerProductListAdapter(ProductData[] data, Activity parentActivity) {
        mData = data;
        mParentActivity = parentActivity;
    }

    public SellerProductListAdapterCallbacks getProductListAdapterCallbacks() {
        return mSellerProductListAdapterCallbacks;
    }

    public void setProductListAdapterCallbacks(SellerProductListAdapterCallbacks sellerProductListAdapterCallbacks) {
        mSellerProductListAdapterCallbacks = sellerProductListAdapterCallbacks;
    }

    @Override
    public SellerProductListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_product_seller_profile, viewGroup, false);
        final ViewHolder viewholder = new ViewHolder(v);
        viewholder.itemView.setOnClickListener(new View.OnClickListener() {
                                                   @Override
                                                   public void onClick(View v) {
                                                       if (mSellerProductListAdapterCallbacks != null)
                                                           mSellerProductListAdapterCallbacks.onProductSelected(viewholder.getAdapterPosition());
                                                   }
                                               }
        );

        return viewholder;
    }

    private final ImageDownloader imageDownloader = new ImageDownloader();

    @Override
    public void onBindViewHolder(SellerProductListAdapter.ViewHolder viewHolder, final int i) {
        //if (i < (getItemCount()-1)) {
            ProductData currProd = mData[i];

            ImageView prodImageView = (ImageView) viewHolder.productView.findViewById(R.id.ProductImage);
            if (i < (getItemCount()-1)) {
                if (currProd.GetImageURL() != null)
                    imageDownloader.download(currProd.GetImageURL(), prodImageView);
                else
                    prodImageView.setImageDrawable(mParentActivity.getResources().getDrawable(R.drawable.fazmart_new_logo));

                TextView prodName = (TextView) viewHolder.productView.findViewById(R.id.ProductName);
                prodName.setText(currProd.GetName());
            } else {
                prodImageView.setImageDrawable(mParentActivity.getResources().getDrawable(R.drawable.view_more_128));
                TextView prodName = (TextView) viewHolder.productView.findViewById(R.id.ProductName);
                prodName.setText("");
            }
        /*} else {
            ImageView prodImageView = (ImageView) viewHolder.productView.findViewById(R.id.ProductImage);
            //prodImageView.setImageDrawable(mParentActivity.getResources().getDrawable(R.drawable.cart_purple));
            prodImageView.setVisibility(View.GONE);

            TextView prodName = (TextView) viewHolder.productView.findViewById(R.id.ProductName);
            prodName.setText("View All Products");
            prodName.setTextSize(18);
            prodName.setTextColor(mParentActivity.getResources().getColor(R.color.blue));
            prodName.setPaintFlags(prodName.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            prodName.setGravity(Gravity.CENTER);
            float scale = mParentActivity.getResources().getDisplayMetrics().density;
            int dpAsPixels = (int) (50 * scale + 0.5f);
            prodName.setPadding(0, dpAsPixels, 0, 0);
        }*/
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.length : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View productView;

        public ViewHolder(View itemView) {
            super(itemView);
            productView = (View) itemView.findViewById(R.id.SellerProduct);
        }
    }
}
