package de.adorsys.multibanking.config.core;

import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * ProxyStreamHandlerFactory
 * Erzeugt eine URL-Connection mit Proxy
 *
 * Format: proxy://proxy_host:proxy_port/http:...
 */
@Configuration
public class ProxyStreamHandlerFactory implements URLStreamHandlerFactory {

    private Map<String, Proxy> proxyCache;

    public ProxyStreamHandlerFactory() {
        proxyCache = new HashMap<>();

        TomcatURLStreamHandlerFactory.getInstance().addUserFactory(this);
    }

    private Proxy createProxy(String host, int port) {
        return proxyCache.computeIfAbsent(String.format("%s:%d", host, port),
                proxy -> new Proxy(Type.HTTP, new InetSocketAddress(host, port)));
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (!"proxy".equals(protocol)) {
            return null;
        }
        return new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL url) throws IOException {
                String proxyHost = url.getHost();
                int proxyPort = url.getPort();
                String destination = clean(url.getFile());
                return new URL(destination).openConnection(createProxy(proxyHost, proxyPort));

            }
        };
    }

    private String clean(String path) {
        if (path != null && path.startsWith("/")) {
            return clean(path.substring(1));
        }
        return path;
    }

}
