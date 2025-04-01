package tsubaki.message;


import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;

import java.nio.charset.StandardCharsets;

@Component("processer")
public class Processer {
    public Processer(){
    }

    //处理文本消息
    public void processTextMessage(TextMessage textMessage){
        //提取真正文本
        String text=new String(textMessage.asBytes(), StandardCharsets.UTF_8);

    }
    //处理二进制消息，并返回消息
    public byte[] processBinaryMessage(BinaryMessage binaryMessage){
        byte [] res = new byte[0];
        return res;
    }

}
