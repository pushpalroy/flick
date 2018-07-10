package com.pushpal.popularmoviesstage1.utilities;

import android.util.Log;

public class DateUtil {
    private static final String TAG = DateUtil.class.getSimpleName();

    // 2017-09-01 ----> Sep 2017
    public static String getFormattedDate(String hyphenatedDate) {
        String[] dateArray = hyphenatedDate.split("-");
        String year = "", monthValue = "", day = "", month;
        try {
            year = dateArray[0];
            monthValue = dateArray[1];
            day = dateArray[2];
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, e.getMessage());
        }

        switch (monthValue) {
            case "01":
            case "1":
                month = "Jan";
                break;
            case "02":
            case "2":
                month = "Feb";
                break;
            case "03":
            case "3":
                month = "Mar";
                break;
            case "04":
            case "4":
                month = "Apr";
                break;
            case "05":
            case "5":
                month = "May";
                break;
            case "06":
            case "6":
                month = "June";
                break;
            case "07":
            case "7":
                month = "July";
                break;
            case "08":
            case "8":
                month = "Aug";
                break;
            case "09":
            case "9":
                month = "Sep";
                break;
            case "10":
                month = "Oct";
                break;
            case "11":
                month = "Nov";
                break;
            case "12":
                month = "Dec";
                break;
            default:
                month = "";
        }

        return month + " " + year;
    }
}
