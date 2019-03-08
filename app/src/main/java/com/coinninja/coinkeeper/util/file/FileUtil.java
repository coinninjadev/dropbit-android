package com.coinninja.coinkeeper.util.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {

    private FileOutputUtil fileOutputUtil;

    public FileUtil(FileOutputUtil fileOutputUtil) {
        this.fileOutputUtil = fileOutputUtil;
    }

    public boolean delete(File file) {
        boolean deleted = false;

        if (file.exists()) {
            deleted = file.delete();
        }

        return deleted;
    }

    public boolean createFile(File file) {
        boolean created = false;

        try {
            created = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return created;
    }

    public void writeBytes(byte[] bytes, File file) throws IOException {
        FileOutputStream fileOutputStream = fileOutputUtil.getStreamFor(file);
        fileOutputStream.write(bytes);
    }

    public void writeByteStream(ByteArrayOutputStream byteArrayOutputStream, File file) {
        try {
            writeBytes(byteArrayOutputStream.toByteArray(), file);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
