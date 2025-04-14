package tsubaki.interpreter;

import tsubaki.util.FileUtil;
import tsubaki.util.SystemUtil;
import java.io.IOException;
import java.util.*;

public class InterpreterDB {
    private final Map<String, ExternalProcess> inner_data = new HashMap<>();
    private static final InterpreterDB singleton = new InterpreterDB();

    public static InterpreterDB get() {
        return singleton;
    }

    public ExternalProcess getProcessByID(String id) {
        return inner_data.get(id);
    }

    public boolean hasProcess(String id) {
        return inner_data.containsKey(id);
    }

    public List<String> getProcessBlog(String id) {
        return hasProcess(id) ?
                getProcessByID(id).output() :
                new ArrayList<>();
    }

    public boolean createProcess(String id) {
        String executePath = get_interpreter_path(SystemUtil.judgeSystem());
        if (executePath.isEmpty()) return false;

        try {
            ExternalProcess process = ExternalProcessFactory.create_process(
                    executePath,
                    buildArguments(id)
            );

            registerExitListener(id, process);
            inner_data.put(id, process);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Map<String, String> buildArguments(String id) {
        String userRoot = getUserSandBoxPath(id);
        return Map.of(
                "Mod", userRoot + "/Mod",
                "Nodeset", userRoot + "/Nodeset"
        );
    }

    private void registerExitListener(String id, ExternalProcess process) {
        process.addExitListener(() -> {
            synchronized (inner_data) {
                if (inner_data.containsKey(id)) {
                    inner_data.remove(id);
                    System.out.println("Auto-removed terminated process: " + id);
                }
            }
        });
    }

    public void deleteProcess(String id) {
        synchronized (inner_data) {
            if (inner_data.containsKey(id)) {
                inner_data.get(id).stop();
                inner_data.remove(id);
            }
        }
    }



    static String get_interpreter_path(String systemType) {

        switch (systemType) {
            case "windows" -> {
                return FileUtil.getUsefulPath(FileUtil.getJarFilePath() + "/interpreter/windows.exe");
            }
            case "linux" -> {
                return FileUtil.getUsefulPath(FileUtil.getJarFilePath() + "/interpreter/linux.x86_64");
            }
            default -> {
                return "";
            }
        }
    }
    //获取某个用户的沙盒目录，如果不存在则创建对应的用户目录
    public static String getUserSandBoxPath(String id){


        String sandRoot=FileUtil.getUsefulPath(FileUtil.getJarFilePath()+"/userSandBox/"+id);
        FileUtil.createDirectoryIfNotExists(sandRoot);
        String modPath=FileUtil.getUsefulPath(sandRoot+"/Mod");
        String nodePath=FileUtil.getUsefulPath(sandRoot+"/Nodeset");
        FileUtil.createDirectoryIfNotExists(modPath);
        FileUtil.createDirectoryIfNotExists(nodePath);

        return sandRoot;
    }


    public void input(String id,String input){
        ExternalProcess process=getProcessByID(id);

        if (process != null && process.isAlive()) {

            process.input(input);
        }
    }



}
