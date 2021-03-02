package memphis.deeptutor.model;

import java.util.Calendar;
import java.util.Date;

public class Utils
{
	public static Date computeDTDay(Date date, int hourDayStarts)
	{
		Date ret = new Date(date.getYear(), date.getMonth(), date.getDate());
		int hourOfTheDay = date.getHours();

		if (hourOfTheDay < hourDayStarts)
		{
			Calendar cal = Calendar.getInstance();
			cal.setTime(ret);
			cal.add(Calendar.DAY_OF_MONTH, -1);
			ret = cal.getTime();

			hourOfTheDay = 24 - hourOfTheDay - 1;
		}

		return ret;
	}
}
