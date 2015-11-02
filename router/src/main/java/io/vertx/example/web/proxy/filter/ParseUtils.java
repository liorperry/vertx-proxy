package io.vertx.example.web.proxy.filter;

public abstract class ParseUtils {

    public static int count(String uri, char letter) {
        return uri.split("\\"+letter, -1).length-1;
    }

}
