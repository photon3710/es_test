package com.jidian.indexing;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


/**
 * This is the fulled processed view of app for given locale,
 * for retrieval/ranking/display purpose.
 *
 * Created by xiaoyun on 4/18/14.
 */
public class RawApp {

    public static class Author {
        public String name;
        public String url;
    }

    public static class Review {
        public String author;
        public String date;
        public String text;
        public String rating;
    }

    public static class Locale {
        public String currency;
        public String description;
        public String icon_url;
        public String last_seen_dt;
        public String name;
        public int price;
        public List<String> screenshot_urls;
    }

    public Author author;
    public String android_id;
    public String apk_link;
    public String category;
    public String delete_dt;
    public int downloads;
    public String first_seen_dt;
    public List<String> labels;

    public Map<String, Locale> locales;

    public String modify_dt;
    public String name;
    public List<String> permissions;

    public int rating_count;

    public String release_dt;
    public List<Review> reviews;

    public int size;
    public String url;
    public String version;
    public String whatsnew;
}
