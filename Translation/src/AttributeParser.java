import java.util.HashMap;


public class AttributeParser {
	private HashMap<String, AttributeDefineItem[]> mAttrInitMap = new HashMap<String, AttributeDefineItem[]>();
	private HashMap<String, ActionItem> mActionMap = new HashMap<String, ActionItem>();
	private String mAction;
	
	public AttributeParser() {
		Object[][] attrInitMap = AttributeConst.ATTRS;
		for (Object[] attrs : attrInitMap) {
			String action = (String)attrs[0];
			String[][] attrList = (String[][])attrs[1];
			AttributeDefineItem[] items = new AttributeDefineItem[attrList.length];
			for (int i = 0; i < items.length; i++) {
				String[] attrGroup = attrList[i];
				String key = attrGroup[AttributeConst.ATTR_NAME_INDEX];
				AttributeDefineItem item = new AttributeDefineItem(key, attrGroup[AttributeConst.ATTR_DESCRIPTION_INDEX], attrGroup[AttributeConst.ATTR_DEFAUL_VALUE_INDEX]);
				items[i] = item;
			}
			mAttrInitMap.put(action, items);
		}
	}
	
	public String getAction() {
		return mAction;
	}
	
	public int getIntValue(String action, String key) {
		return mActionMap.get(action).getIntegerValue(key, getDefaultInteger(action, key));
	}
	
	public boolean getBooleanValue(String action, String key) {
		return mActionMap.get(action).getBooleanValue(key, getDefaultBoolean(action, key));
	}
	
	public String getStringValue(String action, String key) {
		return mActionMap.get(action).getStringValue(key, getDefaultString(action, key));
	}
	
	private Integer getDefaultInteger(String action, String key) {
		String defaultString = getDefaultValue(action, key);
		return Integer.parseInt(defaultString);
	}
	
	private Boolean getDefaultBoolean(String action, String key) {
		return Boolean.parseBoolean(getDefaultValue(action, key));
	}
	
	private String getDefaultString(String action, String key) {
		return getDefaultValue(action, key);
	}
	
	private String getDefaultValue(String action, String key) {
		String ret = null;
		AttributeDefineItem[] items = mAttrInitMap.get(action);
		if (items != null) {
			for (AttributeDefineItem item : items) {
				if (item.mKey.equalsIgnoreCase(key)) {
					return item.mDefaultValue;
				}
			}
		}
		return ret;
	}
	
	public boolean parseArgs(String[] args) {
		if (args.length > 0 && args.length % 2 == 1) {
			String action = args[0];
			if (isActionLegal(action)) {
				mAction = action;
				ActionItem item = new ActionItem(action);
				mActionMap.put(action, item);
				boolean legal = true;
				for (int i = 1; i < args.length; i++) {
					String key = args[i];
					key = key.replace(AttributeConst.ATTR_NAME_PREFIX, "");
					String value = args[i+1];
					i++;
					if (isAttrLegal(action, key)) {
						item.addAttribute(key, value);
					} else {
						legal = false;
						break;
					}
				}
				return legal;
			}
		}
		return false;
	}
	
	private boolean isAttrLegal(String action, String key) {
		for (String s : mAttrInitMap.keySet()) {
			if (s.equalsIgnoreCase(action)) {
				AttributeDefineItem[] items = mAttrInitMap.get(s);
				for (AttributeDefineItem item : items) {
					if (item.mKey.equalsIgnoreCase(key)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean isActionLegal(String action) {
		for (String s : mAttrInitMap.keySet()) {
			if (s.equalsIgnoreCase(action)) {
				return true;
			}
		}
		return false;
	}
	
	public void printHelp() {
		System.out.println("help infomation");
		for (String action : mAttrInitMap.keySet()) {
			printHelp(action);
		}
	}
	
	private void printHelp(String action) {
		AttributeDefineItem[] items = mAttrInitMap.get(action);
		if (items != null) {
			System.out.println(action);
			for (AttributeDefineItem item : items) {
				System.out.println("    " + item.toString());
			}
		}
	}
	
	private class AttributeDefineItem {
		private String mKey;
		private String mDescription;
		private String mDefaultValue;
		public AttributeDefineItem(String key, String description, String defaultValue) {
			mKey = key;
			mDescription = description;
			mDefaultValue = defaultValue;
		}
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(AttributeConst.ATTR_NAME_PREFIX);
			sb.append(mKey);
			int length = sb.toString().length();
			String space = "";
			for (int i = 0; i < AttributeConst.NAME_AREA_LENGTH - length; i++) {
				space += " ";
				
			}
			sb.append(space);
			sb.append(mDescription);
			sb.append("    default value is [");
			sb.append(mDefaultValue);
			sb.append("]");
			return sb.toString();
		}
		
		
	}
}
