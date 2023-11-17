package com.example.bobormapsexplorer;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Writer {
    private String filename = "data";
    private FileOutputStream fos;

    Writer(Context ctx){
        try {
            fos = ctx.openFileOutput(filename, Context.MODE_APPEND);
        } catch (IOException e) {
            //
        }

    }

    public void appendData(String str) {
        try {

            fos.write(str.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while writing to the CSV file.");
        }
    }

    public void close(){
        try {
            fos.close();
        } catch (IOException e) {
            //
        }
    }

    public boolean clear(){
        return new File(filename).delete();
    }
}
