package daris.io;

import java.io.IOException;
import java.io.InputStream;

public class SizedInputStream extends CountingInputStream {

    private long _size = -1L;
    private boolean _canClose;

    public SizedInputStream(InputStream in, long size, boolean canClose) {
        super(in);
        _size = size;
        _canClose = canClose;
    }

    public SizedInputStream(InputStream in, long size) {
        this(in, size, false);
    }

    @Override
    public int read() throws IOException {
        if (bytesRead() >= _size) {
            return -1;
        }
        return super.read();
    }

    @Override
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (bytesRead() >= _size) {
            return -1;
        }
        if (_size > 0 && bytesRead() + len > _size) {
            len = (int) (_size - bytesRead());
        }
        return super.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        if (bytesRead() >= _size) {
            return 0;
        }
        if (_size > 0 && bytesRead() + n > _size) {
            n = _size - bytesRead();
        }
        return super.skip(n);
    }

    @Override
    public int available() throws IOException {
        if (bytesRemaining() == 0) {
            return 0;
        }
        int a = in.available();
        if (_size >= 0 && a > _size) {
            return (int) _size;
        } else {
            return a;
        }
    }

    public long bytesRemaining() {
        if (_size < 0) {
            return -1;
        }
        if (_size == 0) {
            return 0;
        }
        return _size - bytesRead();
    }

    @Override
    public void close() throws IOException {
        if (_canClose) {
            in.close();
        }
    }
}