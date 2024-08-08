package v2.crawler;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Custom cookie store to handle modern cookie attributes.
 */
public class CustomCookieStore {
    private Map<String, HttpCookie> cookieMap;

    public CustomCookieStore() {
        cookieMap = new HashMap<>();
    }

    /**
     * Parses and stores cookies from a Set-Cookie header.
     */
    public void storeCookies(String header) {
        List<HttpCookie> cookies = HttpCookie.parse(header);
        for (HttpCookie cookie : cookies) {
            cookieMap.put(cookie.getName(), cookie);
        }
    }

    /**
     * Retrieves cookies for a given URL.
     */
    public String getCookiesForUrl(String url) {
        return cookieMap.values().stream()
                .filter(cookie -> url.contains(cookie.getDomain()))
                .map(HttpCookie::toString)
                .collect(Collectors.joining("; "));
    }
}
