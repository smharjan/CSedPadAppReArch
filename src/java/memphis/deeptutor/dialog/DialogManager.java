package memphis.deeptutor.dialog;

import java.io.IOException;

import com.sun.xml.bind.v2.runtime.unmarshaller.IntData;
import com.swabunga.spell.event.StringWordTokenizer;

import memphis.deeptutor.gui.model.Components;
import memphis.deeptutor.gui.model.DTResponse;
import memphis.deeptutor.gui.model.Multimedia;
//import memphis.deeptutor.gui.model.Notice;
import memphis.deeptutor.log.DTLogger;
import memphis.deeptutor.model.ExpectAnswer;
import memphis.deeptutor.model.Expectation;
import memphis.deeptutor.model.SessionData;
import memphis.deeptutor.servlets.InitDTServlet;
import memphis.deeptutor.singleton.ConfigManager;
import memphis.deeptutor.singleton.NLPManager;
import memphis.deeptutor.singleton.XMLFilesManager;
import memphis.semanticmatcher.SemanticRepresentation;
import memphis.semanticmatcher.SemanticRepresentation.LexicalTokenStructure;
import memphis.semanticmatcher.tools.WordNetSimilarity;

public class DialogManager
{

	final float MATCH_THRESHOLD = 0.4f;
	final float GOOD_MATCH_THRESHOLD = 0.5f;

	/*
	 * let's pretend that all expectations covered.. just to jump to ANSWER
	 * SUMMARIZATION section...after showing the Question
	 */
	// TODO: log relevant information...
	public Components getAnswerSummary(SessionData data, DTLogger logger,
			String inputText)
	{
		DTResponse response = new DTResponse();
		Components c = new Components();

		// in this case all expectations should be covered
		// Dan: if summary is given in the script, use it
		String summary = data.taskSummary;
		if (summary != null)
		{
			summary = summary.trim();
		}

		String nexMessage = "Let's move now to the next task.";
		if (data.currentTaskIndex + 1 == data.totalNumberOfTasks)
		{
			nexMessage = "Well done!";
		}

		response.addResponseText("Okay. Let's summarize the correct answer to this problem.#WAIT#"
				+ ((summary != null && !summary.equals("") && !summary
						.equals("#EMPTY_SUMMARY")) ? summary + "#WAIT#" : data
						.SummarizeExpectations()) + nexMessage);
		response.addResponseText("#WAIT#");
		c.setResponse(response);
		return c;
	}

	// Dan: adding processing questions
	public Components ProcessQuestion(SAClassifier.SPEECHACT satype,
			SessionData data, DTLogger logger, String inputText)
			throws IOException
	{
		StudentResponseEvaluator se = new StudentResponseEvaluator(
				new TaskManager(data.currentTaskID, data.debugMode));

		Components c = new Components();
		DTResponse response = new DTResponse();
		String responseData = "T" + data.turnNumber + ", "; // this stores a
															// more structured
															// feedback
															// containing all
															// the data elements
															// that were used in
															// the tutor
															// response

		// ****************************************************************
		// * spell check the input *
		// ****************************************************************
		if (InitDTServlet.spellCheck != null)
			inputText = InitDTServlet.spellCheck
					.autoSpelling(new StringWordTokenizer(inputText));

		SemanticRepresentation semText = new SemanticRepresentation(inputText);
		NLPManager.getInstance().PreprocessText(semText);

		String answer = "I really don't know.";

		if (satype == SAClassifier.SPEECHACT.QuestionDefinitional)
		{
			answer = QuestionAnswering.findDefinitionAnswer(semText, null);
		}
		else
		{
			// Dan: if a question doesn't have a main verb, then it's
			// definitional
			boolean okDef = true;
			for (int i = 0; i < semText.tokens.size(); i++)
			{
				LexicalTokenStructure lts = semText.tokens.get(i);
				if (lts.POS.startsWith("VB"))
				{
					okDef = false;
					break;
				}
			}

			if (okDef || semText.tokens.size() == 2)
			{
				satype = SAClassifier.SPEECHACT.QuestionDefinitional;
				answer = QuestionAnswering.findDefinitionAnswer(semText, "0");
			}
			else
			{
				// Dan: different types of questions should be added
			}
		}

		response.addResponseText(answer);
		logger.log(DTLogger.Actor.TUTOR, DTLogger.Level.ONE,
				response.getImmediateResponseText());

		c.setResponse(response);
		return c;
	}

	public Components ProcessContribution(SAClassifier.SPEECHACT satype,
			SessionData data, DTLogger logger, String inputText)
	{
		StudentResponseEvaluator se = new StudentResponseEvaluator(
				new TaskManager(data.currentTaskID, data.debugMode));
		Components c = new Components();
		DTResponse response = new DTResponse();
		String responseData = "T" + data.turnNumber + ", "; // this stores a
															// more structured
															// feedback
															// containing all
															// the data elements
															// that were used in
															// the tutor
															// response

		Expectation currentExpectation = data.expectExpectation;
		Expectation firstExpectationsCovered = null;
		Boolean suggestExpectation = true;
		Boolean expectationsWereHit = false;
		Boolean expectationRejected = false;

		// ****************************************************************
		// * spell check the input *
		// ****************************************************************
		if (InitDTServlet.spellCheck != null)
			inputText = InitDTServlet.spellCheck
					.autoSpelling(new StringWordTokenizer(inputText));

		// ****************************************************************
		// * search for expectations *
		// ****************************************************************

		Expectation[] expectations = se.ExtractValidExpectations(inputText,
				false);
		expectations = se.ComputeSimilarityAndSort(inputText, expectations);
		Expectation[] uncoveredExpectations = data
				.GetUncoveredExpectations(expectations);

		LogExpectationsFound(logger, "Expectations", expectations);
		LogExpectationsFound(logger, "Uncovered Expectations",
				uncoveredExpectations);

		if (uncoveredExpectations.length > 0
				&& uncoveredExpectations[0].matches(inputText, MATCH_THRESHOLD)
				&&
				// only check for expectations if we are not at a prompt
				(currentExpectation == null || (currentExpectation != null && !currentExpectation.hintSuggested)))
		{
			// positive case where there are expectations in the answer
			firstExpectationsCovered = uncoveredExpectations[0];

			if (firstExpectationsCovered.required != null
					&& (firstExpectationsCovered.similarity <= (-1)
							* GOOD_MATCH_THRESHOLD
					// Vasile commented this out on 10/24/2012:
					// firstExpectationsCovered.required.HasWrongWords(inputText)
					// Vasile added the next condition such that when forbidden
					// words are in the input we give negative feedback
					// Dan: instead of looking at the whole input, we only look
					// at the sentence associated with the expectation
					|| (firstExpectationsCovered.forbidden != null && se
							.ValidatesExpression(/*inputText*/firstExpectationsCovered.getMatchedSentenceInInputText(),
									firstExpectationsCovered.forbidden))))
			{
				expectationRejected = true;
				firstExpectationsCovered = null;
				response.addResponseText(XMLFilesManager.getInstance()
						.GetSomeFeedback("NegativeFeedback"));
				responseData += "ShortNegF";
				// Vasile: System.out.println("FIRST NEGATIVE FEEDBACK");
			}
			else
			{
				if (firstExpectationsCovered.required != null
						&& firstExpectationsCovered.required.matchedVariant >= 0)
				{
					response.addResponseText(firstExpectationsCovered.required
							.GetAnswerFeedback());
					responseData += "GoodWithF#"
							+ firstExpectationsCovered.required.matchedVariant
							+ "#" + firstExpectationsCovered.id;
				}
				else if (firstExpectationsCovered.similarity >= GOOD_MATCH_THRESHOLD)
				{
					response.addResponseText(XMLFilesManager.getInstance()
							.GetSomeFeedback("PositiveFeedback"));
					responseData += "PositiveF";
				}
				else
				{
					response.addResponseText(XMLFilesManager.getInstance()
							.GetSomeFeedback("PositiveNeutral"));
					responseData += "NeutralF";
				}
			}

			if (!expectationRejected)
			{
				responseData += ", Cover(";
				for (int i = 0; i < uncoveredExpectations.length; i++)
				{
					if (uncoveredExpectations[0].matches(inputText,
							MATCH_THRESHOLD))
					{
						response.addResponseText(uncoveredExpectations[i].assertion);
						responseData += (i > 0 ? "#" : "")
								+ uncoveredExpectations[i].id;
						data.CoverExpectation(uncoveredExpectations[i].id);
						// Vasile: System.out.println("COVERING EXPECTATION: " +
						// uncoveredExpectations[0].assertion + " vs " +
						// uncoveredExpectations[i].assertion + "\n.");
					}
				}
				responseData += "), ";
				expectationsWereHit = true;
				response.addResponseText("#WAIT#");

				// check if the current expectation is covered; if it is then we
				// must get a new expectation
				if (currentExpectation != null
						&& data.FindExpectation(currentExpectation.id).covered)
					currentExpectation = null;
			}
		}

		if (currentExpectation != null
				&& currentExpectation.sugestedHintIndex >= 0)
		{
			// ****************************************************************
			// * deal with current expectation *
			// ****************************************************************
			responseData += "C#" + data.currentTaskID + "#"
					+ currentExpectation.id;

			// in this particular case we only look at one expectation
			// *******************************************************
			String[] sentences = NLPManager.getInstance().SplitIntoSentences(
					inputText);
			ExpectAnswer expectAnswer = null;
			String negativeFeedback = null;

			if (!currentExpectation.hintSuggested)
			{
				expectAnswer = currentExpectation.hintsAnswer[currentExpectation.sugestedHintIndex];
				negativeFeedback = currentExpectation.hintsCorrection[currentExpectation.sugestedHintIndex];
				responseData += "#H" + currentExpectation.sugestedHintIndex;
			}
			else
			{
				expectAnswer = currentExpectation.promptAnswer;
				negativeFeedback = currentExpectation.promptCorrection;
				responseData += "#Prompt";
			}

			boolean isValidExpectation = false;
			float sim = 0;

			if (expectAnswer != null)
			{
				isValidExpectation = expectAnswer.HasAllTheRequiredWords(
						inputText, true);

				logger.log(
						DTLogger.Actor.NONE,
						DTLogger.Level.THREE,
						"Answer "
								+ (isValidExpectation ? "has" : "does NOT have")
								+ " the required words: "
								+ (expectAnswer.matchedVariant == -1 ? expectAnswer.acceptedAnswer
										: expectAnswer.goodAnswerVariants[expectAnswer.matchedVariant]));
				// Vasile: System.out.println("Answer " +
				// (isValidExpectation?"has":"does NOT have") +
				// " the required words: " +
				// (expectAnswer.matchedVariant==-1?expectAnswer.acceptedAnswer:expectAnswer.goodAnswerVariants[expectAnswer.matchedVariant]));
				sim = 1;
			}
			else
			{
				isValidExpectation = (currentExpectation.required == null || currentExpectation.required
						.HasAllTheRequiredWords(inputText, false))
						&& (currentExpectation.forbidden == null || !se
								.ValidatesExpression(inputText,
										currentExpectation.forbidden));

				sim = se.CompareToExpectation(sentences, currentExpectation);
				logger.log(DTLogger.Actor.NONE, DTLogger.Level.THREE, "Answer "
						+ (isValidExpectation ? "is" : "is NOT")
						+ " valid and has a similarity of: " + sim);
				// Vasile: System.out.println("Answer " +
				// (isValidExpectation?"is":"is NOT") +
				// " valid and has a similarity of: " + sim);
			}

			if (isValidExpectation
					&& currentExpectation.matches(inputText, MATCH_THRESHOLD))
			{
				responseData += "+, ";

				// positive feedback
				if (expectAnswer != null)
				{
					response.addResponseText(expectAnswer.GetAnswerFeedback());
					responseData += "Answer#"
							+ expectAnswer.GetAnswerFeedbackData();
				}
				else
				{
					response.addResponseText(currentExpectation.required
							.GetAnswerFeedback());
					responseData += "Required#"
							+ currentExpectation.required
									.GetAnswerFeedbackData();
				}

				if (!currentExpectation.hintSuggested
						&& currentExpectation.sugestedHintIndex >= 0
						&& currentExpectation.sugestedHintIndex < currentExpectation.hints.length - 1)
				{
					if (currentExpectation.hintsType[currentExpectation.sugestedHintIndex] != null)
					{
						if (currentExpectation.hintsType[currentExpectation.sugestedHintIndex]
								.equals("final"))
							currentExpectation.covered = true;
						else if (currentExpectation.hintsType[currentExpectation.sugestedHintIndex]
								.equals("sequence"))
							currentExpectation.sugestedHintIndex++;
						else
							currentExpectation.sugestedHintIndex = currentExpectation.hints.length - 1; // conditional
					}
					else
						currentExpectation.sugestedHintIndex = currentExpectation.hints.length - 1;
				}
				else
				{
					currentExpectation.covered = true;
				}
			}
			else
			{
				SemanticRepresentation semText = new SemanticRepresentation(
						inputText);
				NLPManager.getInstance().PreprocessText(semText);

				// ask about any "ITs" which may be in the student's answer, if
				// we don't have a specific answer that we expect
				/*
				 * 2/9/2013 disable it handling because it was annoying if
				 * (!expectationsWereHit && expectAnswer == null &&
				 * NLPManager.getInstance().ContainsIt(semText) &&
				 * !data.hadWhatIt) { data.inputReplaceIt =
				 * NLPManager.getInstance().ConstructReplaceIt(semText);
				 * response.clearResponse();
				 * response.addResponseText("What do you mean by \"it\"?" +
				 * (data.replaceItAsked?"":
				 * " Please type only the replacement word or phrase."));
				 * responseData += "-, qIT";
				 * 
				 * data.replaceItAsked = true; data.hadWhatIt = true;
				 * suggestExpectation = false; }
				 * 
				 * //in case of negative answer, if expectations were hit, skip
				 * evaluation and ask the question again else {
				 */
				boolean reiterate = false;
				if (expectationsWereHit)
					reiterate = true;
				else
				{
					responseData += "-, ";
					response.clearResponse();

					if (satype != SAClassifier.SPEECHACT.Contribution)
					{
						// in case of meta-cognitive answers (i.e. I don't know)
						response.addResponseText(XMLFilesManager.getInstance()
								.GetSomeFeedback(satype + "Feedback"));
						responseData += satype + "-F";
					}
					else
					{
						// At this step, we should try as best to avoid giving
						// negative feedback
						// There is a high change that a possible good answer is
						// not accepted by the system (not all required words
						// are hit)
						// and it is very important to avoid the embarrassing
						// moments when the tutor says "no" to a perfectly good
						// answer

						// first, try to see if there are any known
						// misconceptions in the answer
						Expectation[] misconceptions = se
								.ExtractValidExpectations(inputText, true);
						Expectation[] uncoveredMisconceptions = data
								.GetUncoveredMisconceptions(misconceptions);
						misconceptions = se.ComputeSimilarityAndSort(inputText,
								uncoveredMisconceptions);
						LogExpectationsFound(logger, "UncoveredMisconceptions",
								uncoveredMisconceptions);

						if (misconceptions.length > 0
								&& misconceptions[0].similarity >= MATCH_THRESHOLD)
						{
							// Dan:We should never cover misconceptions
							// data.CoverMisconception(misconceptions[0].id);
							response.addResponseText((misconceptions[0].assertion == null) ? "[correction missing for "
									+ misconceptions[0].getId() + "]"
									: misconceptions[0].assertion);
							responseData += "M#" + misconceptions[0].id;
							reiterate = true;
						}
						else
						{
							if (expectAnswer == null)
								expectAnswer = currentExpectation.required;
							// we only give negative answer if:
							// 1. forbidden words are contained in the answer or
							// 2. the answer is a negation of a correct one
							if ( // Vasile commented this out on 10/24/2012:
									// expectAnswer!=null &&
									// (expectAnswer.HasAllTheRequiredWords(inputText,
									// false)) ||
									// expectAnswer.HasWrongWords(inputText)
									// Vasile replaced the above, old condition
									// with the one below to match for the two
									// cases outlined above when we give
									// negative feedback
							(expectAnswer != null && expectAnswer
									.HasWrongWords(inputText))
									|| (currentExpectation != null
											&& currentExpectation.forbidden != null && se
												.ValidatesExpression(
														inputText,
														currentExpectation.forbidden)))
							{
								response.addResponseText(XMLFilesManager
										.getInstance().GetSomeFeedback(
												"NegativeFeedback"));
								responseData += "ShortNegF";
								// Vasile:
								// System.out.println("SECOND NEGATIVE FEEDBACK");
							}
							else
							{
								// we should give some sort of neutral feedback
								// if there is not specific negative feedback
								if (negativeFeedback == null)
								{
									response.addResponseText(XMLFilesManager
											.getInstance().GetSomeFeedback(
													"NegativeNeutral"));
									responseData += "NeutralF";
								}
							}
						}
						response.addResponseText("#WAIT#");
					}
				}

				if (reiterate)
				{
					response.addResponseText(XMLFilesManager.getInstance()
							.GetSomeFeedback("ReiterateFeedback"));
					responseData += "AskAgain";
				}
				else
				{
					// give a specific negative feedback (i.e. The word I was
					// looking for is "zero");
					if (negativeFeedback != null)
					{
						if (satype == SAClassifier.SPEECHACT.Contribution)
							response.addResponseText(negativeFeedback);
						response.addResponseText("#WAIT#");
						responseData += "#SpecificNegF";
					}

					if (!currentExpectation.hintSuggested)
					{
						if (currentExpectation.sugestedHintIndex < currentExpectation.hints.length - 1)
							currentExpectation.sugestedHintIndex++;
						else
							currentExpectation.hintSuggested = true;
					}
					else
						currentExpectation.covered = true;
				}
				// }
			}

			if (currentExpectation.covered)
			{
				response.addResponseText(currentExpectation.assertion);
				response.addResponseText("#WAIT#");
				responseData += ", Cover(" + currentExpectation.id + ")";

				data.CoverExpectation(currentExpectation.id);
				System.out.println("COVERING EXPECTATION: "
						+ currentExpectation.assertion + "\n.");
				firstExpectationsCovered = currentExpectation;
				currentExpectation = null;
			}

			responseData += ", ";
			// *********** end one expectation *******************************
		}
		else
		{
			// ****************************************************************
			// * in case the input does not have expectations *
			// ****************************************************************
			if (!expectationsWereHit && !expectationRejected)
			{
				Expectation[] misconceptions = se.ExtractValidExpectations(
						inputText, true);
				Expectation[] uncoveredMisconceptions = data
						.GetUncoveredMisconceptions(misconceptions);
				misconceptions = se.ComputeSimilarityAndSort(inputText,
						uncoveredMisconceptions);
				LogExpectationsFound(logger, "UncoveredMisconceptions",
						uncoveredMisconceptions);

				response.clearResponse();
				if (misconceptions.length > 0
						&& misconceptions[0].similarity >= MATCH_THRESHOLD)
				{
					// Dan:We should never cover misconceptions
					// data.CoverMisconception(misconceptions[0].id);
					response.addResponseText((misconceptions[0].assertion == null) ? "[correction missing for "
							+ misconceptions[0].getId() + "]"
							: misconceptions[0].assertion);
					response.addResponseText("#WAIT#");
					responseData += "M#" + misconceptions[0].id;
					Expectation yokedExpectation = data
							.FindExpectation(misconceptions[0].yokedExpectation);
					if (yokedExpectation != null && !yokedExpectation.covered)
						currentExpectation = yokedExpectation;
				}
				else
				{
					if (expectations.length > 0
							&& expectations[0].similarity >= MATCH_THRESHOLD
							&& (satype == SAClassifier.SPEECHACT.Contribution))
					{
						if (!data.hadAlready)
						{
							// Dan: replacing answer
							// response.addResponseText("Yes. We already assessed that:");
							response.addResponseText("Yes.");

							response.addResponseText(expectations[0].assertion);
							responseData += "EC#" + expectations[0].id;
							data.hadAlready = true;
							if (currentExpectation != null)
								currentExpectation.sugestedHintIndex++;
						}
						else
						{
							response.addResponseText("Okay. Let me ask you this.");
							responseData += "HELP1";
							if (currentExpectation != null)
								currentExpectation.sugestedHintIndex = 0;
						}
					}
					else
					{
						if (satype == SAClassifier.SPEECHACT.Contribution)
						{
							SemanticRepresentation semText = new SemanticRepresentation(
									inputText);
							NLPManager.getInstance().PreprocessText(semText);

							Expectation[] bonusExpectations = se
									.ExtractBonusExpectations(inputText, false);

							/*
							 * 2/9/2013 disable it as it is annoying to student.
							 * if (NLPManager.getInstance().ContainsIt(semText)
							 * && !data.hadWhatIt) { data.inputReplaceIt =
							 * NLPManager
							 * .getInstance().ConstructReplaceIt(semText);
							 * response
							 * .addResponseText("What do you mean by \"it\"?" +
							 * (data.replaceItAsked?"":
							 * " Please type only the replacement word or phrase."
							 * )); responseData += "qIT";
							 * 
							 * data.replaceItAsked = true; data.hadWhatIt =
							 * true; suggestExpectation = false; }
							 */
							// else
							// Check for bonus words
							if (bonusExpectations.length > 0
									&& !data.hadTooShort)
							{
								response.addResponseText("You are on the right track. Can you think more and be more specific?");
								responseData += "BonusPump";

								data.hadTooShort = true;
								suggestExpectation = false;
							}
							else
							// Check for invalid answer
							// Vasile added on Oct 25, 2012: <&&
							// se.AnswerNotRelevant (semText)>
							// Vasile: the reason is that a student input should
							// be classified as too short only if the student
							// input does not contain relevant content
							if (se.AnswerTooBrief(semText)
									&& se.AnswerNotRelevant(semText)
									&& !data.hadTooShort)
							{
								response.addResponseText(XMLFilesManager
										.getInstance().GetSomeFeedback(
												"AnswerTooShort"));
								responseData += "2Short";
								suggestExpectation = false;
								data.hadTooShort = true;
							}
							else
							{
								if (se.AnswerNotRelevant(semText)
										&& !data.hadIrrelevant)
								{
									response.addResponseText(XMLFilesManager
											.getInstance().GetSomeFeedback(
													"AnswerIrrelevant"));
									responseData += "Irlvnt";
									suggestExpectation = false;
									data.hadIrrelevant = true;
								}
								else
								{
									if (uncoveredExpectations.length > 0)
									{
										response.addResponseText((uncoveredExpectations[0].similarity <= -MATCH_THRESHOLD) ? XMLFilesManager
												.getInstance().GetSomeFeedback(
														"NegativeFeedback")
												: XMLFilesManager
														.getInstance()
														.GetSomeFeedback(
																"NegativeNeutral"));
										responseData += (uncoveredExpectations[0].similarity <= -MATCH_THRESHOLD) ? "NF#"
												: "FF#"
														+ uncoveredExpectations[0].id;
									}
									else
									{
										response.addResponseText("Let me try again.");
										responseData += "Help2";
									}
									if (currentExpectation != null)
										currentExpectation.sugestedHintIndex = 0;
								}
							}
						}
						else
						{
							response.addResponseText(XMLFilesManager
									.getInstance().GetSomeFeedback(
											satype + "Feedback"));
							responseData += satype + "-F";
							if (currentExpectation != null)
								currentExpectation.sugestedHintIndex = 0;
						}
					}
				}
				responseData += ", ";
			}
		}

		if (suggestExpectation)
		{
			data.hadIrrelevant = false;
			data.hadTooShort = false;
			data.hadWhatIt = false;

			if (currentExpectation == null)
			{
				// if we don't have a current expectation, we must choose one
				currentExpectation = data.getFirstUncoveredExpectation();

				// Mihai - in case no expectations were covered we start with a
				// hint instead of a pump; means the student needs help and a
				// pump
				// will not give as much help as a hint
				if (firstExpectationsCovered == null)
					currentExpectation.sugestedHintIndex = 0;
			}

			if (currentExpectation == null /*
											 * || currentExpectation.type ==
											 * EXPECT_TYPE.OPTIONAL
											 */) // still ?
			{
				// in this case all expectations should be covered
				// Dan: if summary is given in the script, use it
				String summary = data.taskSummary;
				if (summary != null)
				{
					summary = summary.trim();
				}

				String nexMessage = "Let's move now to the next task.";
				if (data.currentTaskIndex + 1 == data.totalNumberOfTasks)
				{
					nexMessage = "We are almost done with this session.";
				}

				response.addResponseText("Okay. We are almost done with this task. Let's summarize the correct answer to this problem.#WAIT#"
						+ ((summary != null && !summary.equals("") && !summary
								.equals("#EMPTY_SUMMARY")) ? summary + "#WAIT#"
								: data.SummarizeExpectations()) + nexMessage);
				response.addResponseText("#WAIT#");
				responseData += "DONE";
			}
			else
			{
				data.expectExpectation = currentExpectation;

				responseData += "Suggest#" + currentExpectation.id + "#";

				// Vasile added the next 3 lines to handle EMPTY pumps which
				// were needed when a more sophisticated pump with
				// good-with-feedback was needed
				if (!currentExpectation.hintSuggested
						&& currentExpectation.pump != null
						&& currentExpectation.pump.startsWith("#EMPTY_PUMP"))
				{
					// if EMPTY_PUMP then jumpt to first hint which is a more
					// sophisticated pump
					currentExpectation.sugestedHintIndex = 0;
				}
				;

				if (!currentExpectation.hintSuggested)
				{
					System.out.print("Suggesting..."
							+ currentExpectation.sugestedHintIndex);
					if (currentExpectation.sugestedHintIndex < 0)
					{
						response.addResponseText((currentExpectation.pump == null) ? "[pump missing for "
								+ currentExpectation.getId() + "]"
								: currentExpectation.pump);
						responseData += "Pump";
					}
					else
					{
						if (currentExpectation.hints != null
								&& currentExpectation.hints.length > 0)
						{
							ExpectAnswer expectAnswer = currentExpectation.hintsAnswer[0];

							// for the first hint in sequence we must check the
							// pump answer and give positive feedback if it
							// matches
							if (currentExpectation.sugestedHintIndex == 0
									&& currentExpectation.hints.length > 1
									&& expectAnswer.HasAllTheRequiredWords(
											inputText, false))
							{
								response.clearResponse();
								if (expectAnswer.matchedVariant >= 0)
									response.addResponseText(expectAnswer.goodFeedbackVariants[expectAnswer.matchedVariant]);
								else
									response.addResponseText(XMLFilesManager
											.getInstance().GetSomeFeedback(
													"PositiveFeedback"));

								responseData += "+";

								if (currentExpectation.hintsType[0] != null
										&& currentExpectation.hintsType[0]
												.equals("sequence"))
									currentExpectation.sugestedHintIndex = 1;
								else
									currentExpectation.sugestedHintIndex = currentExpectation.hints.length - 1; // conditional
							}
							response.addResponseText(currentExpectation.hints[currentExpectation.sugestedHintIndex]);
							responseData += "H"
									+ currentExpectation.sugestedHintIndex;
						}
						else
						{
							currentExpectation.hintSuggested = true;
							response.addResponseText((currentExpectation.prompt == null) ? "[prompt missing for "
									+ currentExpectation.getId() + "]"
									: currentExpectation.prompt);
							responseData += "Prompt";
						}
					}
				}
				else
				{
					response.addResponseText((currentExpectation.prompt == null) ? "[prompt missing]"
							: currentExpectation.prompt);
					responseData += "Prompt";
				}
			}
		}
		else
			responseData += "NoSuggest";

		// set post image for the most covered expectation
		if (firstExpectationsCovered != null
				&& firstExpectationsCovered.postImage != null)
		{
			Multimedia multi = new Multimedia();
			multi.setHeight(firstExpectationsCovered.postImageSizeHeight);
			multi.setWidth(firstExpectationsCovered.postImageSizeWidth);
			multi.setSource(ConfigManager.GetMediaWebPath()
					+ firstExpectationsCovered.postImage);
			se.task.MakeMediaAvailable(firstExpectationsCovered.postImage);
			c.setMultimedia(multi);
		}

		logger.log(DTLogger.Actor.TUTOR, DTLogger.Level.ONE,
				response.getImmediateResponseText());
		logger.log(DTLogger.Actor.SYSTEM, DTLogger.Level.ONE, responseData);

		// Notice notice = new Notice();
		// notice.setNotice("Current Task: Covered Expectations = " +
		// data.countCoveredExpectations() + " (out of " +
		// data.taskExpectations.length + ")&#13;&#13;" +
		// "Current Answer:&#13;----------------------&#13;" +
		// "Matched expectations = " + expectations.length + "&#13;" +
		// "Matched expectations that were not covered before = " +
		// uncoveredExpectations.length + "&#13;" +
		// "Detected misconceptions = " + misconceptions.length);
		// c.setNotice(notice);
		data.turnNumber++;

		// resp.setResponseText(se.sessionData.turnNumber+"");
		// resp.setResponseText(FlexContext.getFlexSession().getId());
		// resp.setResponseText(response.trim());
		c.setResponse(response);

		WordNetSimilarity.getInstance().SaveCache();

		return c;
	}

	private void LogExpectationsFound(DTLogger logger, String listName,
			Expectation[] expectations)
	{
		logger.log(DTLogger.Actor.NONE, DTLogger.Level.TWO, listName + ": "
				+ (expectations.length > 0 ? "" : "none"));
		for (int i = 0; i < expectations.length; i++)
		{
			Expectation e = expectations[i];
			logger.log(DTLogger.Actor.NONE, DTLogger.Level.THREE, e.similarity
					+ " ID-" + e.id + " (s=" + e.sentence + ";covered="
					+ e.covered + ") " + e.mostSimilarText);
		}
	}
}
