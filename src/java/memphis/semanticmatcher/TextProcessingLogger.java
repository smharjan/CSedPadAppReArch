package memphis.semanticmatcher;

public interface TextProcessingLogger {

	public void Log(String s);
	public void LogAppend(String s);
	public void LogOverwrite(String s);

	public static TextProcessingLogger TextProcessingLogger_Null = new TextProcessingLogger()
	{
		public void Log(String s) {}
		public void LogAppend(String s) {}
		public void LogOverwrite(String s) {}
	};
}
