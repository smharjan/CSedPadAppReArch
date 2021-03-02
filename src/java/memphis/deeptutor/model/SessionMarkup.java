package memphis.deeptutor.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

//Dan: this class is used for the markups needed on presenting the progress of a student at the beginning of a working session
public class SessionMarkup
{
	private final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

	private String color;
	private String bcolor;
	private String value;
	private String finished;
	private String schedule;
	private String smarkup;
	private String emarkup;
	private boolean selected = false;

	public SessionMarkup()
	{
		this.color = "#666699";
		this.bcolor = "#e8edff";
		this.value = "-";
		this.finished = "-";
		this.schedule = "NOT YET AVAILABLE";
		this.smarkup = "";
		this.emarkup = "";
	}

	public void deepTutorStandardAnnotation(Date now, Date startingDate,
			Date endingDate, Date finishedDate, boolean alreadySelected)
	{
		DateFormat df = new SimpleDateFormat("MM/dd/yy");

		if (finishedDate != null)
		{
			this.bcolor = "#d8ffdc";
			Date finishedDay = new Date(finishedDate.getYear(), finishedDate.getMonth(), finishedDate.getDate());
			
			if (finishedDay.after(endingDate))
			{
				//this.color = "green";
				this.value = "";
				//this.finished = df.format(finishedDate);
				this.schedule = "Completed AFTER deadline";
			}
			else
			{
				this.color = "green";
				this.value = "&#10003;";
				this.finished = df.format(finishedDate);
				this.schedule = "Completed on " + this.finished;
			}
		}
		else if (now.compareTo(endingDate) > 0)
		{
			if (!alreadySelected)
			{
				this.smarkup = "<b>";
				this.emarkup = "</b>";
				this.setSelected(true);
			}
			
			this.color = "red";
			this.bcolor = "#ffe8ed";
			this.value = "&#10005;";
			this.schedule = "Required MAKE-UP Work";
		}
		else if (now.compareTo(startingDate) >= 0)
		{
			if (!alreadySelected)
			{
				this.smarkup = "<b>";
				this.emarkup = "</b>";
				this.setSelected(true);
			}
			
			this.value = "ACTIVE";
			int diffInDays = (int) ((endingDate.getTime() - now.getTime()) / DAY_IN_MILLIS);
			if (diffInDays == 0)
			{
				this.schedule = "Due: TODAY!";
			}
			else if (diffInDays == 1)
			{
				this.schedule = "One Day Left (due on " + df.format(endingDate) + ")";
			}
			else if (diffInDays > 1)
			{
				this.schedule = diffInDays + " Days Left (due on " + df.format(endingDate) + ")";
			}
		}
	}

	public String getColor()
	{
		return color;
	}

	public void setColor(String color)
	{
		this.color = color;
	}

	public String getBcolor()
	{
		return bcolor;
	}

	public void setBcolor(String bcolor)
	{
		this.bcolor = bcolor;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public String getFinished()
	{
		return finished;
	}

	public void setFinished(String finished)
	{
		this.finished = finished;
	}

	public String getSchedule()
	{
		return schedule;
	}

	public void setSchedule(String schedule)
	{
		this.schedule = schedule;
	}

	public String getSmarkup()
	{
		return smarkup;
	}

	public void setSmarkup(String smarkup)
	{
		this.smarkup = smarkup;
	}

	public String getEmarkup()
	{
		return emarkup;
	}

	public void setEmarkup(String emarkup)
	{
		this.emarkup = emarkup;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}
}
