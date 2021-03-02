package memphis.deeptutor.main;

import java.util.Date;
import java.util.Hashtable;

import memphis.deeptutor.gui.commands.DTCommands;
import memphis.deeptutor.gui.model.Components;
import memphis.deeptutor.gui.model.DTResponse;
import memphis.deeptutor.singleton.DerbyConnector;

import com.sun.grizzly.websockets.WebSocket;

import flex.messaging.MessageBroker;
import flex.messaging.messages.AsyncMessage;
 
public class WizardofOzServiceHandler {

	class StudentData{
		public String studentID = "";
		public Components tutorResponse = null;
		
		public StudentData(String _studentID, Components _tutorResponse)
		{
			studentID = _studentID;
			_tutorResponse = tutorResponse;
		}
		
		@Override
		public boolean equals(Object o)
		{
			if (o.getClass().equals(StudentData.class))
			{
				return studentID.equals(((StudentData)o).studentID);
			}
			if (o.getClass().equals(String.class))
			{
				return studentID.equals((String)o);
			}
			else return false;
		}
	}
	
	Hashtable<WebSocket,StudentData> connectedUsers = new Hashtable<WebSocket, StudentData>();
	
	public boolean isTutorConnected(String studentID)
	{
		if (connectedUsers.contains(studentID)) return true;
		else return false;
	}
	
	
    public String onMessage(WebSocket socket, String text) {
    	//System.out.print("Message Received: " + text);
    	if (text.equals("\\woz-getusers"))
    	{
    		//we also disconnect by default, in case the socket is already connected to a student
    		if (connectedUsers.containsKey(socket))
    		{
    			//System.out.println("responding to student...");
    			if (connectedUsers.get(socket).tutorResponse!=null)
    			{
    				//System.out.println("automatic");
    				connectedUsers.get(socket).tutorResponse.disconnectWoz = true;
    				SendResponseToFlexUI(connectedUsers.get(socket).studentID, connectedUsers.get(socket).tutorResponse);
    			}
    			connectedUsers.remove(socket);
    		}
    		String users = DerbyConnector.getInstance().getStudents(connectedUsers.values());
    		return "Users:"+users;
    	}
    	
    	
    	if (text.startsWith("\\woz-connect"))
    	{
    		String userID = text.split(" ")[1];
    		connectedUsers.put(socket, new StudentData(userID,null));
    		return "Connected:"+userID;
    	}
    	
    	if (!connectedUsers.containsKey(socket)) 
    	{
    		return "(Echo message) "+text; //there is no connection; just send back an echo message
    	}

    	if (text.startsWith("\\woz-tutorResponse"))
    	{
    		SendResponseToFlexUI(connectedUsers.get(socket).studentID, connectedUsers.get(socket).tutorResponse);
    		String answer = connectedUsers.get(socket).tutorResponse.getResponse().getAllResponseText();
    		connectedUsers.get(socket).tutorResponse = null;
    		
    		return "Tutor: " + answer;
    	}

    	if (text.startsWith("\\woz-response")) 
    	{
        	//remove the "\\woz-response" string preceeding the message
    		text = text.substring(14);
    	}
    	
    	String studentID = connectedUsers.get(socket).studentID;
    	
    	//for (final WebSocket webSocket : getWebSockets()) {webSocket.send(text);}
        
    	//remove the "Tutor:" string preceeding the message
        String lastTutorResponse = text.substring(7);
        connectedUsers.get(socket).tutorResponse = null;
        
        Components c = new Components();
		DTResponse resp= new DTResponse();
		resp.addResponseText(lastTutorResponse);
		c.setResponse(resp);
		c.inputShowContinue = false;
		
        SendResponseToFlexUI(studentID, c);
        
        return text;
    }
    
    public void SendResponseToFlexUI(String studentID, Components c)
    {
        try {
	        AsyncMessage msg = new AsyncMessage();
	        msg.setTimestamp(new Date().getTime());
	        msg.setClientId("JavaMessageProducer");
	        msg.setMessageId("JavaMessageId"); 
	        //destination configured in messaging-config.xml
	        msg.setDestination("chat-application");
	        msg.setBody(studentID + " " + (new DTCommands()).getCommands(c));
	        //you can set custom message headers
	        //msg.setHeader("headerParam1", "value1");
	        //msg.setHeader("headerParam2", "value2");
	        //send message to destination
	        if (MessageBroker.getMessageBroker(null) != null)
	        	MessageBroker.getMessageBroker(null).routeMessageToService(msg, null);
	    	System.out.print("Flex Message Sent...");
        } catch (Exception e) {
             e.printStackTrace();
        }
        
    }

    public void BroadcastText(String studentID, String text, Components tutorResponse)
    {
    	//System.out.println("Sending Text: "+ text);
    	for (WebSocket webSocket: connectedUsers.keySet())
    	{
    		if (connectedUsers.get(webSocket).studentID.equals(studentID))
    		{
    			webSocket.send(text);
    			if (tutorResponse != null) {
    				connectedUsers.get(webSocket).tutorResponse = tutorResponse;
    				webSocket.send("TutorResponse:" + tutorResponse.getResponse().getAllResponseText());
    			}
    			
    		}
    	}
    }
    
}  