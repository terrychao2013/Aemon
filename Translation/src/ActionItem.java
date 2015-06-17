import java.util.HashMap;

public class ActionItem {
	//key, value
	private HashMap<String, AttributeItem> mActionMap = new HashMap<String, AttributeItem>();
	private String mAction;
	public ActionItem(String action) {
		mAction = action;
	}
	
	public String getAction() {
		return mAction;
	}
	
	public void addAttribute(String key, String value) {
		for (String s : mActionMap.keySet()) {
			if (s.equalsIgnoreCase(key)) {
				return;
			}
		}
		
		AttributeItem item = new AttributeItem(mAction, key, value);
		mActionMap.put(key, item);
	}
	
	public Integer getIntegerValue(String attrName, Integer defaultValue) {
		Integer ret = null;
		String value = getAttrValue(attrName, String.valueOf(defaultValue));
		if (value != null) {
			try {
				ret = Integer.parseInt(value);
			} catch (Exception e) {
				
			}
		}
		return ret;
	}
	
	public Boolean getBooleanValue(String attrName, Boolean defaultValue) {
		Boolean ret = true;
		String value = getAttrValue(attrName, String.valueOf(defaultValue));
		if (value != null) {
			ret = Boolean.parseBoolean(value);
		}
		return ret;
	}
	
	public String getStringValue(String attrName, String defaultValue) {
		return getAttrValue(attrName, defaultValue);
	}
	
	private String getAttrValue(String attrName, String defaultValue) {
		String value = defaultValue;
		AttributeItem item = mActionMap.get(attrName);
		if (item != null) {
			value = item.getValue();
		}
		return value;
	}
}
