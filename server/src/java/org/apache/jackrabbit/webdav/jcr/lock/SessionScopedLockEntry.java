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
package org.apache.jackrabbit.webdav.jcr.lock;

import org.apache.log4j.Logger;
import org.apache.jackrabbit.webdav.lock.LockEntry;
import org.apache.jackrabbit.webdav.lock.Scope;
import org.apache.jackrabbit.webdav.lock.Type;
import org.apache.jackrabbit.webdav.lock.AbstractLockEntry;
import org.apache.jackrabbit.webdav.jcr.ItemResourceConstants;

/**
 * <code>SessionScopedLockEntry</code> represents the 'session-scoped' write
 * lock as defined by JCR.
 */
public class SessionScopedLockEntry extends AbstractLockEntry {

    private static Logger log = Logger.getLogger(SessionScopedLockEntry.class);

    /**
     * @return always returns {@link Type#WRITE write}.
     * @see LockEntry#getType()
     */
    public Type getType() {
        return Type.WRITE;
    }

    /**
     * @return returns {@link ItemResourceConstants#EXCLUSIVE_SESSION}.
     * @see LockEntry#getScope()
     */
    public Scope getScope() {
        return ItemResourceConstants.EXCLUSIVE_SESSION;
    }
}