package com.example.wcp.cutoutavatar.util;

import android.graphics.Bitmap;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by wcp on 2016/7/20.
 */
public class FileUtil {

    public static void saveMyBitmap(Bitmap bitmap,String dirFile) {
        File f = new File(dirFile);
        FileOutputStream fOut = null;
        try {
            f.createNewFile();
            fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fOut != null) {
                    fOut.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param bytes
     * @param fileDir
     */
    public static void writeBytesToFile(byte[] bytes,String fileDir){
        if(TextUtils.isEmpty(fileDir)){
            return;
        }
        try {
            File file = new File(fileDir);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();
            System.out.println("写入成功：");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String getFileContent(File file){
        if(!file.exists()){
            return "";
        }
        StringBuffer sb = new StringBuffer();
        try {
            FileInputStream fInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fInputStream, "UTF-8");

            BufferedReader br = new BufferedReader(inputStreamReader);
            String str = null;
            while ((str = br.readLine()) != null) {
                sb.append(str.trim());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
