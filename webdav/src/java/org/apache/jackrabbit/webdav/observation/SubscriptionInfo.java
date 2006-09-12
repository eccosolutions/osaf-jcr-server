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
package org.apache.jackrabbit.webdav.observation;

import org.apache.log4j.Logger;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>SubscriptionInfo</code> class encapsulates the subscription info
 * that forms the request body of a SUBSCRIBE request.<br>
 * The following xml layout is defined for the subscription info:
 * <pre>
 * &lt;!ELEMENT subscriptioninfo ( eventtype, nolocal?, filter? ) &gt;
 * &lt;!ELEMENT eventtype ANY &gt;
 *
 * ANY defines any sequence of elements where at least one defines a valid
 * eventtype. Note that a single eventtype must not occur multiple times.

 * &lt;!ELEMENT nolocal EMPTY &gt;
 * &lt;!ELEMENT filter ANY &gt;
 *
 * ANY: any sequence of elements identifying a filter for event listening but
 * at least a single element.
 * </pre>
 * @see ObservationConstants#XML_SUBSCRIPTIONINFO
 */
public class SubscriptionInfo implements ObservationConstants, XmlSerializable {

    private static Logger log = Logger.getLogger(SubscriptionInfo.class);

    private final EventType[] eventTypes;
    private final Filter[] filters;
    private final boolean noLocal;
    private final boolean isDeep;
    private final long timeout;

    /**
     * Create a new <code>SubscriptionInfo</code>
     *
     * @param eventTypes
     * @param isDeep
     * @param timeout
     */
    public SubscriptionInfo(EventType[] eventTypes, boolean isDeep, long timeout) {
        this(eventTypes, null, false, isDeep, timeout);
    }

    /**
     * Create a new <code>SubscriptionInfo</code>
     *
     * @param eventTypes
     * @param filters
     * @param noLocal
     * @param isDeep
     * @param timeout
     */
    public SubscriptionInfo(EventType[] eventTypes, Filter[] filters, boolean noLocal, boolean isDeep, long timeout) {
        if (eventTypes == null || eventTypes.length == 0) {
            throw new IllegalArgumentException("'subscriptioninfo' must at least indicate a single event type.");
        }

        this.eventTypes = eventTypes;
        this.noLocal = noLocal;

        if (filters != null) {
            this.filters = filters;
        } else {
            this.filters = new Filter[0];
        }

        this.isDeep = isDeep;
        this.timeout = timeout;
    }

    /**
     * Create a new <code>SubscriptionInfo</code>
     *
     * @param reqInfo Xml element present in the request body.
     * @param timeout as defined by the {@link org.apache.jackrabbit.webdav.DavConstants#HEADER_TIMEOUT timeout header}.
     * @param isDeep as defined by the {@link org.apache.jackrabbit.webdav.DavConstants#HEADER_DEPTH depth header}.
     * @throws IllegalArgumentException if the reqInfo element does not contain the mandatory elements.
     */
    public SubscriptionInfo(Element reqInfo, long timeout, boolean isDeep) throws DavException {
        if (!DomUtil.matches(reqInfo, XML_SUBSCRIPTIONINFO, NAMESPACE)) {
            log.warn("Element with name 'subscriptioninfo' expected");
            throw new DavException(DavServletResponse.SC_BAD_REQUEST);
        }
        List typeList = new ArrayList();
        Element el = DomUtil.getChildElement(reqInfo, XML_EVENTTYPE, NAMESPACE);
        if (el != null) {
            ElementIterator it = DomUtil.getChildren(el);
            while (it.hasNext()) {
                Element typeElem = it.nextElement();
                EventType et = new SimpleEventType(typeElem.getLocalName(), DomUtil.getNamespace(typeElem));
                typeList.add(et);
            }
        } else {
            log.warn("'subscriptioninfo' must contain an 'eventtype' child element.");
            throw new DavException(DavServletResponse.SC_BAD_REQUEST);
        }

        if (typeList.isEmpty()) {
            log.warn("'subscriptioninfo' must at least indicate a single event type.");
            throw new DavException(DavServletResponse.SC_BAD_REQUEST);
        }
        eventTypes = (EventType[]) typeList.toArray(new EventType[typeList.size()]);

        List filters = new ArrayList();
        el = DomUtil.getChildElement(reqInfo, XML_FILTER, NAMESPACE);
        if (el != null) {
            ElementIterator it = DomUtil.getChildren(el);
            while (it.hasNext()) {
                Filter f = new Filter(it.nextElement());
                filters.add(f);
            }
        }
        this.filters = (Filter[])filters.toArray(new Filter[filters.size()]);

        this.noLocal = DomUtil.hasChildElement(reqInfo, XML_NOLOCAL, NAMESPACE);
        this.isDeep = isDeep;
        this.timeout = timeout;
    }

    /**
     * Return array of event type names present in the subscription info.
     *
     * @return array of String defining the names of the events this subscription
     * should listen to.
     *
     */
    public EventType[] getEventTypes() {
        return eventTypes;
    }

    /**
     * Return all filters defined for this <code>SubscriptionInfo</code>
     *
     * @return all filters or an empty Filter array.
     */
    public Filter[] getFilters() {
        return filters;
    }

    /**
     * Return array of filters with the specified name.
     *
     * @param localName the filter elments must provide.
     * @param namespace
     * @return array containing the text of the filter elements with the given
     * name.
     */
    public Filter[] getFilters(String localName, Namespace namespace) {
        List l = new ArrayList();
                for (int i = 0; i < filters.length; i++) {
            if (filters[i].isMatchingFilter(localName, namespace)) {
               l.add(filters[i]);
                }
            }
        return (Filter[])l.toArray(new Filter[l.size()]);
    }

    /**
     * Returns true if the {@link #XML_NOLOCAL} element is present in this
     * subscription info.
     *
     * @return if {@link #XML_NOLOCAL} element is present.
     */
    public boolean isNoLocal() {
        return noLocal;
    }

    /**
     * Returns true if the {@link org.apache.jackrabbit.webdav.DavConstants#HEADER_DEPTH
     * depths header} defined a depth other than '0'.
     *
     * @return true if this subscription info was created with <code>isDeep</code>
     * true.
     */
    public boolean isDeep() {
        return isDeep;
    }

    /**
     * Return the timeout as retrieved from the request.
     *
     * @return timeout.
     */
    public long getTimeOut() {
        return timeout;
    }

    /**
     * Xml representation of this <code>SubscriptionInfo</code>.
     *
     * @return Xml representation
     * @see org.apache.jackrabbit.webdav.xml.XmlSerializable#toXml(Document)
     * @param document
     */
    public Element toXml(Document document) {
        Element subscrInfo = DomUtil.createElement(document, XML_SUBSCRIPTIONINFO, NAMESPACE);
        Element eventType = DomUtil.addChildElement(subscrInfo, XML_EVENTTYPE, NAMESPACE);
        for (int i = 0; i < eventTypes.length; i++) {
            eventType.appendChild(eventTypes[i].toXml(document));
        }

        if (filters.length > 0) {
            Element filter = DomUtil.addChildElement(subscrInfo, XML_FILTER, NAMESPACE);
            for (int i = 0; i < filters.length; i++) {
                filter.appendChild(filters[i].toXml(document));
            }
        }

        if (noLocal) {
            DomUtil.addChildElement(subscrInfo, XML_NOLOCAL, NAMESPACE);
        }
        return subscrInfo;
    }

    //--------------------------------------------------------< inner class >---
    /**
     * Simple EventType implementation that only consists of a qualified event
     * name.
     */
    private class SimpleEventType implements EventType {

        private String localName;
        private Namespace namespace;

        SimpleEventType(String localName, Namespace namespace) {
            this.localName = localName;
            this.namespace = namespace;
        }

        public Element toXml(Document document) {
            return DomUtil.createElement(document, localName, namespace);
        }

        public String getName() {
            return localName;
        }

        public Namespace getNamespace() {
            return namespace;
        }
    }
}