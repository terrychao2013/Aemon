import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;


public class TitleParser {
	private static final String FILE_PATH = "/home/terry/E/work/translation/parse/titles_template.xls";
	private static final String SHEET_NAME = "Sheet1";
	private ArrayList<String> mTitleList = new ArrayList<String>();
	
	public static void main(String[] args) {
		TitleParser parser = new TitleParser();
		parser.parse(FILE_PATH, SHEET_NAME);
		ArrayList<String> titles = parser.getTitles();
		for (String title : titles) {
			System.out.println(title);
		}
	}
	
	public ArrayList<String> getTitles() {
		return mTitleList;
	}
	
	public void parse(String filePath, String sheetName) {
		Workbook resourceBook = getResourceBook(filePath);
		Sheet sheet = getResourceSheet(resourceBook, sheetName);
		parseSheet(sheet);
	}
	
	private void parseSheet(Sheet sheet) {
		if (sheet != null) {
			parseTitles(sheet);
		} else {
			System.err.println("parse ERROR");
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
						mTitleList.add(title);
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
}
