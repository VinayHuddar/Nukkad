package com.nukkad.androidapp.View.SellerProfile;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nukkad.androidapp.Common.APIService;
import com.nukkad.androidapp.Common.CommonDefinitions;
import com.nukkad.androidapp.Common.IntentActionStrings;
import com.nukkad.androidapp.Controller.Common.ImageDownloader;
import com.nukkad.androidapp.Controller.ProductDetailDataController;
import com.nukkad.androidapp.Controller.ProductList.ProductListScrollListenerCallback;
import com.nukkad.androidapp.Controller.ProductList.ProductListViewHelper;
import com.nukkad.androidapp.Controller.SellerReviewListManager;
import com.nukkad.androidapp.FazmartApplication;
import com.nukkad.androidapp.Model.ProductData.ProductData;
import com.nukkad.androidapp.Model.SellerData.SellerProductListData;
import com.nukkad.androidapp.Model.SellerAccountData.SellerInfo;
import com.nukkad.androidapp.R;
import com.nukkad.androidapp.View.Common.SlidingTabLayout;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class SellerProfileActivity extends AppCompatActivity {

    SellerInfoPagerAdapter mSellerInfoPagerAdapter;
    ViewPager mViewPager;

    int mSellerId;
    SellerInfo mSellerInfo;
    ProductData[] mProductList;
    ImageDownloader mImageDownloader;
    boolean mFromProductDetailView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_profile);

        mImageDownloader = new ImageDownloader();

        mSellerId = getIntent().getIntExtra(IntentActionStrings.SELLER_ID, CommonDefinitions.INVALID_ID);
        mFromProductDetailView = getIntent().getBooleanExtra(IntentActionStrings.FROM_PRODUCT_DETAIL_VIEW, false);

        if (mFromProductDetailView == true) {
            mSellerInfo = ProductDetailDataController.GetInstance().GetCurrentProductSellerInfo();
            PopulateViews();
        }
        else {
            //findViewById(R.id.loading_message).setVisibility(View.VISIBLE);
            FetchSellerInfo(mSellerId);
        }
    }

    void PopulateViews () {
        ImageView sellerCompanyImage = (ImageView)findViewById(R.id.SellerCompanyImage);
        if (mSellerInfo.GetBanner() != null)
            mImageDownloader.download(mSellerInfo.GetBanner(), sellerCompanyImage);
        else
            sellerCompanyImage.setImageResource(R.drawable.fazmart_new_logo);

        ImageView sellerImage = (ImageView)findViewById(R.id.SellerImage);
        if (mSellerInfo.GetSellerImage() != null)
            mImageDownloader.download(mSellerInfo.GetSellerImage(), sellerImage);
        else
            sellerImage.setImageResource(R.drawable.fazmart_new_logo);

        TextView sellerName = (TextView)findViewById(R.id.SellerName);
        sellerName.setText(mSellerInfo.GetName());

        TextView sellerLocation = (TextView)findViewById(R.id.SellerAddress);
        sellerLocation.setText(mSellerInfo.GetCompany());

        // About
        TextView aboutTV = (TextView)findViewById(R.id.AboutSeller);
        aboutTV.setText(mSellerInfo.GetDescription());

        // Setup the Pager
        mSellerInfoPagerAdapter = new SellerInfoPagerAdapter (getSupportFragmentManager(), this);
        mViewPager = (ViewPager)findViewById(R.id.SellerInfoPager);
        mViewPager.setAdapter(mSellerInfoPagerAdapter);
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

                return false;
            }
        });

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.SlidingTabs);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);
    }

    int mFetchSellerInfoRetryCnt = 0;
    public void FetchSellerInfo(final int sellerId) {
        FazmartApplication.GetAPIService().GetSellerInfo(sellerId, new Callback<SellerInfo>() {
            @Override
            public void success(SellerInfo sellerInfo, Response response) {
                mSellerInfo = sellerInfo;
                findViewById(R.id.loading_message).setVisibility(View.INVISIBLE);
                findViewById(R.id.ProductList).setVisibility(View.VISIBLE);
                PopulateViews();

                mFetchSellerInfoRetryCnt = 0;
            }

            @Override
            public void failure(RetrofitError error) {
                mFetchSellerInfoRetryCnt++;
                if (mFetchSellerInfoRetryCnt < 5)
                    FetchSellerInfo(sellerId);
                else
                    mFetchSellerInfoRetryCnt = 0;
            }
        });
    }

    public class SellerInfoPagerAdapter extends FragmentPagerAdapter {
        Context mContext;

        public SellerInfoPagerAdapter(FragmentManager fm, Context context) {
            super(fm);

            mContext = context;
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;

            Bundle args = new Bundle();
            args.putInt(IntentActionStrings.SELLER_ID, mSellerId);

            switch(i) {
                case 0:
                    args.putBoolean(IntentActionStrings.FROM_PRODUCT_DETAIL_VIEW, mFromProductDetailView);
                    fragment = new ProductsListFragment();
                    break;

                case 1:
                    fragment = new ReviewsFragment();
                    break;
            }

            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return (position == 0 ? "Products" : "Reviews");
        }
    }

    public static class ProductsListFragment extends Fragment implements ProductListScrollListenerCallback {
        APIService mAPIService;
        ProductListViewHelper mProductListViewHelper;

        //ProductListScrollListener mScrollListener = null;
        SellerInfo mSellerInfo;
        int mSellerId;
        View mRootView;


        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            mRootView = inflater.inflate(
                    R.layout.fragment_seller_products_list, container, false);

            // Setup the ScrollListener
            mAPIService = ((FazmartApplication)getActivity().getApplication()).GetAPIService();
            ListView productListView = (ListView) mRootView.findViewById(R.id.ListViewOfProducts);
            mProductListViewHelper = new ProductListViewHelper(productListView, this, getActivity(),
                    CommonDefinitions.SELLER_PRODS_CATEGORY_ID);
            mProductListViewHelper.ResetDetailViewInitiated();

            mSellerId = getArguments().getInt(IntentActionStrings.SELLER_ID);

            productListView.setOnTouchListener(new View.OnTouchListener() {
                // Setting on Touch Listener for handling the touch inside ScrollView
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // How far the user has to scroll before it locks the parent vertical scrolling.
                    ((ScrollView) getActivity().findViewById(R.id.SellerProfileScrollView)).requestDisallowInterceptTouchEvent(true);

                    return false;
                }
            });

            boolean fromProductDetailView = getArguments().getBoolean(IntentActionStrings.FROM_PRODUCT_DETAIL_VIEW);
            if (fromProductDetailView) {
                ProductDetailDataController detailDataController = ProductDetailDataController.GetInstance();
                mProductListViewHelper.DisplayList(1, detailDataController.GetSellerProducts(),
                        detailDataController.GetCurrentProductSellerInfo().GetTotalProductCount());

                SetupAttributeSpinner ();
            }
            else
                GetSellerProducts(mSellerId, 1, null);

            return mRootView;
        }

        public void OnScrolledToEnd (int nextPageNum) {
            GetSellerProducts(mSellerId, nextPageNum, null);
        }

        int retryCntGetSellerProds = 0;
        public void GetSellerProducts (final int sellerId, final int pageNum, final Map<String, String> queries) {
            // Fetch Product Data from the server
            FazmartApplication.GetAPIService().GetSellerProducts(sellerId, pageNum, queries,
                    new Callback<SellerProductListData>() {
                        @Override
                        public void success(SellerProductListData products, Response response) {
                            //RenderSellerProductList(products, pageNum);
                            mRootView.findViewById(R.id.loading_message).setVisibility(View.GONE);
                            mRootView.findViewById(R.id.ListViewOfProducts).setVisibility(View.VISIBLE);

                            mProductListViewHelper.DisplayList(pageNum, products.GetProductsData(), products.GetProductCount());
                            SetupAttributeSpinner();

                            retryCntGetSellerProds = 0;
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            retryCntGetSellerProds++;
                            if (retryCntGetSellerProds < CommonDefinitions.RETRY_COUNT)
                                GetSellerProducts(sellerId, pageNum, queries);
                            else
                                retryCntGetSellerProds = 0;
                        }
                    });
        }

        void SetupAttributeSpinner () {
                final String[] attributeList = mProductListViewHelper.GetAttributeList(CommonDefinitions.SELLER_PRODS_CATEGORY_ID);

            FloatingActionButton attribFilterFAB = (FloatingActionButton)mRootView.findViewById(R.id.AttributeFilterFAB);
            attribFilterFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(getActivity(), v);
                    int optionCnt = attributeList.length;
                    for (int i = 0; i < optionCnt; i++)
                        popup.getMenu().add(1, i, i, attributeList[i]);
                    popup.show();

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            mProductListViewHelper.onFilterDrawerItemSelected(item.getItemId(), CommonDefinitions.SELLER_PRODS_CATEGORY_ID);

                            return true;
                        }
                    });
                }
            });
        }
    }

    public static class ReviewsFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(
                    R.layout.fragment_seller_reviews_list, container, false);
            int sellerId = getArguments().getInt(IntentActionStrings.SELLER_ID);

            SellerReviewListManager sellerReviewListManager = new SellerReviewListManager(sellerId, getActivity(), rootView, ((FazmartApplication)getActivity().getApplicationContext()).GetAPIService());

            return rootView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_seller_profile, menu);
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
}
