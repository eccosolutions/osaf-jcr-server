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
package org.apache.jackrabbit.webdav.search;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.AbstractDavProperty;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 * <code>QueryGrammerSet</code> is a {@link DavProperty} that
 * encapsulates the 'supported-query-grammer-set' as defined by the
 * Webdav SEARCH internet draft.
 */
public class QueryGrammerSet extends AbstractDavProperty implements SearchConstants {

    private final Set queryGrammers = new HashSet();

    /**
     * Create a new empty <code>QueryGrammerSet</code>. Supported query grammers
     * may be added by calling {@link #addQueryLanguage(String, Namespace).
     */
    public QueryGrammerSet() {
        super(QUERY_GRAMMER_SET, true);
    }

    /**
     * Add another query queryGrammer to this set.
     *
     * @param grammerName
     * @param namespace
     */
    public void addQueryLanguage(String grammerName, Namespace namespace) {
        queryGrammers.add(new Grammer(grammerName, namespace));
    }

    /**
     * Return a String array containing the URIs of the query
     * languages supported.
     *
     * @return names of the supported query languages
     */
    public String[] getQueryLanguages() {
        int size = queryGrammers.size();
        if (size > 0) {
            String[] qLangStr = new String[size];
            Grammer[] grammers = (Grammer[]) queryGrammers.toArray(new Grammer[size]);
            for (int i = 0; i < grammers.length; i++) {
                qLangStr[i] = grammers[i].namespace.getURI() + grammers[i].localName;
            }
            return qLangStr;
        } else {
            return new String[0];
        }
    }

    /**
     * Return the Xml representation of this property according to the definition
     * of the 'supported-query-grammer-set'.
     *
     * @return Xml representation
     * @see SearchConstants#QUERY_GRAMMER_SET
     * @see org.apache.jackrabbit.webdav.xml.XmlSerializable#toXml(Document)
     * @param document
     */
    public Element toXml(Document document) {
        Element elem = getName().toXml(document);
        Iterator qlIter = queryGrammers.iterator();
        while (qlIter.hasNext()) {
            Element sqg = DomUtil.addChildElement(elem, XML_QUERY_GRAMMAR, SearchConstants.NAMESPACE);
            Element grammer = DomUtil.addChildElement(sqg, XML_GRAMMER, SearchConstants.NAMESPACE);
            Grammer qGrammer = (Grammer)qlIter.next();
            DomUtil.addChildElement(grammer, qGrammer.localName, qGrammer.namespace);
        }
        return elem;
    }

    /**
     * Returns the set of supported query grammers.
     *
     * @return list of supported query languages.
     * @see org.apache.jackrabbit.webdav.property.DavProperty#getValue()
     */
    public Object getValue() {
        return queryGrammers;
    }


    private class Grammer {

        private final String localName;
        private final Namespace namespace;
        private final int hashCode;

        Grammer(String localName, Namespace namespace) {
            this.localName = localName;
            this.namespace = namespace;
            hashCode = DomUtil.getQualifiedName(localName, namespace).hashCode();
        }

        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof Grammer) {
                return obj.hashCode() == hashCode();
            }
            return false;
        }
    }
}