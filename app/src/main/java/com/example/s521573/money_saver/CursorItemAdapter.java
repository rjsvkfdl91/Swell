package com.example.s521573.money_saver;


import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.s521573.money_saver.Utils.ColorUtils;
import com.example.s521573.money_saver.Utils.FormatUtils;
import com.example.s521573.money_saver.data.SaverContract;

import java.util.Currency;
import java.util.Locale;
import java.util.concurrent.Callable;

public class CursorItemAdapter extends RecyclerView.Adapter<CursorItemAdapter.ItemViewHolder> {

    private static final String TAG = "Adapater";
    private Cursor mCursor;
    private Context mContext;
    private int mTotalForHigh = 0;
    private int mTotalForMiddle = 0;
    private int mTotalForLow = 0;

    public CursorItemAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {

        mCursor.moveToPosition(position);

        final int rowId = mCursor.getInt(mCursor.getColumnIndex(SaverContract.SaverEntry._ID));
        final int importance = mCursor.getInt(mCursor.getColumnIndex(SaverContract.SaverEntry.COLUMN_IMPORTANCE));
        String product = mCursor.getString(mCursor.getColumnIndex(SaverContract.SaverEntry.COLUMN_PRODUCT));
        final int price = mCursor.getInt(mCursor.getColumnIndex(SaverContract.SaverEntry.COLUMN_PRICE));
        String currentDate = mCursor.getString(mCursor.getColumnIndex(SaverContract.SaverEntry.COLUMN_CURRENT_DATE));

        holder.importanceText.setText(String.valueOf(importance));

        // BackgroundColor for Importance Circle
        GradientDrawable importanceCircle = (GradientDrawable)holder.importanceText.getBackground();
        int backgroundColor = ColorUtils.getImportanceBackgroundColor(mContext, importance);
        importanceCircle.setColor(backgroundColor);

        holder.productText.setText(product);

        Locale locale = Locale.getDefault();
        Currency currency = Currency.getInstance(locale);
        String symbol = currency.getSymbol().replaceAll("\\w", "");
        holder.priceText.setText(String.format("%s %s",symbol,FormatUtils.decimalFormat(price)));
        holder.dateText.setText(currentDate);

        holder.itemView.setTag(rowId);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,ManagementEditorActivity.class);
                Uri currentUri = ContentUris.withAppendedId(SaverContract.SaverEntry.CONTENT_URI,rowId);
                intent.setData(currentUri);
                mContext.startActivity(intent);
            }
        });

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteItem(rowId, importance,price);
                notifyItemChanged(rowId);
            }
        });
    }

    @Override
    public int getItemCount() {

        if (mCursor == null){
            return 0;
        }
        return mCursor.getCount();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder{

        TextView importanceText,productText,priceText,dateText;
        ImageButton deleteBtn;

        public ItemViewHolder(View itemView) {
            super(itemView);

            importanceText = itemView.findViewById(R.id.importance_circle);
            productText = itemView.findViewById(R.id.product);
            priceText = itemView.findViewById(R.id.price);
            dateText = itemView.findViewById(R.id.date);
            deleteBtn = itemView.findViewById(R.id.deleteButton);
        }
    }

    public Cursor swapCursor(Cursor cursor){

        if(mCursor == cursor){
            return null;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;

        if (cursor != null){
            this.notifyDataSetChanged();
        }
        try{
            if (oldCursor != null){
                oldCursor.close();
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return oldCursor;
    }

    private void deleteItem(final int id,int importance,int price){

        final Uri mCurrentUri = ContentUris.withAppendedId(SaverContract.SaverEntry.CONTENT_URI,id);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_view, null);
        TextView txtTitle = view.findViewById(R.id.title);
        txtTitle.setTextSize(20);
        txtTitle.setTextColor(Color.RED);
        txtTitle.setText(mContext.getString(R.string.caution));

        TextView message = view.findViewById(R.id.message);
        message.setTextSize(15);
        message.setText(R.string.delete_item_dialog);


        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (mCurrentUri != null){
                    int rowDeleted = mContext.getContentResolver().delete(mCurrentUri,null,null);
                    if(rowDeleted == 1){
                        Toast.makeText(mContext, "Item is deleted successfully", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(mContext, "Fail to delete the Item", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        builder.setNegativeButton(R.string.no_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();


        SharedPreferences preferences = mContext.getSharedPreferences("item",0);
        String totalForHigh = preferences.getString("priceForHigh","");

        if (totalForHigh.length()>0){
            try{
                mTotalForHigh = Integer.valueOf(totalForHigh);
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }

        String totalForMiddle = preferences.getString("priceForMiddle","");
        if (totalForMiddle.length()>0){
            try{
                mTotalForMiddle = Integer.valueOf(totalForMiddle);
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }

        String totalForLow = preferences.getString("priceForLow","");
        if (totalForLow.length()>0){
            try{
                mTotalForLow = Integer.valueOf(totalForLow);
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }

        if (importance == 1){
            Log.i(TAG, "deleteItem: " + price);
            mTotalForHigh = mTotalForHigh - price;
            Log.i(TAG, "deleteItem: "+ mTotalForHigh);
            SharedPreferences preference = mContext.getSharedPreferences("item",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preference.edit();
            editor.putString("priceForHigh",String.valueOf(mTotalForHigh));
            editor.apply();
        }else if (importance == 2){
            Log.i(TAG, "deleteItem: " + price);
            mTotalForMiddle = mTotalForMiddle - price;
            Log.i(TAG, "deleteItem: "+ mTotalForMiddle);
            SharedPreferences preference = mContext.getSharedPreferences("item",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preference.edit();
            editor.putString("priceForMiddle",String.valueOf(mTotalForMiddle));
            editor.apply();
        }else if (importance == 3){
            Log.i(TAG, "deleteItem: " + price);
            mTotalForLow = mTotalForLow - price;
            Log.i(TAG, "deleteItem: "+ mTotalForLow);
            SharedPreferences preference = mContext.getSharedPreferences("item",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preference.edit();
            editor.putString("priceForLow",String.valueOf(mTotalForLow));
            editor.apply();
        }
        Log.i(TAG, "Total: " + mTotalForHigh + ",," + mTotalForMiddle+",,"+mTotalForLow);
    }
}
