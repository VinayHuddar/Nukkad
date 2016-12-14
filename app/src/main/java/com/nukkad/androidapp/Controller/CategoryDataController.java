package com.nukkad.androidapp.Controller;

import com.nukkad.androidapp.Common.CommonDefinitions;
import com.nukkad.androidapp.Common.Events.EventCategoriesFetched;
import com.nukkad.androidapp.Controller.ProductList.ProductListManager;
import com.nukkad.androidapp.FazmartApplication;
import com.nukkad.androidapp.Model.CategoryData;

import java.util.HashMap;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Vinay on 11-06-2015.
 */

public class CategoryDataController {
    private static String ROOT_CATEGORY_NAME = "Grocery Store";

    // Make this class a Singleton
    private static CategoryDataController instance = null;
    public static CategoryDataController GetInstance() {
        if (instance == null)
            instance = new CategoryDataController();
        return instance;
    }

    // Constructor
    public CategoryDataController() {
        mCategoryTreeHash = new HashMap<Integer, CategoryData.Model.Category>();
    }

    // Root Category - a dummy category
    CategoryData.Model.Category mRootCategory = null;
    public void BuildCategoryTree (CategoryData.Model categoryData) {
        mRootCategory = categoryData.GetNewObject(0, ROOT_CATEGORY_NAME, null, null, 0, categoryData.GetCategoryData());
        BuildCategoryHash (categoryData);
    }
    public CategoryData.Model.Category GetRootCategory() {
        return mRootCategory;
    }

    // A hashmap of all the categories to enable quick access of categories
    HashMap<Integer, CategoryData.Model.Category> mCategoryTreeHash;
    void BuildCategoryHash (CategoryData.Model mainCategories) {
        CategoryData.Model.Category[] topLevelCats = mainCategories.GetCategoryData();
        int numTopLevelCats = topLevelCats.length;
        for (int topCatIdx = 0; topCatIdx < numTopLevelCats; topCatIdx++) {
            CategoryData.Model.Category topCatObj = topLevelCats[topCatIdx];
            mCategoryTreeHash.put(topCatObj.GetId(), topCatObj);

            // Get first level sub categories
            CategoryData.Model.Category[] firstLevelSubCats = topCatObj.GetSubCategories();
            if (firstLevelSubCats.length == 0)
                continue;

            int numFirstLevelSubCats = firstLevelSubCats.length;
            for (int firstLevelCatIdx = 0; firstLevelCatIdx < numFirstLevelSubCats; firstLevelCatIdx++) {
                CategoryData.Model.Category firstLevelCatObj = firstLevelSubCats[firstLevelCatIdx];
                mCategoryTreeHash.put(firstLevelCatObj.GetId(), firstLevelCatObj);

                // Get second level sub categories
                CategoryData.Model.Category[] secondLevelSubCats = firstLevelCatObj.GetSubCategories();
                if (secondLevelSubCats.length == 0)
                    continue;

                int numSecondLevelSubCats = secondLevelSubCats.length;
                for (int secondLevelCatIdx = 0; secondLevelCatIdx < numSecondLevelSubCats; secondLevelCatIdx++) {
                    CategoryData.Model.Category secondLevelCatObj = secondLevelSubCats[secondLevelCatIdx];
                    mCategoryTreeHash.put(secondLevelCatObj.GetId(), secondLevelCatObj);
                }
            }
        }
    }

    // The main first-level categories obtained from the Retrofit API
    public class MainCategories {
        Category[] categories = null;
        public Category[] GetMainCategories () {
            return categories;
        }
    }

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

    public CategoryData.Model.Category GetCategory (int catId) {
        return mCategoryTreeHash.get(catId);
    }

    public CategoryData.Model.Category[] GetMainCategories () { return mRootCategory.GetSubCategories(); }

    public CategoryData.Model.Category GetFirstLevelCategory (int mainCatPos, int firstLevelSubCatPos) {
        return mRootCategory.GetSubCategories()[mainCatPos]
                .GetSubCategories()[firstLevelSubCatPos];
    }

    public CategoryData.Model.Category GetSecondLevelCategory (int mainCatPos, int firstLevelSubCatPos, int secondLevelSubCatPos) {
        return mRootCategory.GetSubCategories()[mainCatPos]
                .GetSubCategories()[firstLevelSubCatPos]
                .GetSubCategories()[secondLevelSubCatPos];
    }

    public int GetProductCount (int catId) {
        return mCategoryTreeHash.get(catId).GetTotalProducts();
    }

    public boolean IsCategoryTreeBuilt() {
        return (mRootCategory != null);
    }

    public boolean WasCategoryViewed (int catId) {
        return ProductListManager.GetInstance().WasCategoryViewed(catId);
    }

    public String GetCategoryName (int catId) {
        return mCategoryTreeHash.get(catId).GetName(); //GetCategory(catId).GetName();
    }
}

