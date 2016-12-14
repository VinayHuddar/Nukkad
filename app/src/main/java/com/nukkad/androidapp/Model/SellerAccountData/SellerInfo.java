package com.nukkad.androidapp.Model.SellerAccountData;

import com.nukkad.androidapp.Model.ProductData.ProductData;

/**
 * Created by vinayhuddar on 26/08/15.
 */

public class SellerInfo{
    Seller seller;
    
    public class Seller {
        String name;
        int seller_id;
        String nickname;
        String email;
        String avatar;
        String company;
        String website;
        String banner;
        String description;
        int country_id;
        int zone_id;
        double total_sales;
        int reviews;
        double rating;
        int total_products;
        ProductData[] products;
    }

    public String GetName () { return seller.name; }
    public int GetSellerId () { return seller.seller_id; }
    public String GetNickname () { return seller.nickname; }
    public String GetEmail () { return seller.email; }
    public String GetSellerImage() { return seller.avatar; }
    public String GetCompany () { return seller.company; }
    public String GetWebsite () { return seller.website; }
    public String GetBanner () { return seller.banner; }
    public String GetDescription () { return seller.description; }
    public int GetCountryId () { return seller.country_id; }
    public int GetZoneId () { return seller.zone_id; }
    public double GetTotalSales () { return seller.total_sales; }
    public int GetReviews () { return seller.reviews; }
    public double GetRating () { return seller.rating; }
    public int GetTotalProductCount () { return seller.total_products; }
    public ProductData[] GetProducts() { return seller.products; }
}
