
public class AttributeConst {
	public static final int NAME_AREA_LENGTH = 25;
	public static String ATTR_NAME_PREFIX = "--";
	
	public static String ATTR_PARAMS_SPERATOR = "/";
	
	public static String ACTION_PARSE = "parse";
	public static String ACTION_REVERT = "revert";
	public static String ACTION_COMPARE = "compare";
	
	public static String ATTR_PARSE_KEY_RESOURCE = "resource";
	public static String ATTR_PARSE_KEY_TARGET = "target";
	public static String ATTR_PARSE_MIN_KEY_LOCALE_COUNT = "min-locale-count";
	public static String ATTR_PARSE_MAX_KEY_LOCALE_COUNT = "max-locale-count";
	public static String ATTR_PARSE_KEY_ESCAPE_FILES = "escape-files";
	public static String ATTR_PARSE_KEY_TEMPLATE_PATH = "template-path";
	public static String ATTR_PARSE_KEY_TEMPLATE_SHEET_NAME = "template-sheetname";
	
	public static String ATTR_REVERT_KEY_RESOURCE = "resource";
	public static String ATTR_REVERT_KEY_TARGET = "target";
	public static String ATTR_REVERT_KEY_SHEET_NAME = "sheet-name";
	public static String ATTR_REVERT_KEY_REVERT_TYPE = "revert-type";
	public static String ATTR_REVERT_KEY_ESCAPE_FILES = "escape-files";
	public static String ATTR_REVERT_KEY_NEW_STRING_FILE_NAME = "new-string-file-name";
	public static String ATTR_REVERT_KEY_OVERWRITE_WITH_EMPTY_STRING = "over-write-with-empty-string";
	
	public static String ATTR_REVERT_VALUE_REVERT_TYPE_WRITE = "write";
	public static String ATTR_REVERT_VALUE_REVERT_TYPE_FRESH = "fresh";
	public static String ATTR_REVERT_VALUE_REVERT_TYPE_WRITE_NEW = "write_new";

	public static String ATTR_COMPARE_KEY_RESOURCE_NEW = "resource-new";
	public static String ATTR_COMPARE_KEY_RESOURCE_OLDE = "resource-old";
	public static String ATTR_COMPARE_KEY_TARGET = "target";
	public static String ATTR_COMPARE_KEY_TEMPLATE_PATH = "template-path";
	public static String ATTR_COMPARE_KEY_SHEET_NAME = "template-sheetname";
	
	public static final int ATTR_NAME_INDEX = 0;
	public static final int ATTR_DESCRIPTION_INDEX = 1;
	public static final int ATTR_DEFAUL_VALUE_INDEX = 2;
	
	private static String[][] ATTRS_PARSE = {
		{ATTR_PARSE_KEY_RESOURCE, "the resource file path, it's the path of res, like res", "~/"},
		{ATTR_PARSE_KEY_TARGET, "the target file, end with .xls", "~/result.xls"},
		{ATTR_PARSE_KEY_ESCAPE_FILES, "the files won't be parsed into the target result", ""},
		{ATTR_PARSE_MIN_KEY_LOCALE_COUNT, "the min localed string count, the string with smaller count of locale won't be processed", "1"},
		{ATTR_PARSE_MAX_KEY_LOCALE_COUNT, "the max localed string count, the string with max count of locale won't be processed -1 means no max count limitation", "-1"},
		{ATTR_PARSE_KEY_TEMPLATE_PATH, "the title template file path", ""},
		{ATTR_PARSE_KEY_TEMPLATE_SHEET_NAME, "the title template file sheet name", ""},
	};
	
	private static String[][] ATTRS_REVERT = {
		{ATTR_REVERT_KEY_RESOURCE, "the resource file path, end with .xls", "~/"},
		{ATTR_REVERT_KEY_TARGET, "the target file path", "~/"},
		{ATTR_REVERT_KEY_SHEET_NAME, "the sheet name of the resource", "Sheet1"},
		{ATTR_REVERT_KEY_REVERT_TYPE, "the type of revert will be invoked(write/fresh/write_new)", "write_new"},
		{ATTR_REVERT_KEY_ESCAPE_FILES, "the files won't be checked, speparated by \"/\"", ""},
		{ATTR_REVERT_KEY_NEW_STRING_FILE_NAME, "the file name for new strings", "strings.xml"},
		{ATTR_REVERT_KEY_OVERWRITE_WITH_EMPTY_STRING, "if overwirte a string when the new string is empty", "false"},
	};
	
	private static String[][] ATTRS_COMPARE = {
		{ATTR_COMPARE_KEY_RESOURCE_NEW, "the new resource file path, put the res folder in the resource folder", "~/"},
		{ATTR_COMPARE_KEY_TARGET, "the target file path", "~/"},
		{ATTR_COMPARE_KEY_RESOURCE_OLDE, "the old reosurce file path, put the res folder int the resource folder", "~/"},
		{ATTR_COMPARE_KEY_TEMPLATE_PATH, "the title template file path", ""},
		{ATTR_COMPARE_KEY_SHEET_NAME, "the title template file sheet name", ""},
	};
	
	public static Object[][] ATTRS = {
		{ACTION_PARSE, ATTRS_PARSE},
		{ACTION_REVERT, ATTRS_REVERT},
		{ACTION_COMPARE, ATTRS_COMPARE},
	};
}
