

public class TranslateRunnerChooser {
	public TranslateRunner getTranslateRunner(AttributeParser parser) {
		String action = parser.getAction();
		if (AttributeConst.ACTION_COMPARE.equals(action)) {
			return new CompareParser();
		} else if (AttributeConst.ACTION_PARSE.equals(action)) {
			return new TranslateParser();
		} else if (AttributeConst.ACTION_REVERT.equals(action)) {
			return new RevertTranslationParser();
		}
		return null;
	}
}
