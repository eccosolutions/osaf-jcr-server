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
package org.apache.jackrabbit.webdav.version;

import org.apache.log4j.Logger;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>OptionsResponse</code> encapsulates the DAV:options-response element
 * present in the response body of a successful OPTIONS request (with body).
 * <br>
 * The DAV:options-response element is defined to have the following format.
 *
 * <pre>
 * &lt;!ELEMENT options-response ANY&gt;
 * ANY value: A sequence of elements
 * </pre>
 *
 * Please note, that <code>OptionsResponse</code> represents a simplified implementation
 * of the given structure. We assume, that there may only entries that consist
 * of a qualified name and a set of href child elements.
 *
 * @see DeltaVConstants#XML_ACTIVITY_COLLECTION_SET
 * @see DeltaVConstants#XML_VH_COLLECTION_SET
 * @see DeltaVConstants#XML_WSP_COLLECTION_SET
 */
public class OptionsResponse implements DeltaVConstants, XmlSerializable {

    private static Logger log = Logger.getLogger(OptionsResponse.class);

    private final Map entries = new HashMap();

    /**
     * Add a new entry to this <code>OptionsResponse</code> and make each
     * href present in the String array being a separate {@link org.apache.jackrabbit.webdav.DavConstants#XML_HREF DAV:href}
     * element within the entry.
     *
     * @param localName
     * @param namespace
     * @param hrefs
     */
    public void addEntry(String localName, Namespace namespace, String[] hrefs) {
        Entry entry = new Entry(localName, namespace, hrefs);
        entries.put(DomUtil.getQualifiedName(localName, namespace), entry);
    }

    /**
     *
     * @param localName
     * @param namespace
     * @return
     */
    public String[] getHrefs(String localName, Namespace namespace) {
        String key = DomUtil.getQualifiedName(localName, namespace);
        if (entries.containsKey(key)) {
            return ((Entry)entries.get(key)).hrefs;
        } else {
            return new String[0];
        }
    }

    /**
     * Return the Xml representation.
     *
     * @return Xml representation.
     * @see org.apache.jackrabbit.webdav.xml.XmlSerializable#toXml(Document)
     * @param document
     */
    public Element toXml(Document document) {
        Element optionsResponse = DomUtil.createElement(document, XML_OPTIONS_RESPONSE, NAMESPACE);
        Iterator it = entries.values().iterator();
        while (it.hasNext()) {
            Entry entry = (Entry)it.next();
            Element elem = DomUtil.addChildElement(optionsResponse, entry.localName, entry.namespace);
            for (int i = 0; i < entry.hrefs.length; i++) {
                elem.appendChild(DomUtil.hrefToXml(entry.hrefs[i], document));
            }
        }
        return optionsResponse;
    }

    /**
     * Build a new <code>OptionsResponse</code> object from the given xml element.
     *
     * @param orElem
     * @return a new <code>OptionsResponse</code> object
     * @throws IllegalArgumentException if the specified element is <code>null</code>
     * or if its name is other than 'DAV:options-response'.
     */
    public static OptionsResponse createFromXml(Element orElem) {
        if (!DomUtil.matches(orElem, XML_OPTIONS_RESPONSE, NAMESPACE)) {
            throw new IllegalArgumentException("DAV:options-response element expected");
        }
        OptionsResponse oResponse = new OptionsResponse();
        ElementIterator it = DomUtil.getChildren(orElem);
        while (it.hasNext()) {
            Element el = it.nextElement();
            List hrefs = new ArrayList();
            ElementIterator hrefIt = DomUtil.getChildren(el, DavConstants.XML_HREF, DavConstants.NAMESPACE);
            while (hrefIt.hasNext()) {
                hrefs.add(DomUtil.getTextTrim(hrefIt.nextElement()));
            }
            oResponse.addEntry(el.getLocalName(), DomUtil.getNamespace(el), (String[])hrefs.toArray(new String[hrefs.size()]));
        }
        return oResponse;
    }

    private static class Entry {

        private final String localName;
        private final Namespace namespace;
        private final String[] hrefs;

        private Entry(String localName, Namespace namespace, String[] hrefs) {
            this.localName = localName;
            this.namespace = namespace;
            this.hrefs = hrefs;
        }
    }
}