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

import javax.jcr.Item;
import javax.jcr.RepositoryException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>DefaultItemFilter</code>...
 */
public class DefaultItemFilter implements ItemFilter {

    private static Logger log = Logger.getLogger(DefaultItemFilter.class);

    private List prefixFilter = new ArrayList();
    private List uriFilter = new ArrayList();
    private List nodetypeFilter = new ArrayList();

    public DefaultItemFilter() {
    }

    /**
     * @see ItemFilter#setFilteredURIs(String[])
     */
    public void setFilteredURIs(String[] uris) {
        if (uris != null) {
            for (int i = 0; i < uris.length; i++) {
                uriFilter.add(uris[i]);
            }
        }
    }

    /**
     * @see ItemFilter#setFilteredPrefixes(String[])
     */
    public void setFilteredPrefixes(String[] prefixes) {
        if (prefixes != null) {
            for (int i = 0; i < prefixes.length; i++) {
                prefixFilter.add(prefixes[i]);
            }
        }
    }

    /**
     * @see ItemFilter#setFilteredNodetypes(String[])
     */
    public void setFilteredNodetypes(String[] nodetypeNames) {
        if (nodetypeNames != null) {
            for (int i = 0; i < nodetypeNames.length; i++) {
                nodetypeFilter.add(nodetypeNames[i]);
            }
        }
    }

    /**
     * Returns true if the given item matches either one of the namespace or
     * of the the nodetype filters specified.
     *
     * @see ItemFilter#isFilteredItem(Item)
     */
    public boolean isFilteredItem(Item item) {
        return isFilteredNamespace(item) || isFilteredNodeType(item);
    }

    /**
     * @see ItemFilter#isFilteredItem(String, Session)
     */
    public boolean isFilteredItem(String displayName, Session session) {
        return isFilteredNamespace(displayName, session);
    }

    /**
     *
     * @param name
     * @param session
     * @return
     */
    private boolean isFilteredNamespace(String name, Session session) {
        // shortcut
        if (prefixFilter.isEmpty() && uriFilter.isEmpty()) {
            return false;
        }
        int pos = name.indexOf(":");
        if (pos < 0) {
            // no namespace info present
            return false;
        }
        try {
            String prefix = name.substring(0, pos);
            String uri = session.getNamespaceURI(prefix);
            return prefixFilter.contains(prefix) || uriFilter.contains(uri);
        } catch (RepositoryException e) {
            log.warn(e.getMessage());
        }
        return false;
    }

    /**
     *
     * @param item
     * @return
     */
    private boolean isFilteredNamespace(Item item) {
        try {
            return isFilteredNamespace(item.getName(), item.getSession());
        } catch (RepositoryException e) {
            log.warn(e.getMessage());
        }
        return false;
    }

    /**
     *
     * @param item
     * @return
     */
    private boolean isFilteredNodeType(Item item) {
        // shortcut
        if (nodetypeFilter.isEmpty()) {
            return false;
        }
        try {
            String ntName;
            if (item.isNode()) {
                //                ntName = ((Node) item).getDefinition().getDeclaringNodeType().getName();
                ntName = ((Node) item).getPrimaryNodeType().getName();
            } else {
                ntName = ((Property) item).getDefinition().getDeclaringNodeType().getName();
            }
            return nodetypeFilter.contains(ntName);
        } catch (RepositoryException e) {
            log.warn(e.getMessage());
        }
        // nodetype info could not be retrieved
        return false;
    }
}
