package com.nukkad.androidapp.Model.SellerAccountData;

import com.nukkad.androidapp.Common.CommonDefinitions;
import com.nukkad.androidapp.FazmartApplication;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by vinayhuddar on 02/09/15.
 */
public class ProductStatus {
    Callback mCallback;

    public ProductStatus (Callback callback) {
        mCallback = callback;
    }

    public class Model {
        Status[] statuses;

        public class Status {
            int id;
            String name;
        }

        public int GetStatusCount () { return statuses.length; }

        public int GetStatusId (int i) { return statuses[i].id; }
        public String GetStatusName (int i) { return statuses[i].name; }
        public String[] GetStatusNames () {
            String[] names = new String [statuses.length];
            for (int i = 0; i < statuses.length; i++)
                names[i] = GetStatusName(i);

            return names;
        }
    }

    int retryCount = 0;
    public void FetchData() {
        FazmartApplication.GetAPIService().GetSellerProductStatusList(new retrofit.Callback<Model>() {
            @Override
            public void success(Model statusData, Response response) {
                mCallback.OnStatusRecieved(statusData);

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
        public void OnStatusRecieved(Model statusData);
    }

}
