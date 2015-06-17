public class Main {
	public static final boolean TEST = false;
	
	public static final String[] TEST_ARGS_REVERT = {
		"revert", 
		"--resource", "Main_UI_new.xls", 
		"--target", "app/src/main/res",
		"--sheet-name", "strings",
		"--revert-type", "fresh",
	};
	
	public static final String[] TEST_ARGS_PARSE = {
		"parse",
		"--resource", "app/src/main/res",
		"--target", "parse.xls",
		"--escape-files", "default_settings.xml",
	};
	
	public static final String[] TEST_ARGS_COMP = {
		"compare",
		"--resource-new", "res",
		"--resource-old", "res",
		"--target", "compare.xls",
	};
	
	public static final String[] TEST_ARGS = TEST_ARGS_PARSE;
	
	public static void main(String[] args) {
		String[] arguments = args;
		if (TEST) {
			arguments = TEST_ARGS;
		}
		
		AttributeParser attributeParser = new AttributeParser();
		if (!attributeParser.parseArgs(arguments)) {
			attributeParser.printHelp();
			return;
		}

		TranslateRunnerChooser chooser = new TranslateRunnerChooser();
		TranslateRunner runner = chooser.getTranslateRunner(attributeParser);
		if (runner != null) {
			runner.run(attributeParser);
		} else {
			System.err.println("can not get the right translate runner");
			attributeParser.printHelp();
		}
		if (TEST) {
			System.err.println("WARNNING!!!\nTHIS IS TEST RUNNING!\nTHE RESULT CAN NOT BE USED!!!");
		}

	}

}
