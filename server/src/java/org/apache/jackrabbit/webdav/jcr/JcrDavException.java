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
package org.apache.jackrabbit.webdav.jcr;

import org.apache.log4j.Logger;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import javax.jcr.query.InvalidQueryException;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;
import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.MergeException;
import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.PathNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.util.HashMap;

/**
 * <code>JcrDavException</code> extends the {@link DavException} in order to
 * wrap various repository exceptions.
 */
public class JcrDavException extends DavException {

    private static Logger log = Logger.getLogger(JcrDavException.class);

    // mapping of Jcr exceptions to error codes.
    private static HashMap codeMap = new HashMap();
    static {
        codeMap.put(AccessDeniedException.class, new Integer(DavServletResponse.SC_FORBIDDEN));
        codeMap.put(ConstraintViolationException.class, new Integer(DavServletResponse.SC_CONFLICT));
        codeMap.put(InvalidItemStateException.class, new Integer(DavServletResponse.SC_CONFLICT));
        codeMap.put(InvalidSerializedDataException.class, new Integer(DavServletResponse.SC_BAD_REQUEST));
        codeMap.put(InvalidQueryException.class, new Integer(DavServletResponse.SC_BAD_REQUEST));
        codeMap.put(ItemExistsException.class, new Integer(DavServletResponse.SC_CONFLICT));
        codeMap.put(ItemNotFoundException.class, new Integer(DavServletResponse.SC_FORBIDDEN));
        codeMap.put(LockException.class, new Integer(DavServletResponse.SC_LOCKED));
        codeMap.put(MergeException.class, new Integer(DavServletResponse.SC_CONFLICT));
        codeMap.put(NamespaceException.class, new Integer(DavServletResponse.SC_CONFLICT));
        codeMap.put(NoSuchNodeTypeException.class, new Integer(DavServletResponse.SC_CONFLICT));
        codeMap.put(NoSuchWorkspaceException.class, new Integer(DavServletResponse.SC_CONFLICT));
        codeMap.put(PathNotFoundException.class, new Integer(DavServletResponse.SC_CONFLICT));
        codeMap.put(ReferentialIntegrityException.class, new Integer(DavServletResponse.SC_CONFLICT));
        codeMap.put(RepositoryException.class, new Integer(DavServletResponse.SC_FORBIDDEN));
        codeMap.put(LoginException.class, new Integer(DavServletResponse.SC_UNAUTHORIZED));
        codeMap.put(UnsupportedRepositoryOperationException.class, new Integer(DavServletResponse.SC_NOT_IMPLEMENTED));
        codeMap.put(ValueFormatException.class, new Integer(DavServletResponse.SC_CONFLICT));
        codeMap.put(VersionException.class, new Integer(DavServletResponse.SC_CONFLICT));
    }

    private Class exceptionClass;

    /**
     * Create a new <code>JcrDavException</code>.
     *
     * @param cause The original cause of this <code>DavException</code>. Note, that
     * in contrast to {@link Throwable#Throwable(Throwable)}, {@link Throwable#Throwable(String, Throwable)} and
     * {@link Throwable#initCause(Throwable)} the cause must not be <code>null</code>.
     * @param errorCode Status code for the response.
     * @throws NullPointerException if the given exception is <code>null</code>.
     * @see DavException#DavException(int, String)
     * @see DavException#DavException(int)
     */
    public JcrDavException(Throwable cause, int errorCode) {
        super(errorCode, cause);
        exceptionClass = cause.getClass();
        if (log.isDebugEnabled()) {
            log.debug("Handling exception with error code " + errorCode, cause);
        }
    }

    /**
     * Same as {@link JcrDavException#JcrDavException(Throwable, int)} where the
     * error code is retrieved from an internal mapping.
     *
     * @param cause Cause of this DavException
     * @throws NullPointerException if the given exception is <code>null</code>.
     * @see JcrDavException#JcrDavException(Throwable, int)
     */
    public JcrDavException(RepositoryException cause) {
        this(cause, ((Integer)codeMap.get(cause.getClass())).intValue());
    }

    /**
     * Always returns true.
     *
     * @return true
     */
    public boolean hasErrorCondition() {
        return true;
    }

    /**
     * Returns a DAV:error Xml element containing the exceptions class and the
     * message as child elements.
     *
     * @return Xml representation of this exception.
     * @see org.apache.jackrabbit.webdav.xml.XmlSerializable#toXml(Document)
     * @param document
     */
    public Element toXml(Document document) {
        Element error = DomUtil.createElement(document, XML_ERROR, DavConstants.NAMESPACE);
        Element excep = DomUtil.createElement(document, "exception", ItemResourceConstants.NAMESPACE);
        DomUtil.addChildElement(excep, "class", ItemResourceConstants.NAMESPACE, exceptionClass.getName());
        DomUtil.addChildElement(excep, "message", ItemResourceConstants.NAMESPACE, getMessage());
        error.appendChild(excep);
        return error;
    }
}
