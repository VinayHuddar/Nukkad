package com.nukkad.androidapp.Model.ProductData;

import com.nukkad.androidapp.Common.CommonDefinitions;

/**
 * Created by Vinay on 09-06-2015.
 */
public class ProductDetailData {
    Product product;

    public Product GetProduct () { return product; }

    public class Product {
        int product_id;
        String title;
        String model;
        String description;
        String thumb_image;
        String image;
        String[] images;
        String price;
        String tax;
        String special;
        String[] discounts;
        ProductOptionsData[] options;
        Manufacturer manufacturer;
        Seller seller;
        int reward_points;
        int reward_points_needed_to_buy;
        AttributeGroup[] attribute_groups;
        int minimum_quantity;
        String stock_status;
        RelatedProduct[] related_products;
        int rating;
        String reviews;
        boolean review_enabled;
        String[] recurrings;

        private class Manufacturer {
            int id;
            String name;
        }

        private class Seller {
            int id;
            String name;
        }

        public int GetProductId() { return product_id; }
        public ProductOptionsData[] GetProductOptionsData () { return options; }
        public String GetTitle() { return title; }
        public String GetDescription() { return description; }
        public String GetThumbImage() { return thumb_image; }
        public String GetImage() { return image; }
        public String[] GetImages() { return images; }

        public String GetManufacturerName () { return (manufacturer != null ? manufacturer.name : null); }
        public int GetManufacturerId () { return (manufacturer != null ? manufacturer.id : CommonDefinitions.INVALID_ID); }

        public String GetSellerName () { return (seller != null ? seller.name : null); }
        public int GetSellerId () { return (seller != null ? seller.id : CommonDefinitions.INVALID_ID); }

        public String GetStockStatus() { return stock_status; }
        public int GetRating() { return rating; }
        public String GetReviews() { return reviews; }
        public RelatedProduct[] GetRelatedProducts() {
            return related_products;
        }

        public class RelatedProduct {
            int product_id;
            boolean thumb2; // To be changed to string when "false" is changed to "null"
            String[] labels;
            String name;
            String description;
            String price;
            String special;
            boolean date_end; // To be changed to string when "false" is changed to "null"
            boolean tax; // To be changed to string when "false" is changed to "null"
            int rating;
            String thumb_image;

            String GetName() { return name; }
            String GetPrice() {
                return price;
            }
            String GetSpecial() {
                return special;
            }
            String GetThumbImage() {
                return thumb_image;
            }
        }

        public int GetPrimaryOptionsCount () {
            return options[CommonDefinitions.PRIMARY_OPTION_ID].GetOptionsCount();
        }

        public String GetPrimaryOptionName(int optionIdx) {
            return options[CommonDefinitions.PRIMARY_OPTION_ID].GetOptionName(optionIdx);
        }
        public String GetPrimaryOptionImage(int optionIdx) {
            return options[CommonDefinitions.PRIMARY_OPTION_ID].GetOptionImageURL(optionIdx);
        }
        public String GetPrimaryOptionPrice(int optionIdx) {
            return options[CommonDefinitions.PRIMARY_OPTION_ID].GetOptionPrice(optionIdx);
        }
        public String GetPrimaryOptionSpecialPrice (int optionIdx) {
            return options[CommonDefinitions.PRIMARY_OPTION_ID].GetOptionSpecialPrice(optionIdx);
        }
        public int GetPrimaryOptionId() {
            return options[CommonDefinitions.PRIMARY_OPTION_ID].GetProductOptionId();
        }
        public int GetPrimaryOptionValueId(int optionIdx) {
            return options[CommonDefinitions.PRIMARY_OPTION_ID].GetProductOptionValueId(optionIdx);
        }

        public ProductOptionsData CloneProductOptionsData(int optionIdx) {
            return (new ProductOptionsData(options[optionIdx]));
        }
    }
}
