package org.example;

import java.io.Serializable;

public class FileTransfer implements Serializable {
    private byte[] fileData;
    private String fileName;

    public FileTransfer(byte[] fileData, String fileName) {
        this.fileData = fileData;
        this.fileName = fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public String getFileName() {
        return fileName;
    }
}
