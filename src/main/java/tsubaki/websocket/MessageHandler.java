package tsubaki.websocket;



import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.*;
import tsubaki.Loader;

import java.nio.charset.StandardCharsets;

@Controller
public class MessageHandler  implements WebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("新连接建立: " + session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
        System.out.println(Loader.getContext().getBean("processer"));
        if(webSocketMessage instanceof BinaryMessage){
            System.out.println("接受到二进制消息");
        }
        else if(webSocketMessage instanceof TextMessage){
                System.out.println("接受到文本消息:"+new String(((TextMessage)webSocketMessage).asBytes(), StandardCharsets.UTF_8));
        }
    }

    @MessageMapping("/hello")
    public void handleHelloMessage(String message) {
        System.out.println("Received message: " + message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("Transport error: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        System.out.println("Connection closed: " + session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

}



