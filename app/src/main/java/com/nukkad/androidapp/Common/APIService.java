package com.nukkad.androidapp.Common;

import com.nukkad.androidapp.Model.AddressData.AddressModel;
import com.nukkad.androidapp.Model.AddressData.AddressList;
import com.nukkad.androidapp.Model.AppSettingsData;
import com.nukkad.androidapp.Model.CartData;
import com.nukkad.androidapp.Model.CategoryData;
import com.nukkad.androidapp.Model.DeliveryData.AccountData;
import com.nukkad.androidapp.Model.DeliveryData.ApartmentsAreaData;
import com.nukkad.androidapp.Model.DeliveryData.DeliverySlots;
import com.nukkad.androidapp.Model.DeliveryData.PaymentMethods;
import com.nukkad.androidapp.Model.OrderSummaryData;
import com.nukkad.androidapp.Model.ProductData.ProductDetailData;
import com.nukkad.androidapp.Model.ProductData.ProductListData;
import com.nukkad.androidapp.Model.SellerAccountData.ProductFilterRange;
import com.nukkad.androidapp.Model.SellerAccountData.ProductStatus;
import com.nukkad.androidapp.Model.SellerAccountData.SellerAccountProductData;
import com.nukkad.androidapp.Model.SellerData.SellerProductListData;
import com.nukkad.androidapp.Model.SearchData;
import com.nukkad.androidapp.Model.SellerAccountData.SellerInfo;
import com.nukkad.androidapp.Model.SellerAccountData.SellerOrderDetails;
import com.nukkad.androidapp.Model.SellerAccountData.SellerOrderList;
import com.nukkad.androidapp.Model.SellerAccountData.SellerOrderSummary;
import com.nukkad.androidapp.Model.SellerData.SellerReviewsData;
import com.nukkad.androidapp.Model.TokenResponse;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;

/**
 * Created by Vinay on 08-06-2015.
 */
public interface APIService {
    @POST("/oauth2/token")
    void GetAuthenticationToken (@Header("Accept") String accept, @Header("Authorization") String authHdr, @Query("grant_type") String grantType, @Body String emptyString,
                                 Callback<TokenResponse> callback);

    @GET("/common/settings")
    void GetAppSettings (Callback<AppSettingsData> callback);

    @GET("/common/city/{cityId}")
    void GetAreaApartmentData (@Path("cityId") int cityId, Callback<ApartmentsAreaData> callBack);

    /****************** Product APIs ******************/

    @GET("/product/category")
    void GetCategoryTree (Callback<CategoryData.Model> callBack);

    @GET("/product/category/{catId}")
    void GetCategoryProducts (@Path("catId") int catId, @Query("limit") int lmt, @Query("page") int pageNum,
                              Callback<ProductListData> callBack);

    @GET("/product/product/{prodId}")
    void GetProductDetails (@Path("prodId") int prodId, Callback<ProductDetailData> callBack);

    @GET("/product/search")
    void GetProductsSearched (@Query("search") String search, @Query("page") int page,
                              @Query("order") String order, Callback<SearchData> callBack);

    /****************** Cart APIs ******************/

    @GET("/cart/cart")
    void GetCart (Callback<CartData> callBack);

    @FormUrlEncoded
    @POST("/cart/product")
    void PostProduct(@Field("product_id") int prodId, @Field("quantity") int qty,
                     @Field("option_id") int option_id, @Field("option_value_id") int option_value_id,
                     Callback<CartData> callback);

    @FormUrlEncoded
    @PUT("/cart/product")
    void PutProduct (@Field("prod_key") String prodKey, @Field("prod_qty") int prodQty, Callback<CartData> callback);

    @DELETE("/cart/product/{prod_key}")
    void RemoveItemFromCart (@Path("prod_key") String prod_key, Callback<String> callback);

    /****************** Checkout APIs ******************/

    @GET("/checkout/delivery_slot")
    void GetDeliverySlots (Callback<DeliverySlots> callBack);

    @FormUrlEncoded
    @POST("/checkout/guest")
    void PostGuestAddress(
            @Field("firstname") String firstname,
            @Field("lastname") String lastname,
            @Field("email") String email,
            @Field("mobile") String mobile,
            @Field("apt_num") String apt_num,
            @Field("apt_id") int apt_id,
            @Field("area_id") int area_id,
            @Field("city_id") int city_id,
            @Field("postcode") String postcode,
            @Field("country_id") int country_id,
            @Field("zone_id") int zone_id,
            @Field("payment_address") boolean payment_address,
            Callback<String> callback);

    @GET("/checkout/payment_method")
    void GetPaymentMethods(Callback<PaymentMethods> callback);

    @FormUrlEncoded
    @POST("/checkout/delivery_slot")
    void PostDeliverySlot (
            @Field("delivery_date") String delivery_date,
            @Field("delivery_time") String delivery_time,
            Callback<String> callback);

    @FormUrlEncoded
    @POST("/checkout/payment_method")
    void PostPaymentMethod (@Field("payment_method") String payment_method, Callback<String> callback);

    @GET("/checkout/confirm")
    void GetConfirm(@Query("access_token") String access_token, Callback<OrderSummaryData> callback);

    @FormUrlEncoded
    @POST("/checkout/shipping_address")
    void PostAddress(
            @Field("shipping_address") String shipping_address,
            @Field("firstname") String firstname,
            @Field("lastname") String lastname,
            @Field("apt_num") String apt_num,
            @Field("apt_id") int apt_id,
            @Field("area_id") int area_id,
            @Field("city_id") int city_id,
            @Field("postcode") String postcode,
            @Field("zone_id") int zone_id,
            Callback<String> callback);

    @GET("/checkout/pay")
    void GetPay(@Query("access_token") String access_token, Callback<String> callback);

    @GET("/checkout/success")
    void GetSuccess(Callback<String> callback);

    /****************** Account APIs ******************/

    @FormUrlEncoded
    @POST("/account/login")
    void PostLogin (
            @Field("email") String email,
            @Field("password") String password,
            Callback<AccountData> callback);

    @GET("/account/logout")
    void GetLogout (Callback<String> callback);

    @GET("/account/address/{id}")
    void GetAddressId (@Path("id") int id, Callback<AddressModel> callback);

    @GET("/account/account")
    void GetAccount(Callback<AccountData> callback);

    @FormUrlEncoded
    @POST("/checkout/shipping_address")
    void PostExistingShippingAddress(@Field("shipping_address") String shipping_address,
                                     @Field("address_id") int addrId, Callback<String> callback);

    @GET("/account/address")
    void GetAllAddresses (Callback<AddressList> callback);

    @DELETE("/account/address/{addrId}")
    void DeleteAddress(@Path("addrId") int addrId, Callback<String> callback);

    @FormUrlEncoded
    //@PUT("/account/address/{addrId}")
    @PUT("/account/address")
    void PutAddress(@Field("firstname") String firstname,
                    @Field("lastname") String lastname,
                    @Field("apt_num") String apt_num,
                    @Field("apt_id") int apt_id,
                    @Field("area_id") int area_id,
                    @Field("city_id") int city_id,
                    @Field("postcode") String postcode,
                    @Field("country_id") int country_id,
                    @Field("zone_id") int zone_id,
                    @Field("default") boolean default_addr,
                   Callback<String> callback);

    // Seller related APIs
    @GET("/product/seller/{id}")
    void GetSellerInfo(@Path("id") int id, Callback<SellerInfo> callback);

    @GET("/product/seller/order_summary/{id}")
    void GetSellerOrderSummary(@Path("id") int Id, Callback<SellerOrderSummary> callback);

    @GET("/product/seller/{id}/product")
    void GetSellerProducts(@Path("id") int id, @Query("page") int page, @QueryMap Map<String, String> filters,
                           Callback<SellerProductListData> callback);

    @GET("/product/seller/{id}/review")
    void GetSellerReviews(@Path("id") int id, @Query("page") int page_num, Callback<SellerReviewsData> callback);

    @GET("/product/seller/{id}/order")
    void GetSellerOrders(@Path("id") int id, @QueryMap Map<String, String> filters,
                         Callback<SellerOrderList> callback);

    @GET("/product/seller/order/{orderId}")
    void GetSellerOrderDetails(@Path("order_id") int orderId, @Query("id") int id,
                         Callback<SellerOrderDetails> callback);

    @FormUrlEncoded
    @PUT("/product/seller/order/{orderId}")
    void UpdateOrderState(@Path("order_id") int orderId, @Field("id") int id,
                               @FieldMap Map<String, String> fields, Callback<SellerOrderDetails> callback);

    // Seller account related APIs
    @GET("/seller/product")
    void GetSellerAccountProducts(@QueryMap Map<String, String> filters, Callback<SellerAccountProductData.Model> callback);

    @GET("/seller/product/status")
    void GetSellerProductStatusList(Callback<ProductStatus.Model> callback);

    @GET("/seller/product/filterRange")
    void GetSellerProductFilters(Callback<ProductFilterRange.Model> callback);

}
