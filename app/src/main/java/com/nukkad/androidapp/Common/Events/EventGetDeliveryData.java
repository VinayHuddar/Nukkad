package com.nukkad.androidapp.Common.Events;

import com.nukkad.androidapp.Common.APIService;

/**
 * Created by Vinay on 08-06-2015.
 */
public class EventGetDeliveryData {
    int mDefaultAddressId;
    public EventGetDeliveryData(int defAddrId) {
        mDefaultAddressId = defAddrId;
    }

    public int GetDefaultAddressId () { return mDefaultAddressId; }
}
