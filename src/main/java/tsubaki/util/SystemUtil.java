package tsubaki.util;

public class SystemUtil {
    // 判断是否为 Linux 系统
    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    // 判断是否为 Windows 系统
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    // 返回系统类型
    public static String judgeSystem() {
        if (isLinux()) {
            return "linux";
        } else if (isWindows()) {
            return "windows";
        } else {
            return "unknown";
        }
    }


}
