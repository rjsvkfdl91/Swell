package com.example.s521573.money_saver.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;


public class SaverProvider extends ContentProvider {

    public static final int SAVER = 10;

    public static final int SAVER_ID = 11;


    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(SaverContract.CONTENT_AUTHORITY,SaverContract.PATH_SAVER,SAVER);

        sUriMatcher.addURI(SaverContract.CONTENT_AUTHORITY,SaverContract.PATH_SAVER + "/#", SAVER_ID);
    }

    private SaverDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new SaverDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch (match){
            case SAVER:
                cursor = database.query(SaverContract.SaverEntry.TABLE_NAME,projection,selection,selectionArgs,
                        null,null,sortOrder);
                break;

            case SAVER_ID:
                selection = SaverContract.SaverEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(SaverContract.SaverEntry.TABLE_NAME,projection,selection,selectionArgs,
                        null,null,sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        if (getContext() != null){
            cursor.setNotificationUri(getContext().getContentResolver(),uri);
        }
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);

        switch (match){
            case SAVER:
                return insertProduct(uri,contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for "+uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues contentValues) {

        String item = contentValues.getAsString(SaverContract.SaverEntry.COLUMN_PRODUCT);
        Integer price = contentValues.getAsInteger(SaverContract.SaverEntry.COLUMN_PRICE);
        Integer priority = contentValues.getAsInteger(SaverContract.SaverEntry.COLUMN_IMPORTANCE);

        if (item == null || price == null){
            throw new IllegalArgumentException("Item and Price need to be entered");
        }if (priority == null || !SaverContract.SaverEntry.isValidLevel(priority)){
            throw new IllegalArgumentException("Requires valid Level of Importance");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(SaverContract.SaverEntry.TABLE_NAME,null,contentValues);

        if (getContext() != null){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return ContentUris.withAppendedId(uri,id);
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match){
            case SAVER:
                rowDeleted = database.delete(SaverContract.SaverEntry.TABLE_NAME,selection,selectionArgs);
                break;
            case SAVER_ID:
                selection = SaverContract.SaverEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowDeleted = database.delete(SaverContract.SaverEntry.TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for "+uri);
        }if (rowDeleted != 0 && getContext() != null){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match){
            case SAVER:
                return update(uri,contentValues,selection,selectionArgs);
            case SAVER_ID:
                selection = SaverContract.SaverEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateSaver(uri,contentValues,selection,selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for "+uri);
        }
    }

    private int updateSaver(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        if (contentValues.containsKey(SaverContract.SaverEntry.COLUMN_PRODUCT)){
            String item = contentValues.getAsString(SaverContract.SaverEntry.COLUMN_PRODUCT);
            if (item == null){
                throw new IllegalArgumentException("Item is required");
            }
        }if (contentValues.containsKey(SaverContract.SaverEntry.COLUMN_PRICE)){
            Integer price = contentValues.getAsInteger(SaverContract.SaverEntry.COLUMN_PRICE);
            if (price < 0){
                throw new IllegalArgumentException("Price must be over 0");
            }
        }if (contentValues.containsKey(SaverContract.SaverEntry.COLUMN_IMPORTANCE)){
            Integer priority = contentValues.getAsInteger(SaverContract.SaverEntry.COLUMN_IMPORTANCE);
            if(priority == null){
                throw new IllegalArgumentException("Importance must be between 1 and 3");
            }
        }if (contentValues.size() == 0){
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowUpdated = database.update(SaverContract.SaverEntry.TABLE_NAME,contentValues,selection,selectionArgs);

        if (rowUpdated != 0 && getContext() != null){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowUpdated;
    }

    @Override
    public String getType(@NonNull Uri uri) {

        int match = sUriMatcher.match(uri);

        switch (match) {
            case SAVER:
                return SaverContract.SaverEntry.CONTENT_LIST_TYPE;
            case SAVER_ID:
                return SaverContract.SaverEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI" + uri + " with match" + match);
        }
    }

}
