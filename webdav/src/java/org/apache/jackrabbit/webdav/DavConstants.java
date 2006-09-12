/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.webdav;

import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.jackrabbit.webdav.util.HttpDateFormat;

import java.text.DateFormat;

/**
 * <code>DavConstants</code> provide constants for request and response
 * headers, Xml elements and property names defined by
 * <a href="http://www.ietf.org/rfc/rfc2518.txt">RFC 2518</a>. In addition
 * common date formats (creation date and modification time) are included.
 */
public interface DavConstants {

    /**
     * Request and response headers and some value constants
     */
    //-------------------------------------------------------------- Headers ---
    public static final String HEADER_DAV = "DAV";
    public static final String HEADER_DESTINATION = "Destination";
    public static final String HEADER_IF = "If";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_CONTENT_LANGUAGE = "Content-Language";
    public static final String HEADER_ETAG = "ETag";
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";

    //---------------------------------------------------- Lock-Token header ---
    public static final String HEADER_LOCK_TOKEN = "Lock-Token";
    public static final String OPAQUE_LOCK_TOKEN_PREFIX = "opaquelocktoken:";

    //------------------------------------------------------- Timeout header ---
    public static final String HEADER_TIMEOUT = "Timeout";
    public static final String TIMEOUT_INFINITE = "Infinite";
    // RFC 2518: timeout value for TimeType "Second" MUST NOT be greater than 2^32-1
    public static final long INFINITE_TIMEOUT = Integer.MAX_VALUE;
    public static final long UNDEFINED_TIMEOUT = Integer.MIN_VALUE;
    
    //----------------------------------------------------- Overwrite header ---
    public static final String HEADER_OVERWRITE = "Overwrite";

    //--------------------------------------------------------- Depth header ---
    public static final String HEADER_DEPTH = "Depth";
    public static final String DEPTH_INFINITY_S = "infinity";
    public static final int DEPTH_INFINITY = Integer.MAX_VALUE;
    public static final int DEPTH_0 = 0;
    public static final int DEPTH_1 = 1;

    /**
     * Default Namespace constant
     */
    public static final Namespace NAMESPACE = Namespace.getNamespace("D", "DAV:");
    /**
     * XML Namespace constant
     */
    public static final Namespace NAMESPACE_XML =
        Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace");

    /**
     * Xml element names used for response and request body
     */
    public static final String XML_ALLPROP = "allprop";
    public static final String XML_COLLECTION = "collection";
    public static final String XML_DST = "dst";
    public static final String XML_HREF = "href";
    public static final String XML_KEEPALIVE = "keepalive";
    public static final String XML_LINK = "link";
    public static final String XML_MULTISTATUS = "multistatus";
    public static final String XML_OMIT = "omit";
    public static final String XML_PROP = "prop";
    public static final String XML_PROPERTYBEHAVIOR = "propertybehavior";
    public static final String XML_PROPERTYUPDATE = "propertyupdate";
    public static final String XML_PROPFIND = "propfind";
    public static final String XML_PROPNAME = "propname";
    public static final String XML_PROPSTAT = "propstat";
    public static final String XML_REMOVE = "remove";
    public static final String XML_RESPONSE = "response";
    public static final String XML_RESPONSEDESCRIPTION = "responsedescription";
    public static final String XML_SET = "set";
    public static final String XML_SOURCE = "source";
    public static final String XML_STATUS = "status";

    /**
     * XML element names related to locking and security
     */
    public static final String XML_ACTIVELOCK = "activelock";
    public static final String XML_DEPTH = "depth";
    public static final String XML_LOCKTOKEN = "locktoken";
    public static final String XML_TIMEOUT = "timeout";
    public static final String XML_LOCKSCOPE = "lockscope";
    public static final String XML_EXCLUSIVE = "exclusive";
    public static final String XML_SHARED = "shared";
    public static final String XML_LOCKENTRY = "lockentry";
    public static final String XML_LOCKINFO = "lockinfo";
    public static final String XML_LOCKTYPE = "locktype";
    public static final String XML_WRITE = "write";
    public static final String XML_OWNER = "owner";
    public static final String XML_PRIVILEGE = "privilege";
    public static final String XML_READ = "read";

    /**
     * XML attribute names
     */
    public static final String XML_LANG = "lang";

    /**
     * Webdav property names as defined by RFC 2518<br>
     * Note: Microsoft webdav clients as well as Webdrive request additional
     * property (e.g. href, name, owner, isRootLocation, isCollection)  within the
     * default namespace, which are are ignored by this implementation, except
     * for the 'isCollection' property, needed for XP built-in clients.
     */
    public static final String PROPERTY_CREATIONDATE = "creationdate";
    public static final String PROPERTY_DISPLAYNAME = "displayname";
    public static final String PROPERTY_GETCONTENTLANGUAGE = "getcontentlanguage";
    public static final String PROPERTY_GETCONTENTLENGTH = "getcontentlength";
    public static final String PROPERTY_GETCONTENTTYPE = "getcontenttype";
    public static final String PROPERTY_GETETAG = "getetag";
    public static final String PROPERTY_GETLASTMODIFIED = "getlastmodified";
    public static final String PROPERTY_LOCKDISCOVERY = "lockdiscovery";
    public static final String PROPERTY_RESOURCETYPE = "resourcetype";
    public static final String PROPERTY_SOURCE = "source";
    public static final String PROPERTY_SUPPORTEDLOCK = "supportedlock";

    public static final String VALUE_YES = "yes";
    public static final String VALUE_NO = "no";

    //--------------------------------------------------- Propfind constants ---
    public static final int PROPFIND_BY_PROPERTY = 0;
    public static final int PROPFIND_ALL_PROP = 1;
    public static final int PROPFIND_PROPERTY_NAMES = 2;

    //--------------------------------------------------------- date formats ---
    /**
     * modificationDate date format per RFC 1123
     */
    public static DateFormat modificationDateFormat = new HttpDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

    /**
     * Simple date format for the creation date ISO representation (partial).
     */
    public static DateFormat creationDateFormat = new HttpDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
}
