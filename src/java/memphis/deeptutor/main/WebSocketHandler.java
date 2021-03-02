package memphis.deeptutor.main;

import java.util.Hashtable;

import memphis.deeptutor.model.BusinessModel;
import memphis.deeptutor.model.DTSession;
import memphis.deeptutor.model.SessionData;
import memphis.deeptutor.model.Student;
import memphis.deeptutor.servlets.InitDTServlet;

import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.websockets.ProtocolHandler;
import com.sun.grizzly.websockets.WebSocket;
import com.sun.grizzly.websockets.WebSocketApplication;
import com.sun.grizzly.websockets.WebSocketListener;

//make sure you enable the web-sockets in glassfish
//use command: asadmin set configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.websockets-support-enabled=true
//To access GlassFish Server 3.1.2 use the plugin from here (for Indigo): http://dlc.sun.com.edgesuite.net/glassfish/eclipse/indigo/
//http://localhost:8080/DeeptutorApp/wizardofoz.jsp

public class WebSocketHandler extends WebSocketApplication {

	public WebSocketHandler(){
	}
	
	// send data to the client
    private void send(WebSocket socket, String text) {
        try {
            socket.send(text);
        } catch (Exception e) {  //not throwing IOException??
            onClose(socket);
        }
    }
	
	Hashtable<WebSocket, DTSession> connectedUsers = new Hashtable<WebSocket, DTSession>();
	
	@Override
    public WebSocket createWebSocket(ProtocolHandler handler, WebSocketListener... listeners) {
		ChatWebSocket socket = new ChatWebSocket(handler, listeners);
		//connectedUsers.put(socket, null); /* build and add session data later, when id is read */
		System.out.print("createSocket in websockethandler...");
		return socket;
    }
	
	public void onConnect(WebSocket socket) {
		System.out.println("Socket connecting...");
    }
	
	public void onClose(WebSocket socket)
	{
    	System.out.print("Socket Closing...");
		if (connectedUsers.containsKey(socket))
		{
			//close connection for WoZ instances
			WizardofOzServiceHandler woz = InitDTServlet.wozHandler;
			if (woz.connectedUsers.contains(socket) && woz.connectedUsers.get(socket).tutorResponse!=null)
			{
				woz.connectedUsers.get(socket).tutorResponse.disconnectWoz = true;
				woz.SendResponseToFlexUI(woz.connectedUsers.get(socket).studentID, woz.connectedUsers.get(socket).tutorResponse);
			}

			connectedUsers.remove(socket);
		}
    	this.remove(socket);
    	PrintCurrentSockets();
	}
	
    //when message/data is received from the client
    public void onMessage(WebSocket socket, String data) {
    	System.out.print("Message Received from client: " + data);
    	send(socket, getResults((ChatWebSocket)socket, data));
    }

    public String getResults(ChatWebSocket socket, String inputText){
    	
		//this is another way of pooling data from the server
		//if (inputText.equals("\\pollTutorResponse"))
		//{
		//	if (!WebSocketsServlet.app.tutorResponded) return "noTutorResp";
		//	else return ResponseToClient(WebSocketsServlet.app.lastTutorResponse, false);
		//}

		/* Getting empty session..
		HttpSession httpSession = socket.getRequest().getSession();
		if (httpSession != null) {
			System.out.println("I have got the session...");
			if(httpSession.getAttribute("student") != null)
				System.out.println("Student is available in the session!!!");
			if(httpSession.getAttribute("MYKEY") == null)
				httpSession.setAttribute("MYKEY", "My Value");
			else
				System.out.println("mykey value is " + httpSession.getAttribute("MYKEY")); 
		}*/
		
		//*************************************************************************
		//Wizard of OZ interface
		if (inputText.startsWith("\\woz-")){
			return InitDTServlet.wozHandler.onMessage(socket, inputText);
		}
		//*************************************************************************
			
		//get previous session if it exists
		DTSession dtSession = connectedUsers.get(socket);
		RemoteServiceHandler rsHandler = new RemoteServiceHandler();
		
		if (dtSession == null)
		{
			dtSession = new DTSession();
			
			String studentID  = null;
			/* protocol string: \\initialize <initial task> <user id> 
			 * Example: \\initialize demo1 student1
			 * */
			if (inputText.trim().startsWith("\\initialize"))
			{
				studentID = inputText.split(" ")[2];
				rsHandler.student = BusinessModel.getInstance().getStudentFromDatabaseByID(studentID);
				System.out.println("Client has sent The student id >>> " + studentID);
			}
			
			dtSession.setAttribute(DTSession.getSESSION_STUDENT_ID(), rsHandler.student);
			
			//Session data is built and added later..

			dtSession.setAttribute(DTSession.getSESSION_TASKS_ID(), null);
			
			connectedUsers.put(socket, dtSession); 
			//return getNewQuestion();
		}
		
		
		if (dtSession != null)
		{
			rsHandler.data = (SessionData)dtSession.getAttribute(DTSession.getSESSION_DATA_ID());
			rsHandler.student = (Student)dtSession.getAttribute(DTSession.getSESSION_STUDENT_ID());
			rsHandler.tasksStr = (String)dtSession.getAttribute(DTSession.getSESSION_TASKS_ID());
		}

		rsHandler.mySession = dtSession;
		String response = rsHandler.getResults(inputText);
		
		return response;
	}
	
	@Override
	public boolean isApplicationRequest(Request arg0) {
		System.out.println(arg0.toString() + "isApplicationRequest...");
		// TODO Auto-generated method stub
    	PrintCurrentSockets();
		return true;
	}

	public void PrintCurrentSockets()
	{
		String result = "Current open sockets:";
		int i=0;
        for (final WebSocket webSocket : getWebSockets()) {
        	result += (i++)+"-"+webSocket.isConnected()+" ";
        }		
    	System.out.println(result);
	}
	
}