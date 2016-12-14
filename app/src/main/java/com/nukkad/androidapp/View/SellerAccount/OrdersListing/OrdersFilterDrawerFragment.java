package com.nukkad.androidapp.View.SellerAccount.OrdersListing;

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
public class OrdersFilterDrawerFragment extends Fragment{
    public static final String ORDER_STATES = "order_states";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREFERENCES_FILE = "my_app_settings"; //TODO: change this to your file

    private OrdersFilterDrawerCallbacks mCallbacks;
    private View mFragmentContainerView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle=null;
    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;
    private int mCurrentSelectedPosition;

    private String[] mOrderStates;
    private String[] mSortFields;

    public class Filters {
        int sortFieldSelectPos;
        boolean[] orderStatusFilters;
        int orderStatusFiltersCheckedCnt; // This is used to handle the all-checked scenario
        long dateStart;
        long dateEnd;
        double minTotal;
        double maxTotal;
    }

    Filters mFilters;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders_filter_drawer, container, false);

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
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (OrdersFilterDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement FilterDrawerCallbacks.");
        }
    }

    ListView mSortFieldsList = null;
    public void setup(int fragmentId, DrawerLayout drawerLayout, String[] sortFields, String[] orderStates,
                      final double minTotal, double maxTotal) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        if(mFragmentContainerView.getParent() instanceof ScrimInsetsFrameLayout){
            mFragmentContainerView = (View) mFragmentContainerView.getParent();
        }
        mDrawerLayout = drawerLayout;

        mOrderStates = orderStates;
        ListView statusFilter = (ListView)getActivity().findViewById(R.id.StatusFilterList);
        StatusFilterAdapter statusFilterAdapter = new StatusFilterAdapter(orderStates);
        statusFilter.setAdapter(statusFilterAdapter);
        UnrollListView.setListViewHeightBasedOnItems(statusFilter);

        mSortFields = sortFields;
        mSortFieldsList = (ListView)getActivity().findViewById(R.id.SortFieldList);
        SortFieldListAdapter adapter = new SortFieldListAdapter(mSortFields);
        mSortFieldsList.setAdapter(adapter);
        UnrollListView.setListViewHeightBasedOnItems(mSortFieldsList);

        // Initialize filters
        mFilters.sortFieldSelectPos = -1;
        mFilters.orderStatusFilters = new boolean[orderStates.length];
        for (int i = 0; i < orderStates.length; i++) {
            mFilters.orderStatusFilters[i] = false;
        }
        mFilters.orderStatusFiltersCheckedCnt = 0;
        mFilters.dateStart = mFilters.dateEnd = 0;
        mFilters.minTotal = mFilters.maxTotal = 0;

        // Setup the Total range seek bar
        RangeSeekBar<Double> totalRangeSeekBar = (RangeSeekBar<Double>)getActivity().findViewById(R.id.TotalSeekbar);
        totalRangeSeekBar.setRangeValues(minTotal, maxTotal);
        totalRangeSeekBar.showThumbLabels(false);
        String range = String.format("\u20B9%.2f - \u20B9%.2f", minTotal, maxTotal);
        ((TextView)getActivity().findViewById(R.id.TotalRange)).setText(range);
        totalRangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Double>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Double minValue, Double maxValue) {
                // handle changed range values
                mFilters.minTotal = minValue;
                mFilters.maxTotal = maxValue;

                String range = String.format("\u20B9%.2f - \u20B9%.2f", minValue, maxValue);
                ((TextView)getActivity().findViewById(R.id.TotalRange)).setText(range);

                mCallbacks.onFilterSelectionChanged(mFilters);
            }
        });


        Date minDate = null;
        try {
            minDate = new SimpleDateFormat("yyyy-MM-dd").parse("2015-01-01");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date maxDate = new Date();
        RangeSeekBar<Long> dateRangeSeekBar = (RangeSeekBar<Long>)getActivity().findViewById(R.id.DateSeekbar);
        dateRangeSeekBar.setRangeValues(minDate.getTime(), maxDate.getTime());
        dateRangeSeekBar.showThumbLabels(false);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        range = String.format("%s\t-\t%s", sdf.format(new Date(minDate.getTime())), sdf.format(new Date(maxDate.getTime())));
        ((TextView)getActivity().findViewById(R.id.DateRange)).setText(range);

        dateRangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Long>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Long minValue, Long maxValue) {
                // handle changed range values
                mFilters.dateStart = minValue;
                mFilters.dateEnd = maxValue;

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                String range = String.format("%s\t-\t%s", sdf.format(new Date(minValue)), sdf.format(new Date(maxValue)));
                ((TextView)getActivity().findViewById(R.id.DateRange)).setText(range);

                mCallbacks.onFilterSelectionChanged(mFilters);
            }
        });


        //selectItem(mCurrentSelectedPosition);

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
                    if (position != mFilters.sortFieldSelectPos) {
                        mFilters.sortFieldSelectPos = position;
                        mSortFieldsList.setItemChecked(position, true);
                        if (mDrawerLayout != null)
                            mDrawerLayout.closeDrawer(mFragmentContainerView);

                        mCallbacks.onFilterSelectionChanged(mFilters);
                    }
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
                        mFilters.orderStatusFiltersCheckedCnt--;
                    }
                    else {
                        statusFilterTV.setChecked(true);
                        mFilters.orderStatusFiltersCheckedCnt++;
                    }

                    mFilters.orderStatusFilters[position] = statusFilterTV.isChecked();

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
