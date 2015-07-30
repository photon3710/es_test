package com.qd.indexing;


public enum UixField {

    INTENT("intent"),
    UIXFILENAME("uixFileName"),
    PATH("path"),
    CONTENT("content");

    private final String keyStr;
    public String getKeyStr() { return keyStr; }
    
    UixField(final String k) {
        keyStr = k;
    }
}
