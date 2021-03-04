package io.crowdcode.webdav.data;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertySet;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * WebDav File Representation
 */
public class WebDavFile implements WebDavElement {

    private final URI baseURI;
    private final DavPropertySet propertiesPresent;

    public WebDavFile(URI baseURI, DavPropertySet propertiesPresent) {
        this.baseURI = baseURI;
        this.propertiesPresent = propertiesPresent;
    }

    public String getCreated(){
        return propertiesPresent.get(DavConstants.PROPERTY_CREATIONDATE).getValue().toString();
    }

    public String getLastModified(){
        return  propertiesPresent.get(DavConstants.PROPERTY_GETLASTMODIFIED).getValue().toString();
    }

    public String getContentType() {
        return propertiesPresent.get(DavConstants.PROPERTY_GETCONTENTTYPE).getValue().toString();
    }

    public Integer getLength() {
        return Integer.valueOf(propertiesPresent.get(DavConstants.PROPERTY_GETCONTENTLENGTH).getValue().toString());
    }

    public String getNameUrlEncoded() {
        try {
            return URLEncoder.encode(getName(), StandardCharsets.UTF_8.toString()).replaceAll("\\+","%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public URI getURI() {
        String b = baseURI.toString();
        return URI.create(b.endsWith("/") ? b + getNameUrlEncoded() : b + "/" + getNameUrlEncoded());
    }

    public URI getBaseURI() {
        return baseURI;
    }

    @Override
    public DavPropertySet getPropertiesPresent() {
        return propertiesPresent;
    }
}
