
public class AttributeItem {
	private String mAction;
	private String mKey;
	private String mValue;
	public AttributeItem(String action, String key, String value) {
		mAction = action;
		mKey = key;
		mValue = value;
	}
	
	public String getAction() {
		return mAction;
	}
	
	public String getKey() {
		return mKey;
	}
	
	public String getValue() {
		return mValue;
	}
}
