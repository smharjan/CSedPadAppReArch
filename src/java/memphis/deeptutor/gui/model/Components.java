package memphis.deeptutor.gui.model;

public class Components
{
	private Question question;
	private Multimedia multimedia;
	private Notice notice;
	private DTResponse response;
	private Avatar avatar;
	public boolean inputShowContinue = false;
	public boolean clearHistory = false;
	public boolean requestLogin = false;
	public String studentID = null;
	public boolean listen4woz = false;
	public boolean disconnectWoz = false;
	private boolean showAnswersMode = false;
	private boolean finishedAllTasks = false;
	private String progress = "";

	public boolean isFinishedAllTasks()
	{
		return finishedAllTasks;
	}

	public void setFinishedAllTasks(boolean finishedAllTasks)
	{
		this.finishedAllTasks = finishedAllTasks;
	}

	public boolean isShowAnswersMode()
	{
		return showAnswersMode;
	}

	public void setShowAnswersMode(boolean showAnswersMode)
	{
		this.showAnswersMode = showAnswersMode;
	}

	public Avatar getAvatar()
	{
		return avatar;
	}

	public void setAvatar(Avatar avatar)
	{
		this.avatar = avatar;
	}

	public DTResponse getResponse()
	{
		return response;
	}

	public void setResponse(DTResponse response)
	{
		this.response = response;
	}

	public Question getQuestion()
	{
		return question;
	}

	public void setQuestion(Question question)
	{
		this.question = question;
	}

	public Multimedia getMultimedia()
	{
		return multimedia;
	}

	public void setMultimedia(Multimedia multimedia)
	{
		this.multimedia = multimedia;
	}

	public Notice getNotice()
	{
		return notice;
	}

	public void setNotice(Notice notice)
	{
		this.notice = notice;
	}

	public String getProgress()
	{
		return progress;
	}

	public void setProgress(String progress)
	{
		this.progress = progress;
	}

}
