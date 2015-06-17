import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;






public class TranslateParser implements TranslateRunner{
	public static final String STRING_ID = "String ID";
	public static final String RES = "res";
	HashMap<String, TranslationItem> mTranslateMap = new HashMap<String, TranslationItem>();
	private ArrayList<String> mEscapeFileList = new ArrayList<String>();
	private int mMinLocaleCount;
	private int mMaxLocaleCount;

	private ArrayList<String> sheetTitles = new ArrayList<String>();
	public void setMinLocaleCount(int minLocaleCount) {
		mMinLocaleCount = minLocaleCount;
	}
	
	public void setMaxLocaleCount(int maxLocaleCount) {
		mMaxLocaleCount = maxLocaleCount;
	}
	
	public HashMap<String, TranslationItem> getParsedMap() {
		return mTranslateMap;
	}
	
	private void filtResult() {
		ArrayList<String> toBeRemovedKeyList = new ArrayList<String>();
		for (String key : mTranslateMap.keySet()) {
			TranslationItem item = mTranslateMap.get(key);
			if (item.getLocalizedSize() < mMinLocaleCount) {
				toBeRemovedKeyList.add(key);
			} else if (mMaxLocaleCount > 0 && item.getLocalizedSize() > mMaxLocaleCount) {
				toBeRemovedKeyList.add(key);
			}
		}
		
		for (String key : toBeRemovedKeyList) {
			mTranslateMap.remove(key);
		}
	}
	
	private void updateEscapeFiles(String escapeFileString) {
		if (escapeFileString != null && escapeFileString.length() > 0) {
			String[] escapeFileNames = escapeFileString.split(AttributeConst.ATTR_PARAMS_SPERATOR);
			for (String s : escapeFileNames) {
				mEscapeFileList.add(s);
			}
		}
	}
	
	public void parse(String resourcePath) {
		File[] values = getValueFiles(resourcePath);
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				parseValueFiles(values[i]);
			}
			filtResult();
		} else {
			System.err.println("GET values files ERROR!");
		}
		
	}
	
	private void parseValueFiles(File valueFile) {
		if (valueFile != null) {
			if (valueFile.getName().endsWith("dpi")) {
				return;
			}
			System.out.println("pase value file: " + valueFile.getName());
			FilenameFilter xmlFilter = new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					if (name != null && name.endsWith(".xml")) {
						return true;
					}
					return false;
				}
			};
			
			File[] xmlFiles = valueFile.listFiles(xmlFilter);
			if (xmlFiles != null && xmlFiles.length > 0) {
				for (int i = 0; i < xmlFiles.length; i++) {
					parseXmlFile(xmlFiles[i], valueFile.getName());
				}
			}
		}
	}
	
	private void parseXmlFile(File xmlFile, String valueFileName) {
		if (xmlFile != null) {
			System.out.println("parse xml file: " + xmlFile.getName());
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = null;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Document doc = null;
			if (builder != null) {
				try {
					doc = builder.parse(xmlFile);
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (doc != null) {
				parseDoc(doc, valueFileName);
			}
		}
	}
	
	private void addTranslationItem(String stringId, String value, String locale) {
		addTranslationItem(stringId, new String[]{value}, locale);
	}
	
	private void addTranslationItem(String stringId, String[] values, String locale) {
		TranslationItem item = mTranslateMap.get(stringId);
		if (item == null) {
			item = new TranslationItem(stringId, values.length > 1);
			mTranslateMap.put(stringId, item);
		}
		
		if (values == null) {
			values = new String[]{""};
		}
		StringItem stringItem = new StringItem(stringId, locale);
		for (int i = 0; i < values.length; i++) {
			String value = values[i];
			if (value == null) {
				value = "";
			}
			stringItem.addValue(value);
		}
		item.addTranslation(stringItem);
		
	}
	
	private void parseDoc(Document doc, String valueFileName) {
		if (doc != null) {
			Element rootElement = doc.getDocumentElement();
			NodeList nodeList = null;
			nodeList = rootElement.getChildNodes();
			if (nodeList != null) {
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node parseNode = nodeList.item(i);
					if ("string".equalsIgnoreCase(parseNode.getNodeName())) {
						String stringId, value;
						NamedNodeMap nodeMap = parseNode.getAttributes();
						if (nodeMap != null) {
							Node stringIdNode = nodeMap.getNamedItem("name");
							if (stringIdNode != null) {
								stringId = stringIdNode.getNodeValue();
								if (stringId != null && stringId.length() > 0) {
									NodeList valueNodes = parseNode.getChildNodes();
									if (valueNodes != null && valueNodes.getLength() > 0) {
										value = valueNodes.item(0).getNodeValue();
									} else {
										value = "";
									}
									addTranslationItem(stringId, value, valueFileName);
								}
							}
						}
					} else if ("string-array".equalsIgnoreCase(parseNode.getNodeName())) {
						String stringId;
						String[] items = new String[0];
						NamedNodeMap nodeMap = parseNode.getAttributes();
						if (nodeMap != null) {
							Node stringIdNode = nodeMap.getNamedItem("name");
							if (stringIdNode != null) {
								stringId = stringIdNode.getNodeValue();
								if (stringId != null && stringId.length() > 0) {
									NodeList valueNodes = parseNode.getChildNodes();
									if (valueNodes != null && valueNodes.getLength() > 0) {
										int length = 0;
										for (int j = 0; j < valueNodes.getLength(); j++) {
											Node itemNode = valueNodes.item(j);
											if (itemNode != null && "item".equalsIgnoreCase(itemNode.getNodeName())) {
												length++;
												Node stringNode = itemNode.getFirstChild();
												String item = "";
												if (stringNode != null) {
													item = stringNode.getNodeValue();
												}
												String[] tmp = new String[length];
												for (int m = 0; m < length - 1; m++) {
													tmp[m] = items[m];
												}
												
												tmp[length - 1] = item;
												items = tmp;
											}
										}
										
										addTranslationItem(stringId, items, valueFileName);
									}
								}
							}
						}
					}
				}
			}
		}
		
	}
	
	private File[] getValueFiles(String resourcePath) {
		if (resourcePath == null || resourcePath.length() == 0) {
			System.err.println("resource path is incorrect!");
			return null;
		}
		
		File res = new File(resourcePath);
		
		if (res == null || !res.isDirectory()) {
			System.err.println("the [res] directory is incorrect!");
			return null;
		}
		
		FilenameFilter valuesFilter = new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name != null 
						&& !mEscapeFileList.contains(name)
						&& name.startsWith("values");
			}
		};
		
		File[] valueFiles = res.listFiles(valuesFilter);
		return valueFiles;
	}
	
	public void writeToFile(String outputPath) {
		if (mTranslateMap.size() > 0 && outputPath != null && outputPath.endsWith(".xls")) {
			/*File file = new File("/home/terry/log.txt");
			try {
				FileOutputStream os = new FileOutputStream(file);
				for (TranslationItem item : mTranslateMap.values()) {
					System.out.println(item.toString());
					os.write((item.toString() + "\n").getBytes());
				}
				os.flush();
				os.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
			File outputFile = new File(outputPath);
			if (!outputFile.exists()) {
				File parent = outputFile.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				try {
					outputFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (outputFile.exists()) {
				writeToFile(outputFile);
			} else {
				System.err.println("create output file failed");
			}
		} else {
			if (mTranslateMap.size() > 0) {
				System.err.println("the output file path is incorrect [" + outputPath + "]");
				System.err.println("the output file path must end with .xls");
			} else {
				System.err.println("NO STRING PARSED!");
			}
		}
	}
	
	private void writeToFile(File outputFile) {
		WritableWorkbook outputBook = null;
		try {
			WorkbookSettings workbookSettings = new WorkbookSettings();
            workbookSettings.setEncoding("ISO-8859-1");
			outputBook = Workbook.createWorkbook(outputFile, workbookSettings);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (outputBook != null) {
			WritableSheet sheet = outputBook.createSheet("strings", 0);
			writeTitles(sheet);
			writeIntoSheet(sheet, false);
			sheet = outputBook.createSheet("string-arrays", 1);
			writeTitles(sheet);
			writeIntoSheet(sheet, true);
			try {
				outputBook.write();
				outputBook.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.err.println("create xls file failed");
		}
	}

	private int index = 0;
	
	private void writeTitles(WritableSheet sheet) {
		for (int i = 0; i < sheetTitles.size(); i++) {
			WritableCell cell = new Label(i, 0, sheetTitles.get(i));
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
	
	private int getIndexInTitles(String locale, WritableSheet sheet) {
		int index;
		int ret = -1;
		for (int i = 0; i < sheetTitles.size(); i++) {
			if (locale.equals(sheetTitles.get(i))) {
				ret = i;
				break;
			}
		}
		if (ret == -1) {
			sheetTitles.add(locale);
			ret = sheetTitles.size() - 1;
		}
		index = ret;
		WritableCell cell = new Label(index, 0, locale);
		try {
			sheet.addCell(cell);
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	private void writeIntoSheet(WritableSheet sheet, TranslationItem item) {
		HashMap<String, StringItem> itemTranlMap = item.getItemMap();
		index++;
		String stringId = item.getStringId();
		int stringIdIndex = getIndexInTitles("String ID", sheet);
		WritableCell stringIdCell = new Label(stringIdIndex, index, stringId);
		try {
			sheet.addCell(stringIdCell);
		} catch (RowsExceededException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (WriteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int increaseIndex = 0;
		for (String locale : itemTranlMap.keySet()) {
			StringItem stringItem = itemTranlMap.get(locale);
			String[] values = stringItem.getValues();
			increaseIndex = values.length;
			
			int localeIndex = getIndexInTitles(locale, sheet);
			
			for (int i = 0; i < values.length; i++) {
				int rowIndex = index + i;
				WritableCell cell = new Label(localeIndex, rowIndex, values[i]);
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
		index += increaseIndex - 1;
	}
	
	private TranslationItem[] getSortedItems() {
		TranslationItem[] ret = new TranslationItem[mTranslateMap.keySet().size()];
		ret = mTranslateMap.values().toArray(ret);
		Comparator<TranslationItem> itemSorter = new Comparator<TranslationItem>() {

			@Override
			public int compare(TranslationItem o1, TranslationItem o2) {
				return o2.getLocalizedSize() - o1.getLocalizedSize();
			}
		};
		Arrays.sort(ret, itemSorter);
		
		return ret;
	}
	
	private HashMap<String, Integer> mLocaleSizeMap = new HashMap<String, Integer>();
	private void buildLocalSizeMap(TranslationItem[] items) {
		for (TranslationItem item : items) {
			for (StringItem stringItem : item.getItemMap().values()) {
				String locale = stringItem.getLocale();
				boolean find = false;
				for (String key : mLocaleSizeMap.keySet()) {
					if (locale.equals(key)) {
						find = true;
						Integer value = mLocaleSizeMap.get(key);
						value = new Integer(value.intValue() + 1);
						mLocaleSizeMap.remove(key);
						mLocaleSizeMap.put(locale, value);
						break;
					}
				}
				if (!find) {
					mLocaleSizeMap.put(locale, 1);
				}
			}
		}
	}
	
	private String[] getSortedLocales() {
		String[] ret = new String[mLocaleSizeMap.size()];
		ret = mLocaleSizeMap.keySet().toArray(ret);
		Comparator<String> localeSorter = new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				int size1 = mLocaleSizeMap.get(o1);
				int size2 = mLocaleSizeMap.get(o2);
				return size2 - size1;
			}
		};
		
		Arrays.sort(ret, localeSorter);
		return ret;
	}
	
	private void buildTitles(WritableSheet sheet) {
		getIndexInTitles(STRING_ID, sheet);
		String[] locales = getSortedLocales();
		for (String locale : locales) {
			getIndexInTitles(locale, sheet);
		}
	}
	
	private void writeIntoSheet(WritableSheet sheet, boolean array) {
		index = 0;
		TranslationItem[] items = getSortedItems();
		buildLocalSizeMap(items);
		buildTitles(sheet);
		
		for (TranslationItem item : items) {
			if (item.isArray() ^ !array) {
				writeIntoSheet(sheet, item);
			}
		}
		
	}

	private ArrayList<String> mTitleList;
	@Override
	public void run(AttributeParser parser) {
		String action = AttributeConst.ACTION_PARSE;
		String resource = parser.getStringValue(action, AttributeConst.ATTR_PARSE_KEY_RESOURCE);
		String target = parser.getStringValue(action, AttributeConst.ATTR_PARSE_KEY_TARGET);
		int max = parser.getIntValue(action, AttributeConst.ATTR_PARSE_MAX_KEY_LOCALE_COUNT);
		int min = parser.getIntValue(action, AttributeConst.ATTR_PARSE_MIN_KEY_LOCALE_COUNT);
		String escapeFileString = parser.getStringValue(action, AttributeConst.ATTR_PARSE_KEY_ESCAPE_FILES);
		String templatePath = parser.getStringValue(action, AttributeConst.ATTR_PARSE_KEY_TEMPLATE_PATH);
		String templateSheetname = parser.getStringValue(action, AttributeConst.ATTR_COMPARE_KEY_SHEET_NAME);
		TitleParser titlePaser = new TitleParser();
		titlePaser.parse(templatePath, templateSheetname);
		ArrayList<String> titles = titlePaser.getTitles();
		mTitleList = titles;
		sheetTitles = mTitleList;
		
		updateEscapeFiles(escapeFileString);
		setMaxLocaleCount(max);
		setMinLocaleCount(min);
		parse(resource);
		writeToFile(target);
	}
	
}
