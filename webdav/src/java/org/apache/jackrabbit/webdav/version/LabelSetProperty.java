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
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.property.AbstractDavProperty;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * <code>LabelSetProperty</code>...
 */
public class LabelSetProperty extends AbstractDavProperty {

    private static Logger log = Logger.getLogger(LabelSetProperty.class);

    private final String[] value;

    /**
     * Create a new <code>LabelSetProperty</code>.
     *
     * @param labels
     */
    public LabelSetProperty(String[] labels) {
        super(VersionResource.LABEL_NAME_SET, true);
        this.value = labels;
    }


    public Object getValue() {
        return value;
    }

    /**
     * @see org.apache.jackrabbit.webdav.xml.XmlSerializable#toXml(Document)
     * @param document
     */
    public Element toXml(Document document) {
        Element elem = getName().toXml(document);
        for (int i = 0; i < value.length; i++) {
            DomUtil.addChildElement(elem, DeltaVConstants.XML_LABEL_NAME, DeltaVConstants.NAMESPACE, value[i]);
        }
        return elem;
    }

}