package com.nukkad.androidapp.Common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nukkad.androidapp.FazmartApplication;
import com.nukkad.androidapp.Model.AppSettingsData;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Admin on 02-05-2015.
 */
public class ApplicationSettings {
    public static final String APPLICATION_SETTINGS_CRC = "application_settings_crc";
    public static final String SELLER_ORDERS_LIST_SORT_FIELDS = "seller_order_list_sort_fields";
    public static final String ORDER_STATES = "order_states";
    public static final String SELLER_PRODUCTS_LIST_SORT_FIELDS = "seller_product_list_sort_fields";
    public static final String PRODUCT_STATES = "product_states";

    private static ApplicationSettings instance = null;
    public static ApplicationSettings GetInstance () {
        if (instance == null) {
            instance = new ApplicationSettings();
        }
        return instance;
    }

    public void Init (Context appCtxt) {
        mApplContext = appCtxt;
        mPrefFile = PreferenceManager.getDefaultSharedPreferences(appCtxt);
    }

    Context mApplContext;
    SharedPreferences mPrefFile;
    public void SetApplSettings (AppSettingsData appSettingsData) {
        int storedCRC = mPrefFile.getInt(APPLICATION_SETTINGS_CRC, -1);
        // The following "if" construct must be enabed when the APIs are available
        //if ((storedCRC == -1) || (storedCRC != appSettingsData.GetCRC())) {
            SharedPreferences.Editor editor = mPrefFile.edit();

            editor.putInt(APPLICATION_SETTINGS_CRC, appSettingsData.GetCRC());

            WriteStringSet(editor, ORDER_STATES, appSettingsData.GetOrderStates());
            WriteStringSet(editor, SELLER_ORDERS_LIST_SORT_FIELDS, appSettingsData.GetSellerOrdersListSortFields());
            WriteStringSet(editor, PRODUCT_STATES, appSettingsData.GetProductStates());
            WriteStringSet(editor, SELLER_PRODUCTS_LIST_SORT_FIELDS, appSettingsData.GetSellerProductsListSortFields());

            editor.commit();
        //}
    }

    private void WriteStringSet (SharedPreferences.Editor editor, String key, String[] strArray) {
        Set<String> strSet = new HashSet<>();
        for (int i = 0; i < strArray.length; i++)
            strSet.add(strArray[i]);
        editor.putStringSet(key, strSet);
    }

    public String[] GetOrderStates () {
        return ReadStringSet(ORDER_STATES);
    }

    public String[] GetSellerOrderListSortFields () {
        return ReadStringSet(SELLER_ORDERS_LIST_SORT_FIELDS);
    }

    public String[] GetProductStates () {
        return ReadStringSet(PRODUCT_STATES);
    }

    public String[] GetSellerProductListSortFields () {
        return ReadStringSet(SELLER_PRODUCTS_LIST_SORT_FIELDS);
    }

    public String[] ReadStringSet (String key) {
        Set<String> strSet = mPrefFile.getStringSet(key, null);

        String[] strArray = null;
        if (strSet != null) {
            strArray = new String[strSet.size()];
            strSet.toArray(strArray);
        }
        return (strArray);
    }

}
