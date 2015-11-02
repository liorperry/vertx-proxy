package io.vertx.example.web.proxy.repository;

import io.vertx.example.web.proxy.filter.ParseUtils;

import java.util.Map;

import static io.vertx.example.web.proxy.filter.ParseUtils.*;

/**
 * Created by INTERNET on 29/10/2015.
 */
public interface Repository {

    public Map<String, String> getServices();

    public Map<String, String> getProducts();

    public boolean getService(String uri);

    public boolean getProduct(String uri);

    public static String extractService(String uri) {
        if(!checkValid(uri) ) {
            return "";
        }
        int beginIndex = uri.indexOf("/");
        if(count(uri, '/') ==1)
            return uri.replace("/","");
        //else
        return uri.substring(beginIndex+1, uri.indexOf("/",beginIndex+1));
    }

     public static boolean checkValid(String uri) {
         return !uri.isEmpty() && (count(uri, '/') > 0);

     }

    public static String extractProduct(String uri) {
        int beginIndex = uri.lastIndexOf("/");
        if(count(uri, '/') == 1)
            return uri.replace("/","");
        //else
        return uri.substring(beginIndex+1,uri.length());
    }

}
