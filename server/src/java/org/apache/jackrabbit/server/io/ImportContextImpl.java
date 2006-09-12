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
package org.apache.jackrabbit.server.io;

import org.apache.log4j.Logger;
import org.apache.jackrabbit.webdav.io.InputContext;

import javax.jcr.Item;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.util.Date;

/**
 * <code>ImportContextImpl</code>...
 */
public class ImportContextImpl implements ImportContext {

    private static Logger log = Logger.getLogger(ImportContextImpl.class);

    private final IOListener ioListener;
    private final Item importRoot;
    private final String systemId;
    private final File inputFile;

    private InputContext inputCtx;
    private boolean completed;

    /**
     * Creates a new item import context with the given root item and the
     * specified <code>InputContext</code>. If the input context provides an
     * input stream, the stream is written to a temporary file in order to avoid
     * problems with multiple IOHandlers that try to run the import but fail.
     * The temporary file is deleted as soon as this context is informed that
     * the import has been completed and it will not be used any more.
     *
     * @param importRoot the import root node
     * @param inputCtx wrapped by this <code>ImportContext</code>
     */
    public ImportContextImpl(Item importRoot, String systemId, InputContext inputCtx) throws IOException {
        this(importRoot, systemId, (inputCtx != null) ? inputCtx.getInputStream() : null, null);
        this.inputCtx = inputCtx;
        // if there's an input stream and a content length was
        // specified, ensure that the temp file is exactly that many
        // bytes in size
        if (this.inputFile != null) {
            long cl = getContentLength();
            long fl = this.inputFile.length();
            if (cl != IOUtil.UNDEFINED_LENGTH && cl != fl) {
                inputFile.delete();
                throw new IOException("read only " + fl+ " of " + cl +
                                      " bytes");
            }
        }
    }

    /**
     * Creates a new item import context. The specified InputStream is written
     * to a temporary file in order to avoid problems with multiple IOHandlers
     * that try to run the import but fail. The temporary file is deleted as soon
     * as this context is informed that the import has been completed and it
     * will not be used any more.
     *
     * @param importRoot
     * @param systemId
     * @param in
     * @param ioListener
     * @throws IOException
     * @see ImportContext#informCompleted(boolean)
     */
    public ImportContextImpl(Item importRoot, String systemId, InputStream in, IOListener ioListener) throws IOException {
        this.importRoot = importRoot;
        this.systemId = systemId;
        this.inputFile = IOUtil.getTempFile(in);
        this.ioListener = (ioListener != null) ? ioListener : new DefaultIOListener(log);
    }

    /**
     * @see ImportContext#getIOListener()
     */
    public IOListener getIOListener() {
        return ioListener;
    }

    /**
     * @see ImportContext#getImportRoot()
     */
    public Item getImportRoot() {
        return importRoot;
    }

    /**
     * @see ImportContext#hasStream()
     */
    public boolean hasStream() {
        return inputFile != null;
    }

    /**
     * Returns a new <code>InputStream</code> to the temporary file created
     * during instanciation or <code>null</code>, if this context does not
     * provide a stream.
     *
     * @see ImportContext#getInputStream()
     * @see #hasStream()
     */
    public InputStream getInputStream() {
        checkCompleted();
        InputStream in = null;
        if (inputFile != null) {
            try {
                in = new FileInputStream(inputFile);
            } catch (IOException e) {
                // unexpected error... ignore and return null
            }
        }
        return in;
    }

    /**
     * @see ImportContext#getSystemId()
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * @see ImportContext#getModificationTime()
     */
    public long getModificationTime() {
        return (inputCtx != null) ? inputCtx.getModificationTime() : new Date().getTime();
    }

    /**
     * @see ImportContext#getContentLanguage()
     */
    public String getContentLanguage() {
        return (inputCtx != null) ? inputCtx.getContentLanguage() : null;
    }

    /**
     * @see ImportContext#getContentLength()
     */
    public long getContentLength() {
        return (inputCtx != null) ? inputCtx.getContentLength() : IOUtil.UNDEFINED_LENGTH;
    }

    /**
     * @return the content type present on the <code>InputContext</code> or
     * <code>null</code>
     * @see InputContext#getContentType()
     */
    private String getContentType() {
        return (inputCtx != null) ? inputCtx.getContentType() : null;
    }

    /**
     * @see ImportContext#getMimeType()
     */
    public String getMimeType() {
        String contentType = getContentType();
        String mimeType = null;
        if (contentType != null) {
            mimeType = IOUtil.getMimeType(contentType);
        } else if (getSystemId() != null) {
            mimeType = IOUtil.MIME_RESOLVER.getMimeType(getSystemId());
        }
        return mimeType;
    }

    /**
     * @see ImportContext#getEncoding()
     */
    public String getEncoding() {
        String contentType = getContentType();
        return (contentType != null) ? IOUtil.getEncoding(contentType) : null;
    }

    /**
     * @see ImportContext#getProperty(Object)
     */
    public Object getProperty(Object propertyName) {
        return (inputCtx != null) ? inputCtx.getProperty(propertyName.toString()) : null;
    }

    /**
     * @see ImportContext#informCompleted(boolean)
     */
    public void informCompleted(boolean success) {
        checkCompleted();
        completed = true;
        if (inputFile != null) {
            inputFile.delete();
        }
    }

    /**
     * @see ImportContext#isCompleted()
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * @throws IllegalStateException if the context is already completed.
     * @see #isCompleted()
     * @see #informCompleted(boolean)
     */
    private void checkCompleted() {
        if (completed) {
            throw new IllegalStateException("ImportContext has already been consumed.");
        }
    }
}
