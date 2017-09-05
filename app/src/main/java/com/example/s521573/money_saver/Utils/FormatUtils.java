package com.example.s521573.money_saver.Utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class FormatUtils  {

    private static final String DATE_SEPARATOR = "-";

    public FormatUtils() {

    }

    public static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new java.util.Date());
    }

    public static String decimalFormat(int price){
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
        return decimalFormat.format(price);
    }

    public static String getMonth() {

        String originalDate = getDateTime();
        String currentMonth = null;

        if (originalDate.contains(DATE_SEPARATOR)) {

            String[] parts = originalDate.split(DATE_SEPARATOR);

            currentMonth = parts[1];

        }
        return getStringMonth(currentMonth);
    }
    private static String getStringMonth(String month){

        String currentMonth = null;

        switch (month){
            case "01":
                currentMonth = "J A N U A R Y";
                break;
            case "02":
                currentMonth = "F E B R U A R Y";
                break;
            case "03":
                currentMonth = "M A R C H";
                break;
            case "04":
                currentMonth = "A P R I L";
                break;
            case "05":
                currentMonth = "M A R C H";
                break;
            case "06":
                currentMonth = "J U N E";
                break;
            case "07":
                currentMonth = "J U L Y";
                break;
            case "08":
                currentMonth = "A U G U S T";
                break;
            case "09":
                currentMonth = "S E P T E M B E R";
                break;
            case "10":
                currentMonth = "O C T O B E R";
                break;
            case "11":
                currentMonth = "N O V E M B E R";
                break;
            case "12":
                currentMonth = "D E C E M B E R";
                break;
        }
        return currentMonth;
    }
}
