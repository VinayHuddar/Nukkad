package com.nukkad.androidapp.Controller;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nukkad.androidapp.Common.APIService;
import com.nukkad.androidapp.Common.CommonDefinitions;
import com.nukkad.androidapp.Model.SellerData.SellerReviewsData;
import com.nukkad.androidapp.R;
import com.nukkad.androidapp.View.TestReviewData;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by vinayhuddar on 05/08/15.
 */
public class SellerReviewListManager {
    Activity mActivity;
    View mParentLayout;
    APIService mAPIService;

    ReviewListAdapter mReviewListAdapter;
    ReviewListScrollListener mScrollListener;
    SellerReviewsData mSellerReviewsData;

    public SellerReviewListManager(int sellerId, final Activity parentActivity, View parentLayout, APIService apiService) {
        mActivity = parentActivity;
        mParentLayout = parentLayout;
        mAPIService = apiService;

        GetReviews (sellerId, 1);
    }

    int retryCntGetReviews = 0;
    void GetReviews (final int sellerId, final int pageNum) {
        mAPIService.GetSellerReviews(sellerId, pageNum, new Callback<SellerReviewsData>() {
            @Override
            public void success(SellerReviewsData sellerReviewsData, Response response) {
                mSellerReviewsData = sellerReviewsData;

                boolean appendData = pageNum > 1;
                if (!appendData) {
                    mParentLayout.findViewById(R.id.loading_message).setVisibility(View.GONE);

                    mReviewListAdapter = new ReviewListAdapter(mSellerReviewsData.GetReviews());
                    ListView reviewListView = (ListView)mParentLayout.findViewById(R.id.SellerReviewsList);
                    reviewListView.setAdapter(mReviewListAdapter);

                    reviewListView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            ((ScrollView) mActivity.findViewById(R.id.SellerProfileScrollView)).requestDisallowInterceptTouchEvent(true);
                            return false;
                        }
                    });
                    reviewListView.setVisibility(View.VISIBLE);

                    mScrollListener = new ReviewListScrollListener(sellerId);
                    reviewListView.setOnScrollListener(mScrollListener);

                    //mScrollListener.SetTotalProductCount(sellerReviewsData.GetTotalReviewsCount());
                    mScrollListener.SetTotalProductCount(sellerReviewsData.GetReviews().length);
                } else {
                    mReviewListAdapter.GrowList(sellerReviewsData.GetReviews());
                }

                retryCntGetReviews = 0;
            }

            @Override
            public void failure(RetrofitError error) {
                retryCntGetReviews++;
                if (retryCntGetReviews < CommonDefinitions.RETRY_COUNT)
                    GetReviews(sellerId, pageNum);
                else
                    retryCntGetReviews = 0;

            }
        });
    }

    class ReviewListAdapter  extends ArrayAdapter<SellerReviewsData.Review> {
        ArrayList<SellerReviewsData.Review> mReviewList;

        public ReviewListAdapter(SellerReviewsData.Review[] reviewList) {
            super(mActivity, R.id.SellerReviewsList);
            mReviewList = new ArrayList<SellerReviewsData.Review>(Arrays.asList(reviewList));
        }

        public void GrowList (SellerReviewsData.Review[] newReviews) {
            for (int i = 0; i < newReviews.length; i++)
                mReviewList.add(newReviews[i]);

            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            final View reviewItem;
            if (convertView == null) {
                reviewItem = inflater.inflate(R.layout.layout_review, null);
            } else {
                reviewItem = (View) convertView;
            }

            TextView reviewerName = (TextView) reviewItem.findViewById(R.id.ReviewerName);
            reviewerName.setText(mReviewList.get(position).GetReviewerName());

            TextView reviewDate = (TextView) reviewItem.findViewById(R.id.ReviewDate);
            reviewDate.setText(mReviewList.get(position).GetReviewDate());

            int rating = mReviewList.get(position).GetRating();
            Drawable greyStar = mActivity.getResources().getDrawable(R.drawable.rating_16);
            Drawable purpleStar = mActivity.getResources().getDrawable(R.drawable.rating_16_purple);
            for (int i = 0; i < 5; i++, rating--) {
                ImageView starIV = null;
                switch (i) {
                    case 0:
                        starIV = (ImageView) reviewItem.findViewById(R.id.Star1);
                        break;
                    case 1:
                        starIV = (ImageView) reviewItem.findViewById(R.id.Star2);
                        break;
                    case 2:
                        starIV = (ImageView) reviewItem.findViewById(R.id.Star3);
                        break;
                    case 3:
                        starIV = (ImageView) reviewItem.findViewById(R.id.Star4);
                        break;
                    case 4:
                        starIV = (ImageView) reviewItem.findViewById(R.id.Star5);
                        break;
                }
                if (rating > 0)
                    starIV.setImageDrawable(purpleStar);
                else
                    starIV.setImageDrawable(greyStar);
            }

            TextView comment = (TextView) reviewItem.findViewById(R.id.ReviewComment);
            comment.setText(mReviewList.get(position).GetComment());

            return reviewItem;
        }

        public int getCount() {
            return mReviewList.size();
        }

        public SellerReviewsData.Review getItem(int position) {
            return (mReviewList.get(position));
        }

        public long getItemId(int position) {
            return position;
        }
    }

    public class ReviewListScrollListener implements AbsListView.OnScrollListener {
        final int SCROLL_LISTENER_THRESHOLD = 8;

        // how many entries earlier to start loading next page
        private int currentPage = 1;
        private int previousTotal = 0;
        private boolean loading = true;
        private int mTotalReviewCount = 0;

        private int mSellerId = 0;

        public ReviewListScrollListener(int sellerId) {
            mSellerId = sellerId;
        }

        public void SetTotalProductCount (int cnt) {
            mTotalReviewCount = cnt;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    currentPage++;
                }
            }
            //if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + SCROLL_LISTENER_THRESHOLD)) {
            if (!loading && (firstVisibleItem  >= (totalItemCount - SCROLL_LISTENER_THRESHOLD)) && (totalItemCount < mTotalReviewCount)) {
                loading = true;

                GetReviews(mSellerId, currentPage);
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {}
    }

}
