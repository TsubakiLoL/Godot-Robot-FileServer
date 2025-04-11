package tsubaki.interpreter;

import org.apache.tomcat.jni.Time;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class test {
    public static void main(String[] args) throws IOException, InterruptedException {
        Map<String, String> arguments=new HashMap<String,String>();

        arguments.put("Mod","D://RobotRelease/Mod装载版本测试/mod");
        arguments.put("Nodeset","D://RobotRelease/Mod装载版本测试/nodeset");
        ExternalProcess externalProcess=ExternalProcessFactory.create_process("D://RobotRelease/Mod装载版本测试/StarBotInterpreter.exe",arguments);

        while(true){
            System.out.println(externalProcess.output());
            Thread.sleep(1000);
        }

    }
}
