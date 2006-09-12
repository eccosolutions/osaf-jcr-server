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
package org.apache.jackrabbit.webdav.simple;

import org.apache.log4j.Logger;
import org.apache.jackrabbit.server.io.IOManager;
import org.apache.jackrabbit.server.io.DefaultIOManager;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;

/**
 * <code>ResourceConfig</code>...
 */
public class ResourceConfig {

    private static Logger log = Logger.getLogger(ResourceConfig.class);

    private ItemFilter itemFilter;
    private IOManager ioManager;
    private String[] nodetypeNames = new String[0];
    private boolean collectionNames = false;

    /**
     * Tries to parse the given xml configuration file.
     * The xml must match the following structure:<br>
     * <pre>
     * &lt;!ELEMENT config (iomanager, (collection | noncollection)?, filter?) &gt;
     * &lt;!ELEMENT iomanager (class) &gt;
     * &lt;!ELEMENT collection (nodetypes) &gt;
     * &lt;!ELEMENT noncollection (nodetypes) &gt;
     * &lt;!ELEMENT filter (class, namespaces?, nodetypes?) &gt;
     * &lt;!ELEMENT class &gt;
     *    &lt;!ATTLIST class
     *      name  CDATA #REQUIRED
     *    &gt;
     * &lt;!ELEMENT namespaces (prefix|uri)* &gt;
     * &lt;!ELEMENT prefix (CDATA) &gt;
     * &lt;!ELEMENT uri (CDATA) &gt;
     * &lt;!ELEMENT nodetypes (nodetype)* &gt;
     * &lt;!ELEMENT nodetype (CDATA) &gt;
     * </pre>
     *
     * @param configURL
     */
    public void parse(URL configURL) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            InputStream in = configURL.openStream();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(in);
            Element config = document.getDocumentElement();

            if (config == null) {
                log.error("Resource configuration: mandatory 'iomanager' element is missing.");
                return;
            }

            Element el = DomUtil.getChildElement(config, "iomanager", null);
            if (el != null) {
                Object inst = buildClassFromConfig(el);
                if (inst != null && inst instanceof IOManager) {
                   ioManager = (IOManager)inst;
                }
            } else {
                log.error("Resource configuration: mandatory 'iomanager' element is missing.");
            }

            el = DomUtil.getChildElement(config, "collection", null);
            if (el != null) {
                nodetypeNames = parseNodeTypesEntry(el);
                    collectionNames = true;
            } else if ((el = DomUtil.getChildElement(config, "noncollection", null)) != null) {
                nodetypeNames = parseNodeTypesEntry(el);
                    collectionNames = false;
                }
            // todo: should check if both 'noncollection' and 'collection' are present and write a warning

            el = DomUtil.getChildElement(config, "filter", null);
            if (el != null) {
                Object inst = buildClassFromConfig(el);
                if (inst != null && inst instanceof ItemFilter) {
                    itemFilter = (ItemFilter)inst;
                }
                if (itemFilter != null) {
                    itemFilter.setFilteredNodetypes(parseNodeTypesEntry(el));
                    parseNamespacesEntry(el);
                }
            } else {
                log.debug("Resource configuration: no 'filter' element specified.");
            }
        } catch (IOException e) {
            log.debug("Invalid resource configuration: " + e.getMessage());
        } catch (ParserConfigurationException e) {
            log.warn("Failed to parse resource configuration: " + e.getMessage());
        } catch (SAXException e) {
            log.warn("Failed to parse resource configuration: " + e.getMessage());
        }
    }

    private Object buildClassFromConfig(Element parent) {
        Object instance = null;
        Element classElem = DomUtil.getChildElement(parent, "class", null);
        if (classElem != null) {
            // contains a 'class' child node
        try {
                String className = DomUtil.getAttribute(classElem, "name", null);
            if (className != null) {
                Class c = Class.forName(className);
                instance = c.newInstance();
                } else {
                log.error("Invalid configuration: missing 'class' element");
            }
        } catch (Exception e) {
            log.error("Error while create class instance: " + e.getMessage());
        }
        }
        return instance;
    }

    private void parseNamespacesEntry(Element parent) {
        Element namespaces = DomUtil.getChildElement(parent, "namespaces", null);
        if (namespaces != null) {
            List l = new ArrayList();
            // retrieve prefix child elements
            ElementIterator it = DomUtil.getChildren(namespaces, "prefix", null);
            while (it.hasNext()) {
                Element e = it.nextElement();
                l.add(DomUtil.getText(e));
            }
            String[] prefixes = (String[])l.toArray(new String[l.size()]);
            l.clear();
            // retrieve uri child elements
            it = DomUtil.getChildren(namespaces, "uri", null);
            while (it.hasNext()) {
                Element e = it.nextElement();
                l.add(DomUtil.getText(e));
            }
            String[] uris = (String[])l.toArray(new String[l.size()]);
            itemFilter.setFilteredPrefixes(prefixes);
            itemFilter.setFilteredURIs(uris);
        }
    }

    private String[] parseNodeTypesEntry(Element parent) {
        String[] ntNames;
        Element nodetypes = DomUtil.getChildElement(parent, "nodetypes", null);
        if (nodetypes != null) {
            List l = new ArrayList();
            ElementIterator it = DomUtil.getChildren(nodetypes, "nodetype", null);
            while (it.hasNext()) {
                Element e = it.nextElement();
                l.add(DomUtil.getText(e));
        }
            ntNames = (String[])l.toArray(new String[l.size()]);
        } else {
            ntNames = new String[0];
        }
        return ntNames;
    }


    public IOManager getIOManager() {
        if (ioManager == null) {
            log.debug("ResourceConfig: missing io-manager > building DefaultIOManager ");
            ioManager = new DefaultIOManager();
        }
        return ioManager;
    }

    /**
     * Returns true, if the given item represents a {@link Node node} that is
     * either any of the nodetypes specified to represent a collection or
     * none of the nodetypes specified to represent a non-collection, respectively.
     * If no valid configuration entry is present, this method returns true
     * for node items. For items which are not a node, this method always
     * returns false.
     *
     * @param item
     * @return true if the given item is a node that represents a webdav
     * collection, false otherwise.
     */
    public boolean isCollectionResource(Item item) {
        if (item.isNode()) {
            boolean isCollection = true;
            Node n = (Node)item;
            try {
                if (n.getPath().equals("/")) {
                    return true;
                }
                for (int i = 0; i < nodetypeNames.length && isCollection; i++) {
                    isCollection = collectionNames ? n.isNodeType(nodetypeNames[i]) : !n.isNodeType(nodetypeNames[i]);
                }
            } catch (RepositoryException e) {
                log.warn(e.getMessage());
            }
            return isCollection;
        } else {
            return false;
        }
    }

    /**
     * Returns the item filter specified with the configuration or {@link DefaultItemFilter}
     * if the configuration was missing the corresponding entry or the parser failed
     * to build a <code>ItemFilter</code> instance from the configuration.
     *
     * @return item filter as defined by the config or {@link DefaultItemFilter}
     */
    public ItemFilter getItemFilter() {
        if (itemFilter == null) {
            log.debug("ResourceConfig: missing resource filter > building DefaultItemFilter ");
            itemFilter = new DefaultItemFilter();
        }
        return itemFilter;
    }
}
