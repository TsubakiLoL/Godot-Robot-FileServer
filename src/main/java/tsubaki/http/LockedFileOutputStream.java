package tsubaki.http;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class LockedFileOutputStream extends FilterOutputStream {

    private final FileOutputStream fos;
    private final FileChannel channel;
    private final FileLock lock;
    public LockedFileOutputStream(FileOutputStream in) throws IOException {
        super(in);
        this.fos=in;
        channel=fos.getChannel();
        //尝试加锁
        System.out.println("尝试加独占锁");
        lock=channel.lock(0,Long.MAX_VALUE,false);
        System.out.println("加独占锁");
    }

    @Override
    public void close() throws IOException {
        try {
            fos.close();
        } finally {
            if (lock != null && lock.isValid()) {
                lock.release();
                System.out.println("锁释放");
            }
            if (channel != null) {
                channel.close();
            }
        }
    }
}