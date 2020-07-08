package io.crowdcode.webdav.data;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Marcus Nörder-Tuitje (CROWDCODE)
 * @author Ingo Düppe (CROWDCODE)
 *
 * Inputstream to a webdav file resource
 */
public class WebDavFileInputStream extends InputStream implements WebDavElement {

    private static final Logger log = LoggerFactory.getLogger(WebDavFileInputStream.class);

    private final DavPropertySet propertiesPresent;
    private final InputStream inputStream;
    private final File tmpFile;

    public WebDavFileInputStream(DavPropertySet propertiesPresent, InputStream inputStream, File tmpFile) {
        this.propertiesPresent = propertiesPresent;
        this.tmpFile = tmpFile;
        this.inputStream = loadingInputStreamToTmpFile( inputStream, tmpFile);
    }

    private InputStream loadingInputStreamToTmpFile(InputStream inputStream, File tmpFile) {
        try (OutputStream outputStream = new FileOutputStream(tmpFile)){
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            log.error("Couldn't load file {} due to io exception.", tmpFile, e);
        }
        try {
            return new FileInputStream(tmpFile);
        } catch (FileNotFoundException e) {
            log.error("Cannot open input stream of local file {}", tmpFile , e);
        }
        return null;
    }

    @Override
    public DavPropertySet getPropertiesPresent() {
        return propertiesPresent;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return inputStream.readAllBytes();
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        return inputStream.readNBytes(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        try {
            tmpFile.delete();
        } finally {

        }
    }

    @Override
    public void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        return inputStream.transferTo(out);
    }
}
