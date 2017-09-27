package com.example.s521573.money_saver;


import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.s521573.money_saver.Utils.ColorUtils;
import com.example.s521573.money_saver.Utils.FormatUtils;
import com.example.s521573.money_saver.data.SaverContract;

import java.util.Currency;
import java.util.Locale;

public class CursorItemAdapter extends RecyclerView.Adapter<CursorItemAdapter.ItemViewHolder> {

    private Cursor mCursor;
    private Context mContext;


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

        public ItemViewHolder(View itemView) {
            super(itemView);

            importanceText = itemView.findViewById(R.id.importance_circle);
            productText = itemView.findViewById(R.id.product);
            priceText = itemView.findViewById(R.id.price);
            dateText = itemView.findViewById(R.id.date);
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
}
