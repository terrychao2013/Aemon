
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;


public class CompareParser implements TranslateRunner{
	HashMap<String, TranslationItem> mNewStringMap = new HashMap<String, TranslationItem>();
	HashMap<String, TranslationItem> mModifiedStringMap = new HashMap<String, TranslationItem>();
	
	public void compare(String oldResourcePath, String newResourcePath) {
		TranslateParser oldParser = new TranslateParser();
		oldParser.setMinLocaleCount(1);
		oldParser.setMaxLocaleCount(-1);
		TranslateParser newParser = new TranslateParser();
		newParser.setMinLocaleCount(1);
		newParser.setMaxLocaleCount(-1);
		oldParser.parse(oldResourcePath);
		newParser.parse(newResourcePath);
		
		HashMap<String, TranslationItem> oldMap = oldParser.getParsedMap();
		HashMap<String, TranslationItem> newMap = newParser.getParsedMap();
		compare(oldMap, newMap);
	}
	
	private void writeIntoFile(File target) {
		WritableWorkbook targetBook = null;
		try {
			WorkbookSettings workbookSettings = new WorkbookSettings();
			workbookSettings.setEncoding("ISO-8859-1");
			targetBook = Workbook.createWorkbook(target, workbookSettings);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (targetBook != null) {
			writeIntoWorkbook(targetBook);
		} else {
			System.err.println("Failed to get workbook!");
		}
	}
	
	private void writeIntoWorkbook(WritableWorkbook workbook) {
		WritableSheet newSheet = getWritableSheet(workbook, "new");
		WritableSheet modifiedSheet = getWritableSheet(workbook, "modified");
		writeIntoSheet(newSheet, mNewStringMap);
		writeIntoSheet(modifiedSheet, mModifiedStringMap);
		try {
			workbook.write();
			workbook.close();
			System.out.println("write into file success!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private WritableSheet getWritableSheet(WritableWorkbook workbook, String sheetName) {
		WritableSheet[] sheets = workbook.getSheets();
		int index = sheets.length;
		return workbook.createSheet(sheetName, index);
	}
	
	public void writeIntoFile(String filePath) {
		if (filePath != null && filePath.length() > 0) {
			File target = new File(filePath);
			if (!target.exists()) {
				target.getParentFile().mkdirs();
				try {
					target.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (target.exists()) {
				writeIntoFile(target);
			} else {
				System.err.println("create target file failed file path: " + filePath);
			}
			
		} else {
			System.err.println("the target file path is incorrect!");
		}
		
		/*System.err.println("new string");
		for (String key : mNewStringMap.keySet()) {
			TranslationItem item = mNewStringMap.get(key);
			System.out.println(item.toString());
		}
		
		System.err.println("old string");
		for (String key : mModifiedStringMap.keySet()) {
			TranslationItem item = mModifiedStringMap.get(key);
			System.out.println(item.toString());
		}*/
	}
	
	private TranslationItem[] getSortedItems(HashMap<String, TranslationItem> map) {
		TranslationItem[] items = new TranslationItem[map.size()];
		items = map.values().toArray(items);
		Comparator<TranslationItem> sorter = new Comparator<TranslationItem>() {

			@Override
			public int compare(TranslationItem o1, TranslationItem o2) {
				int size1 = o1.getLocalizedSize();
				int size2 = o2.getLocalizedSize();
				
				return size2 - size1;
			}
			
		};
		
		Arrays.sort(items, sorter);
		return items;
	}
	
	private void writeIntoSheet(WritableSheet sheet, HashMap<String, TranslationItem> map) {
		TranslationItem[] items = getSortedItems(map);
		ArrayList<String> titleList = getTitleList(map);
		writeTitles(sheet, titleList);
		int index = 1;
		for (TranslationItem item : items) {
			index = writeIntoSheet(index, sheet, item, titleList);
		}
	}
	
	private void writeTitles(WritableSheet sheet, ArrayList<String> titles) {
		for (int i = 0; i < titles.size(); i++) {
			WritableCell cell = new Label(i, 0, titles.get(i));
			try {
				sheet.addCell(cell);
			} catch (RowsExceededException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private int writeIntoSheet(int row, WritableSheet sheet, TranslationItem item, ArrayList<String> sheetTitles) {
		int rowIndex = row;
		
		String stringId = item.getStringId();
		int columnIndex = getIndexInTitles(TranslateParser.STRING_ID, sheetTitles);
		WritableCell cell = new Label(columnIndex, rowIndex, stringId);
		
		try {
			sheet.addCell(cell);
		} catch (RowsExceededException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (WriteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HashMap<String, StringItem> stringItemMap = item.getItemMap();
		
		for (String locale : stringItemMap.keySet()) {
			rowIndex = row;
			columnIndex = getIndexInTitles(locale, sheetTitles);
			StringItem stringItem = stringItemMap.get(locale);
			for (String value : stringItem.getValues()) {
				cell = new Label(columnIndex, rowIndex, value);
				try {
					sheet.addCell(cell);
				} catch (RowsExceededException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (WriteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				rowIndex++;
			}
		}
		return rowIndex;
	}
	
	private ArrayList<String> getTitleList(HashMap<String, TranslationItem> map) {
		if (mTitleList != null && mTitleList.size() > 0) {
			return mTitleList;
		}
		
		ArrayList<String> titles = new ArrayList<String>();
		titles.add(TranslateParser.STRING_ID);
		
		ArrayList<String> locales = new ArrayList<String>();
		final HashMap<String, Integer> sizeMap = new HashMap<String, Integer>();
		
		for (String key : map.keySet()) {
			TranslationItem item = map.get(key);
			HashMap<String, StringItem> stringItems = item.getItemMap();
			for (String locale : stringItems.keySet()) {
				int index = getIndexInTitles(locale, locales);
				if (index == -1) {
					locales.add(locale);
					sizeMap.put(locale, 0);
				} else {
					int size = sizeMap.get(locale);
					sizeMap.put(locale, size+1);
				}
				
			}
			
		}
		
		String[] localeList = new String[locales.size()];
		localeList = locales.toArray(localeList);
		Comparator<String> sorter = new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				int size1 = sizeMap.get(o1);
				int size2 = sizeMap.get(o2);
				return size2 - size1;
			}
			
		};
		Arrays.sort(localeList, sorter);
		for (String s : localeList) {
			titles.add(s);
		}
		
		return titles;
	}
	
	private int getIndexInTitles(String title, ArrayList<String> sheetTitles) {
		for (int i = 0; i < sheetTitles.size(); i++) {
			if (title.equals(sheetTitles.get(i))) {
				return i;
			}
		}
		
		return -1;
	}
	
	private void compare(HashMap<String, TranslationItem> oldMap, HashMap<String, TranslationItem> newMap) {
		for (String key : newMap.keySet()) {
			TranslationItem oldItem = oldMap.get(key);
			if (oldItem == null) {
				mNewStringMap.put(key, newMap.get(key));
			} else {
				TranslationItem newItem = newMap.get(key);
				if (!englishEqual(oldItem, newItem)) {
					mModifiedStringMap.put(key, newItem);
				}
			}
		}
	}
	
	private boolean englishEqual(TranslationItem oldItem, TranslationItem newItem) {
		boolean ret = false;
		StringItem oldEnglishItem = oldItem.getItemMap().get("values");
		StringItem newEnglishItem = newItem.getItemMap().get("values");
		if (newEnglishItem != null) {
			ret = newEnglishItem.equals(oldEnglishItem);
		}
		return ret;
	}

	private ArrayList<String> mTitleList;
	@Override
	public void run(AttributeParser parser) {
		String action = AttributeConst.ACTION_COMPARE;
		String newResourcePath = parser.getStringValue(action, AttributeConst.ATTR_COMPARE_KEY_RESOURCE_NEW);
		String target = parser.getStringValue(action, AttributeConst.ATTR_COMPARE_KEY_TARGET);
		String oldResourcePath = parser.getStringValue(action, AttributeConst.ATTR_COMPARE_KEY_RESOURCE_OLDE);
		String templatePath = parser.getStringValue(action, AttributeConst.ATTR_PARSE_KEY_TEMPLATE_PATH);
		String templateSheetname = parser.getStringValue(action, AttributeConst.ATTR_COMPARE_KEY_SHEET_NAME);
		TitleParser titlePaser = new TitleParser();
		titlePaser.parse(templatePath, templateSheetname);
		ArrayList<String> titles = titlePaser.getTitles();
		mTitleList = titles;
		
		compare(oldResourcePath, newResourcePath);
		writeIntoFile(target);
	}
}
