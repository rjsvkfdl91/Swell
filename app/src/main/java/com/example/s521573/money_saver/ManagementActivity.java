package com.example.s521573.money_saver;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.s521573.money_saver.Utils.FormatUtils;
import com.example.s521573.money_saver.data.SaverContract;

import java.util.Currency;
import java.util.Locale;


public class ManagementActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 1;
    private static final String TAG ="Management" ;
    private TextView mTotalText;
    private long lastBackPressed;
    private SharedPreferences mPreference;
    private CursorItemAdapter mCursorItemAdapter;

    private ActionBarDrawerToggle mDrawableToggle;
    private DrawerLayout mDrawer;
    private Toolbar mToolbar;

    private String mSelection;
    private String[] mSelectionArgs;

    private int mTotalForHigh = 0;
    private int mTotalForMiddle = 0;
    private int mTotalForLow = 0;
    private static final String MENU_ITEM = "menu_item";
    private int mMenuId = 0;
    private int mAllId = 0;
    private int mHighId = 0;
    private int mMiddleId = 0;
    private int mLowId = 0;
    private String mCurrencySymbol;
    private Toast mToast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);

        Locale locale = Locale.getDefault();
        Currency currency = Currency.getInstance(locale);
        mCurrencySymbol= currency.getSymbol().replaceAll("\\w", "");

        mPreference = getSharedPreferences("item",MODE_PRIVATE);

        mDrawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        NavigationView mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mToolbar = (Toolbar)findViewById(R.id.toolBar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setUpDrawer();
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                item.setChecked(true);

                int id = item.getItemId();
                switch (id){
                    case R.id.navigation_item_all:
                        if (mToast != null){
                            mToast.cancel();
                        }
                        mToast = Toast.makeText(ManagementActivity.this, "All Items", Toast.LENGTH_SHORT);
                        mToast.show();
                        mSelection = null;
                        mSelectionArgs = null;
                        showTotalPrice();
                        mMenuId = item.getItemId();
                        mAllId = item.getItemId();
                        break;

                    case R.id.navigation_item_high:
                        if (mToast != null){
                            mToast.cancel();
                        }
                        mToast = Toast.makeText(ManagementActivity.this, "Don't forget to buy!", Toast.LENGTH_SHORT);
                        mToast.show();
                        mSelection = SaverContract.SaverEntry.COLUMN_IMPORTANCE + "=?";
                        mSelectionArgs = new String[]{String.valueOf(SaverContract.SaverEntry.HIGH_LEVEL)};
                        mMenuId = item.getItemId();
                        mHighId = item.getItemId();
                        showPriceDependsOnPriority(SaverContract.SaverEntry.HIGH_LEVEL);
                        break;

                    case R.id.navigation_item_middle:
                        if (mToast != null){
                            mToast.cancel();
                        }
                        mToast = Toast.makeText(ManagementActivity.this, "Think one more time!", Toast.LENGTH_SHORT);
                        mToast.show();
                        mSelection = SaverContract.SaverEntry.COLUMN_IMPORTANCE + "=?";
                        mSelectionArgs = new String[]{String.valueOf(SaverContract.SaverEntry.MIDDLE_LEVEL)};
                        mMenuId = item.getItemId();
                        mMiddleId = item.getItemId();
                        showPriceDependsOnPriority(SaverContract.SaverEntry.MIDDLE_LEVEL);
                        break;

                    case R.id.navigation_item_low:
                        if (mToast != null){
                            mToast.cancel();
                        }
                        mToast = Toast.makeText(ManagementActivity.this, "Do you really need these Items?", Toast.LENGTH_LONG);
                        mToast.show();
                        mSelection = SaverContract.SaverEntry.COLUMN_IMPORTANCE + "=?";
                        mSelectionArgs = new String[]{String.valueOf(SaverContract.SaverEntry.LOW_LEVEL)};
                        mMenuId = item.getItemId();
                        mLowId = item.getItemId();
                        showPriceDependsOnPriority(SaverContract.SaverEntry.LOW_LEVEL);
                        break;
                }
                mDrawer.closeDrawers();
                getLoaderManager().restartLoader(LOADER_ID,null,ManagementActivity.this);
                return true;
            }
        });

        TextView monthText = (TextView)findViewById(R.id.month_text);
        monthText.setText(FormatUtils.getMonth());

        mTotalText = (TextView)findViewById(R.id.total_text);
        mTotalText.setText(getString(R.string.total_label));

        final FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManagementActivity.this,ManagementEditorActivity.class);
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCursorItemAdapter = new CursorItemAdapter(this);
        recyclerView.setAdapter(mCursorItemAdapter);

//        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
//            @Override
//            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
//                return false;
//            }
//
//            @Override
//            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
//
//                int id = (int)viewHolder.itemView.getTag();
//
//                String stringId = Integer.toString(id);
//                Uri uri = SaverContract.SaverEntry.CONTENT_URI;
//                uri = uri.buildUpon().appendPath(stringId).build();
//
//                getContentResolver().delete(uri, null, null);
//
//                getLoaderManager().restartLoader(LOADER_ID, null,ManagementActivity.this);
//            }
//        }).attachToRecyclerView(recyclerView);

        // To hide fab button while scrolling
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    fab.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (dy > 0 || dy < 0 && fab.isShown()){
                    fab.hide();
                }
            }
        });

        getLoaderManager().initLoader(LOADER_ID,null,this);
    }


    private void showTotalPrice() {

        mPreference = getSharedPreferences("item", MODE_PRIVATE);
        String totalForHigh = mPreference.getString("priceForHigh", "");
        if (totalForHigh.length() > 0) {
            try {
                mTotalForHigh = Integer.valueOf(totalForHigh);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        String totalForMiddle = mPreference.getString("priceForMiddle", "");
        if (totalForMiddle.length() > 0) {
            try {
                mTotalForMiddle = Integer.valueOf(totalForMiddle);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        String totalForLow = mPreference.getString("priceForLow", "");
        if (totalForLow.length() > 0) {
            try {
                mTotalForLow = Integer.valueOf(totalForLow);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }try{
            if (mTotalForHigh + mTotalForMiddle + mTotalForLow == 0 || mTotalForHigh + mTotalForMiddle + mTotalForLow <0 ) {
                mTotalText.setText(getString(R.string.total_label));
                mPreference = getSharedPreferences("item",MODE_PRIVATE);
                SharedPreferences.Editor editor = mPreference.edit();
                editor.putString("priceForHigh","0");
                editor.putString("priceForMiddle","0");
                editor.putString("priceForLow","0");
                editor.apply();
            }else {
                mTotalText.setText(String.format
                        ("%s %s",mCurrencySymbol,FormatUtils.decimalFormat(mTotalForHigh + mTotalForMiddle + mTotalForLow)));
            }
        }catch (NumberFormatException e){
            e.printStackTrace();
        }
        getLoaderManager().restartLoader(LOADER_ID,null,ManagementActivity.this);
    }

    private void showPriceDependsOnPriority(int priority){

        mPreference = getSharedPreferences("item",MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreference.edit();

        switch (priority){
            case SaverContract.SaverEntry.HIGH_LEVEL:
                String totalForHigh = mPreference.getString("priceForHigh","");
                try{
                    mTotalForHigh = Integer.valueOf(totalForHigh);
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
                if (mTotalForHigh == 0 || mTotalForHigh < 0){
                    mTotalText.setText(getString(R.string.total_label));
                    editor.putString("priceForHigh","0");
                    editor.apply();
                }else {
                    mTotalText.setText(String.format(
                            "%s %s",mCurrencySymbol,FormatUtils.decimalFormat(mTotalForHigh)));
                }
                break;
            case SaverContract.SaverEntry.MIDDLE_LEVEL:
                String totalForMiddle = mPreference.getString("priceForMiddle","");
                    try{
                        mTotalForMiddle = Integer.valueOf(totalForMiddle);
                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                if (mTotalForMiddle == 0|| mTotalForMiddle < 0){
                    mTotalText.setText(getString(R.string.total_label));
                    editor.putString("priceForMiddle","0");
                    editor.apply();
                }else {
                    mTotalText.setText(String.format(
                            "%s %s",mCurrencySymbol,FormatUtils.decimalFormat(mTotalForMiddle)));
                }
                break;
            case SaverContract.SaverEntry.LOW_LEVEL:
                String totalForLow = mPreference.getString("priceForLow","");
                try{
                    mTotalForLow = Integer.valueOf(totalForLow);
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
                if (mTotalForLow == 0 || mTotalForLow < 0){
                    mTotalText.setText(getString(R.string.total_label));
                    editor.putString("priceForLow","0");
                    editor.apply();
                }else {
                    mTotalText.setText(String.format(
                            "%s %s",mCurrencySymbol,FormatUtils.decimalFormat(mTotalForLow)));
                }
                break;
        }
    }

    private void setUpDrawer() {
        mDrawableToggle = new ActionBarDrawerToggle(this,mDrawer,mToolbar,R.string.drawer_open,R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };

        mDrawableToggle.setDrawerIndicatorEnabled(true);
        mDrawer.addDrawerListener(mDrawableToggle);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MENU_ITEM, mMenuId);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mMenuId = savedInstanceState.getInt(MENU_ITEM);
        getLoaderManager().restartLoader(LOADER_ID,null,ManagementActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mMenuId == mAllId){
            showTotalPrice();
        } else if (mMenuId == mHighId){
            showPriceDependsOnPriority(SaverContract.SaverEntry.HIGH_LEVEL);
        }else if(mMenuId == mMiddleId){
            showPriceDependsOnPriority(SaverContract.SaverEntry.MIDDLE_LEVEL);
        }else if(mMenuId == mLowId){
            showPriceDependsOnPriority(SaverContract.SaverEntry.LOW_LEVEL);
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getLoaderManager().restartLoader(LOADER_ID,null,ManagementActivity.this);
    }


    private void deleteAll() {
        int rowDeleted = getContentResolver().delete(SaverContract.SaverEntry.CONTENT_URI,null,null);
        mTotalText.setText(getString(R.string.total_label));
        if (rowDeleted >1){
            Toast.makeText(this, rowDeleted+" Items are successfully deleted", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "Item is successfully deleted", Toast.LENGTH_SHORT).show();
        }

        SharedPreferences.Editor editor = mPreference.edit();
        editor.putString("priceForHigh","0");
        editor.putString("priceForMiddle","0");
        editor.putString("priceForLow","0");
        editor.apply();

        getLoaderManager().restartLoader(LOADER_ID, null,ManagementActivity.this);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawableToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawableToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.management_main_menu,menu);
        return true;
    }

    private void unsavedChangeDialog(DialogInterface.OnClickListener discardButton) {

        @SuppressLint("InflateParams")
        View view = this.getLayoutInflater().inflate(R.layout.dialog_view, null);
        TextView txtTitle = view.findViewById(R.id.title);
        txtTitle.setTextSize(20);
        txtTitle.setTextColor(Color.RED);
        txtTitle.setText(getString(R.string.caution));

        TextView message = view.findViewById(R.id.message);
        message.setTextSize(15);
        message.setText(R.string.delete_all_dialog);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setPositiveButton(R.string.deleteAll,discardButton);
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.deleteAll:
                DialogInterface.OnClickListener discardButton = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteAll();
                    }
                };
                unsavedChangeDialog(discardButton);
                return true;
            case R.id.aboutSwell:
                Intent intent = new Intent(ManagementActivity.this, AboutSwellActivity.class);
                startActivity(intent);
                break;
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }
        // Activate the navigation drawer toggle
        return mDrawableToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {

        if (System.currentTimeMillis() - lastBackPressed < 1500){
            finishAffinity();
            return;
        }
        if (mDrawer.isShown()){
            mDrawer.closeDrawers();
        }
        Toast.makeText(this, "Press once again to exit", Toast.LENGTH_SHORT).show();
        lastBackPressed = System.currentTimeMillis();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        return new AsyncTaskCursorLoader(this,mSelection,mSelectionArgs);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        mCursorItemAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorItemAdapter.swapCursor(null);
    }
}
