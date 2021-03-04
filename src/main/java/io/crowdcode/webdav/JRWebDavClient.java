package io.crowdcode.webdav;

import io.crowdcode.webdav.data.WebDavDirectory;
import io.crowdcode.webdav.data.WebDavFile;
import io.crowdcode.webdav.data.WebDavFileInputStream;
import io.crowdcode.webdav.data.WebDavLsResult;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.HttpMkcol;
import org.apache.jackrabbit.webdav.client.methods.HttpOptions;
import org.apache.jackrabbit.webdav.client.methods.HttpPropfind;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * @author Marcus Nörder-Tuitje (CROWDCODE)
 * @author Ingo Düppe (CROWDCODE)
 *
 * <p>
 * Very basic client class for abstraction of DAV accesses
 */
public class JRWebDavClient {

    private static final Logger logger = LoggerFactory.getLogger(JRWebDavClient.class);

    private HttpClientContext context;
    private CloseableHttpClient client;
    private String root;
    private URI baseUri;

    /**
     * prepare the client
     *
     * @param uri      - base URI
     * @param username - (sic)
     * @param password - (sic)
     */
    public void init(URI uri, String username, String password) {
        init(uri,username,password,null);
    }
    /**
     * prepare the client
     *
     * @param uri      - base URI
     * @param username - (sic)
     * @param password - (sic)
     * @param timeoutInSec - timeout in seconds
     */
    public void init(URI uri, String username, String password, Integer timeoutInSec) {
        this.root = uri.toASCIIString();
        if (!this.root.endsWith("/")) {
            this.root += "/";
        }

        baseUri = uri;

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort());

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials(username, password));

        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        this.context = HttpClientContext.create();
        this.context.setCredentialsProvider(credsProvider);
        this.context.setAuthCache(authCache);

        if (timeoutInSec == null){
            this.client = HttpClients.custom().setConnectionManager(cm).build();
            return;
        }

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeoutInSec * 1000)
                .setConnectionRequestTimeout(timeoutInSec * 1000)
                .setSocketTimeout(timeoutInSec * 1000).build();
        this.client = HttpClients.custom().setDefaultRequestConfig(config).setConnectionManager(cm).build();

    }

    /**
     * @param uri - the URI to query
     * @return the list of DAV compliance classes
     */
    public Set<String> getDavComplianceClasses(URI uri) throws IOException {
        HttpOptions options = new HttpOptions(uri);
        HttpResponse response = this.client.execute(options, this.context);
        int status = response.getStatusLine().getStatusCode();

        if (!Integer.valueOf(200).equals(status)) {
            logger.error("ERROR! INSTEAD OF HTTP 200 I GOT {}", status);
            throw new DavAccessFailedException("EXPECTED HTTP 200. GOT " + status);
        }

        Set<String> allow = options.getAllowedMethods(response);
        Set<String> complianceClasses = options.getDavComplianceClasses(response);
        return complianceClasses;
    }

    /**
     * @param file the file to load
     * @return an inputstream to the file content
     */
    public WebDavFileInputStream readFile(WebDavFile file) {
        WebDavFileInputStream result = null;
        try {
            File tmp = File.createTempFile(file.getName() + "-", ".tmp");
            logger.debug("Using temporal file {}", tmp);
            tmp.deleteOnExit();

            URI source = file.getURI();

            HttpGet get = new HttpGet(source);
            CloseableHttpResponse response = client.execute(get);

            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {

                InputStream stream = response.getEntity().getContent();
                result = new WebDavFileInputStream(file.getPropertiesPresent(), stream, tmp);

            } else {
                logger.error("ERROR! INSTEAD OF HTTP 200 I GOT {}", status);
                throw new DavAccessFailedException("EXPECTED HTTP 200. GOT " + status);
            }
        } catch (IOException e) {
            logger.error("Error while reading file", e);
        }
        return result;
    }

    /**
     * list a directory
     *
     * @param resource e.g. a subdirectory relative to the base URL
     * @return lsResult containing all subdirs and files
     */
    public WebDavLsResult ls(String resource) throws IOException, DavException {
        WebDavLsResult result = new WebDavLsResult();

        DavPropertyNameSet set = new DavPropertyNameSet();
        set.add(DavPropertyName.create(DavConstants.PROPERTY_DISPLAYNAME));
        set.add(DavPropertyName.create(DavConstants.PROPERTY_RESOURCETYPE));
        set.add(DavPropertyName.create(DavConstants.PROPERTY_SOURCE));
        set.add(DavPropertyName.create(DavConstants.PROPERTY_GETCONTENTLENGTH));
        set.add(DavPropertyName.create(DavConstants.PROPERTY_GETCONTENTTYPE));
        set.add(DavPropertyName.create(DavConstants.PROPERTY_CREATIONDATE));
        set.add(DavPropertyName.create(DavConstants.PROPERTY_GETLASTMODIFIED));

        String uri = baseUri.toString() + ("/" + resource).replace("//", "/");
        URI baseURI = URI.create(uri);
        HttpPropfind propfind = new HttpPropfind(uri, set, 1);
        HttpResponse resp = this.client.execute(propfind, this.context);
        int status = resp.getStatusLine().getStatusCode();
        if (status / 100 != 2) {
            throw new DavAccessFailedException(
                    "Access to " + propfind.toString() + " failed with a non 2xx status. Status was " + status);
        }

        MultiStatus multistatus = propfind.getResponseBodyAsMultiStatus(resp);
        MultiStatusResponse[] responses = multistatus.getResponses();

        for (MultiStatusResponse respons : responses) {
            DavPropertySet found = respons.getProperties(200);
            DavPropertySet notfound = respons.getProperties(404);

            if (notfound.contains(DavPropertyName.GETCONTENTLENGTH)) {
                result.addDirectory(new WebDavDirectory(baseURI, found));
            } else {
                result.addFile(new WebDavFile(baseURI, found));
            }
        }
        return result;
    }

    /**
     * create a directory
     *
     * @param resource e.g. the subdirectory relative to the base URL
     * @return directory containing the properties
     */
    public WebDavDirectory mkdir(String resource) throws IOException, DavException {
        String uri = baseUri.toString() + ("/" + resource).replace("//", "/");
        HttpMkcol mkcol = new HttpMkcol(uri);
        HttpResponse resp = this.client.execute(mkcol, this.context);
        int status = resp.getStatusLine().getStatusCode();
        if (status / 100 != 2) {
            throw new DavAccessFailedException(
                    "Access to " + mkcol.toString() + " failed with a non 2xx status. Status was " + status);
        }
        WebDavLsResult webDavLsResult = ls(resource);
        List<WebDavDirectory> webDavDirectories = webDavLsResult.getDirectories();
        if (webDavDirectories.isEmpty()) {
            throw new DavAccessFailedException(
                    "Directory " + mkcol.toString() + " will not be found.");
        }
        return webDavDirectories.get(0);


    }

    /**
     * put data to a directory
     *
     * @param fileName the file name relative to the base URL
     * @return file containing the properties
     */
    public WebDavFile put(byte[] content, String fileName) throws IOException, DavException {
        String uri = baseUri.toString() + ("/" + fileName).replace("//", "/");
        HttpPut httpPut = new HttpPut(uri);
        ByteArrayEntity entity = new ByteArrayEntity(content);
        httpPut.setEntity(entity);
        HttpResponse resp = this.client.execute(httpPut, this.context);
        int status = resp.getStatusLine().getStatusCode();
        if (status / 100 != 2) {
            throw new DavAccessFailedException(
                    "Access to " + httpPut.toString() + " failed with a non 2xx status. Status was " + status);
        }
        WebDavLsResult webDavLsResult = ls(fileName);
        List<WebDavFile> webDavFileList = webDavLsResult.getFiles();
        if (webDavFileList.isEmpty()) {
            throw new DavAccessFailedException(
                    "No File " + httpPut.toString() + " will be found.");
        }
        return webDavFileList.get(0);
    }

    /**
     * Exception fired if an DAV access screws up
     */
    public static final class DavAccessFailedException extends RuntimeException {

        public DavAccessFailedException(String message) {
            super(message);
        }
    }

}
