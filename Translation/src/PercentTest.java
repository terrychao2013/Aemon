import java.util.HashMap;



public class PercentTest {
	private static final String RES_PATH = "";
	public static void main(String[] args) {
		TranslateParser parser = new TranslateParser();
		parser.parse(RES_PATH);
		HashMap<String, TranslationItem> map = parser.getParsedMap();
		test(map);
	}
	
	private static void test(HashMap<String, TranslationItem> map) {
		if (map != null) {
			for (TranslationItem item : map.values()) {
				if (item != null) {
					test(item);
				}
			}
		}
	}
	
	private static void test(TranslationItem item) {
		if (hasPercent(item)) {
			testContent(item);
		}
	}
	
	private static String[] escapes = {
	};
	
	private static void testContent(TranslationItem item) {
		HashMap<String, StringItem> map = item.getItemMap();
		for (String string : escapes) {
			if (item.getStringId().equals(string)) {
				return;
			}
		}
		for (StringItem stringItem : map.values()) {
			String[] values = stringItem.getValues();
			for (String value : values) {
				if (!value.contains("%")) {
					System.err.println("id: " + item.getStringId() + " locale: " + stringItem.getLocale());
				}
			}
		}
	
	}
	
	private static boolean hasPercent(TranslationItem item) {
		boolean ret = false;
		if (item != null) {
			HashMap<String, StringItem> map = item.getItemMap();
			for (StringItem stringItem : map.values()) {
				String[] values = stringItem.getValues();
				if (values != null) {
					for (String value : values) {
						if (value != null && value.contains("%")) {
							ret = true;
							return ret;
						}
					}
				}
			}
		}
		return ret;
	}
}
