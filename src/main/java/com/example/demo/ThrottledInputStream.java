package com.example.demo;

import java.io.IOException;
import java.io.InputStream;

public class ThrottledInputStream extends InputStream {
    private final InputStream wrapped;
    private final long maxBytesPerSecond;
    private long bytesRead;
    private long startTime;

    public ThrottledInputStream(InputStream in, long bytesPerSecond) {
        this.wrapped = in;
        this.maxBytesPerSecond = bytesPerSecond;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public int read() throws IOException {
        throttle(1);
        return wrapped.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = wrapped.read(b, off, len);
        if (read > 0) throttle(read);
        return read;
    }

    private void throttle(int bytes) throws IOException {
        if (maxBytesPerSecond <= 0) return;

        bytesRead += bytes;
        long elapsed = System.currentTimeMillis() - startTime;
        long requiredTime = (bytesRead * 1000) / maxBytesPerSecond;

        if (requiredTime > elapsed) {
            try {
                Thread.sleep(requiredTime - elapsed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("下载中断", e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        wrapped.close();
    }

    // 其他方法委托给被包装流
    @Override
    public int available() throws IOException {
        return wrapped.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        wrapped.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        wrapped.reset();
    }

    @Override
    public boolean markSupported() {
        return wrapped.markSupported();
    }
}