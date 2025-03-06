package tsubaki.http;

import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class LockedFileInputStream extends FilterInputStream {

    private final FileInputStream fos;
    private final FileChannel channel;
    private final FileLock lock;
    public LockedFileInputStream(FileInputStream in) throws IOException {
        super(in);
        this.fos=in;
        channel=fos.getChannel();
        //尝试加锁
        System.out.println("尝试加共享锁");
        lock=channel.lock(0,Long.MAX_VALUE,true);
        System.out.println("加共享锁");
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
            System.out.println("锁释放");
        }
    }
}