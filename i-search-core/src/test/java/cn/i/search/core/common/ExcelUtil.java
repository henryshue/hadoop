package cn.i.search.core.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * excel工具类
 * 
 * @author henryshue
 *
 */

public class ExcelUtil {
	public static String getCellValue(Cell cell) {
		if (null == cell) {
			return "";
		}
		if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
			return trimString(cell.getStringCellValue());
		}
		if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
			return trimString(String.valueOf(cell.getBooleanCellValue()));
		}
		if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			return trimString(String.valueOf(cell.getNumericCellValue()));
		}
		return "";
	}

	/*去除前后空格*/
	private static String trimString(String str) {
		if (null == str) {
			return "";
		}
		return str.trim();
	}

	/**
	 * @param path
	 *            路径
	 * @param sheetNum
	 *            在第几标签
	 * @return
	 */
	public static List<String[]> getRows(String path, int sheetNum) {
		try (InputStream in = new FileInputStream(path);) {
			return getRows(in, sheetNum);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<String[]> getRows(InputStream in, int sheetNum) {
		List<String[]> rows = new ArrayList<>();
		String[] cells = null;
		try {
			Workbook wb = WorkbookFactory.create(in);
			if (null == wb) {
				return null;
			}
			Sheet sheet = wb.getSheetAt(sheetNum);
			if (null == sheet) {
				return null;
			}
			int rowNum = sheet.getLastRowNum();
			for (int i = 0; i <= rowNum; i++) {
				Row row = sheet.getRow(i);
				if (null != row) {
					cells = new String[row.getLastCellNum()];
					for (int j = 0; j < row.getLastCellNum(); j++) {
						cells[j] = getCellValue(row.getCell(j));
					}
					rows.add(cells);
				}
			}
			return rows;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		}
		return null;
	}
}
