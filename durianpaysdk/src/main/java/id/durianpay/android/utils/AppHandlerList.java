package id.durianpay.android.utils;

import java.util.HashMap;
import java.util.Map;

public class AppHandlerList {

    //return a hashmap of bank VA applications with their packages names to be tracked
    public static Map<String, String> getBankAppList() {
        Map<String, String> bankAppList;
        bankAppList = new HashMap<>();

        bankAppList.put("bca", "com.bca");
        bankAppList.put("cimb", "id.co.cimbniaga.mobile.android");
        bankAppList.put("bri", "bri.delivery.brimobile");
        bankAppList.put("permata", "net.myinfosys.PermataMobileX");
        bankAppList.put("bni", "src.com.bni");
        bankAppList.put("mandiri", "com.bankmandiri.mandirionline");

        return bankAppList;
    }
}
