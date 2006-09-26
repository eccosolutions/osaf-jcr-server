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

import org.apache.jackrabbit.webdav.DavServletRequest;
import org.apache.jackrabbit.webdav.DavException;

/**
 * <code>ObservationDavServletRequest</code> provides extensions to the
 * {@link DavServletRequest} interface used for dealing with observation.
 */
public interface ObservationDavServletRequest extends DavServletRequest {

    /**
     * Return the {@link ObservationConstants#HEADER_SUBSCRIPTIONID SubscriptionId header}
     * or <code>null</code> if no such header is present.
     *
     * @return the {@link ObservationConstants#HEADER_SUBSCRIPTIONID SubscriptionId header}
     */
    public String getSubscriptionId();

    /**
     * Return a {@link SubscriptionInfo} object representing the subscription
     * info present in the SUBSCRIBE request body or <code>null</code> if
     * retrieving the subscription info fails.
     *
     * @return subscription info object encapsulating the SUBSCRIBE request body
     * or <code>null</code> if the subscription info cannot be built.
     * @throws DavException if an invalid request body was encountered.
     */
    public SubscriptionInfo getSubscriptionInfo() throws DavException;
}