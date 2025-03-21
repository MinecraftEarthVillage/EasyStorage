package com.example.demo;

import java.io.IOException;
import java.io.InputStream;

public class ThrottledInputStream extends InputStream {
    private final InputStream wrapped;
    private final long maxBytesPerSecond;
    private long bytesRead;
    private long startTime;

// 构造函数，用于创建一个限速输入流
    public ThrottledInputStream(InputStream in, long bytesPerSecond) {
        // 将传入的输入流赋值给成员变量wrapped
        this.wrapped = in;
        // 将传入的每秒字节数赋值给成员变量maxBytesPerSecond
        this.maxBytesPerSecond = bytesPerSecond;
        // 获取当前时间，赋值给成员变量startTime
        this.startTime = System.currentTimeMillis();
    }

    @Override
    // 重写父类的方法
    public int read() throws IOException {
        // 调用throttle方法，参数为1
        throttle(1);
        // 返回wrapped对象的read方法的结果
        return wrapped.read();
    }

    @Override
    // 重写父类方法
    public int read(byte[] b) throws IOException {
        // 从输入流中读取一定数量的字节，并将其存储在字节数组b中
        return read(b, 0, b.length);
    }

    @Override
    // 重写read方法，用于从输入流中读取数据
    public int read(byte[] b, int off, int len) throws IOException {
        // 从输入流中读取数据，返回读取的字节数
        int read = wrapped.read(b, off, len);
        // 如果读取的字节数大于0，则调用throttle方法进行流量控制
        if (read > 0) throttle(read);
        // 返回读取的字节数
        return read;
    }

    private void throttle(int bytes) throws IOException {
        // 如果最大字节数每秒小于等于0，则直接返回
        if (maxBytesPerSecond <= 0) return;

        // 累加已读取的字节数
        bytesRead += bytes;
        // 计算已过去的时间
        long elapsed = System.currentTimeMillis() - startTime;
        // 计算需要的时间
        long requiredTime = (bytesRead * 1000) / maxBytesPerSecond;

        // 如果需要的时间大于已过去的时间，则进行休眠
        if (requiredTime > elapsed) {
            try {
                Thread.sleep(requiredTime - elapsed);
            } catch (InterruptedException e) {
                // 如果休眠被中断，则设置当前线程的中断状态，并抛出IO异常
                Thread.currentThread().interrupt();
                throw new IOException("下载中断", e);
            }
        }
    }

    @Override
    // 重写close()方法
    public void close() throws IOException {
        // 关闭wrapped对象
        wrapped.close();
    }

    // 其他方法委托给被包装流
    @Override
    public int available() throws IOException {
        return wrapped.available();
    }

    @Override
    // 重写父类方法
    public synchronized void mark(int readlimit) {
        // 使用synchronized关键字保证线程安全
        wrapped.mark(readlimit);
        // 调用wrapped对象的mark方法，传入readlimit参数
    }

    @Override
    public synchronized void reset() throws IOException {
        wrapped.reset();
    }

    @Override
    // 重写markSupported()方法
    public boolean markSupported() {
        // 返回wrapped对象的markSupported()方法的返回值
        return wrapped.markSupported();
    }
}