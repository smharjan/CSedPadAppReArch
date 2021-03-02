package memphis.deeptutor.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.output.StringBuilderWriter;

import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import memphis.deeptutor.model.Expectation.EXPECT_TYPE;

public class SessionData
{
	public Expectation[] taskExpectations = null;
	public String taskSummary = null;
	public HashSet<String> coveredMisconceptions = new HashSet<String>();

	public int turnNumber = 0;
	public String inputReplaceIt = null;
	public boolean replaceItAsked = false;
	public String currentTaskID = null;

	// Dan: index in the TASKS_IDS list of tasks in RemoteServiceHandler
	public int currentTaskIndex;
	public int currentStrandIndex;
	public int totalNumberOfTasks;

	public boolean debugMode = false;

	public boolean hadTooShort = false;
	public boolean hadIrrelevant = false;
	public boolean hadWhatIt = false;
	public boolean hadAlready = false;

	public boolean postFeedback = false;

	public Expectation expectExpectation = null;

	// added by Dan for keeping the previous expectation
	public Expectation previousExpectation = null;

	public ArrayList<String> leftoverText = new ArrayList<String>(); // we use
																		// this
																		// in
																		// case
																		// the
																		// feedback
																		// of
																		// the
																		// agent
																		// is
																		// split
																		// into
																		// two
																		// or
																		// more
																		// dialog
																		// turns

	public Expectation getFirstUncoveredExpectation()
	{
		boolean isTheOrderSet = false;
		boolean validOrder = true;
		int tmpOrder = -1;
		int nextExpIdx = -1;
		System.out.println("GETTING NEXT UNCOVERED EXPECTATION!\n");
		// Vasile: added imposed order functionality based on order information
		// provided at authoring time

		// check first that all order values are either >0 or -1
		for (int i = 0; i < (taskExpectations.length - 1); i++)
			if (((taskExpectations[i].getOrder() >= 0) && (taskExpectations[i + 1]
					.getOrder() >= 0))
					|| ((taskExpectations[i].getOrder() == -1) && (taskExpectations[i + 1]
							.getOrder() == -1)))
			{
				validOrder = true;
			}
			else
				validOrder = false;

		if (!validOrder)
		{
			System.out.println("WARNING: Expectations ordering is CORRUPT!\n");
			return null;
		}

		// the rest of the code deals with the case when the ordering is valid,
		// i.e. either the default ordering of Abstract, Concrete, Short when
		// order values are -1 or all non-negative
		for (int i = 0; i < taskExpectations.length; i++)
		{
			// System.out.println("Expectation " + taskExpectations[i].getId() +
			// " has order " + taskExpectations[i].getOrder() + "\n");
			if (taskExpectations[i].getOrder() >= 0)
			{
				isTheOrderSet = true;
				break;
			}
		}

		if (isTheOrderSet)
		{
			// impose the set order
			System.out.println("ORDER IS SET - USE THIS ORDER!\n");
			nextExpIdx = -1;
			tmpOrder = 1000;

			for (int i = 0; i < taskExpectations.length; i++)
				if (!taskExpectations[i].covered
				// Dan&Vasile: OPTIONAL expectations should not be
				// solicited, only
				// acknowledged
				// && taskExpectations[i].type != EXPECT_TYPE.OPTIONAL
						&& (tmpOrder > taskExpectations[i].getOrder()))
				{
					tmpOrder = taskExpectations[i].getOrder();
					nextExpIdx = i;
				}

			if (nextExpIdx >= 0)
			{
				System.out.println("Next uncovered expectation is "
						+ taskExpectations[nextExpIdx].getId() + "\n");
				return taskExpectations[nextExpIdx];
			}

		}
		else
		{
			System.out.println("ORDER IS NOT SET - USE DEFAULT ORDER!\n");
			// default ordering: ABSTRACT, CONCRETE, SHORT
			// first, try to get the abstract expectation
			for (int i = 0; i < taskExpectations.length; i++)
				if (!taskExpectations[i].covered
						&& taskExpectations[i].type == EXPECT_TYPE.ABSTRACT)
					return taskExpectations[i];

			// then, try to get the concrete expectation
			for (int i = 0; i < taskExpectations.length; i++)
				if (!taskExpectations[i].covered
						&& taskExpectations[i].type == EXPECT_TYPE.CONCRETE)
					return taskExpectations[i];

			// Dan&Vasile: OPTIONAL expectations should not be solicited, only
			// acknowledged
			// finally, get the short or other kind of expectation
			for (int i = 0; i < taskExpectations.length; i++)
				if (!taskExpectations[i].covered
						&& taskExpectations[i].type != EXPECT_TYPE.OPTIONAL)
					return taskExpectations[i];

		}

		return null;
	}

	public int countCoveredExpectations()
	{
		int result = 0;
		for (int i = 0; i < taskExpectations.length; i++)
			if (taskExpectations[i].covered
					|| taskExpectations[i].type == EXPECT_TYPE.OPTIONAL)
				result++;
		return result;
	}

	public Expectation[] GetUncoveredMisconceptions(Expectation[] input)
	{
		ArrayList<Expectation> a = new ArrayList<Expectation>();
		for (int i = 0; i < input.length; i++)
		{
			if (!coveredMisconceptions.contains(input[i].id))
				a.add(input[i]);
		}

		Expectation[] result = new Expectation[a.size()];
		a.toArray(result);
		return result;
	}

	public Expectation[] GetUncoveredExpectations(Expectation[] input)
	{
		ArrayList<Expectation> a = new ArrayList<Expectation>();
		for (int i = 0; i < input.length; i++)
		{
			if (!IsExpectationCovered(input[i].id))
				a.add(input[i]);
		}

		Expectation[] result = new Expectation[a.size()];
		a.toArray(result);
		return result;
	}

	public boolean IsExpectationCovered(String id)
	{
		for (int i = 0; i < taskExpectations.length; i++)
		{
			if (taskExpectations[i].id.equals(id))
			{
				if (taskExpectations[i].covered)
					return true;
				else
					return false;
			}
		}
		return false;
	}

	public void CoverExpectation(String id)
	{
		hadAlready = false;
		for (int i = 0; i < taskExpectations.length; i++)
		{
			if (taskExpectations[i].id.equals(id))
			{
				taskExpectations[i].covered = true;
				return;
			}
		}
	}

	public void CoverMisconception(String id)
	{
		coveredMisconceptions.add(id);
	}

	public Expectation FindExpectation(String id)
	{
		for (int i = 0; i < taskExpectations.length; i++)
		{
			if (taskExpectations[i].id.equals(id))
				return taskExpectations[i];
		}
		return null;
	}

	public boolean AllExpectationsCovered()
	{
		if (countCoveredExpectations() == taskExpectations.length)
			return true;
		else
			return false;
	}

	public String SummarizeExpectations()
	{
		// Dan: modifications according to Dr. Rus' specs

		boolean ordered = true;

		List<Integer> orderList = new ArrayList<Integer>();
		List<String> assertionList = new ArrayList<String>();

		for (int i = 0; i < taskExpectations.length; i++)
		{
			if (taskExpectations[i].type != EXPECT_TYPE.OPTIONAL)
			{
				int orderIdx = taskExpectations[i].getOrder();

				if (orderIdx == -1)
				{
					ordered = false;
					break;
				}

				orderList.add(orderIdx);
				assertionList.add(taskExpectations[i].assertion);
			}
		}

		StringBuilder result = new StringBuilder();

		if (ordered)
		{
			Integer[] idx = new Integer[orderList.size()];
			for (int i = 0; i < idx.length; i++)
				idx[i] = i;

			final Integer[] orderArray = new Integer[orderList.size()];
			orderList.toArray(orderArray);

			Arrays.sort(idx, new Comparator<Integer>()
			{
				@Override
				public int compare(final Integer o1, final Integer o2)
				{
					return orderArray[o1].compareTo(orderArray[o2]);
				}
			});

			for (int i = 0; i < idx.length; i++)
			{
				result.append(" " + assertionList.get(idx[i]) + "#WAIT#");
			}
		}
		else
		{
			for (int i = 0; i < taskExpectations.length; i++)
				if (taskExpectations[i].type != EXPECT_TYPE.OPTIONAL
						&& taskExpectations[i].type != EXPECT_TYPE.SHORT)
					result.append(" " + taskExpectations[i].assertion
							+ "#WAIT#");
			// show the short answer last
			for (int i = 0; i < taskExpectations.length; i++)
				if (taskExpectations[i].type == EXPECT_TYPE.SHORT)
					result.append(" " + taskExpectations[i].assertion
							+ "#WAIT#");
		}

		return result.toString().trim();
	}
}
