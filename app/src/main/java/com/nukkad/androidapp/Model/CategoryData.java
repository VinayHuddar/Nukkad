package com.nukkad.androidapp.Model;

import com.nukkad.androidapp.Common.CommonDefinitions;
import com.nukkad.androidapp.FazmartApplication;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by vinayhuddar on 30/08/15.
 */
public class CategoryData {
    Callback mCallback;

    public CategoryData (Callback callback) {
        mCallback = callback;
    }

    public class Model {
        Category[] categories;

        // The Category class
        public class Category {
            // Category Data
            int category_id;
            String name;
            String description;
            String thumb_image;
            int total_products;
            Category[] categories;

            // Accessors
            public int GetId() { return category_id; }
            public String GetName() { return name; }
            public String GetImageURL() { return thumb_image; }
            public int GetTotalProducts() { return total_products; }
            public Category[] GetSubCategories() { return categories; }

            public Category(int pId, String pName, String pDescription, String pImageURL,
                            int pTotalProducts, Category[] pSubCats) {
                category_id = pId;
                name = pName;
                description = pDescription;
                thumb_image = pImageURL;
                total_products = pTotalProducts;
                categories = pSubCats;
            }
        }

        public Category[] GetCategoryData() {
            return categories;
        }

        public Category GetNewObject(int _category_id, String _name, String _description, String _thumb_image,
                                    int _total_products, Category[] _categories) {
            return new Category (_category_id, _name, _description, _thumb_image, _total_products, _categories);
        }

    }

    int retryCount = 0;
    public void FetchData() {
        FazmartApplication.GetAPIService().GetCategoryTree(new retrofit.Callback<Model>() {
            @Override
            public void success(Model categoryData, Response response) {
                mCallback.OnCategoryDataReceived(categoryData);

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
                        Intent intent = new Intent(mActivity, LandingScreen.class);
                        mActivity.startActivity(intent);*/
                }
            }
        });
    }

    public interface Callback {
        public void OnCategoryDataReceived (Model categoryData);
    }
}
