package com.nukkad.androidapp.View;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.nukkad.androidapp.Common.CommonDefinitions;
import com.nukkad.androidapp.Common.Events.EventItemAddedToCart;
import com.nukkad.androidapp.Common.IntentActionStrings;
import com.nukkad.androidapp.Controller.Cart.CartController;
import com.nukkad.androidapp.Controller.Common.ImageDownloader;
import com.nukkad.androidapp.Model.ProductData.ProductData;
import com.nukkad.androidapp.Model.ProductData.ProductDetailData;
import com.nukkad.androidapp.Controller.ProductDetailDataController;
import com.nukkad.androidapp.Model.SellerAccountData.SellerInfo;
import com.nukkad.androidapp.R;
import com.nukkad.androidapp.View.SellerProfile.SellerProductListAdapter;
import com.nukkad.androidapp.View.SellerProfile.SellerProductListAdapterCallbacks;
import com.nukkad.androidapp.View.SellerProfile.SellerProfileActivity;

import de.greenrobot.event.EventBus;

public class ProductDetailActivity extends Activity implements ProductDetailViewCallbacks, SellerProductListAdapterCallbacks {

    ProductDetailDataController mProductDetailDataController;
    ProductDetailDataController.ProductDetailData mCurrentProductData;
    Context mContext;
    Activity mActivity;
    ImageDownloader imageDownloader;
    int mQuantity;

    CartController mCartController;
    ProductData[] mSellerProdList = null;

    int mSellerId = 0;
    boolean mHasSeller = false;
    boolean mSellerInfoFetched = false;

    public ProductDetailActivity() {
        mProductDetailDataController = ProductDetailDataController.GetInstance();
        mProductDetailDataController.RegisterCallback(this);

        mCartController = CartController.GetInstance();
        imageDownloader = new ImageDownloader();

        mContext = mActivity = this;
        mCurrentProductData = null;
        mQuantity = 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail_screen);
        this.setTitle("Product Detail Information");

        FetchData(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        FetchData(intent);
    }

    void FetchData (Intent intent) {
        int productId = intent.getIntExtra(IntentActionStrings.PRODUCT_ID, 0);
        int optionIdx = intent.getIntExtra(IntentActionStrings.OPTION_SELECTION, 0);
        mSellerId = intent.getIntExtra(IntentActionStrings.SELLER_ID, CommonDefinitions.INVALID_ID);

        findViewById(R.id.loading_message).setVisibility(View.VISIBLE);
        findViewById(R.id.WaitForSellerInfo).setVisibility(View.INVISIBLE);
        findViewById(R.id.ProductDetails).setVisibility(View.INVISIBLE);
        findViewById(R.id.SellerInfo).setVisibility(View.INVISIBLE);

        mProductDetailDataController.FetchProductDetailData(productId, optionIdx);  //categoryId, productId, optionIdx);
        if (mSellerId != CommonDefinitions.INVALID_ID) {
            mProductDetailDataController.FetchSellerInfo(mSellerId);
            mProductDetailDataController.FetchSellerProducts(mSellerId, 1, null);
            mHasSeller = true;
        }
    }


    @Override
    public void OnDetailDataFetched () {
        if ((mHasSeller == true) && (mSellerInfoFetched == false))
            findViewById(R.id.WaitForSellerInfo).setVisibility(View.VISIBLE);
        mCurrentProductData = mProductDetailDataController.GetCurrentProductData();

        findViewById(R.id.loading_message).setVisibility(View.GONE);
        findViewById(R.id.ProductDetails).setVisibility(View.VISIBLE);

        FillupProductDetailViews();
    }

    @Override
    public void OnSellerInfoFetched (final SellerInfo sellerInfo) {
        findViewById(R.id.WaitForSellerInfo).setVisibility(View.INVISIBLE);
        findViewById(R.id.SellerInfo).setVisibility(View.VISIBLE);

        mSellerInfoFetched = true;

        //ShowSellerInfo();

        ImageView sellerImageView = (ImageView) findViewById(R.id.SellerImage);
        if (sellerInfo.GetSellerImage() != null) {
            imageDownloader.download(sellerInfo.GetSellerImage(), sellerImageView);
        } else {
            sellerImageView.setImageDrawable(getResources().getDrawable(R.drawable.seller_large));
        }
        sellerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, SellerProfileActivity.class);
                intent.putExtra(IntentActionStrings.SELLER_ID, 1);
                intent.putExtra(IntentActionStrings.FROM_PRODUCT_DETAIL_VIEW, true);
                startActivity(intent);
            }
        });

        TextView sellerName = (TextView) findViewById(R.id.SellerName);
        sellerName.setPaintFlags(sellerName.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        sellerName.setText(sellerInfo.GetName());
        sellerName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, SellerProfileActivity.class);
                intent.putExtra(IntentActionStrings.SELLER_ID, 1);
                intent.putExtra(IntentActionStrings.FROM_PRODUCT_DETAIL_VIEW, true);
                startActivity(intent);
            }
        });

        TextView sellerLocation = (TextView) findViewById(R.id.SellerAddress);
        // Enable this when Area is added to seller info
        //sellerLocation.setText(String.format("%s %s", productSellerData.GetCompany()), productSellerData.GetArea());
        sellerLocation.setText(sellerInfo.GetCompany());

        TextView ratingTV = (TextView) findViewById(R.id.SellerAverageRating);
        ratingTV.setText(String.format("%.1f", sellerInfo.GetRating()));

        TextView sellerProdCnt = (TextView) findViewById(R.id.SellerProductCount);
        sellerProdCnt.setText(String.format("%d", sellerInfo.GetProducts().length));

        TextView sellerProfileButton = (TextView)findViewById(R.id.SellerProfileButton);
        sellerProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, SellerProfileActivity.class);
                intent.putExtra(IntentActionStrings.SELLER_ID, 1); // productSellerData.GetSellerId();
                intent.putExtra(IntentActionStrings.FROM_PRODUCT_DETAIL_VIEW, true);
                startActivity(intent);
            }
        });
    }

    @Override
    public void OnSellerProductsFetched (final ProductData[] productData) {
        mSellerProdList = productData;

        // Show Other Products From Seller
        RecyclerView sellerProdListRV = (RecyclerView) findViewById(R.id.ProductsFromSeller);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        sellerProdListRV.setLayoutManager(layoutManager);

        SellerProductListAdapter adapter = new SellerProductListAdapter(productData, this);
        adapter.setProductListAdapterCallbacks(this);
        sellerProdListRV.setAdapter(adapter);
    }

    private void FillupProductDetailViews() {
        final int currOption = mCurrentProductData.GetOptionSelection();
        final ProductDetailData.Product productStaticData = mCurrentProductData.GetProductDetailData();

        // Product Title
        String title;
        if (productStaticData.GetManufacturerName() != null)
            title = String.format("%s: %s", productStaticData.GetManufacturerName(), productStaticData.GetTitle());
        else
            title = productStaticData.GetTitle();
        TextView titleView = (TextView) findViewById(R.id.ProductName_DetailView);
        titleView.setText(title);

        // Product Image
        final ImageView imageView = (ImageView) findViewById(R.id.ProductImage_DetailView);
        String imgURL = productStaticData.GetImage();
        if (imgURL.compareTo("null") != 0)
            imageDownloader.download(imgURL, imageView);
        else
            imageView.setImageResource(R.drawable.fazmart_new_logo);

        // Product Options
        final TextView optionsMenu = (TextView) findViewById(R.id.ProductOptions_DetailView);
        optionsMenu.setText(productStaticData.GetPrimaryOptionName(currOption));

        optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(mContext, v);
                int optionCnt = productStaticData.GetPrimaryOptionsCount();
                for (int i = 0; i < optionCnt; i++)
                    popup.getMenu().add(1, i, i, productStaticData.GetPrimaryOptionName(i));
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int optionIdx = item.getItemId();

                        // Show selected option
                        optionsMenu.setText(productStaticData.GetPrimaryOptionName(optionIdx));
                        mCurrentProductData.SetOptionSelection(optionIdx);

                        UpdateOptionBasedViews();

                        // Replace existing image with option image, if one exists
                        String imgURL = productStaticData.GetPrimaryOptionImage(optionIdx);
                        if (imgURL != null) {
                            imageDownloader.download(imgURL, imageView);
                        }

                        return true;
                    }
                });
            }
        });

        // Set default quantity to 0
        final TextView quantity = (TextView) findViewById(R.id.QuantityInCart);
        mQuantity = mCurrentProductData.GetQuantityInCart(mCurrentProductData.GetOptionSelection());
        quantity.setText(String.format("%d in Cart", mQuantity));

        // Handle Quantity Increment/Decrement
        TextView qntyDecr = (TextView) findViewById(R.id.QtyDecr);
        qntyDecr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQuantity > 0)
                    --mQuantity;

                //quantity.setText(String.valueOf(mQuantity));
                mProductDetailDataController.RemoveFromCart(currOption);
            }
        });

        TextView qntyIncr = (TextView) findViewById(R.id.QtyIncr);
        qntyIncr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuantity++;
                //quantity.setText(String.valueOf(mQuantity));

                mProductDetailDataController.AddToCart(currOption);
            }
        });

        // Handle Add To Wish List
        final LinearLayout addToWishList = (LinearLayout) findViewById(R.id.AddToWishList_DetailView);
        final ImageView addToWishListImage = (ImageView) findViewById(R.id.AddToWishListImage_DetailView);
        if (!mCurrentProductData.IsAddedToWishList()) {
            addToWishListImage.setImageResource(R.drawable.wishlist_32);
        } else {
            addToWishListImage.setImageResource(R.drawable.wishlist_32_purple);
        }
        addToWishList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg;
                if (mCurrentProductData.IsAddedToWishList()) {
                    mCurrentProductData.RemoveFromWishList();
                    addToWishListImage.setImageResource(R.drawable.wishlist_32);
                    msg = String.format("\"%s\" - removed from Wish List", productStaticData.GetTitle());
                } else {
                    mCurrentProductData.AddToWishList();
                    addToWishListImage.setImageResource(R.drawable.wishlist_32_purple);
                    msg = String.format("\"%s\" - added to Wish List", productStaticData.GetTitle());
                }

                // Show added-to/removed-from wish list message
                Toast toast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
                LinearLayout toastLayout = (LinearLayout) toast.getView();
                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                toastTV.setTextSize(18);
                toast.show();
            }
        });

        UpdateOptionBasedViews();
    }

    void UpdateOptionBasedViews () {
        int currOption = mCurrentProductData.GetOptionSelection();
        ProductDetailData.Product productStaticData = mCurrentProductData.GetProductDetailData();

        // Product Prices
        final TextView oldPriceTV = (TextView) findViewById(R.id.ProductOldPrice_DetailView);
        final TextView currentPriceTV = (TextView) findViewById(R.id.ProductCurrentPrice_DetailView);
        final TextView savingsTV = (TextView) findViewById(R.id.ProductSavings_DetailView);
        if (productStaticData.GetPrimaryOptionSpecialPrice(currOption) != null) {
            // Set Old Price
            oldPriceTV.setPaintFlags(oldPriceTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            oldPriceTV.setText(productStaticData.GetPrimaryOptionPrice(currOption));

            // Set Special Price
            currentPriceTV.setText(productStaticData.GetPrimaryOptionSpecialPrice(currOption));

            // Set Savings
            Double savedAmt = Double.parseDouble(productStaticData.GetPrimaryOptionPrice(currOption).substring(1).replace(",", "")) -
                    Double.parseDouble(productStaticData.GetPrimaryOptionSpecialPrice(currOption).substring(1).replace(",", ""));
            savingsTV.setText(String.format("(save \u20B9 %.2f)", savedAmt));
        } else {
            // Set current price
            currentPriceTV.setText(productStaticData.GetPrimaryOptionPrice(currOption));
        }
    }

    public void onEvent(EventItemAddedToCart evt) {
        //mCurrentProductData.UpdateQuantityInCart(optIdx, qty);
        mProductDetailDataController.GetCurrentProductData().UpdateQuantityInCart(evt.GetProdOptValId(), evt.GetQuantity());

        TextView qntyInCart = (TextView) findViewById(R.id.QuantityInCart);

        int qty = mCurrentProductData.GetQuantityInCart(mCurrentProductData.GetOptionSelection());
        if (qty != 0)
            qntyInCart.setText(String.format("%d in Cart", qty));
    }

    // Callback function for handling product selections from the Seller's product list
    public void onProductSelected (int position) {
        if (position < (mSellerProdList.length - 1)) {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra(IntentActionStrings.PRODUCT_ID, mSellerProdList[position].GetProductId());
            intent.putExtra(IntentActionStrings.OPTION_SELECTION, 0);
            intent.putExtra(IntentActionStrings.SELLER_ID, mSellerId);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, SellerProfileActivity.class);
            intent.putExtra(IntentActionStrings.SELLER_ID, mSellerId);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }
}
