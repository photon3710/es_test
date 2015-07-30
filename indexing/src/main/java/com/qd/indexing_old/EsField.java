package com.jidian.indexing;

/**
 * Created by xiaoyun on 4/27/14.
 */
public enum EsField {

    ID("id"),
    TITLE("title"),
    KEYWORDS("keywords"),
    BODY("body"),
    AVG_RATING("avg_rating"),
    NUM_OF_RATINGS("num_of_ratings"),
    NUM_OF_DOWNLOADS("num_of_downloads"),
    GAMINESS("gaminess"),
    SPAMNESS("spamness"),
    ICON_URL("icon_url"),
    APP_URL("app_url"),
    PACKAGE("package"),
    VERSION("version"),
    INTENT("intent");

    private final String keyStr;
    public String getKeyStr() { return keyStr; }
    EsField(final String k) {
        keyStr = k;
    }
}
