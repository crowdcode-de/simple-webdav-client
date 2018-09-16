package io.crowdcode.webdav.data;

import org.apache.jackrabbit.webdav.property.DavPropertySet;

import static org.apache.jackrabbit.webdav.DavConstants.*;

/**
 * WebDav Element interface to provide a unified object model
 */
public interface WebDavElement {

    DavPropertySet getPropertiesPresent();

    default String getName() {
        return getPropertiesPresent().get(PROPERTY_DISPLAYNAME).getValue().toString();
    }

    default String getResourceType() {
        return getPropertiesPresent().get(PROPERTY_RESOURCETYPE).getValue().toString();
    }

    default String getSource() {
        return getPropertiesPresent().get(PROPERTY_SOURCE).getValue().toString();
    }
}
