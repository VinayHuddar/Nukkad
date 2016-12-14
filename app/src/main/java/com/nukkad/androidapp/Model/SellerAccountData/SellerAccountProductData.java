package com.nukkad.androidapp.Model.SellerAccountData;

import com.nukkad.androidapp.Common.CommonDefinitions;
import com.nukkad.androidapp.FazmartApplication;

import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by vinayhuddar on 25/08/15.
 */
public class SellerAccountProductData {
    Callback mCallback;

    public SellerAccountProductData (Callback callback) {
        mCallback = callback;
    }

    public class Model {
        int product_total;
        Product[] products;

        public class Product {
            int id;
            String image;
            String name;
            double price;
            double special;
            int quantity;
            String status;
            int sales;
            double earnings;
            String date_added;

            public int GetProductId() {
                return id;
            }

            public String GetImage() {
                return image;
            }

            public String GetName() {
                return name;
            }

            public double GetPrice() {
                return price;
            }

            public double GetSpecial() {
                return special;
            }

            public int GetQuantity() {
                return quantity;
            }

            public String GetStatus() {
                return status;
            }

            public int GetSales() {
                return sales;
            }

            public double GetEarnings() {
                return earnings;
            }

            public String GetDateAdded() {
                return date_added;
            }
        }

        public int GetProductCount() {
            return product_total;
        }

        public Product[] GetProducts() {
            return products;
        }
    }

    int retryCount = 0;
    public void FetchData(final Map<String, String> productFilters) {
        FazmartApplication.GetAPIService().GetSellerAccountProducts(productFilters, new retrofit.Callback<Model>() {
            @Override
            public void success(Model data, Response response) {
                mCallback.OnProductDataRecieved(data);

                retryCount = 0;
            }

            @Override
            public void failure(RetrofitError error) {
                // Attempt five retries before giving up
                retryCount++;
                if (retryCount < CommonDefinitions.RETRY_COUNT)
                    FetchData(productFilters);
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
        public void OnProductDataRecieved (Model data);
    }

}
