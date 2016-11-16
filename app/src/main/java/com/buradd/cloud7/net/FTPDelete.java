package com.buradd.cloud7.net;

import android.os.AsyncTask;

import com.buradd.cloud7.MainActivity;

import org.apache.commons.net.ftp.FTPClient;

import java.net.InetAddress;

/**
 * Created by bradley.miller on 11/16/2016.
 */

public class FTPDelete extends AsyncTask<String, String, Boolean> {


    MainActivity mainActivity = MainActivity.getInstance();

    @Override
    protected Boolean doInBackground(String... strings) {
        FTPClient aFtp = new FTPClient();
        boolean deleted = false;
        try{
            aFtp.connect(InetAddress.getByName(strings[0]));
            aFtp.login(strings[1], strings[2]);
            deleted = aFtp.deleteFile(strings[3]);
            aFtp.logout();
            aFtp.disconnect();
        }catch(Exception e){
            e.printStackTrace();
        }
        return deleted;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if(aBoolean){
            mainActivity.refreshRemoteLists();
        }
    }



}
