package com.example.s521573.money_saver.Utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.example.s521573.money_saver.R;

public class ColorUtils {

    public static int getImportanceBackgroundColor(Context context, int numOfImportance){

        int importanceColorId = 0;

        switch (numOfImportance){

            case 1:
                importanceColorId = R.color.highestImportance;
                break;
            case 2:
                importanceColorId = R.color. middleImportance;
                break;
            case 3:
                importanceColorId = R.color.lowestImportance;
                break;
        }
        return ContextCompat.getColor(context,importanceColorId);
    }
}
