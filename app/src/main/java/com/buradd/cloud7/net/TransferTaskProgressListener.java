package com.buradd.cloud7.net;

public interface TransferTaskProgressListener {

    void onBeginTransferTask(TransferTask task);

    void onBeginTransfer(TransferTask task, int transferId);

    void onProgressUpdate(TransferTask task, int transferId, final int aProgress);

    void onEndTransfer(TransferTask task, int transferId);

    void onTransferFailed(TransferTask task, int transferId, Exception aException);
}