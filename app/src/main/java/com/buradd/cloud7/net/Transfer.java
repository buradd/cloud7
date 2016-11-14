package com.buradd.cloud7.net;

import java.io.File;

public class Transfer {

    private int id;
    private boolean pending;
    private TransferDirection direction;
    private String name;
    private String sourcePath;
    private String destinationPath;
    private long fileSize;
    private int progress;
    private String lastModified;

    private int retryCount;

    public Transfer(int id){
        this.id = id;
        pending = true;
        retryCount = 0;
    }

    /**
     * @return the id
     */
    public int getId(){
        return id;
    }

    /**
     * @return the pending
     */
    public boolean isPending(){
        return pending;
    }

    /**
     * @param pending the pending to set
     */
    public void setPending(boolean pending){
        this.pending = pending;
    }

    /**
     * @return the direction
     */
    public TransferDirection getDirection(){
        return direction;
    }

    /**
     * @param direction the direction to set
     */
    public void setDirection(TransferDirection direction){
        this.direction = direction;
    }

    /**
     * @return the name
     */
    public String getName(){
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     * @return the sourcePath
     */
    public String getSourcePath(){
        return sourcePath;
    }

    /**
     * @param sourcePath the sourcePath to set
     */
    public void setSourcePath(String sourcePath){
        this.sourcePath = sourcePath;
    }

    /**
     * @return the destinationPath
     */
    public String getDestinationPath(){
        return destinationPath;
    }

    /**
     * @param destinationPath the destinationPath to set
     */
    public void setDestinationPath(String destinationPath){
        this.destinationPath = destinationPath;
    }

    /**
     * @return the fileSize
     */
    public long getFileSize(){
        return fileSize;
    }

    /**
     * @param fileSize the fileSize to set
     */
    public void setFileSize(long fileSize){
        this.fileSize = fileSize;
    }

    /**
     * @return the progress
     */
    public int getProgress(){
        return progress;
    }

    /**
     * @param progress the progress to set
     */
    public void setProgress(int progress){
        this.progress = progress;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public String toStringSourcePath(){

        if(direction == TransferDirection.UPLOAD){
            return "Local:" + getFullSourcePath();
        } /* else { */
        return "FTP:" + getFullSourcePath();
                /* } */
    }

    public String getLastModified(){
        return lastModified;
    }

    public void setLastModified(final String aLastModified){
        lastModified = aLastModified;
    }

    public String getFullSourcePath(){
        StringBuilder sb = new StringBuilder();

        // Path
        sb.append(sourcePath);
        if(!sourcePath.endsWith(File.separator)){
            sb.append(File.separator);
        }

        // File name
        sb.append(name);

        return sb.toString();
    }

    /**
     *
     */
    public String toStringDestinationPath(){

        if(direction == TransferDirection.DOWNLOAD){
            return "Local:" + getFullDestinationPath();
        } /* else { */
        return "FTP:" + getFullDestinationPath();
                /* } */
    }

    /**
     *
     */
    public String getFullDestinationPath(){
        StringBuilder sb = new StringBuilder();

        // Path
        sb.append(destinationPath);
        if(!destinationPath.endsWith(File.separator)){
            sb.append(File.separator);
        }

        // File name
        sb.append(name);

        return sb.toString();
    }
}