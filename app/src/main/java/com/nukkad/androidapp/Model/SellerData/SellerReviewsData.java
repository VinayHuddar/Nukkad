package com.nukkad.androidapp.Model.SellerData;

/**
 * Created by vinayhuddar on 05/08/15.
 */
public class SellerReviewsData {
    int total_reviews;
    Review[] reviews;

    public int GetTotalReviewsCount () { return total_reviews; }
    public Review[] GetReviews () { return reviews; }

    public static class Review {
        String author;
        String text;
        int rating;
        String date_added;

        public Review (String _name, String _date, int _rating, String _comment) {
            author = _name;
            date_added = _date;
            rating = _rating;
            text = _comment;
        }

        public String GetReviewerName () { return author; }
        public String GetReviewDate () { return date_added; }
        public int GetRating () { return rating; }
        public String GetComment () { return text; }
    }
}
