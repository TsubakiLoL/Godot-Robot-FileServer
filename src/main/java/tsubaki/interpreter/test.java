package tsubaki.interpreter;

import com.google.gson.Gson;
import org.apache.tomcat.jni.Time;
import tsubaki.util.FileUtil;
import tsubaki.util.SystemUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static tsubaki.interpreter.InterpreterDB.getUserSandBoxPath;

public class test {
    public static void main(String[] args) throws IOException, InterruptedException {
//        Map<String, String> arguments=new HashMap<String,String>();
//
//        arguments.put("Mod","D:\\RobotRelease\\Mod装载版本测试\\mod");
//        arguments.put("Nodeset","D:\\RobotRelease\\Mod装载版本测试\\nodeset");
//        ExternalProcess externalProcess=ExternalProcessFactory.create_process("D:\\RobotRelease\\Mod装载版本测试\\StarBotInterpreter.exe",arguments);

        String execute_path=InterpreterDB.get_interpreter_path(SystemUtil.judgeSystem());
        if(execute_path==""){
            return;
        }
        System.out.println(execute_path);
        //获取角色沙盒路径
        String userRoot=getUserSandBoxPath("d34bfe1d-5c7a-41f2-8714-60ec04664789");
        Map<String,String> arguments=new HashMap<String,String>();
        arguments.put("Mod", FileUtil.getUsefulPath(userRoot+"/Mod"));
        arguments.put("Nodeset",FileUtil.getUsefulPath(userRoot+"/Nodeset"));
        Gson gson=new Gson();
        System.out.println(gson.toJson(arguments));
        ExternalProcess externalProcess=ExternalProcessFactory.create_process(execute_path, arguments);
        while(true){
            System.out.println(externalProcess.output());
            Thread.sleep(1000);
        }

    }
}
