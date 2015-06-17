import java.util.ArrayList;


public class StringItem {
	private String mStringId;
	private String mLocale;
	private ArrayList<String> values = new ArrayList<String>();

	public StringItem(String id, String locale) {
		mStringId = id;
		mLocale = locale;
	}
	
	public void addValue(String value) {
		values.add(value);
	}

	public String getStringId() {
		return mStringId;
	}

	public String[] getValues() {
		String[] ret = new String[values.size()];
		ret = values.toArray(ret);
		return ret;
	}

	public String getLocale() {
		return mLocale;
	}
	
	public boolean hasValueContent() {
		boolean ret = false;
		for (String s : values) {
			if (s != null && s.length() > 0) {
				ret = true;
				break;
			}
		}
		return ret;
	}
	
	public String getXMLString() {
		StringBuilder sb = new StringBuilder();
		if (values.size() > 1) {
			sb.append("    <string-array name=\"");
			sb.append(mStringId);
			sb.append("\">\n");
			for (String value : values) {
				sb.append("        <item>");
				sb.append(value);
				sb.append("</item>\n");
			}
			
			sb.append("<string-array>");
		} else if (values.size() == 1){
			sb.append("    <string name=\"");
			sb.append(mStringId);
			sb.append("\">");
			sb.append(getXmlValueString(values.get(0)));
			sb.append("</string>");
		}
		
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (!(obj instanceof StringItem)) {
			return false;
		}
		StringItem item = (StringItem)obj;
		if (mLocale.equals(item.getLocale())) {
			if (values.size() == item.getValues().length) {
				boolean equal = false;
				for (String value : values) {
					for (String s : item.getValues()) {
						if (value.equals(s)) {
							equal = true;
						}
					}
					
				}
				
				return equal;
			}
		}
		
		return false;
	}
	
	private String getXmlValueString(String originalString) {
		String ret = originalString;
		ret = correctSymbol(ret);
		ret = addQuoteInSpecialString(ret);
		return ret;
	}
	
	private String addQuoteInSpecialString(String originalString) {
		String ret = originalString;
		if (originalString != null 	&& originalString.contains("'")) {
			if (!originalString.contains("\\'")) {
				ret = ret.replace("'", "\\'");
			} 
			if (!originalString.startsWith("\"")) {
				ret = "\"" + ret + "\"";
			}
		}
		return ret;
	}
	
	public static String correctSymbol(String original) {
		String ret = correctAnd(original);
		ret = correctPercentage(ret);
		return ret;
	}
	
	private static String correctAnd(String original) {
		String ret = original;
		if (original != null 
				&& original.contains("&") 
				&& !original.contains("&amp;")
				&& !original.contains("&lt")
				&& !original.contains("&gt")
				&& !original.contains("&#")/*for color string*/) {
			ret = original.replace("&", "&amp;");
		}
		return ret;
	}
	
	private static String[][] PERCENT_CORRECTION_LIST = {
		{"%1$ s", "%1$s"},
		{"%2$ s", "%2$s"},
		{"%1$ d", "%1$d"},
		{"%2$ d", "%2$d"},
		{"% s", "%s"},
		{"% S", "%S"},
		{"%S", "%s"},
		{"%1$ ", "%1$s "},
		{"%1$!", "%1$s!"},
		{"\"curve_uninstall_msg_inte\">% ", "\"curve_uninstall_msg_inte\">%s "},
	};
	
	private static String correctPercentage(String original) {
		String ret = original;
		if (ret.contains("%")) {
			for (String[] corrections : PERCENT_CORRECTION_LIST) {
				String error = corrections[0];
				if (ret.contains(error)) {
					ret = ret.replace(error, corrections[1]);
				}
			}
		}
		return ret;
	}
	
}