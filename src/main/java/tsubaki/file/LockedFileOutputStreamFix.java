package tsubaki.file;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;


public class LockedFileOutputStreamFix extends FileOutputStream {
    private FileLock lock;

    private String filePath;
    public LockedFileOutputStreamFix(String filename) throws IOException {
        super(filename);
        FileChannel channel = getChannel();
        filePath=filename;
        // 获取排他锁（阻止其他读写）
        System.out.println("尝试为文件加独占锁:"+filename);
        lock = channel.lock(); // 默认是排他锁

    }

    @Override
    public void close() throws IOException {
        try {
            if (lock != null && lock.isValid()) {
                System.out.println("独占锁释放:"+filePath);
                lock.release();
            }
        } finally {
            super.close();
        }
    }
}