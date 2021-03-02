package memphis.deeptutor.model;

import java.sql.Timestamp;

public class TodoItem {
	private int todoID;
	private String creator;
	private String assignee;
	private String text;
	private String response;
	private Timestamp dateCreated;
	private Timestamp dateClosed;
	
	public int getTodoID() {
		return todoID;
	}
	public void setTodoID(int todoID) {
		this.todoID = todoID;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public String getAssignee() {
		return assignee;
	}
	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public Timestamp getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}
	public Timestamp getDateClosed() {
		return dateClosed;
	}
	public void setDateClosed(Timestamp dateClosed) {
		this.dateClosed = dateClosed;
	}
	
}
