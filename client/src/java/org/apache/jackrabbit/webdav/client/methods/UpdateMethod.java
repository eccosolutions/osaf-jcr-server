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
import org.apache.jackrabbit.webdav.DavMethods;
import org.apache.jackrabbit.webdav.version.UpdateInfo;

import java.io.IOException;

/**
 * <code>UpdateMethod</code>...
 */
public class UpdateMethod extends DavMethodBase {

    private static Logger log = Logger.getLogger(UpdateMethod.class);

    public UpdateMethod(String uri, UpdateInfo updateInfo) throws IOException {
        super(uri);
        setRequestBody(updateInfo);
    }

    /**
     * @see org.apache.commons.httpclient.HttpMethod#getName()
     */
    public String getName() {
        return DavMethods.METHOD_UPDATE;
    }
}