import java.util.HashMap;


public class TranslationItem {
	private boolean mIsArray = false;
	private final String mStringId;
	//key:locale value: StringItem
	private HashMap<String, StringItem> mTranslationMap = new HashMap<String, StringItem>();
	
	public TranslationItem(String stringId, boolean isArray) {
		mStringId = stringId;
		mIsArray = isArray;
	}
	
	public int getLocalizedSize() {
		return mTranslationMap.keySet().size();
	}
	
	public void addTranslation(StringItem item) {
		mTranslationMap.put(item.getLocale(), item);
	}
	
	public boolean isArray() {
		return mIsArray;
	}
	
	public HashMap<String, StringItem> getItemMap() {
		return mTranslationMap;
	}
	
	public String getStringId() {
		return mStringId;
	}	

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (!(obj instanceof TranslationItem)) {
			return false;
		}
		
		TranslationItem item = (TranslationItem)obj;
		
		if (mStringId.equals(item.getStringId())) {
			HashMap<String, StringItem> itemMap = item.getItemMap();
			boolean equal = true;
			for (String locale : mTranslationMap.keySet()) {
				StringItem stringItem = mTranslationMap.get(locale);
				StringItem stringItem2 = itemMap.get(locale);
				if (stringItem != null && stringItem2 != null) {
					if (!stringItem.equals(stringItem2)) {
						equal = false;
						break;
					}
				}
			}
			return equal;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id: " + mStringId + " is array " + mIsArray + " \n");
		for (String locale : mTranslationMap.keySet()) {
			sb.append("    locale: " + locale + "\n");
			sb.append("        values: ");
			String[] values = mTranslationMap.get(locale).getValues();
			for (String value : values) {
				sb.append(value + "/");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	
}
