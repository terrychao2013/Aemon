

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;


public class RevertTranslationParser implements TranslateRunner{
	private static final String XML_HEAD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n";
	private static final String XML_TAIL = "</resources>";
	
	private ArrayList<TranslationItem> mTranslateList = new ArrayList<TranslationItem>();
	
	private HashMap<String, Integer> mTitleMap = new HashMap<String, Integer>();
	private ArrayList<String> mUpdatedStrings = new ArrayList<String>();
	private String mNewFileName = "";
	private boolean mOverWriteWithEmptyString = false;
	
	public void parse(String resourcePath, String sheetName) {
		System.out.println("RevertTranslationParser parse");
		Workbook resourceBook = getResourceBook(resourcePath);
		Sheet sheet = getResourceSheet(resourceBook, sheetName);
		parseSheet(sheet);
	}
	
	public void writeIntoFile(String target, String revertType) {
		System.out.println("write into file " + target);
		for (String locale : mTitleMap.keySet()) {
			if (!TranslateParser.STRING_ID.equalsIgnoreCase(locale)) {
				if (AttributeConst.ATTR_REVERT_VALUE_REVERT_TYPE_FRESH.equalsIgnoreCase(revertType)) {
					freshXML(target, locale);
				} else if (AttributeConst.ATTR_REVERT_VALUE_REVERT_TYPE_WRITE.equalsIgnoreCase(revertType)){
					writeIntoXml(target, locale, false);
				} else if (AttributeConst.ATTR_REVERT_VALUE_REVERT_TYPE_WRITE_NEW.equalsIgnoreCase(revertType)) {
					writeIntoXml(target, locale, true);
				}
			}
		}
	}
	
	public void freshXML(String targetFolderPath, String locale) {
		System.out.println("freshXML " + locale);
		String targetResourceFolderPath = targetFolderPath + "/" + locale;
		File resFolder = new File(targetResourceFolderPath);
		if (resFolder.exists() && resFolder.isDirectory()) {
			FilenameFilter filter = new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".xml")) {
						for (String s : mEscapeFileList) {
							if (s.equalsIgnoreCase(name)) {
								return false;
							}
						}
						return true;
					}
					return false;
				}
			};
			File[] files = resFolder.listFiles(filter);
			if (files != null) {
				mUpdatedStrings.clear();
				for (File file : files) {
					freshXMLFile(locale, file);
				}
				String newFilePath = resFolder.getAbsolutePath() + "/" + mNewFileName;
				createNewFile(newFilePath, locale);
			}
		}
	}
	
	public void freshXMLFile(String locale, File file) {
		if (file != null && file.exists()) {
			String newFilePath = file.getAbsolutePath() + "_tmp";
			File newFile = new File(newFilePath);
			try {
				newFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (newFile.exists()) {
				updateNewFile(locale, file, newFile);
				file.delete();
				newFile.renameTo(file);
			}
		}
	}
	
	private void updateNewFile(String locale, File resourceFile, File targetFile) {
		Scanner scanner = null;
		try {
			scanner = new Scanner(resourceFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(targetFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (scanner != null && os != null) {
			updateFile(locale, scanner, os);
			try {
				scanner.close();
				os.flush();
				os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private static final String XML_ITEM_STARTER = "<string name=\"";
	private static final String XML_ITEM_ENDER = "</string>";
	private String getStringId(String input) {
		String value = null;
		
		String s = input.trim();
		if (s.contains(XML_ITEM_STARTER)) {
			s = s.replace(XML_ITEM_STARTER, "");
			int endIndex = s.indexOf("\"");
			if (endIndex != -1) {
				value = s.substring(0, endIndex);
			}
		}
		
		return value;
	}
	
	private String getNewValue(String stringId, String locale) {
		for (TranslationItem item : mTranslateList) {
			if (item.getStringId().equals(stringId)) {
				StringItem stringItem = item.getItemMap().get(locale);
				if (stringItem != null) {
					if (!mOverWriteWithEmptyString && !stringItem.hasValueContent()) {
						return null;
					}
					return stringItem.getXMLString();
				}
			}
		}
		
		return null;
	}
	
	private boolean hasUpdated(String stringId) {
		for (String s : mUpdatedStrings) {
			if (s.equals(stringId)) {
				return true;
			}
		}
		return false;
	}
	
	private void writeNewStrings(FileOutputStream os, String locale) {
		for (TranslationItem item : mTranslateList) {
			String id = item.getStringId();
			if (!hasUpdated(id)) {
				mUpdatedStrings.add(id);
				StringItem stringItem = item.getItemMap().get(locale);
				if (stringItem != null && stringItem.hasValueContent()) {
					String value = stringItem.getXMLString();
					if (value != null & value.length() > 0) {
						value += "\n";
						try {
							os.write(value.getBytes());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	
	}
	
	private void writeNewFile(File newFile, String locale) {
		FileOutputStream os = null; 
		try {
			os = new FileOutputStream(newFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (os != null) {
			try {
				os.write(XML_HEAD.getBytes());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			writeNewStrings(os, locale);
			try {
				os.write(XML_TAIL.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private boolean hasNewStringNeedToWrite(String locale) {
		for (TranslationItem item : mTranslateList) {
			String id = item.getStringId();
			if (!hasUpdated(id)) {
				StringItem stringItem = item.getItemMap().get(locale);
				if (stringItem != null && stringItem.hasValueContent()) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void createNewFile(String newFilePath, String locale) {
		File file = new File(newFilePath);
		if (hasNewStringNeedToWrite(locale)) {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				try {
					file.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (file.exists()) {
					writeNewFile(file, locale);
				}
			} else {
				appendNewFile(file, locale);
			}
			
		}
	}
	
	private void appendNewFile(File oldFile, String locale) {
		File tmp = new File(oldFile.getAbsoluteFile() + "_tmp");
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(tmp);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Scanner scanner = null;
		try {
			scanner = new Scanner(oldFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (scanner != null && os != null) {
			appendNewFile(scanner, os, locale);
			scanner.close();
			try {
				os.flush();
				os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			oldFile.delete();
			tmp.renameTo(oldFile);
		}
	}
	
	private void appendNewFile(Scanner scanner, FileOutputStream os, String locale) {
		while (scanner.hasNext()) {
			String input = scanner.nextLine();
			if (input.contains(XML_TAIL)) {
				writeNewStrings(os, locale);
			} 
			
			try {
				os.write((getNewString(input) + "\n").getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void updateFile(String locale, Scanner scanner, FileOutputStream os) {
		boolean handling = false;
		while (scanner.hasNextLine()) {
			String input = scanner.nextLine();
			
			/*if (isTargetNewFile && input.contains(XML_TAIL)) {
				writeNewStrings(os, locale);
			}*/
			boolean writeIntoTarget = true;
			if (handling) {
				writeIntoTarget = false;
				if (input.contains(XML_ITEM_ENDER)) {
					handling = false;
				}
			} else {
				if (input.contains(XML_ITEM_STARTER)) {
					String id = getStringId(input);
					if (id != null) {
						mUpdatedStrings.add(id);
						String newValue = getNewValue(id, locale);
						if (newValue != null && newValue.length() > 0) {
							boolean wrong = false;
							if (!newValue.contains("%") && input.contains("%")) {
								wrong = true;
							}
							if (!wrong) {
								writeIntoTarget = false;
								handling = true;
								try {
									os.write((newValue + "\n").getBytes());
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
					
					if (input.contains(XML_ITEM_ENDER)) {
						handling = false;
					}
				} 
			}
			
			if (writeIntoTarget) {
				try {
					os.write((getNewString(input) + "\n").getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private String getNewString(String input) {
		String ret = input;
		ret = StringItem.correctSymbol(ret);
		return ret;
	}
	
	private String getXmlItem(StringItem item) {
		String stringId = item.getStringId();
		String value = item.getValues()[0];
		return getXmlItem(stringId, value);
	}
	
	private String getXmlItem(String id, String value) {
		StringBuilder sb = new StringBuilder();
		sb.append("    <string name=\"");
		sb.append(id);
		sb.append("\">");
		sb.append(value);
		sb.append("</string>");
		return sb.toString();
	}
	
	public void writeIntoXml(String targetFolderpath, String locale, boolean newLocale) {
		String xmlFilePath = targetFolderpath + "/" + locale + "/strings.xml";
		
		File file = new File(xmlFilePath);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			FileOutputStream os = new FileOutputStream(file);
			if (newLocale) {
				os.write(XML_HEAD.getBytes());
			}
			for (TranslationItem item : mTranslateList) {
				HashMap<String, StringItem> map = item.getItemMap();
				StringItem stringItem = map.get(locale);
				String ret = getXmlItem(stringItem);
				System.out.println(ret);
				os.write((ret + "\n").getBytes());
			}
			if (newLocale) {
				os.write(XML_TAIL.getBytes());
			}
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void parseSheet(Sheet sheet) {
		if (sheet != null) {
			parseTitles(sheet);
			parseTranslate(sheet);
		} else {
			System.err.println("parse ERROR");
		}
	}
	
	private void parseTranslate(Sheet sheet) {
		int stringCount = sheet.getRows();
		for (int i = 1; i < stringCount; i++) {
			Cell[] row = sheet.getRow(i);
			parseRow(row);
		}
	}
	
	private void parseRow(Cell[] row) {
		if (row != null && row.length > 0) {
			int length = row.length;
			String stringId = row[0].getContents();
			if (stringId.length() > 0) {
				TranslationItem item = new TranslationItem(stringId, false);
				for (String key : mTitleMap.keySet()) {
					int index = mTitleMap.get(key);
					if (index != 0 && index < length) {
						Cell valueCell = row[index];
						if (valueCell != null) {
							String value = valueCell.getContents();
							StringItem stringItem = new StringItem(stringId, key);
							stringItem.addValue(value);
							item.addTranslation(stringItem);
						}
					}
				}
				mTranslateList.add(item);
			}
		}
	}
	
	private void parseTitles(Sheet sheet) {
		Cell[] titleCells = sheet.getRow(0);
		if (titleCells != null) {
			for (int i = 0; i < titleCells.length; i++) {
				Cell titleCell = titleCells[i];
				if (titleCell != null) {
					String title = titleCell.getContents();
					if (title.length() > 0) {
						mTitleMap.put(title, i);
					}
				}
			}
		} else {
			System.err.println("title format error!");
		}
	}
	
	private Sheet getResourceSheet(Workbook resourceBook, String sheetName) {
		if (resourceBook != null) {
			Sheet sheet = resourceBook.getSheet(sheetName);
			return sheet;
		}
		return null;
	}
	
	private Workbook getResourceBook(String resourcePath) {
		if (resourcePath != null && resourcePath.length() > 0) {
			File resource = new File(resourcePath);
			if (resource.exists()) {
				Workbook resourceBook = null;
				try {
					WorkbookSettings workbookSettings = new WorkbookSettings();
		            workbookSettings.setEncoding("ISO-8859-1");
					resourceBook = Workbook.getWorkbook(resource, workbookSettings);
				} catch (BiffException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return resourceBook;
				
			} else {
				System.err.println(resourcePath + " does not exists!");
			}
		} else {
			System.err.println("resource file path is incorrect");
		}
		return null;
	
	}
	
	private boolean isRevertTypeLegal(String type) {
		if (AttributeConst.ATTR_REVERT_VALUE_REVERT_TYPE_FRESH.equalsIgnoreCase(type) 
				|| AttributeConst.ATTR_REVERT_VALUE_REVERT_TYPE_WRITE.equalsIgnoreCase(type)
				|| AttributeConst.ATTR_REVERT_VALUE_REVERT_TYPE_WRITE_NEW.equalsIgnoreCase(type)) {
			return true;
		}
		return false;
	}
	
	private ArrayList<String> mEscapeFileList = new ArrayList<String>();
	private void updateEscapeFiles(String escapeFileString) {
		if (escapeFileString != null && escapeFileString.length() > 0) {
			String[] escapeFileNames = escapeFileString.split(AttributeConst.ATTR_PARAMS_SPERATOR);
			for (String s : escapeFileNames) {
				mEscapeFileList.add(s);
			}
		}
		
	}

	@Override
	public void run(AttributeParser parser) {
		String action = AttributeConst.ACTION_REVERT;
		String resource = parser.getStringValue(action, AttributeConst.ATTR_REVERT_KEY_RESOURCE);
		String target = parser.getStringValue(action, AttributeConst.ATTR_REVERT_KEY_TARGET);
		String sheetName = parser.getStringValue(action, AttributeConst.ATTR_REVERT_KEY_SHEET_NAME);
		String revertType = parser.getStringValue(action, AttributeConst.ATTR_REVERT_KEY_REVERT_TYPE);
		String escapeFilesString = parser.getStringValue(action, AttributeConst.ATTR_REVERT_KEY_ESCAPE_FILES);
		mNewFileName = parser.getStringValue(action, AttributeConst.ATTR_REVERT_KEY_NEW_STRING_FILE_NAME);
		
		mOverWriteWithEmptyString = parser.getBooleanValue(action, AttributeConst.ATTR_REVERT_KEY_OVERWRITE_WITH_EMPTY_STRING);
		
		if (isRevertTypeLegal(revertType)) {
			updateEscapeFiles(escapeFilesString);
			parse(resource, sheetName);
			writeIntoFile(target, revertType);
		} else {
			System.err.println("illegal revert type [" + revertType + "]");
		}
	}
}
