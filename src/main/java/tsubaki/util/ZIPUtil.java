package tsubaki.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZIPUtil {

    /**
     * 将 ZIP 输入流解压到指定目录（自动清空目标目录）
     * @param zipInputStream ZIP 文件输入流（会自动关闭）
     * @param targetDir 目标目录（必须已存在）
     * @throws IOException 解压失败或目录操作异常
     */
    public static void unzipStreamToDirectory(InputStream zipInputStream, Path targetDir) throws IOException {
        // 参数校验
        if (!Files.isDirectory(targetDir)) {
            throw new IllegalArgumentException("目标路径不是目录: " + targetDir);
        }

        // 清空目标目录
        emptyDirectory(targetDir.toFile());
        // 使用 try-with-resources 确保流关闭
        try (ZipInputStream zis = new ZipInputStream(zipInputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // 解析目标路径并安全检查
                Path resolvedPath = resolveSafePath(targetDir, entry.getName());

                if (entry.isDirectory()) {
                    // 创建目录（即使没有目录条目也兼容）
                    Files.createDirectories(resolvedPath);
                } else {
                    // 确保父目录存在
                    Path parent = resolvedPath.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }

                    // 写入文件内容
                    Files.copy(zis, resolvedPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }


    public static void emptyDirectory(File directory) throws IOException {
        // 检查目录是否存在
        if (!directory.exists()) {
            throw new IOException("Directory does not exist: " + directory.getAbsolutePath());
        }
        // 确认是否为目录
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Provided path is not a directory: " + directory.getAbsolutePath());
        }
        // 获取目录下的所有子项
        File[] files = directory.listFiles();
        if (files == null) {
            throw new IOException("Failed to access contents of directory: " + directory.getAbsolutePath());
        }
        // 遍历并递归删除每个子项
        for (File file : files) {
            deleteRecursively(file);
        }
    }
    private static void deleteRecursively(File file) throws IOException {
        if (file.isDirectory()) {
            // 递归删除子目录内容
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        // 删除文件或空目录
        if (!file.delete()) {
            throw new IOException("Failed to delete: " + file.getAbsolutePath());
        }
    }
    /**

    /**
     * 安全解析路径，防止 ZIP 路径遍历攻击
     */
    private static Path resolveSafePath(Path targetDir, String entryName) throws IOException {
        // 过滤非法字符（可选）
        String sanitizedName = entryName.replaceAll("[\\\\/]", "/");

        // 解析路径并标准化
        Path resolvedPath = targetDir.resolve(sanitizedName).normalize();

        // 检查是否在目标目录内
        if (!resolvedPath.startsWith(targetDir)) {
            throw new IOException("非法 ZIP 条目路径: " + entryName);
        }
        return resolvedPath;
    }
}