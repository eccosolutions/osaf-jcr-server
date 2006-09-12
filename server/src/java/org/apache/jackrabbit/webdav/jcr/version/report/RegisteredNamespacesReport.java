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
package org.apache.jackrabbit.webdav.jcr.version.report;

import org.apache.log4j.Logger;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.jcr.ItemResourceConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;

/**
 * <code>RegisteredNamespacesReport</code> let the client retrieve the namespaces
 * registered on the repository.<p/>
 *
 * Request body:
 * <pre>
 * &lt;!ELEMENT registerednamespaces EMPTY &gt;
 * </pre>
 *
 * Response body:
 * <pre>
 * &lt;!ELEMENT registerednamespaces-report (namespace)* &gt;
 * &lt;!ELEMENT namespace (prefix, uri) &gt;
 * &lt;!ELEMENT prefix (#PCDATA) &gt;
 * &lt;!ELEMENT uri (#PCDATA) &gt;
 * </pre>
 *
 * @see javax.jcr.Workspace#getNamespaceRegistry() 
 */
public class RegisteredNamespacesReport extends AbstractJcrReport implements ItemResourceConstants {

    private static Logger log = Logger.getLogger(RegisteredNamespacesReport.class);

    /**
     * The registered type of this report.
     */
    public static final ReportType REGISTERED_NAMESPACES_REPORT = ReportType.register("registerednamespaces", ItemResourceConstants.NAMESPACE, RegisteredNamespacesReport.class);

    private NamespaceRegistry nsReg;

    /**
     * Returns {@link #REGISTERED_NAMESPACES_REPORT} type.
     * @return {@link #REGISTERED_NAMESPACES_REPORT}
     * @see org.apache.jackrabbit.webdav.version.report.Report#getType()
     */
    public ReportType getType() {
        return REGISTERED_NAMESPACES_REPORT;
    }

    /**
     * Always returns <code>false</code>.
     *
     * @return false
     * @see org.apache.jackrabbit.webdav.version.report.Report#isMultiStatusReport()
     */
    public boolean isMultiStatusReport() {
        return false;
    }

    /**
     * @see Report#init(DavResource, ReportInfo)
     */
    public void init(DavResource resource, ReportInfo info) throws DavException {
        // delegate validation to abstract super class
        super.init(resource, info);
        try {
            nsReg = getRepositorySession().getWorkspace().getNamespaceRegistry();
        } catch (RepositoryException e) {
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Returns a Xml representation of the registered namespace(s).
     *
     * @return Xml representation of the registered namespace(s)
     * error occurs while retrieving the namespaces.
     * @see org.apache.jackrabbit.webdav.xml.XmlSerializable#toXml(Document)
     * @param document
     */
    public Element toXml(Document document)  {
        Element report = DomUtil.createElement(document, "registerednamespaces-report", NAMESPACE);
        try {
            String[] prefixes = nsReg.getPrefixes();
            for (int i = 0; i < prefixes.length; i++) {
                Element elem = DomUtil.addChildElement(report, XML_NAMESPACE, NAMESPACE);
                DomUtil.addChildElement(elem, XML_PREFIX, NAMESPACE, prefixes[i]);
                DomUtil.addChildElement(elem, XML_URI, NAMESPACE, nsReg.getURI(prefixes[i]));
            }
        } catch (RepositoryException e) {
            // should not occur.
            log.error(e.getMessage());
        }
        return report;
    }

}