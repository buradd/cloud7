package com.buradd.cloud7.net;

import android.os.AsyncTask;

import com.buradd.cloud7.Filenames;
import com.buradd.cloud7.MainActivity;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.net.InetAddress;

/**
 * Created by bradley.miller on 11/14/2016.
 */

public class RefreshLists extends AsyncTask<String, Void, Void> {

    MainActivity mainActivity = MainActivity.getInstance();


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mainActivity.refreshLocalLists();
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            boolean connected;
            FTPClient aFtp = new FTPClient();
            aFtp.connect(InetAddress.getByName("ftp.buradd.com"));
            connected = aFtp.login(params[0], params[1]);
            if (connected) {
                aFtp.enterLocalPassiveMode();
                aFtp.setFileType(FTP.BINARY_FILE_TYPE);
                aFtp.changeWorkingDirectory("/public_html/cloud7/files");
                FTPFile[] filesList = aFtp.listFiles();
                Filenames.fileList.clear();
                for (FTPFile file : filesList) {
                    String currFile = file.getName();
                    Filenames.fileList.add(currFile);
                }
                aFtp.changeWorkingDirectory("/public_html/cloud7/images/");
                FTPFile[] imagesList = aFtp.listFiles();
                Filenames.imageList.clear();
                for (FTPFile file : imagesList) {
                    String currFile = file.getName();
                    Filenames.imageList.add(currFile);
                }
                aFtp.changeWorkingDirectory("/public_html/cloud7/videos/");
                FTPFile[] videoList = aFtp.listFiles();
                Filenames.videoList.clear();
                for (FTPFile file : videoList) {
                    String currFile = file.getName();
                    Filenames.videoList.add(currFile);
                }
            }
            aFtp.logout();
            aFtp.disconnect();
        }catch(Exception e){
            e.printStackTrace();
        }



        return null;
    }
}
