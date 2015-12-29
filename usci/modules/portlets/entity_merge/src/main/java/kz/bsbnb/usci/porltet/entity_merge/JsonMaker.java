package kz.bsbnb.usci.porltet.entity_merge;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import java.util.*;

public class JsonMaker {

    private static Gson gson = new Gson();

    public static class JsonFormat{
        int totalCount = 0;
        List<Object> data;
        boolean success;


        public JsonFormat(List data) {
            this.data = data;
            totalCount = data.size();
            success = true;
        }
    }

    public static String getJson(List data){
        return gson.toJson(new JsonFormat(data));
    }

    public static String getJson(Object data){
        Map m = new HashMap();
        m.put("data",data);
        m.put("success",true);
        return gson.toJson(m);
    }

    public static String getNegativeJson(Object data){
        Map m = new HashMap();
        m.put("data", data);
        m.put("success",false);
        return gson.toJson(m);
    }

    public static String getJson(Map m){
        m.put("success", true);
        return gson.toJson(m);
    }

    public static String getCaptionedArray(List<String[]> data, String captions[]){
        List<Map> ret = new LinkedList<>();

        for(String[] row : data) {
            Map m = new HashMap<>();
            for(int i=0;i<Math.min(row.length, captions.length);i++)
                m.put(captions[i], row[i]);
            ret.add(m);
        }

        return gson.toJson(ret);
    }
}

