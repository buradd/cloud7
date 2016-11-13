package com.buradd.cloud7.net;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FTPSingleFileTransferTask
        extends TransferTask{

    private final ConnectionParams mParams;
    private FTPClient mFTPClient;
    private String sourcePath;
    private String destPath;

    public FTPSingleFileTransferTask(Context aContext, TransferTaskProgressListener progressListener,
                                     List<Transfer> transferList,
                                     ConnectionParams aParams){
        super(progressListener, transferList, aContext);
        mParams = aParams;
    }

    @Override
    protected String doInBackground(Transfer... transfers){

        // Create ftp client
        mFTPClient = new FTPClient();
        mFTPClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

        String retval = super.doInBackground(transfers);

        // Disconnect ftp client
        if((mFTPClient != null) && mFTPClient.isConnected()){
            try{
                mFTPClient.logout();
                mFTPClient.disconnect();
            } catch(IOException e){
                e.printStackTrace();
            }
        }

        return retval;
    }

    @Override
    protected void doInBackgroundDownload(){
        try{
            if(!mFTPClient.isConnected()){
                connect();
            }

            mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);

            sourcePath = mCurrentTransfer.getSourcePath();
            destPath = mCurrentTransfer.getDestinationPath();


            if(!mFTPClient.printWorkingDirectory().equals(mCurrentTransfer.getSourcePath())){
                mFTPClient.changeWorkingDirectory(mCurrentTransfer.getSourcePath());
            }

            FTPFile ftpFile = getFTPFile();
            if(ftpFile == null){
                throw new FileNotFoundException("No such file: " + mCurrentTransfer.getFullSourcePath());
            }
            mCurrentTransfer.setFileSize(ftpFile.getSize());
            boolean fileModified;
            String modificationTime;
            try{
                final String modificationTime1 = mFTPClient.getModificationTime(mCurrentTransfer.getFullSourcePath());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                modificationTime = dateFormat.parse(modificationTime1.split(" ")[1]).toString();
            } catch(Exception e){
                modificationTime = null;
            }

            fileModified = modificationTime == null ||
                    (!TextUtils.isEmpty(modificationTime) &&
                            !modificationTime.equals(mCurrentTransfer.getLastModified()));
            if(!fileModified){
                Log.v("dt.ftp", mCurrentTransfer.getName() + " is not modified since last download time");
            }
            else{
                mCurrentTransfer.setLastModified(modificationTime);
                OutputStream fos = new BufferedOutputStream(
                        new FileOutputStream(mCurrentTransfer.getFullDestinationPath()));
                CountingOutputStream cos = new CountingOutputStream(fos){

                    private int prevoiusProgress = 0;

                    protected void beforeWrite(int n){
                        super.beforeWrite(n);

                        int progress = Math.round((getCount() * 100) / mCurrentTransfer.getFileSize());
                        if(progress != prevoiusProgress){
                            prevoiusProgress = progress;
                            mCurrentTransfer.setProgress(progress);
                            publishProgress(new ProgressDescription(mCurrentTransfer.getName(), mCurrentTransfer.getId(), progress));
                        }
                    }
                };
                // Download file
                mFTPClient.retrieveFile(mCurrentTransfer.getFullSourcePath(), cos);
                // Close local file
                fos.close();
            }

            // End of transfer
            publishProgress(new ProgressDescription(mCurrentTransfer.getName(), mCurrentTransfer.getId(), 101));
        } catch(IOException e){
            publishProgress(new ProgressDescription(destPath, e, mCurrentTransfer.getId(), -1));
            e.printStackTrace();
        }
    }

    private FTPFile getFTPFile() throws IOException{
        final FTPFile[] files = mFTPClient.listFiles(
                mCurrentTransfer.getSourcePath() + "/" + mCurrentTransfer.getName());
        if(files != null && files.length > 0){
            return files[0];
        }
        return null;
    }

    @Override
    protected void doInBackgroundUpload(){
        try{
            // Connect it if disconnected
            if(!mFTPClient.isConnected()){
                connect();
            }
            mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
            FTPFile file = mFTPClient.mlistFile(mCurrentTransfer.getName());
            mCurrentTransfer.setFileSize(file.getSize());

            // Open local file
            InputStream fis = new BufferedInputStream(new FileInputStream(mCurrentTransfer.getFullSourcePath()));
            CountingInputStream cis = new CountingInputStream(fis){

                protected void afterRead(int n){
                    super.afterRead(n);

                    int progress = Math.round((getCount() * 100) / mCurrentTransfer.getFileSize());
                    mCurrentTransfer.setProgress(progress);
                    publishProgress(new ProgressDescription(mCurrentTransfer.getDestinationPath(), mCurrentTransfer.getId(), progress));
                }
            };
            // Go to directory
            if(!mFTPClient.printWorkingDirectory().equals(mCurrentTransfer.getDestinationPath())){
                mFTPClient.changeWorkingDirectory(mCurrentTransfer.getDestinationPath());
            }
            // Upload file
            mFTPClient.storeFile(mCurrentTransfer.getName(), cis);
            // Close local file
            fis.close();
            // End of transfer
            publishProgress(new ProgressDescription(mCurrentTransfer.getDestinationPath(), mCurrentTransfer.getId(), 101));
        } catch(IOException e){
            publishProgress(new ProgressDescription(destPath, e, mCurrentTransfer.getId(), -1));
            e.printStackTrace();
        }
    }

    private void connect() throws IOException{
        // Connect to server
        mFTPClient.setDataTimeout((int) TimeUnit.SECONDS.toMillis(15));
        mFTPClient.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(20));
        mFTPClient.connect(mParams.host);
        // Check the reply code to verify success.
        int reply = mFTPClient.getReplyCode();
        if(!FTPReply.isPositiveCompletion(reply)){
            return;
        }
        if(!mFTPClient.login(mParams.username, mParams.password)){
            mFTPClient.logout();
            return;
        }
        mFTPClient.enterLocalPassiveMode();
    }
}