package com.nukkad.androidapp.Common.Events;

import android.app.usage.UsageEvents;

import com.nukkad.androidapp.Model.OrderSummaryData;

/**
 * Created by vinayhuddar on 27/06/15.
 */
public class EventDeliveryInfoPosted {
    OrderSummaryData mOrderSummaryData;
    public EventDeliveryInfoPosted (OrderSummaryData osd) {
        mOrderSummaryData = osd;
    }

    public OrderSummaryData GetOrderSummaryData () {
        return mOrderSummaryData;
    }
}
