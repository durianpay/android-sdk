package id.durianpay.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.os.Build;
import android.os.FileUtils;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

//A Worker class to download all the resources i.e., image, css and js to implement a local cache and load into the webview when required
public class PeriodicResourceDownload extends Worker {

    public PeriodicResourceDownload(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    //download resources on a background thread and return a result
    @NonNull
    @Override
    public Result doWork() {
        try {
            // get all the the urls from the resource_list.json file present in the assets folder
            JSONObject obj = new JSONObject(Utils.loadJSONFromAsset(getApplicationContext()));
            JSONArray imgArray = obj.getJSONArray("url");
            JSONArray jsArray = obj.getJSONArray("js");
            JSONArray cssArray = obj.getJSONArray("css");
            JSONArray svgArray = obj.getJSONArray("svg");

            //download all the jpeg, png images
            for (int i = 0; i < imgArray.length(); i++){
                String[] parts = imgArray.get(i).toString().split("/");
                String name = parts[parts.length-1];
                URL url = new URL(imgArray.get(i).toString());
                //Create Path to save Image
                File directory = new File(getApplicationContext().getFilesDir(), "image_data");
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                Bitmap bm = BitmapFactory.decodeStream(url.openConnection().getInputStream()); //get the image as bytes and convert to bitmap
                File imageFile = new File(directory.getAbsolutePath()+"/" +name); //create a file with the resource name
                FileOutputStream out = new FileOutputStream(imageFile); //store the file in local path and directory image_data
                bm.compress(Bitmap.CompressFormat.PNG, 100, out); // Compress Image
                out.flush();
                out.close(); //close the stream
            }

            //download all the svg images
            for (int i = 0; i < svgArray.length(); i++){
                String[] parts = svgArray.get(i).toString().split("/");
                String name = parts[parts.length-1];
                URL url = new URL(svgArray.get(i).toString());
                //Create Path to save Image
                File directory = new File(getApplicationContext().getFilesDir(), "svg_data");
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                InputStream svgInputStream = url.openConnection().getInputStream(); //get the image as bytes
                File imageFile = new File(directory.getAbsolutePath()+"/" +name); //create a file with the resource name
                FileOutputStream out = new FileOutputStream(imageFile); //store the file in local path and directory svg_data
                copyBytes(svgInputStream, out); //save the image in the file created
                out.flush();
                out.close(); //close the stream
            }

            //download all the js files
            for (int i = 0; i < jsArray.length(); i++){
                writeToFile(jsArray.get(i).toString());
            }

            //download all the css files
            for (int i = 0; i < cssArray.length(); i++){
                writeToFile(cssArray.get(i).toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.success(); //return a successful result
    }

    public void copyBytes(InputStream input, OutputStream output) {
        try {
            //Creating byte array
            byte[] buffer = new byte[1024];
            int length = input.read(buffer);

            //Transferring data
            while (length != -1) {
                output.write(buffer, 0, length);
                length = input.read(buffer);
            }
            //Finalizing
            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToFile(String url) {
        //use the WebResourceHelper class to get the name, path, mime type from the URL
        String name = WebResourceHelper.getLocalFileNameForUrl(url);
        String childDir = new WebResourceHelper().getChildDir(url);
        try {
            URL urlConn = new URL(url);
            //Create Path to save files
            File directory = new File(getApplicationContext().getFilesDir(), childDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            //download the resource, create a file with the name, store the file
            InputStream inputStream = urlConn.openConnection().getInputStream();
            File file = new File(directory.getAbsolutePath()+"/" +name);
            FileOutputStream outputStream = new FileOutputStream(file);

            //transferring data
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                FileUtils.copy(inputStream, outputStream);
            } else {
                byte[] buffer = new byte[10 * 1024];
                for (int n; (n = inputStream.read(buffer)) != -1; ) {
                    outputStream.write(buffer, 0, n);
                }
            }
            //close the stream
            outputStream.flush();
            outputStream.close();
        } catch (MalformedURLException e) {
            System.out.println(e.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.toString());
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}
