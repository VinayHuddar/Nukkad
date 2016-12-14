package com.nukkad.androidapp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;
import com.digits.sdk.android.Digits;
import com.nukkad.androidapp.Common.ApplicationSettings;
import com.nukkad.androidapp.Model.AppSettingsData;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import io.fabric.sdk.android.Fabric;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

import com.nukkad.androidapp.Common.AuthenticationData;
import com.nukkad.androidapp.Common.APIService;
import com.nukkad.androidapp.Common.CommonDefinitions;
import com.nukkad.androidapp.Model.TokenResponse;
import com.nukkad.androidapp.Model.UserAccountData;
import com.nukkad.androidapp.View.Common.NavDrawerItemList;
import com.squareup.okhttp.OkHttpClient;

/**
 * Created by Vinay on 07-06-2015.
 */
public class FazmartApplication extends Application {
    private static final String PREFERENCES_FILE = "my_app_settings"; //TODO: change this to your file

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "rHESkVmE35i89v2IhppL3IkA7";
    private static final String TWITTER_SECRET = "z7Hh05m6EQRpDL4jlsME16PEHCxMuyzh0iUy1lXEBob1OYuunN";

    private static AuthenticationData mAuthenticationData = null;
    private RestAdapter mRestAdapter;
    private static APIService mAPIService;
    private String Url = "http://ec2-54-173-60-3.compute-1.amazonaws.com/Nukkad/api/v1";

    private Context mApplCtxt;

    public static APIService GetAPIService () {
        return mAPIService;
    }

    public void onCreate() {
        //PsiMethod:onCreateFabric.with(this, new Crashlytics());
        try {
            TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
            Fabric.with(this, new Crashlytics(), new TwitterCore(authConfig), new Digits());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mApplCtxt = this;
        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint(Url)
                .build();
        mAPIService = mRestAdapter.create(APIService.class);

        mAuthenticationData = AuthenticationData.GetInstance();

        /*if (UserAccountData.GetInstance().GetAccountData(this) != null) {
            mAuthenticationData.SetAuthenticationToken(UserAccountData.GetInstance().GetAuthenticationToken(this));
            InitAppSettings();
        }
        else*/
            FetchAuthKeyAndInitApp();
    }

    private int fetchAuthKeyRetryCnt = 0;
    void FetchAuthKeyAndInitApp() {
        //AuthenticationData authData = AuthenticationData.GetInstance();
        String strAuthHdr = "Basic ZGV2aWNlY2xpZW50MzAxMjpyYWplc2g5OTg2MDE4NjQzYW5pdGhhOTk4NjAwNjA2Mw=="; // Base64 encoded string for "deviceclient3012:rajesh9986018643anitha9986006063"
        //mAuthenticationData.SetAuthenticationToken("5gippKykpq974YfkwVHu9cQr8JFhVJHDwxQy2xYR"); //tokenResponse.GetAccessToken());
        //InitAppSettings();

        mAPIService.GetAuthenticationToken("application/json", strAuthHdr, "client_credentials", "", new Callback<TokenResponse>() {
            @Override
            public void success(TokenResponse tokenResponse, Response response) {
                mAuthenticationData.SetAuthenticationToken(tokenResponse.GetAccessToken()); //"xoCJMhySMnAsllStM59P42WI6INB4B5jyHToelFl"); //

                InitAppSettings();
            }

            @Override
            public void failure(RetrofitError error) {
                fetchAuthKeyRetryCnt++;
                if (fetchAuthKeyRetryCnt < CommonDefinitions.RETRY_COUNT)
                    FetchAuthKeyAndInitApp();
            }
        });
    }

    void InitAppSettings() {
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestInterceptor.RequestFacade request) {
                request.addHeader("Accept", "application/json");
                request.addHeader("Authorization", "Bearer ".concat(mAuthenticationData.GetAuthenticationToken())); //mTokenResponse.GetTokenType()
            }
        };

        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint(Url)
                .setClient(new OkClient(new OkHttpClient()))
                .setRequestInterceptor(requestInterceptor)
                .build();
        mAPIService = mRestAdapter.create(APIService.class);

        FetchAppSettings();
    }

    private int fetchAppSettingsRetryCnt = 0;
    void FetchAppSettings () {
        /*mAPIService.GetAppSettings(new Callback<AppSettingsData>() {
            @Override
            public void success(AppSettingsData appSettingsData, Response response) {
                ApplicationSettings.GetInstance().Init(mApplCtxt);
                ApplicationSettings.GetInstance().SetApplSettings(appSettingsData);
                NavDrawerItemList.GetInstance().PrepareNavDrawerLists(getApplicationContext());

                Intent intent = new Intent(getApplicationContext(), LandingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void failure(RetrofitError error) {
                fetchAppSettingsRetryCnt++;
                if (fetchAppSettingsRetryCnt < CommonDefinitions.RETRY_COUNT)
                    FetchAuthKeyAndInitApp();
            }
        });*/

        String[] orderStates = new String[4];
        orderStates[0] = "Pending";
        orderStates[1] = "Shipped";
        orderStates[2] = "Completed";
        orderStates[3] = "Canceled";

        String[] sellerOrdersListSortFields = new String[3];
        sellerOrdersListSortFields[0] = "Status";
        sellerOrdersListSortFields[1] = "Date";
        sellerOrdersListSortFields[2] = "Total";

        String[] productStates = new String[2];
        productStates[0] = "Enabled";
        productStates[1] = "Disabled";

        String[] sellerProductsListSortFields = new String[4];
        sellerProductsListSortFields[0] = "Name";
        sellerProductsListSortFields[1] = "Price";
        sellerProductsListSortFields[2] = "Sales";
        sellerProductsListSortFields[3] = "Earnings";

        AppSettingsData appSettingsData = new AppSettingsData();
        appSettingsData.SetCRC(1234);
        appSettingsData.SetAppSettings(orderStates, sellerOrdersListSortFields, productStates, sellerProductsListSortFields);

        ApplicationSettings.GetInstance().Init(mApplCtxt);
        ApplicationSettings.GetInstance().SetApplSettings(appSettingsData);

        NavDrawerItemList.GetInstance().PrepareNavDrawerLists(getApplicationContext());

        if (CommonDefinitions.ENABLE_INTEGRATED_SERVICES_PAGE) {
            Intent intent = new Intent(getApplicationContext(), LandingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), LandingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
