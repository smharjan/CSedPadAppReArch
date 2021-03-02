package dt.persistent.xml;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class DTCommands {

	public static String getCommands(Components c){
		String command="<DTCommands>";
		
		if(c.getQuestion()!=null){
			command+="<command type=\"loadNewQuestion\">"; 
			command+="<question>";
			command+="<text>"+c.getQuestion().getText()+"</text>";
			command+="<text2>"+c.getQuestion().getText2()+"</text2>";
			if (c.getQuestion().getImage()!=null)
			{
				command+="<image>"+
					 "<src>"+c.getQuestion().getImage().getSource()+"</src>"+
					 "<height>"+c.getQuestion().getImage().getHeight()+"</height>"+
					 "<width>"+c.getQuestion().getImage().getWidth()+"</width>"+
					 "</image>";
			}
			
			if(c.getMultimedia()!=null){
				command+="<multimedia>" +
				"<src>"+c.getMultimedia().getSource()+"</src>" +
				"<type>"+c.getMultimedia().getType()+"</type>" +
				"<height>"+c.getMultimedia().getHeight() + "</height>" +
				"<width>"+ c.getMultimedia().getWidth() +"</width>" +
				"</multimedia>" ;
			}
			
			command+="</question>";
			
			command+="</command>";
		}
		
		if(c.getMultimedia()!=null){
			command+="<command type=\"changeMultimedia\">" +
			"<multimedia>" +
			"<src>"+c.getMultimedia().getSource()+"</src>" +
			"<type>"+c.getMultimedia().getType()+"</type>" +
			"<height>"+c.getMultimedia().getHeight() + "</height>" +
			"<width>"+ c.getMultimedia().getWidth() +"</width>" +
			"</multimedia>"+
			"</command>";			
		}
		
		//the clear history command must be put before setting up the tutor response
		if (c.clearHistory==true)
		{
			command+="<command type=\"clearHistory\"/>";
		}

		if (c.listen4woz==true)
		{
			command+="<command type=\"listen4woz\"/>";
		}

		if (c.disconnectWoz==true)
		{
			command+="<command type=\"disconnectWoz\"/>";
		}
		
		if (c.studentID!=null)
		{
			command+="<command type=\"setStudentID\">"+c.studentID+"</command>";
		}

		if(c.getResponse()!=null){
			command+="<command type=\"responseToStudent\">";
			for (int i=0;i<c.getResponse().getResponseCount();i++) command+="<response>"+c.getResponse().getResponseText(i) +"</response>";
			command+="</command>";
		}
		
		if(c.getAvatar()!=null){
			command+="<command type=\"changeAvatar\">" +
					 "<source>"+c.getAvatar().getSource()+"</source>" +
			"</command>";	
		}
		
		if(c.getNotice()!=null){
			command+="<command type=\"changeInformation\">" +
			 "<information>"+c.getNotice().getNotice()+"</information>" +
			 "</command>";	
		}

		if (c.inputShowContinue==true)
		{
			command+="<command type=\"inputShowContinue\"/>";
		}
		
		if (c.requestLogin == true)
		{
			command+="<command type=\"requestLogin\"/>";
		}
		if (c.isFinishedAllTasks() == true)
		{
			command+="<command type=\"finishedTasks\"/>";
		}
		
		if(c.isShowAnswersMode() && (!c.inputShowContinue)){
			command+="<command type=\"enableShowAnswerBtn\"/>";
		}
		
		command+="</DTCommands>";	
		//System.out.println("Sending to client is >>> : " + command);
		
		return command;
	}
	
	public static void main(String args[]){

		Question q= new Question();
		q.setText("Suppose a truck (2000 kg) is towing a car (1000 kg), and the truck is picking up speed.\n How do the amounts of these two forces compare?\na) the force of the truck pulling the car, and \nb) the force of the car pulling the truck\n\n2. By itself, when not towing anything, the truck can accelerate at 3 meters per second per second.  What acceleration can the truck attain while towing the car?");
		QImage img=new QImage();
		img.setSource("../includes/FCI-RV95_withInstr-2_page7_image1.gif");
		img.setHeight(100);
		img.setWidth(100);
		q.setImage(img);
		
		Multimedia m=new Multimedia();
		m.setSource("http://www.cs.memphis.edu/~vrus/DeepTutor/DeepT-Proto-NewSim3.swf");
		m.setType("flash");
		m.setHeight(10);
		m.setWidth(20);
		
		DTResponseOld resp= new DTResponseOld();
		resp.addResponseText("Sample response from xml");
		
		Avatar av= new Avatar();
		av.setSource("http://localhost:8080/CSedPadAppReArch/includes/Avatar.swf");
		
		Components c= new Components();
		c.setQuestion(q);
		c.setMultimedia(m);
		c.setAvatar(av);
		c.setResponse(resp);
		
		DTCommands dtc= new DTCommands();
		String commandString=dtc.getCommands(c);
		commandString="<?xml version=\"1.0\"?>\n"+commandString;
		try {
			BufferedWriter bw=new BufferedWriter(new FileWriter("test/command.xml"));
			bw.write(commandString);
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(commandString);
		
	}
	
}
