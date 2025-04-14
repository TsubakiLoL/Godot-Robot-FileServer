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
    private final Thread monitorThread;
    private final List<ProcessExitListener> exitListeners = new ArrayList<>();
    private static final int MAX_BUFFER_SIZE = 100;

    // 进程终止监听接口
    public interface ProcessExitListener {
        void onProcessExit();
    }

    public ExternalProcess(String executablePath, Map<String, String> arguments) throws IOException {
        List<String> command = buildCommand(executablePath, arguments);
        ProcessBuilder pb = new ProcessBuilder(command);
        this.process = pb.start();

        this.inputWriter = new PrintWriter(process.getOutputStream());
        this.outputBuffer = new LinkedBlockingQueue<>(MAX_BUFFER_SIZE);

        // 启动流处理线程
        this.outputGobbler = new Thread(this::handleOutputStream);
        this.errorGobbler = new Thread(this::handleErrorStream);
        outputGobbler.start();
        errorGobbler.start();

        // 启动监控线程
        this.monitorThread = new Thread(this::monitorProcess);
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private List<String> buildCommand(String path, Map<String, String> args) {
        List<String> command = new ArrayList<>();
        command.add(path);
        command.add("--");
        args.forEach((k, v) -> command.add(String.format("--%s=%s", k, v)));
        return command;
    }

    private void handleOutputStream() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                synchronized (outputBuffer) {
                    manageBuffer(line);
                }
            }
        } catch (IOException e) {
            if (!process.isAlive()) return;
        }
    }

    private void manageBuffer(String line) {
        if (outputBuffer.size() >= MAX_BUFFER_SIZE) outputBuffer.poll();
        outputBuffer.offer(line);
    }

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

    private void monitorProcess() {
        try {
            process.waitFor();
            notifyExitListeners();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void notifyExitListeners() {
        for (ProcessExitListener listener : exitListeners) {
            listener.onProcessExit();
        }
    }

    public void addExitListener(ProcessExitListener listener) {
        exitListeners.add(listener);
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
            // 发送到进程输入流
            inputWriter.println(str);
            inputWriter.flush();

            // 将输入内容同步记录到输出缓冲区
            synchronized (outputBuffer) {
                manageBuffer("[INPUT] " + str); // 添加输入标记
            }
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