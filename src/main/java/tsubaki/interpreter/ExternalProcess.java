package tsubaki.interpreter;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class ExternalProcess {
    private final Process process;
    private final PrintWriter inputWriter;
    private final BlockingQueue<String> outputBuffer;
    private final Thread outputGobbler;
    private final Thread errorGobbler;
    private static final int MAX_BUFFER_SIZE = 100;

    /**
     * 创建并启动外部进程
     * @param executablePath 可执行文件路径
     * @param arguments 参数字典（参数名 -> 参数值）
     */
    public ExternalProcess(String executablePath, Map<String, String> arguments) throws IOException {
        // 构建命令行参数列表
        List<String> command = new ArrayList<>();
        command.add(executablePath);
        command.add("--");  // 添加必要的前导--

        // 转换字典参数为 --key=value 格式
        for (Map.Entry<String, String> entry : arguments.entrySet()) {
            command.add(String.format("--%s=%s", entry.getKey(), entry.getValue()));
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        this.process = pb.start();

        // 初始化输入输出流
        this.inputWriter = new PrintWriter(process.getOutputStream());
        this.outputBuffer = new LinkedBlockingQueue<>(MAX_BUFFER_SIZE);

        // 启动输出处理线程
        this.outputGobbler = new Thread(this::handleOutputStream);
        this.errorGobbler = new Thread(this::handleErrorStream);
        outputGobbler.start();
        errorGobbler.start();
    }

    /**
     * 处理标准输出流
     */
    private void handleOutputStream() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                synchronized (outputBuffer) {
                    // 维护环形缓冲区
                    if (outputBuffer.size() >= MAX_BUFFER_SIZE) {
                        outputBuffer.poll();
                    }
                    outputBuffer.offer(line);
                }
            }
        } catch (IOException e) {
            if (!process.isAlive()) return;
        }
    }

    /**
     * 处理错误输出流
     */
    private void handleErrorStream() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.err.println("[ERROR] " + line);
            }
        } catch (IOException e) {
            if (!process.isAlive()) return;
        }
    }

    /**
     * 停止进程
     */
    public void stop() {
        if (process.isAlive()) {
            process.destroyForcibly();
            outputGobbler.interrupt();  // 显式中断
            errorGobbler.interrupt();
            closeResources();
        }
    }

    /**
     * 向进程标准输入发送字符串
     */
    public void input(String str) {
        if (process.isAlive()) {
            inputWriter.println(str);
            inputWriter.flush();
        }
    }

    /**
     * 获取缓存的输出（最近100行）
     */
    public List<String> output() {
        synchronized (outputBuffer) {
            return new ArrayList<>(outputBuffer);
        }
    }

    /**
     * 清理资源
     */
    private void closeResources() {
        inputWriter.close();
        closeQuietly(process.getInputStream());
        closeQuietly(process.getErrorStream());
        closeQuietly(process.getOutputStream());
    }

    private void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) closeable.close();
        } catch (IOException ignored) {}
    }


    /**
     * 检查进程是否存活
     */
    public boolean isAlive() {
        return process.isAlive();
    }
}