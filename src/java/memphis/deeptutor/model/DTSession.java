package memphis.deeptutor.model;

import java.util.HashMap;
import java.util.Map;

import memphis.deeptutor.model.BusinessModel.DTMode;

public class DTSession
{

	private Map<Object, Object> sessionAttributes;

	private static String SESSION_DATA_ID = "data";
	private static String SESSION_STUDENT_ID = "student";
	private static String SESSION_TASKS_ID = "tasks";
	private DTMode dtMode = DTMode.INTERACTIVE;

	public DTSession()
	{
		sessionAttributes = new HashMap<Object, Object>();
	}

	public DTMode getDTMode()
	{
		return dtMode;
	}

	public void setDTMode(DTMode dtMode)
	{
		this.dtMode = dtMode;
	}

	public Object getAttribute(String key)
	{
		return sessionAttributes.get(key);
	}

	public void setAttribute(Object key, Object value)
	{
		sessionAttributes.put(key, value);
	}

	public void removeAttribute(Object key)
	{
		if (sessionAttributes.containsKey(key))
		{
			sessionAttributes.remove(key);
		}
	}

	public static String getSESSION_STUDENT_ID()
	{
		return SESSION_STUDENT_ID;
	}

	public static String getSESSION_TASKS_ID()
	{
		return SESSION_TASKS_ID;
	}

	public static String getSESSION_DATA_ID()
	{
		return SESSION_DATA_ID;
	}
}
