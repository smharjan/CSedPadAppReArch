package memphis.deeptutor.dialog;

public class Definition
{
	private String text = null;
	private String type = null;
	private String source = null;

	public Definition(String text, String type, String source)
	{
		this.text = text;
		this.type = type;
		this.source = source;
	}

	public String getText()
	{
		return this.text;
	}

	public String getType()
	{
		return this.type;
	}

	public String getSource()
	{
		return this.source;
	}
}
