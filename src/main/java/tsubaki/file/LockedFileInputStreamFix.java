package tsubaki.file;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;


public class LockedFileInputStreamFix extends FileInputStream {
    private FileLock lock;

    private String filePath;
    public LockedFileInputStreamFix(String filename) throws IOException {
        super(filename);
        FileChannel channel = getChannel();
        // 获取共享锁（允许其他读，阻止写）
        System.out.println("尝试为文件加共享锁:"+filename);
        lock = channel.lock(0, Long.MAX_VALUE, true); // true表示共享锁
        filePath=filename;
    }

    @Override
    public void close() throws IOException {
        try {
            if (lock != null && lock.isValid()) {
                System.out.println("共享锁释放:"+filePath);
                lock.release();
            }
        } finally {
            super.close();
        }
    }
}