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
package org.apache.jackrabbit.webdav.client.methods;

import org.apache.log4j.Logger;
import org.apache.jackrabbit.webdav.search.SearchInfo;
import org.apache.jackrabbit.webdav.DavMethods;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.xml.Namespace;

import java.io.IOException;

/**
 * <code>SearchMethod</code>...
 */
public class SearchMethod extends DavMethodBase {

    private static Logger log = Logger.getLogger(SearchMethod.class);

    public SearchMethod(String uri, String statement, String language) throws IOException {
        this(uri, statement, language, Namespace.EMPTY_NAMESPACE);
    }

    public SearchMethod(String uri, String statement, String language, Namespace languageNamespace) throws IOException {
        super(uri);
        if (language != null && statement != null) {
            setRequestHeader(DavConstants.HEADER_CONTENT_TYPE, "text/xml; charset=UTF-8");            
            // build the request body
            SearchInfo searchInfo = new SearchInfo(language, languageNamespace, statement);
            setRequestBody(searchInfo);
        }
    }
    
    public SearchMethod(String uri, SearchInfo searchInfo) throws IOException {
        super(uri);
        setRequestHeader(DavConstants.HEADER_CONTENT_TYPE, "text/xml; charset=UTF-8");
        setRequestBody(searchInfo);
    }

    /**
     * @see org.apache.commons.httpclient.HttpMethod#getName()
     */
    public String getName() {
        return DavMethods.METHOD_SEARCH;
    }
}