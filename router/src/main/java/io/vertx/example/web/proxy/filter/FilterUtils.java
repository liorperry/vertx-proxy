package io.vertx.example.web.proxy.filter;

import java.util.Optional;

import static io.vertx.example.web.proxy.filter.ParseUtils.count;

public interface FilterUtils {
    static Optional<String> extractService(String uri) {
        if(!checkValid(uri) ) {
            return Optional.empty();
        }
        int beginIndex = uri.indexOf("/");
        if(count(uri, '/') ==1)
            return Optional.of(uri.replace("/",""));
        //else
        return Optional.of(uri.substring(beginIndex + 1, uri.indexOf("/", beginIndex + 1)));
    }

    static boolean checkValid(String uri) {
        return !uri.isEmpty() && (count(uri, '/') > 0);

    }

    static Optional<String> extractProduct(String uri) {
        int beginIndex = uri.lastIndexOf("/");
        if(count(uri, '/') < 2)
            return Optional.empty();
        //else
        return Optional.of(uri.substring(beginIndex + 1, uri.length()));
    }

}
