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
import org.apache.jackrabbit.webdav.jcr.nodetype.NodeTypeConstants;
import org.apache.jackrabbit.webdav.jcr.nodetype.PropertyDefinitionImpl;
import org.apache.jackrabbit.webdav.jcr.nodetype.NodeDefinitionImpl;
import org.apache.jackrabbit.webdav.jcr.JcrDavException;
import org.apache.jackrabbit.util.IteratorHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import javax.jcr.Session;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.nodetype.NodeTypeManager;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * <code>NodeTypesReport</code> allows to retrieve the definition of a single
 * or multiple node types. The request body must be a 'dcr:nodetypes' element:
 * <pre>
 * &lt;!ELEMENT nodetypes ( nodetype+ | all-nodetypes | mixin-nodetypes | primary-nodetypes ) &gt;
 *
 * &lt;!ELEMENT nodetype ( nodetypename ) &gt;
 * &lt;!ELEMENT nodetypename (#PCDATA) &gt;
 *
 * &lt;!ELEMENT all-nodetypes EMPTY &gt;
 * &lt;!ELEMENT mixin-nodetypes EMPTY &gt;
 * &lt;!ELEMENT primary-nodetypes EMPTY &gt;
 * </pre>
 *
 * @see NodeTypeConstants#NAMESPACE
 */
//todo: currently the nodetype report is not consistent with the general way of representing nodetype names (with NodetypeElement) in order to be compatible with the jackrabbit nodetype registry...
//todo: for the same reason, not the complete nodetype-definition, but only the nodetype def as stored is represented.
//todo: no namespace definition with response (> jackrabbit)... and nodetype element has same name as the one used with dav-properties
public class NodeTypesReport extends AbstractJcrReport implements NodeTypeConstants {

    private static Logger log = Logger.getLogger(NodeTypesReport.class);

    /**
     * The registered type of this report.
     */
    public static final ReportType NODETYPES_REPORT = ReportType.register("nodetypes", NodeTypeConstants.NAMESPACE, NodeTypesReport.class);

    private NodeTypeIterator ntIter;

    /**
     * Returns {@link #NODETYPES_REPORT} type.
     * @return {@link #NODETYPES_REPORT}
     * @see org.apache.jackrabbit.webdav.version.report.Report#getType()
     */
    public ReportType getType() {
        return NODETYPES_REPORT;
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
        // delegate basic validation to super class
        super.init(resource, info);
        // report specific validation and preparation for xml serialization
        try {
            ntIter = getNodeTypes(getRepositorySession(), info);
        } catch (RepositoryException e) {
            throw new JcrDavException(e);
        }
        if (ntIter == null) {
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Returns a Xml representation of the node type definition(s) according
     * to the info object.
     *
     * @param document
     * @return Xml representation of the node type definition(s)
     * @see org.apache.jackrabbit.webdav.xml.XmlSerializable#toXml(Document)
     */
    public Element toXml(Document document) {
        Element report = document.createElement(NODETYPES_ELEMENT);
        // loop over the nodetypes to be returned in the report
        while (ntIter.hasNext()) {
            NodeType nt = ntIter.nextNodeType();
            Element ntDef = document.createElement(NODETYPE_ELEMENT);
            ntDef.setAttribute(NAME_ATTRIBUTE, nt.getName());
            ntDef.setAttribute(ISMIXIN_ATTRIBUTE, Boolean.toString(nt.isMixin()));
            ntDef.setAttribute(HASORDERABLECHILDNODES_ATTRIBUTE, Boolean.toString(nt.hasOrderableChildNodes()));

            // declared supertypes
            NodeType[] snts = nt.getDeclaredSupertypes();
            Element supertypes = DomUtil.addChildElement(ntDef, SUPERTYPES_ELEMENT, null);
            for (int i = 0; i < snts.length; i++) {
                DomUtil.addChildElement(supertypes, SUPERTYPE_ELEMENT, null, snts[i].getName());
            }

            // declared childnode defs
            NodeDefinition[] cnd = nt.getChildNodeDefinitions();
            for (int i = 0; i < cnd.length; i++) {
                if (cnd[i].getDeclaringNodeType().getName().equals(nt.getName())) {
                    ntDef.appendChild(NodeDefinitionImpl.create(cnd[i]).toXml(document));
                }
            }

            // declared propertyDefs
            PropertyDefinition[] pd = nt.getPropertyDefinitions();
            for (int i = 0; i < pd.length; i++) {
                if (pd[i].getDeclaringNodeType().getName().equals(nt.getName())) {
                    ntDef.appendChild(PropertyDefinitionImpl.create(pd[i]).toXml(document));
                }
            }

            String primaryItemName = nt.getPrimaryItemName();
            if (primaryItemName != null) {
                ntDef.setAttribute(PRIMARYITEMNAME_ATTRIBUTE, primaryItemName);
            }
            report.appendChild(ntDef);
        }
        return report;
    }

    /**
     * Parse the Xml element in the info object an return an interator over
     * the specified node types.
     *
     * @return
     * @throws RepositoryException
     * @throws DavException
     */
    private static NodeTypeIterator getNodeTypes(Session session, ReportInfo info) throws RepositoryException, DavException {
        NodeTypeIterator ntIter = null;
        NodeTypeManager ntMgr = session.getWorkspace().getNodeTypeManager();

        // check the simple types first...
        if (info.containsContentElement(XML_REPORT_ALLNODETYPES, NAMESPACE)) {
            ntIter = ntMgr.getAllNodeTypes();
        } else if (info.containsContentElement(XML_REPORT_MIXINNODETYPES, NAMESPACE)) {
            ntIter = ntMgr.getMixinNodeTypes();
        } else if (info.containsContentElement(XML_REPORT_PRIMARYNODETYPES, NAMESPACE)) {
            ntIter = ntMgr.getPrimaryNodeTypes();
        }
        // None of the simple types. test if a report for individual nodetypes
        // was request. If not, the request body is not valid.
        if (ntIter == null) {
            List ntList = new ArrayList();
            List elemList = info.getContentElements(XML_NODETYPE, NAMESPACE);
            if (elemList.isEmpty()) {
                // throw exception if the request body does not contain a single nodetype element
                throw new DavException(DavServletResponse.SC_BAD_REQUEST, "NodeTypes report: request body has invalid format.");
            }

            // todo: find better solution...
            Iterator elemIter = elemList.iterator();
            while (elemIter.hasNext()) {
                Element el = ((Element)elemIter.next());
                String nodetypeName = DomUtil.getChildTextTrim(el, XML_NODETYPENAME, NAMESPACE);
                if (nodetypeName != null) {
                    ntList.add(ntMgr.getNodeType(nodetypeName));
                }
            }
            ntIter = new IteratorHelper(Collections.unmodifiableCollection(ntList));
        }

        return ntIter;
    }
}