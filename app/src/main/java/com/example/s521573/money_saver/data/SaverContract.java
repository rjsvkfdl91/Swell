package com.example.s521573.money_saver.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


public class SaverContract {

    public SaverContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.s521573.money_saver";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_SAVER = "products";


    public static final class SaverEntry implements BaseColumns{

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SAVER;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SAVER;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_SAVER);

        public static final String TABLE_NAME = "products";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_IMPORTANCE = "importance";
        public static final String COLUMN_PRODUCT = "product";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_CURRENT_DATE = "date";
        public static final String COLUMN_CATEGORY = "category";


        public static final int HIGH_LEVEL = 1;
        public static final int MIDDLE_LEVEL = 2;
        public static final int LOW_LEVEL = 3;

        public static boolean isValidLevel(int importance){
            return importance == 1 || importance == 2 || importance == 3;
        }
    }
}
