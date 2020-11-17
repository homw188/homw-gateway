package com.homw.gateway.admin.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFCellUtil;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;

public class PoiExcelUtil {
	private static final Log log = LogFactory.getLog(PoiExcelUtil.class);

	/**
	 * 功能：将HSSFWorkbook写入Excel文件 并写入指定路径
	 * 
	 * @param wb       HSSFWorkbook
	 * @param fileName 文件名 (带路径的比如：D:data\doc\aaa.xls)
	 */
	public static void writeWorkbook(HSSFWorkbook wb, String fileName) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(fileName);
			wb.write(fos);
		} catch (FileNotFoundException e) {
			log.error(new StringBuffer("[").append(e.getMessage()).append("]").append(e.getCause()));
			e.printStackTrace();
		} catch (IOException e) {
			log.error(new StringBuffer("[").append(e.getMessage()).append("]").append(e.getCause()));
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				log.error(new StringBuffer("[").append(e.getMessage()).append("]").append(e.getCause()));
				e.printStackTrace();
			}
		}
	}

	/**
	 * 功能：将HSSFWorkbook写入Excel文件,保存于客户端
	 * 
	 * @param wb       HSSFWorkbook
	 * @param response 相应对象，为了客户端响应时提示保存
	 * @param fileName 文件名
	 */
	public static void writeWorkbookToDesk(HSSFWorkbook wb, String fileName, HttpServletResponse response) {
		try {
			response.setContentType("application/msexcel");// 定义输出类型
			// String downFileName= new String(fileName.getBytes("gbk"),"ISO8859-1");//本地乱码解决
			String downFileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");// 服务器乱码解决
			// String downFileName="importdataFile" + new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date()) +".xls";// 设定输出文件头
			String inlineType = "attachment"; // 是否内联附件
			response.setHeader("Content-Disposition", inlineType + ";filename=\"" + downFileName
					+ new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".xls" + "\"");
			response.resetBuffer();
			OutputStream outs = response.getOutputStream();
			wb.write(outs); // 直接把Excel转成输出流
			outs.flush();
			outs.close();
		} catch (FileNotFoundException e) {
			log.error(new StringBuffer("[").append(e.getMessage()).append("]").append(e.getCause()));
			e.printStackTrace();
		} catch (IOException e) {
			log.error(new StringBuffer("[").append(e.getMessage()).append("]").append(e.getCause()));
			e.printStackTrace();
		} 
	}

	/**
	 * 功能：创建HSSFSheet工作簿
	 * 
	 * @param wb        HSSFWorkbook
	 * @param sheetName String
	 * @return HSSFSheet
	 */
	public static HSSFSheet createSheet(HSSFWorkbook wb, String sheetName) {
		HSSFSheet sheet = wb.createSheet(sheetName);// 创建sheet并设置sheet名字
		sheet.setDefaultColumnWidth(20);// 设置默认列宽
		sheet.setGridsPrinted(true);// 设置网格打印
		sheet.setDisplayGridlines(true);// 设置显示网格线
		return sheet;
	}

	/**
	 * 功能：创建HSSFRow
	 * 
	 * @param sheet  HSSFSheet
	 * @param rowNum int
	 * @param height int
	 * @return HSSFRow
	 */
	public static HSSFRow createRow(HSSFSheet sheet, int rowNum, int height) {
		HSSFRow row = sheet.createRow(rowNum);// 根据行号创建行
		row.setHeight((short) height);// 设置行高
		return row;
	}

	/**
	 * 功能：创建CellStyle样式
	 * 
	 * @param wb              HSSFWorkbook
	 * @param backgroundColor 背景色
	 * @param foregroundColor 前置色
	 * @param font            字体
	 * @return CellStyle
	 */
	public static CellStyle createCellStyle(HSSFWorkbook wb, short backgroundColor, short foregroundColor, short halign,
			Font font) {
		CellStyle cs = wb.createCellStyle();
		cs.setAlignment(halign);
		cs.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		cs.setFillBackgroundColor(backgroundColor);
		cs.setFillForegroundColor(foregroundColor);
		cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cs.setFont(font);
		return cs;
	}

	/**
	 * 功能：创建带边框的CellStyle样式
	 * 
	 * @param wb              HSSFWorkbook
	 * @param backgroundColor 背景色
	 * @param foregroundColor 前置色
	 * @param font            字体
	 * @return CellStyle
	 */
	public static CellStyle createBorderCellStyle(HSSFWorkbook wb, short backgroundColor, short foregroundColor,
			short halign, Font font) {
		CellStyle cs = wb.createCellStyle();
		cs.setAlignment(halign);
		cs.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		cs.setFillBackgroundColor(backgroundColor);
		cs.setFillForegroundColor(foregroundColor);
		cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cs.setFont(font);
		cs.setBorderLeft(CellStyle.BORDER_THIN);
		cs.setBorderRight(CellStyle.BORDER_THIN);
		cs.setBorderTop(CellStyle.BORDER_THIN);
		cs.setBorderBottom(CellStyle.BORDER_THIN);
		return cs;
	}

	/**
	 * 功能：创建CELL
	 * 
	 * @param row     HSSFRow
	 * @param cellNum int
	 * @param style   HSSFStyle
	 * @return HSSFCell
	 */
	public static HSSFCell createCell(HSSFRow row, int cellNum, CellStyle style) {
		HSSFCell cell = row.createCell(cellNum);
		cell.setCellStyle(style);
		return cell;
	}

	/**
	 * 功能：合并单元格
	 * 
	 * @param sheet       HSSFSheet
	 * @param firstRow    int
	 * @param lastRow     int
	 * @param firstColumn int
	 * @param lastColumn  int
	 * @return int 合并区域号码
	 */
	public static int mergeCell(HSSFSheet sheet, int firstRow, int lastRow, int firstColumn, int lastColumn) {
		return sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstColumn, lastColumn));
	}

	/**
	 * 功能：创建字体
	 * 
	 * @param wb         HSSFWorkbook
	 * @param boldweight short
	 * @param color      short
	 * @return Font
	 */
	public static Font createFont(HSSFWorkbook wb, short boldweight, short color, short size) {
		Font font = wb.createFont();
		font.setBoldweight(boldweight);
		font.setColor(color);
		font.setFontHeightInPoints(size);
		return font;
	}

	/**
	 * 设置合并单元格的边框样式
	 * 
	 * @param sheet HSSFSheet
	 * @param ca    CellRangAddress
	 * @param style CellStyle
	 */
	public static void setRegionStyle(HSSFSheet sheet, CellRangeAddress ca, CellStyle style) {
		for (int i = ca.getFirstRow(); i <= ca.getLastRow(); i++) {
			HSSFRow row = HSSFCellUtil.getRow(i, sheet);
			for (int j = ca.getFirstColumn(); j <= ca.getLastColumn(); j++) {
				HSSFCell cell = HSSFCellUtil.getCell(row, j);
				cell.setCellStyle(style);
			}
		}
	}
}
