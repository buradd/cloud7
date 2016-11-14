package com.buradd.cloud7;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Bradley Miller on 11/13/2016.
 */

public class Files {

    public static void setLastModified(String filename, String aLastModified, final Context aContext){
        PreferenceManager.getDefaultSharedPreferences(aContext).edit().
                putString(filename + ".last-modified", aLastModified).
                apply();
    }


    public static String getLastModified(String filename, Context aContext){
        return PreferenceManager.getDefaultSharedPreferences(aContext).getString(filename + ".last-modified", "");
    }
}
