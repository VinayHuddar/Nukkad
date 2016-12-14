package com.nukkad.androidapp.Model.SellerAccountData;

import android.widget.TextView;

import com.nukkad.androidapp.Common.CommonDefinitions;
import com.nukkad.androidapp.FazmartApplication;
import com.nukkad.androidapp.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by vinayhuddar on 02/09/15.
 */
public class ProductFilterRange {
    Callback mCallback;

    public ProductFilterRange (Callback callback) {
        mCallback = callback;
    }

    public ProductFilterRange () {
        mCallback = null;
    }

    public class Model {
        DateFilter date_added;
        PriceFilter price;
        QuantityFilter quantity;
        SalesFilter sales;
        EarningsFilter earnings;

        public Model () {
            date_added = new DateFilter();
            price = new PriceFilter();
            quantity = new QuantityFilter();
            sales = new SalesFilter();
            earnings = new EarningsFilter();
        }

        public class PriceFilter {
            double min;
            double max;
        }

        public double GetMinPrice() { return price.min; }
        public void SetMinPrice(double _price) { price.min = _price; }
        public double GetMaxPrice() { return price.max; }
        public void SetMaxPrice(double _price) { price.max = _price; }

        public class DateFilter {
            String min;
            String max;
        }

        public String GetMinDate() { return date_added.min; }
        public void SetMinDate(String _date_added) { date_added.min =_date_added; }
        public String GetMaxDate() { return date_added.max; }
        public void SetMaxDate(String _date_added) { date_added.max =_date_added; }

        public class QuantityFilter {
            double min;
            double max;
        }

        public double GetMinQuantity() { return quantity.min; }
        public void SetMinQuantity(double qty) { quantity.min = qty; }
        public double GetMaxQuantity() { return quantity.max; }
        public void SetMaxQuantity(double qty) { quantity.max = qty; }

        public class SalesFilter {
            int min;
            int max;
        }

        public int GetMinSales() { return sales.min; }
        public void SetMinSales(int sold) { sales.min = sold; }
        public int GetMaxSales() { return sales.max; }
        public void SetMaxSales(int sold) { sales.max = sold; }

        public class EarningsFilter {
            double min;
            double max;
        }

        public double GetMinEarnings() { return earnings.min; }
        public void SetMinEarnings(double earning) { earnings.min = earning; }
        public double GetMaxEarnings() { return earnings.max; }
        public void SetMaxEarnings(double earning) { earnings.max = earning; }
    }

    public Model GetNewFilterRangeModelObj() {
        return (new Model());
    }

    int retryCount = 0;
    public void FetchData() {
        FazmartApplication.GetAPIService().GetSellerProductFilters(new retrofit.Callback<Model>() {
            @Override
            public void success(Model statusData, Response response) {
                if (mCallback != null)
                    mCallback.OnFilterRangeRecieved(statusData);

                retryCount = 0;
            }

            @Override
            public void failure(RetrofitError error) {
                // Attempt five retries before giving up
                retryCount++;
                if (retryCount < CommonDefinitions.RETRY_COUNT)
                    FetchData();
                else {
                    // Show a "Network Problem" message to user and take him to landing screen
                    retryCount = 0;
                        /*Toast.makeText(mActivity, "There seems to be some problem with the network - " +
                                "either you are not connected to the network or the network is too slow or ",
                                Toast.LENGTH_SHORT);
                        Intent intent = new Intent(mActivity, LandingActivity.class);
                        mActivity.startActivity(intent);*/
                }
            }
        });
    }

    public interface Callback {
        public void OnFilterRangeRecieved(Model statusData);
    }
}
