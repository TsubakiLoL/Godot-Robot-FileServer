package tsubaki.interpreter;

import java.util.HashMap;
import java.util.Map;

//用来维护
public class InterpreterDB {


    //当前持有的线程实例
    private final Map<String,ExternalProcess> inner_data=new HashMap<String,ExternalProcess>();

    //单例
    private final static InterpreterDB singleton=new InterpreterDB();

    //获取单例
    public static InterpreterDB get(){
        return InterpreterDB.singleton;
    }

    //获取实例
    public ExternalProcess getProcessByID(String id){

        if(inner_data.containsKey(id)){
            return inner_data.get(id);
        }
        else{
            return null;
        }
    }

    //是否持有实例
    public boolean hasProcess(String id){
        return inner_data.containsKey(id);
    }



    enum systemType{windows,linux}
    static void get_interpreter_path(systemType systemType){

        switch (systemType){
            case windows -> {}
            case linux -> {}
        }

    }
}
