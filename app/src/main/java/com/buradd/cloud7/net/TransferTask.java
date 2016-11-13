package com.buradd.cloud7.net;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import com.buradd.cloud7.MainActivity;

import java.io.IOException;
import java.util.List;

public abstract class TransferTask
        extends AsyncTask<Transfer, TransferTask.ProgressDescription, String>{

    private final static String TAG = "TransferTask";

    protected final List<Transfer> mTransferList;
    protected Context mContext;
    protected TransferTaskProgressListener mProgressListener;
    protected Transfer mCurrentTransfer;

    public TransferTask(TransferTaskProgressListener progressListener, List<Transfer> transferList,
                        final Context aContext){
        mProgressListener = progressListener;
        mTransferList = transferList;
        mContext = aContext;
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        mProgressListener.onBeginTransferTask(this);
    }

    @Override
    protected String doInBackground(Transfer... transfers){

        for(; ; ){
            mCurrentTransfer = null;
            synchronized(mTransferList){
                for(Transfer t : mTransferList){
                    if(t.isPending()){
                        mCurrentTransfer = t;
                        mCurrentTransfer.setPending(false);
                        break;
                    }
                }
            }

            // No pending transfer
            if(mCurrentTransfer == null){
                break;
            }

            // Prepare chosen transfer
            mCurrentTransfer.setProgress(0);
            publishProgress(new ProgressDescription(mCurrentTransfer.getName(), mCurrentTransfer.getId(), 0));

            // Download or upload
            if(mCurrentTransfer.getDirection() == TransferDirection.DOWNLOAD){
                Log.d(TAG, "Now Downloading '" + mCurrentTransfer.getFullSourcePath() + "/" + mCurrentTransfer.getName());
                try {
                    doInBackgroundDownload();
                }
                catch (IOException e)
                {
                    publishProgress(new ProgressDescription(mCurrentTransfer.getFullSourcePath(), e, mCurrentTransfer.getId(), -1));
                }
            }
            else /* if (transfer.getDirection() == TransferDirection.UPLOAD) */
            {
                int retryCount = 0;
                do {
                    try {
                        doInBackgroundUpload();
                    } catch (IOException e) {
                        publishProgress(new ProgressDescription(mCurrentTransfer.getName(), e, mCurrentTransfer.getId(), -1));
                        e.printStackTrace();
                        retryCount++;
                    }
                } while(retryCount >  0 && retryCount < 5);
            }
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(ProgressDescription... values){
        super.onProgressUpdate(values);
        Integer[] codes = values[0].codes;
        int transferId = codes[0];
        int progress = codes[1];
        String filename = values[0].filename;
        MainActivity mainActivity = MainActivity.getInstance();

        if(filename != null && !filename.isEmpty()) {
            //filename may end in '/'
            if(filename.endsWith("/"))
            {
                filename = filename.substring(0, filename.length()-1);
            }
            int index = filename.lastIndexOf("/");
            if (index > 0) {
                filename = filename.substring(index + 1);
            }
        }

        // Publish update
        switch(progress){
            case -1:
                mProgressListener.onTransferFailed(this, transferId, values[0].exception);
                if(mainActivity != null)
                {
                    mainActivity.hideDownloadProgressDialog();;
                }
                break;
            case 0:
                mProgressListener.onBeginTransfer(this, transferId);
                if(mainActivity != null) {
                    mainActivity.showDownloadProgressDialog();
                    String msg = "Transferring:";  //mContext.getResources().getString(R.string.Transferring);
                    if(filename != null)
                    {
                        msg += "\n" + filename;
                    }
                    mainActivity.updateDownloadProgress(msg, progress);
                }
                break;
            case 101: /* Finished */
                mProgressListener.onEndTransfer(this, transferId);
                if(mainActivity != null)
                {
                    mainActivity.hideDownloadProgressDialog();
                }
                break;
            default:
                mProgressListener.onProgressUpdate(this, transferId, progress);
                if(mainActivity != null)
                {
                    String msg = "Transferring:"; //mContext.getResources().getString(R.string.Transferring);
                    if(filename != null) {
                        msg += "\n" + filename;
                    }
                    mainActivity.updateDownloadProgress(msg, progress);
                }

        }
    }

    protected abstract void doInBackgroundDownload() throws IOException;

    protected abstract void doInBackgroundUpload() throws IOException;

    public static class ProgressDescription{
        public Exception exception;
        public Integer[] codes;
        public String filename;

        public ProgressDescription(final String aFilename, final Exception aException, final Integer... aCodes){
            exception = aException;
            codes = aCodes;
            filename = aFilename;
        }

        public ProgressDescription(final String aFilename, final Integer... aCodes){
            codes = aCodes;
            filename = aFilename;
        }
    }
}