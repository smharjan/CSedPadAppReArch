package memphis.deeptutor.tools;

public class StringTools {

	static public String RemoveComments(String input)
	{
		char[] chars = input.toCharArray();
		char[] result = new char[chars.length+1];
		int bracketLevel = 0;
		int j = 0;
		for(int i=0;i<chars.length;i++)
		{
			if (chars[i] == '[') bracketLevel++; 
			
			if (bracketLevel == 0) result[j++] = chars[i];

			if (chars[i] == ']') bracketLevel--;
		}
		return new String(result,0,j);
	}
}
