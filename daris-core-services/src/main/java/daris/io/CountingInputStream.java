package daris.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CountingInputStream extends FilterInputStream {

    private long _bytesRead;

    private long _mark;

    public CountingInputStream(InputStream in) {
        super(in);
        _bytesRead = 0;
    }

    public int read() throws IOException {
        int b = in.read();
        if (b != -1) {
            _bytesRead++;
        }
        return b;
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int n = in.read(b, off, len);
        if (n != -1) {
            _bytesRead += n;
        }
        return n;
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = in.skip(n);
        _bytesRead += skipped;
        return skipped;
    }

    public long bytesRead() {
        return _bytesRead;
    }

    @Override
    public void mark(int readlimit) {
        in.mark(readlimit);
        _mark = bytesRead();
    }

    @Override
    public void reset() throws IOException {
        /*
         * A call to reset can still succeed if mark is not supported, but the
         * resulting stream position is undefined, so it's not allowed here.
         */
        if (!markSupported()) {
            throw new IOException("Mark not supported.");
        }
        in.reset();
        _bytesRead = _mark;
    }

}
