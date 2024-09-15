package id.durianpay.android.utils;

import android.content.Context;

import java.io.File;
import java.util.HashMap;

//class to get provide naming, extension based on file names
public class WebResourceHelper {
    private static HashMap<String, String> hashMap = new HashMap<>(); //hasmap to store all the mime type and directory names

    public WebResourceHelper() {
        hashMap.put("cssMime", "text/css");
        hashMap.put("jsMime", "text/javascript");
        hashMap.put("pngMime", "image/png");
        hashMap.put("jpgMime", "image/jpeg");
        hashMap.put("svgMime", "image/svg+xml");

        hashMap.put("cssChildDir", "css_data");
        hashMap.put("jsChildDir", "js_data");
        hashMap.put("pngChildDir", "image_data");
        hashMap.put("jpgChildDir", "image_data");
        hashMap.put("svgChildDir", "svg_data");
    }

    //returns the mime type of file
    public String getMimeType(String fileExtension){
        String key = fileExtension + "Mime";
        return hashMap.get(key);
    }

    //return extension of file
    public static String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    //return a local path for file creation by creating or using an existing directory
    public String getLocalFilePath(String url, Context context){
        String fileNameForUrl = getLocalFileNameForUrl(url);
        String child = getChildDir(url);

        String localFilePath = "";
        if (child != null) {
            System.out.println(child);
            File directory = new File(context.getFilesDir(), child);
            localFilePath = directory.getAbsolutePath()+"/" +fileNameForUrl;
        }

        return localFilePath;
    }

    //return name of file from url
    public static String getLocalFileNameForUrl(String url){
        String localFileName = "";
        String[] parts = url.split("/");
        if(parts.length > 0){
            localFileName = parts[parts.length-1];
        }
        return localFileName;
    }

    //return the sub directory to store the files or resources
    public String getChildDir(String url){
        String fileNameForUrl = getLocalFileNameForUrl(url);
        String fileExt = getFileExt(fileNameForUrl);
        String key = fileExt + "ChildDir";

        return hashMap.get(key);
    }
}
