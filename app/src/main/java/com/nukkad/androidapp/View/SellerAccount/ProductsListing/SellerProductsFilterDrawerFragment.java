package com.nukkad.androidapp.View.SellerAccount.ProductsListing;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nukkad.androidapp.Model.SellerAccountData.ProductFilterRange;
import com.nukkad.androidapp.Model.SellerAccountData.ProductStatus;
import com.nukkad.androidapp.R;
import com.nukkad.androidapp.View.Common.ScrimInsetsFrameLayout;
import com.nukkad.androidapp.View.Common.UnrollListView;
import com.nukkad.androidapp.Utilities.RangeSeekBar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by poliveira on 24/10/2014.
 */
public class SellerProductsFilterDrawerFragment extends Fragment{
    public static final String ORDER_STATES = "order_states";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREFERENCES_FILE = "my_app_settings"; //TODO: change this to your file

    private ProductsFilterDrawerCallbacks mCallbacks;
    private View mFragmentContainerView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle=null;
    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;
    private int mCurrentSelectedPosition;

    public class Filters {
        //int sortFieldSelectPos;
        String sortFieldName;
        boolean[] productStatusFilters;
        int productStatusFiltersCheckedCnt; // This is used to handle the all-checked scenario
        ProductFilterRange.Model filterRanges;

        public void SetFilterRanges (ProductFilterRange.Model _filterRanges) {
            filterRanges = _filterRanges;
        }
    }

    Filters mFilters;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_products_filter_drawer, container, false);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserLearnedDrawer = Boolean.valueOf(readSharedSetting(getActivity(), PREF_USER_LEARNED_DRAWER, "false"));
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        } else
            mCurrentSelectedPosition = 0;

        mFilters = new Filters();
        mFilters.SetFilterRanges((new ProductFilterRange()).GetNewFilterRangeModelObj());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (ProductsFilterDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement FilterDrawerCallbacks.");
        }
    }

    ListView mSortFieldsList = null;
    public void setup(int fragmentId, DrawerLayout drawerLayout, String[] sortFields, ProductStatus.Model productStates,
                      ProductFilterRange.Model filterRange) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        if(mFragmentContainerView.getParent() instanceof ScrimInsetsFrameLayout){
            mFragmentContainerView = (View) mFragmentContainerView.getParent();
        }
        mDrawerLayout = drawerLayout;

        ListView statusFilter = (ListView)getActivity().findViewById(R.id.StatusFilterList);
        StatusFilterAdapter statusFilterAdapter = new StatusFilterAdapter(productStates.GetStatusNames());
        statusFilter.setAdapter(statusFilterAdapter);
        UnrollListView.setListViewHeightBasedOnItems(statusFilter);

        mSortFieldsList = (ListView)getActivity().findViewById(R.id.SortFieldList);
        SortFieldListAdapter adapter = new SortFieldListAdapter(sortFields);
        mSortFieldsList.setAdapter(adapter);
        UnrollListView.setListViewHeightBasedOnItems(mSortFieldsList);

        // Initialize filters
        mFilters.sortFieldName = null;
        int numStates = productStates.GetStatusCount();
        mFilters.productStatusFilters = new boolean[numStates];
        for (int i = 0; i < numStates; i++) {
            mFilters.productStatusFilters[i] = false;
        }
        mFilters.productStatusFiltersCheckedCnt = 0;
        mFilters.filterRanges.SetMinPrice(filterRange.GetMinPrice());
        mFilters.filterRanges.SetMaxPrice(filterRange.GetMaxPrice());
        mFilters.filterRanges.SetMinSales(filterRange.GetMinSales());
        mFilters.filterRanges.SetMaxSales(filterRange.GetMaxSales());
        mFilters.filterRanges.SetMinEarnings(filterRange.GetMinEarnings());
        mFilters.filterRanges.SetMaxEarnings(filterRange.GetMaxEarnings());
        mFilters.filterRanges.SetMinQuantity(filterRange.GetMinQuantity());
        mFilters.filterRanges.SetMaxQuantity(filterRange.GetMaxQuantity());
        mFilters.filterRanges.SetMinDate(filterRange.GetMinDate());
        mFilters.filterRanges.SetMaxDate(filterRange.GetMaxDate());

        // Setup the Price range seek bar
        RangeSeekBar<Double> priceRangeSeekBar = (RangeSeekBar<Double>)getActivity().findViewById(R.id.PriceSeekbar);
        double minPrice = mFilters.filterRanges.GetMinPrice();
        double maxPrice = mFilters.filterRanges.GetMaxPrice();
        priceRangeSeekBar.setRangeValues(minPrice, maxPrice);
        priceRangeSeekBar.showThumbLabels(false);
        String range = String.format("\u20B9%.2f - \u20B9%.2f", minPrice, maxPrice);
        ((TextView)getActivity().findViewById(R.id.PriceRange)).setText(range);
        priceRangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Double>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Double minValue, Double maxValue) {
                // handle changed range values
                mFilters.filterRanges.SetMinPrice(minValue);
                mFilters.filterRanges.SetMaxPrice(maxValue);

                String range = String.format("\u20B9%.2f - \u20B9%.2f", minValue, maxValue);
                ((TextView) getActivity().findViewById(R.id.PriceRange)).setText(range);

                mCallbacks.onFilterSelectionChanged(mFilters);
            }
        });

        // Setup the Price range seek bar
        RangeSeekBar<Integer> salesRangeSeekBar = (RangeSeekBar<Integer>)getActivity().findViewById(R.id.SalesSeekbar);
        int minSales = mFilters.filterRanges.GetMinSales();
        int maxSales = mFilters.filterRanges.GetMaxSales();
        salesRangeSeekBar.setRangeValues(minSales, maxSales);
        salesRangeSeekBar.showThumbLabels(false);
        range = String.format("%d - %d", minSales, maxSales);
        ((TextView)getActivity().findViewById(R.id.SalesRange)).setText(range);
        salesRangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                // handle changed range values
                mFilters.filterRanges.SetMinSales(minValue);
                mFilters.filterRanges.SetMaxSales(maxValue);

                String range = String.format("%d - %d", minValue, maxValue);
                ((TextView) getActivity().findViewById(R.id.SalesRange)).setText(range);

                mCallbacks.onFilterSelectionChanged(mFilters);
            }
        });

        // Setup the Earnings range seek bar
        RangeSeekBar<Double> earningsRangeSeekBar = (RangeSeekBar<Double>)getActivity().findViewById(R.id.EarningsSeekbar);
        double minEarnings = mFilters.filterRanges.GetMinEarnings();
        double maxEarnings = mFilters.filterRanges.GetMaxEarnings();
        earningsRangeSeekBar.setRangeValues(minEarnings, maxEarnings);
        earningsRangeSeekBar.showThumbLabels(false);
        range = String.format("\u20B9%.2f - \u20B9%.2f", minEarnings, maxEarnings);
        ((TextView)getActivity().findViewById(R.id.EarningsRange)).setText(range);
        earningsRangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Double>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Double minValue, Double maxValue) {
                // handle changed range values
                mFilters.filterRanges.SetMinEarnings(minValue);
                mFilters.filterRanges.SetMaxEarnings(maxValue);

                String range = String.format("\u20B9%.2f - \u20B9%.2f", minValue, maxValue);
                ((TextView) getActivity().findViewById(R.id.EarningsRange)).setText(range);

                mCallbacks.onFilterSelectionChanged(mFilters);
            }
        });


        /*Date minDate = null;
        Date maxDate = null;
        try {
            minDate = new SimpleDateFormat("dd/MM/yyyy").parse(mFilters.filterRanges.GetMinDate());
            maxDate = new SimpleDateFormat("dd/MM/yyyy").parse(mFilters.filterRanges.GetMaxDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        RangeSeekBar<Long> dateRangeSeekBar = (RangeSeekBar<Long>)getActivity().findViewById(R.id.DateSeekbar);
        dateRangeSeekBar.setRangeValues(minDate.getTime(), maxDate.getTime());
        dateRangeSeekBar.showThumbLabels(false);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        range = String.format("%s\t-\t%s", sdf.format(new Date(minDate.getTime())), sdf.format(new Date(maxDate.getTime())));
        ((TextView)getActivity().findViewById(R.id.DateRange)).setText(range);

        dateRangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Long>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Long minValue, Long maxValue) {
                // handle changed range values
                mFilters.filterRanges.SetMinDate((new Date(minValue).toString()));
                mFilters.filterRanges.SetMaxDate((new Date(maxValue).toString()));

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String range = String.format("%s\t-\t%s", sdf.format(new Date(minValue)), sdf.format(new Date(maxValue)));
                ((TextView)getActivity().findViewById(R.id.DateRange)).setText(range);

                mCallbacks.onFilterSelectionChanged(mFilters);
            }
        });*/

        mDrawerLayout.setDrawerListener((new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (!isAdded()) return;
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (!isAdded()) return;
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        }));
    }

    public class SortFieldListAdapter extends BaseAdapter {
        public static final String ORDER_ID = "order_id";
        String[] mSortFieldList;

        public SortFieldListAdapter(String[] list) { mSortFieldList = list; }

        public int getCount() {
            if (mSortFieldList != null)
                return mSortFieldList.length;
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
            final View listItem;
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                listItem = inflater.inflate(R.layout.order_sort_field_list_item, null);
            } else {
                listItem = (View) convertView;
            }

            final CheckedTextView sortFieldTV = (CheckedTextView)listItem.findViewById(android.R.id.text1);
            sortFieldTV.setText(String.valueOf(mSortFieldList[position]));
            listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mFilters.sortFieldName == null) {
                        mFilters.sortFieldName = mSortFieldList[position];
                        mSortFieldsList.setItemChecked(position, true);
                        mCallbacks.onFilterSelectionChanged(mFilters);
                    }
                    else if (mSortFieldList[position].compareTo(mFilters.sortFieldName) != 0) {
                            mFilters.sortFieldName = mSortFieldList[position];
                            mSortFieldsList.setItemChecked(position, true);
                            mCallbacks.onFilterSelectionChanged(mFilters);
                    }
                    if (mDrawerLayout != null)
                        mDrawerLayout.closeDrawer(mFragmentContainerView);
                }
            });

            return listItem;
        }
    }

    public class StatusFilterAdapter extends BaseAdapter {
        public static final String ORDER_ID = "order_id";
        String[] mStatusList;

        public StatusFilterAdapter(String[] list) {
            mStatusList = list;
        }

        public int getCount() {
            if (mStatusList != null)
                return mStatusList.length;
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
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                listItem = inflater.inflate(R.layout.order_status_filter_list_item, null);
            } else {
                listItem = (View) convertView;
            }

            final CheckedTextView statusFilterTV = (CheckedTextView) listItem.findViewById(android.R.id.text1);
            statusFilterTV.setText(String.valueOf(mStatusList[position]));
            statusFilterTV.setChecked(false);

            listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Toggle the check box
                    if (statusFilterTV.isChecked()) {
                        statusFilterTV.setChecked(false);
                        mFilters.productStatusFiltersCheckedCnt--;
                    }
                    else {
                        statusFilterTV.setChecked(true);
                        mFilters.productStatusFiltersCheckedCnt++;
                    }

                    mFilters.productStatusFilters[position] = statusFilterTV.isChecked();
                    mCallbacks.onFilterSelectionChanged(mFilters);
                }
            });

            return listItem;
        }
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(mFragmentContainerView);
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(mFragmentContainerView);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mActionBarDrawerToggle != null)
            mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    public void setDrawerLayout(DrawerLayout drawerLayout) {
        mDrawerLayout = drawerLayout;
    }

    public static void saveSharedSetting(Context ctx, String settingName, String settingValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }

    public static String readSharedSetting(Context ctx, String settingName, String defaultValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(settingName, defaultValue);
    }

    /**
     * Sets ListView height dynamically based on the height of the items.
     *
     * @param listView to be resized
     * @return true if the listView is successfully resized, false otherwise
     */
    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }

    }
}
