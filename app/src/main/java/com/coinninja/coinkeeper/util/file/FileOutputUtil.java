package com.coinninja.coinkeeper.util.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FileOutputUtil {

    public FileOutputStream getStreamFor(File file) throws FileNotFoundException {
        return new FileOutputStream(file);
    }

}
