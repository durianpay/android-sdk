package id.durianpay.android.utils;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public class Utils {

    //function to load json from the resource_list.json file in assets folder
    public static String loadJSONFromAsset(Context context) {
        try {
            InputStream is = context.getAssets().open("resource_list.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
