package io.crowdcode.webdav.data;

import static org.apache.jackrabbit.webdav.DavConstants.PROPERTY_DISPLAYNAME;
import static org.apache.jackrabbit.webdav.DavConstants.PROPERTY_RESOURCETYPE;
import static org.apache.jackrabbit.webdav.DavConstants.PROPERTY_SOURCE;

import org.apache.jackrabbit.webdav.property.DavPropertySet;

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
