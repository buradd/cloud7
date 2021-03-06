package com.buradd.cloud7.net;

import android.os.AsyncTask;
import android.webkit.MimeTypeMap;

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
            aFtp.connect(InetAddress.getByName("cloud7.buradd.com"));
            connected = aFtp.login(params[0], params[1]);
            if (connected) {
                aFtp.enterLocalPassiveMode();
                aFtp.setFileType(FTP.BINARY_FILE_TYPE);
                FTPFile[] filesList = aFtp.listFiles();
                Filenames.fileList.clear();
                Filenames.imageList.clear();
                Filenames.videoList.clear();
                for (FTPFile file : filesList) {
                    String currFile = file.getName();
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(currFile.substring(currFile.lastIndexOf(".") + 1));
                    if (mimeType.contains("image")) {
                        Filenames.imageList.add(currFile);
                    } else if (mimeType.contains("video")) {
                        Filenames.videoList.add(currFile);
                    } else {
                        Filenames.fileList.add(currFile);
                    }
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
