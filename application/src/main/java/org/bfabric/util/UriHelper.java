/*
    MIT License

    Copyright (c) 2005-2026 Functional Genomics Center Zurich, UZH/ETH Zurich

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package org.bfabric.util;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bfabric.Constants;
import org.bfabric.entity.AccessProtocol;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.manager.AbstractHttpServletManager;

@Named
@ApplicationScoped
public class UriHelper extends AbstractHttpServletManager {

    protected static final Logger logger = Logger.getLogger(UriHelper.class.getName());

    public static int checkUrl(String url) {
        try {
            URL connectionURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) connectionURL.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            return connection.getResponseCode();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            return 0;
        }
    }

    public static URI createUri(String uriString) throws InvalidDataException {
        return createUri(uriString, true);
    }

    public static URI createUri(String uriString, boolean logWarning) throws InvalidDataException {
        try {
            return new URI(uriString);
        } catch (URISyntaxException e) {
            if (logWarning) {
                logger.warning("Create URI failed for " + uriString + ": " + e.getMessage());
            }
            throw new InvalidDataException("There is a syntax problem with uri " + uriString + ": " + e.getMessage());
        }
    }

    @SuppressFBWarnings("NP_BOOLEAN_RETURN_NULL")
    public static Boolean existsUrl(String url) {
        int responseCode = checkUrl(url);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return Boolean.TRUE;
        }
        if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            return Boolean.FALSE;
        }
        return null;
    }

    public static String getCid(String uri) {
        String ret = null;
        if (!StringHelper.isEmpty(uri) && uri.contains("?")) {
            String[] parameters = uri.split("\\?")[1].split("&");
            for (String parameter : parameters) {
                String[] splits = parameter.split("=");
                if (splits[0].equals("cid")) {
                    ret = splits[1];
                    break;
                }
            }
        }
        return ret;
    }

    public static String getHost(String uri) throws InvalidDataException {
        String host = createUri(uri).getHost();
        if (host == null || host.isEmpty()) {
            throw new InvalidDataException("Invalid host: " + uri);
        }
        return host;
    }

    public static String getPath(String uri) throws InvalidDataException {
        String path = createUri(uri).getPath();
        if (path == null || path.isEmpty()) {
            throw new InvalidDataException("Invalid path: " + uri);
        }
        return path;
    }

    public static int getPort(String uri) throws InvalidDataException {
        int port = createUri(uri).getPort();
        if (port == -1) {
            throw new InvalidDataException("Invalid port: " + uri);
        }
        return port;
    }

    public static String getProtocol(String uri) throws InvalidDataException {
        if (uri == null || !uri.contains(Constants.PROTOCOL_SEPARATOR)) {
            throw new InvalidDataException("No protocol specified in uri " + uri + ".");
        }
        return uri.substring(0, uri.indexOf(Constants.PROTOCOL_SEPARATOR));
    }

    public static String getUri(AccessProtocol protocol, String host, String path) {
        StringBuilder uri = new StringBuilder();
        if (protocol != null) {
            uri.append(protocol.getName());
            uri.append(Constants.PROTOCOL_SEPARATOR);
        }
        if (StringHelper.isNotEmpty(host)) {
            uri.append(host);
        }
        if (StringHelper.isNotEmpty(path)) {
            uri.append(path);
        }
        return uri.toString();
    }

    public static String getUriWithoutQueryDetails(String uri) {
        if (uri != null && uri.contains("?")) {
            return uri.substring(0, uri.indexOf("?"));
        }
        return uri;
    }

    public static String getUrlScreen(String className, String screen) {
        return StringHelper.isNotEmpty(className) && StringHelper.isNotEmpty(screen) ? "/" + className.toLowerCase() + "/" + screen + ".xhtml" : Constants.EMPTY_STRING;
    }

    public static String getUrlShowScreen(String className) {
        return getUrlScreen(className, Constants.SHOW);
    }

    public static boolean isValidUri(String uriString) {
        try {
            createUri(uriString, false);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidUrl(String url) {
        return url != null && (url.startsWith(Constants.HTTP) || url.startsWith(Constants.HTTPS)) && isValidUri(url);
    }

    public static String normalize(String uri) {
        try {
            if (StringHelper.isNotEmpty(uri)) {
                return new URI(uri).normalize().toString();
            }
        } catch (Exception e) {
            logger.fine("Cannot normalize: invalid uri " + uri);
        }
        return uri;
    }

    public static String removeCid(String uri) {
        if (StringHelper.isNotEmpty(uri)) {
            // Remove all non-first cid parameters: &cid=...&...
            String ret = uri.replaceAll("&cid=([^&]*)", Constants.EMPTY_STRING);
            // Remove cid parameter when it is the first one in the uri followed by other parameters: ?cid=...&...
            ret = ret.replaceAll("\\?cid=([^&]*&)", "\\?");
            // Remove cid parameter when it is the single parameter in the uri ?cid=...
            ret = ret.replaceAll("\\?cid=([^&]*)", Constants.EMPTY_STRING);
            return ret;
        }
        return null;
    }

    public static String removeProtocol(String uri) {
        String ret = uri;
        if (StringHelper.isNotEmpty(ret)) {
            int protocolLength = ret.indexOf(Constants.PROTOCOL_SEPARATOR);
            if (protocolLength > -1) {
                ret = ret.substring(protocolLength + Constants.PROTOCOL_SEPARATOR.length());
            }
        }
        return ret;
    }

    public static String replaceProtocol(String uri, String protocol) {
        String ret = uri;
        if (ret != null) {
            int protocolLength = ret.indexOf(Constants.PROTOCOL_SEPARATOR);
            if (protocolLength > -1 && StringHelper.isNotEmpty(protocol)) {
                ret = protocol + ret.substring(protocolLength);
            }
        }
        return ret;
    }

    public List<Pair<String, String>> getRequestParametersWithoutCid() {
        List<Pair<String, String>> ret = new ArrayList<>();
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        if (httpServletRequest != null) {
            String queryString = removeCid(httpServletRequest.getQueryString());
            if (!StringHelper.isEmpty(queryString)) {
                String[] parameters = queryString.split("&");
                for (String parameter : parameters) {
                    String[] splits = parameter.split("=");
                    if (splits.length == 2) {
                        ret.add(new ImmutablePair<>(splits[0], splits[1]));
                    } else if (splits.length == 1) {
                        if (parameter.startsWith("=")) {
                            ret.add(new ImmutablePair<>("", splits[0]));
                        } else if (parameter.endsWith("=")) {
                            ret.add(new ImmutablePair<>(splits[0], Constants.EMPTY_STRING));
                        }
                    }
                }
            }
        }
        return ret;
    }

    public boolean isRenderRefreshedUrl() {
        String url = getRequestURL();
        return url != null && (url.contains("/show") || url.contains("/list") || url.contains("/manage"));
    }
}
