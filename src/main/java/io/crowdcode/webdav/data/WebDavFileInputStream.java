package io.crowdcode.webdav.data;

import org.apache.jackrabbit.webdav.property.DavPropertySet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Inputstream to a webdav file resource
 */
public class WebDavFileInputStream extends InputStream implements WebDavElement {

    private final DavPropertySet propertiesPresent;
    private final InputStream inputstream;
    private final File tmpfile;


    public WebDavFileInputStream(DavPropertySet propertiesPresent, InputStream stream, File tmpfile) {
        this.propertiesPresent = propertiesPresent;
        inputstream = stream;
        this.tmpfile = tmpfile;
    }

    @Override
    public DavPropertySet getPropertiesPresent() {
        return propertiesPresent;
    }

    @Override
    public int read() throws IOException {
        return inputstream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputstream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputstream.read(b, off, len);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return inputstream.readAllBytes();
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        return inputstream.readNBytes(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return inputstream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return inputstream.available();
    }

    @Override
    public void close() throws IOException {
        inputstream.close();
        try {
            tmpfile.delete();
        } finally {

        }
    }

    @Override
    public void mark(int readlimit) {
        inputstream.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        inputstream.reset();
    }

    @Override
    public boolean markSupported() {
        return inputstream.markSupported();
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        return inputstream.transferTo(out);
    }
}
