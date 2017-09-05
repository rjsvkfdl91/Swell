package com.example.s521573.money_saver;


import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

import com.example.s521573.money_saver.data.SaverContract;


public class AsyncTaskCursorLoader extends AsyncTaskLoader<Cursor> {

    private String mSelection;
    private String[] mSelectionArgs;

    private Cursor mItemData = null;

    public AsyncTaskCursorLoader(Context context, String selection, String[] selectionArgs) {
        super(context);
        mSelection = selection;
        mSelectionArgs = selectionArgs;
    }

    @Override
    protected void onStartLoading() {
        if (mItemData != null){
            deliverResult(mItemData);
        }else {
            forceLoad();
        }
    }

    @Override
    public Cursor loadInBackground() {
        try{
            return getContext().getContentResolver().query(SaverContract.SaverEntry.CONTENT_URI,null,mSelection,mSelectionArgs,SaverContract.SaverEntry.COLUMN_IMPORTANCE);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deliverResult(Cursor data) {
        mItemData = data;
        super.deliverResult(data);
    }
}
