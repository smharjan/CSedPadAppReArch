package memphis.deeptutor.main;
 
import com.sun.grizzly.websockets.DefaultWebSocket;
import com.sun.grizzly.websockets.ProtocolHandler;
import com.sun.grizzly.websockets.WebSocketListener;
 
public class ChatWebSocket extends DefaultWebSocket {
    public ChatWebSocket(ProtocolHandler protocolHandler, WebSocketListener... listeners) {
        super(protocolHandler, listeners);
    }
    
}