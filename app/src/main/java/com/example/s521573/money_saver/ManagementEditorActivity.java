package com.example.s521573.money_saver;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.s521573.money_saver.Utils.FormatUtils;
import com.example.s521573.money_saver.data.SaverContract;

public class ManagementEditorActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener,LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "Total";
    private EditText mItemEditText;
    private EditText mPriceEditText;
    private RadioGroup mPriorityGroup;
    private int mPriorityLevel = 0;
    private boolean mHasChanged = false;
    private Uri mCurrentUri;

    private int mTotalForHigh = 0;
    private int mTotalForMiddle = 0;
    private int mTotalForLow = 0;

    private int mCurrentItemPrice,mCurrentItemPriority;
    private String mCurrentItem;
    private static final int EXISTING_ITEM_LOADER = 0;

    private SharedPreferences mPreference;


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management_editor);

        // To show keyboard when activity start
        InputMethodManager imm = (InputMethodManager) ManagementEditorActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null){
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        }

        FloatingActionButton addButton = (FloatingActionButton)findViewById(R.id.addItemBtn);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveItem();
            }
        });

        mPreference = getSharedPreferences("item",0);
        String totalForHigh = mPreference.getString("priceForHigh","");
        if (totalForHigh.length()>0){
            try{
                mTotalForHigh = Integer.valueOf(totalForHigh);
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }

        String totalForMiddle = mPreference.getString("priceForMiddle","");

        if (totalForMiddle.length()>0){
            try{
                mTotalForMiddle = Integer.valueOf(totalForMiddle);
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }

        String totalForLow = mPreference.getString("priceForLow","");

        if (totalForLow.length()>0){
            try{
                mTotalForLow = Integer.valueOf(totalForLow);
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.editor_toolBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        mCurrentUri = intent.getData();

        if (mCurrentUri == null){
            setTitle(R.string.addItem);
        }else{
            setTitle(R.string.editItem);
        }

        mItemEditText = (EditText)findViewById(R.id.edit_item);
        mPriceEditText = (EditText)findViewById(R.id.edit_price);
        mPriorityGroup = (RadioGroup)findViewById(R.id.priority_group);
        mPriorityGroup.setOnCheckedChangeListener(this);

        mItemEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mPriorityGroup.setOnTouchListener(mTouchListener);

        getLoaderManager().initLoader(EXISTING_ITEM_LOADER,null,this);
    }


    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (mPriorityGroup.getCheckedRadioButtonId()){
            case R.id.highLevel:
                mPriorityLevel = SaverContract.SaverEntry.HIGH_LEVEL;
                break;
            case R.id.middleLevel:
                mPriorityLevel = SaverContract.SaverEntry.MIDDLE_LEVEL;
                break;
            case R.id.lowLevel:
                mPriorityLevel = SaverContract.SaverEntry.LOW_LEVEL;
                break;
        }
    }

    public void saveItem() {

        String item = mItemEditText.getText().toString().trim();
        String price = mPriceEditText.getText().toString().trim();

        if (mCurrentUri == null){

            if (TextUtils.isEmpty(item)){
                Toast.makeText(this, "Item should be entered!", Toast.LENGTH_LONG).show();
                return;
            }if (TextUtils.isEmpty(price)){
                Toast.makeText(this, "Price should be entered!", Toast.LENGTH_LONG).show();
                return;
            }if (TextUtils.isEmpty(item) && TextUtils.isEmpty(price)){
                Toast.makeText(this, "Item and Price should be entered!", Toast.LENGTH_LONG).show();
                return;
            }
            if (!SaverContract.SaverEntry.isValidLevel(mPriorityLevel)){
                Toast.makeText(this, "Priority should be selected!", Toast.LENGTH_LONG).show();
                return;
            }

            ContentValues value = new ContentValues();
            value.put(SaverContract.SaverEntry.COLUMN_IMPORTANCE,mPriorityLevel);
            value.put(SaverContract.SaverEntry.COLUMN_PRODUCT, item);

            if (price.length()>0){
                try{
                    value.put(SaverContract.SaverEntry.COLUMN_PRICE, Integer.parseInt(price));
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }
            value.put(SaverContract.SaverEntry.COLUMN_CATEGORY,0);
            value.put(SaverContract.SaverEntry.COLUMN_CURRENT_DATE,FormatUtils.getDateTime());

            mPreference = getSharedPreferences("item",MODE_PRIVATE);
            SharedPreferences.Editor editor = mPreference.edit();
            if (price.length()>0){
                if(mPriorityLevel == 1){
                    try{
                        mTotalForHigh = mTotalForHigh + Integer.valueOf(price);
                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                    editor.putString("priceForHigh",String.valueOf(mTotalForHigh));
                    editor.apply();
                }else if (mPriorityLevel == 2){
                    try{
                        mTotalForMiddle = mTotalForMiddle + Integer.valueOf(price);
                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                    editor.putString("priceForMiddle",String.valueOf(mTotalForMiddle));
                    editor.apply();
                }else if (mPriorityLevel == 3){
                    try{
                        mTotalForLow = mTotalForLow + Integer.valueOf(price);
                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                    editor.putString("priceForLow",String.valueOf(mTotalForLow));
                    editor.apply();
                }
            }

            Uri newUri = getContentResolver().insert(SaverContract.SaverEntry.CONTENT_URI,value);

            if (newUri == null){
                Toast.makeText(this, "Editor inserted failed", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Item is successfully added", Toast.LENGTH_SHORT).show();
            }
        }else {

            ContentValues value = new ContentValues();
            value.put(SaverContract.SaverEntry.COLUMN_IMPORTANCE,mPriorityLevel);
            value.put(SaverContract.SaverEntry.COLUMN_PRODUCT, item);
            try{
                value.put(SaverContract.SaverEntry.COLUMN_PRICE, price);
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
            value.put(SaverContract.SaverEntry.COLUMN_CATEGORY,0);
            value.put(SaverContract.SaverEntry.COLUMN_CURRENT_DATE,FormatUtils.getDateTime());

            mPreference = getSharedPreferences("item",MODE_PRIVATE);
            SharedPreferences.Editor editor = mPreference.edit();
            if (mCurrentItemPriority != mPriorityLevel){
                if(mCurrentItemPriority == 1){
                    try{
                        mTotalForHigh = (mTotalForHigh - mCurrentItemPrice);
                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                    editor.putString("priceForHigh",String.valueOf(mTotalForHigh));
                }else if (mCurrentItemPriority == 2){
                    try{
                        mTotalForMiddle = (mTotalForMiddle - mCurrentItemPrice);
                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                    editor.putString("priceForMiddle",String.valueOf(mTotalForMiddle));
                }else if (mCurrentItemPriority == 3){
                    try{
                        mTotalForLow = (mTotalForLow - mCurrentItemPrice);
                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                    editor.putString("priceForLow",String.valueOf(mTotalForLow));
                }
                if(mPriorityLevel == 1){
                    try{
                        mTotalForHigh = mTotalForHigh + mCurrentItemPrice;
                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                    editor.putString("priceForHigh",String.valueOf(mTotalForHigh));
                }else if (mPriorityLevel == 2){
                    try{
                        mTotalForMiddle = mTotalForMiddle + mCurrentItemPrice;
                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                    editor.putString("priceForMiddle",String.valueOf(mTotalForMiddle));
                }else if (mPriorityLevel == 3){
                    try{
                        mTotalForLow = mTotalForLow + mCurrentItemPrice;
                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                    editor.putString("priceForLow",String.valueOf(mTotalForLow));
                }
            }else {
                if(mPriorityLevel == 1){
                    try{
                        mTotalForHigh = (mTotalForHigh - mCurrentItemPrice)+Integer.parseInt(price);
                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                    editor.putString("priceForHigh",String.valueOf(mTotalForHigh));
                }else if (mPriorityLevel == 2){
                    try{
                        mTotalForMiddle = (mTotalForMiddle - mCurrentItemPrice)+ Integer.parseInt(price);
                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                    editor.putString("priceForMiddle",String.valueOf(mTotalForMiddle));
                }else if (mPriorityLevel == 3){
                    try{
                        mTotalForLow = (mTotalForLow - mCurrentItemPrice) + Integer.parseInt(price);
                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                    editor.putString("priceForLow",String.valueOf(mTotalForLow));
                }
            }
            editor.apply();

            int rowAffected = getContentResolver().update(mCurrentUri,value,null,null);

            if (rowAffected == 0){
                Toast.makeText(this, "Editor update failed", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Item is successfully updated", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    public void deleteItem() {

        if (mCurrentUri != null){

            int rowDeleted = getContentResolver().delete(mCurrentUri,null,null);

            if(rowDeleted == 1){
                Toast.makeText(this, "Item is deleted successfully", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Fail to delete the Item", Toast.LENGTH_SHORT).show();
            }
        }

        if (mCurrentItemPriority == 1){
            mTotalForHigh = mTotalForHigh - mCurrentItemPrice;
            SharedPreferences preference = getSharedPreferences("item",MODE_PRIVATE);
            SharedPreferences.Editor editor = preference.edit();
            editor.putString("priceForHigh",String.valueOf(mTotalForHigh));
            editor.apply();
        }else if (mCurrentItemPriority == 2){
            mTotalForMiddle = mTotalForMiddle - mCurrentItemPrice;
            SharedPreferences preference = getSharedPreferences("item",MODE_PRIVATE);
            SharedPreferences.Editor editor = preference.edit();
            editor.putString("priceForMiddle",String.valueOf(mTotalForMiddle));
            editor.apply();
        }else if (mCurrentItemPriority == 3){
            mTotalForLow = mTotalForLow - mCurrentItemPrice;
            SharedPreferences preference = getSharedPreferences("item",MODE_PRIVATE);
            SharedPreferences.Editor editor = preference.edit();
            editor.putString("priceForLow",String.valueOf(mTotalForLow));
            editor.apply();
        }
        Log.i(TAG, "deleteItem: " + mTotalForHigh + ",," + mTotalForMiddle+",,"+mTotalForLow);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.management_editor_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_delete:
                deleteItem();
            case android.R.id.home:
                if (!mHasChanged){
                    NavUtils.navigateUpFromSameTask(ManagementEditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButton = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(ManagementEditorActivity.this);
                    }
                };
                unsavedChangeDialog(discardButton);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // To hide delete button when user add new Item because there is no item to delete
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(mCurrentUri == null){
            MenuItem deleteItem = menu.findItem(R.id.action_delete);
            deleteItem.setVisible(false);
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        if(!mHasChanged){
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButton = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };

        unsavedChangeDialog(discardButton);
    }

    private void unsavedChangeDialog(DialogInterface.OnClickListener discardButton) {

        @SuppressLint("InflateParams")
        View view = this.getLayoutInflater().inflate(R.layout.dialog_view, null);
        TextView txtTitle = view.findViewById(R.id.title);
        txtTitle.setTextSize(20);
        txtTitle.setTextColor(Color.RED);
        txtTitle.setGravity(Gravity.CENTER);
        txtTitle.setText(getString(R.string.caution));

        TextView message = view.findViewById(R.id.message);
        message.setTextSize(15);
        message.setText(R.string.unsaved_changes_dialog);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setPositiveButton(R.string.discard,discardButton);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
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
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        if (mCurrentUri == null){
            return null;
        }
        String[] projections = {
                SaverContract.SaverEntry._ID,
                SaverContract.SaverEntry.COLUMN_IMPORTANCE,
                SaverContract.SaverEntry.COLUMN_PRODUCT,
                SaverContract.SaverEntry.COLUMN_PRICE};

        return new CursorLoader(this,mCurrentUri,projections,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount()<1){
            return;
        }

        try{
            if(cursor.moveToFirst()){
                mCurrentItem = cursor.getString(cursor.getColumnIndex(SaverContract.SaverEntry.COLUMN_PRODUCT));
                mCurrentItemPrice = cursor.getInt(cursor.getColumnIndex(SaverContract.SaverEntry.COLUMN_PRICE));
                mCurrentItemPriority = cursor.getInt(cursor.getColumnIndex(SaverContract.SaverEntry.COLUMN_IMPORTANCE));

                mItemEditText.setText(mCurrentItem);
                mItemEditText.setSelection(mCurrentItem.length());
                mPriceEditText.setText(String.valueOf(mCurrentItemPrice));
                mPriceEditText.setSelection(String.valueOf(mCurrentItemPrice).length());

                switch (mCurrentItemPriority){
                    case 1:
                        mPriorityGroup.check(R.id.highLevel);
                        break;
                    case 2:
                        mPriorityGroup.check(R.id.middleLevel);
                        break;
                    default:
                        mPriorityGroup.check(R.id.lowLevel);
                        break;
                }
            }
        }finally {
            cursor.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mItemEditText.setText("");
        mPriceEditText.setText("");
        mPriorityGroup.setEnabled(true);
    }
}
