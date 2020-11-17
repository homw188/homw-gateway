package com.homw.gateway.admin.util;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

public class ExcelUtil {
	
	private static final Log log = LogFactory.getLog(ExcelUtil.class);
	
	/**
	 * 创建excel,无系统指定存放目录，生成excel并保存至客户端
	 * @param dateList String数组数据集合
	 * @param titleArray 表头数组
	 * @param sheetName	 sheet名称
	 * @param fileName 文件名称***.xls
	 */
	public static boolean createExcel(List<String[]> dateList,String[] titleArray,String sheetName,String fileName,HttpServletResponse response){
		try {
			if (null!=dateList && dateList.size()>0){
				HSSFWorkbook wb = new HSSFWorkbook();
				HSSFSheet sheet = PoiExcelUtil.createSheet(wb, sheetName);
				Font font = PoiExcelUtil.createFont(wb, HSSFFont.BOLDWEIGHT_NORMAL, HSSFFont.COLOR_NORMAL, (short)10);//2：字体是否粗体，3：字体颜色，4：字体大小
				CellStyle cellStyle = PoiExcelUtil.createBorderCellStyle(wb, HSSFColor.BLACK.index, HSSFColor.WHITE.index, HSSFCellStyle.ALIGN_CENTER, font);
				HSSFRow row = PoiExcelUtil.createRow(sheet, 0, 250);//第一行
				HSSFCell cell = null;
				Font titleFont = PoiExcelUtil.createFont(wb, HSSFFont.BOLDWEIGHT_BOLD, HSSFFont.COLOR_NORMAL, (short)10);//2：字体是否粗体，3：字体颜色，4：字体大小
				CellStyle titleCellStyle = PoiExcelUtil.createBorderCellStyle(wb, HSSFColor.BLACK.index, HSSFColor.WHITE.index, HSSFCellStyle.ALIGN_CENTER, titleFont);
				for (int j=0;j<titleArray.length;j++){
					cell = PoiExcelUtil.createCell(row, j, titleCellStyle);
					cell.setCellValue(titleArray[j]);
					cell.setCellStyle(titleCellStyle);
				}
				for (int i=0;i<dateList.size();i++){
					row = PoiExcelUtil.createRow(sheet, i+1, 250);
					String [] dateArray = dateList.get(i);
					for (int j=0;j<dateArray.length;j++){
						cell = PoiExcelUtil.createCell(row, j, cellStyle);
						cell.setCellValue(dateArray[j]);
						cell.setCellStyle(cellStyle);
					}
				}
				PoiExcelUtil.writeWorkbookToDesk(wb, fileName, response);
				return true;
			}else{
				log.error("当前数据为空，不能导出数据");
				return false;
			}
		} catch (Exception e) {
			log.error("导出excel过程中发生异常："+e.getMessage(),e);
			return false;
		}
	}
	
	/**
	 * 创建excel,无系统指定存放目录，生成excel并保存至客户端 (List<Object[]>)
	 * @param dateList String数组数据集合
	 * @param titleArray 表头数组
	 * @param sheetName	 sheet名称
	 * @param fileName 文件名称***.xls
	 */
	public static boolean createExcelForObject(List<Object[]> dateList,String[] titleArray,String sheetName,String fileName,HttpServletResponse response){
		try {
			if (null!=dateList && dateList.size()>0){
				HSSFWorkbook wb = new HSSFWorkbook();
				HSSFSheet sheet = PoiExcelUtil.createSheet(wb, sheetName);
				Font font = PoiExcelUtil.createFont(wb, HSSFFont.BOLDWEIGHT_NORMAL, HSSFFont.COLOR_NORMAL, (short)10);//2：字体是否粗体，3：字体颜色，4：字体大小
				CellStyle cellStyle = PoiExcelUtil.createBorderCellStyle(wb, HSSFColor.BLACK.index, HSSFColor.WHITE.index, HSSFCellStyle.ALIGN_CENTER, font);
				HSSFRow row = PoiExcelUtil.createRow(sheet, 0, 250);//第一行
				HSSFCell cell = null;
				Font titleFont = PoiExcelUtil.createFont(wb, HSSFFont.BOLDWEIGHT_BOLD, HSSFFont.COLOR_NORMAL, (short)10);//2：字体是否粗体，3：字体颜色，4：字体大小
				CellStyle titleCellStyle = PoiExcelUtil.createBorderCellStyle(wb, HSSFColor.BLACK.index, HSSFColor.WHITE.index, HSSFCellStyle.ALIGN_CENTER, titleFont);
				for (int j=0;j<titleArray.length;j++){
					cell = PoiExcelUtil.createCell(row, j, titleCellStyle);
					cell.setCellValue(titleArray[j]);
					cell.setCellStyle(titleCellStyle);
				}
				for (int i=0;i<dateList.size();i++){
					row = PoiExcelUtil.createRow(sheet, i+1, 250);
//					String [] dateArray =  (String[])dateList.get(i);
					//此处把原有object对象数组转换为string数组
					Object [] dateObject=dateList.get(i);
					String [] dateArray=new String[dateObject.length];
					for(int k=0;k<dateObject.length;k++){
						if (null==dateObject[k] || "".equals(dateObject[k])){
							dateArray[k]="";
						}else{
							dateArray[k]=dateObject[k].toString();
						}
					}
					
					for (int j=0;j<dateArray.length;j++){
						cell = PoiExcelUtil.createCell(row, j, cellStyle);
						cell.setCellValue(dateArray[j]);
						cell.setCellStyle(cellStyle);
					}
				}
				PoiExcelUtil.writeWorkbookToDesk(wb, fileName, response);
				return true;
			}else{
				log.error("当前数据为空，不能导出数据");
				return false;
			}
		} catch (Exception e) {
			log.error("导出excel过程中发生异常："+e.getMessage(),e);
			return false;
		}
	}
	
	/**
	 * 创建excel,用于系统指定固定目录，一般用于自动生成excel并存放于服务端
	 * @param dateList String数组数据集合
	 * @param titleArray 表头数组
	 * @param sheetName	 sheet名称
	 * @param fileName 文件名称.xls
	 * @param path 文件存放路径 ，比如：d:data\doc\
	 * @param dateStr 时间戳，为了避免文件名重名
	 */
	public static boolean createExcel(List<String[]> dateList,String[] titleArray,String sheetName,String fileName,String path,String dateStr){
		try {
			if (null!=dateList && dateList.size()>0){
				HSSFWorkbook wb = new HSSFWorkbook();
				HSSFSheet sheet = PoiExcelUtil.createSheet(wb, sheetName);
				Font font = PoiExcelUtil.createFont(wb, HSSFFont.BOLDWEIGHT_NORMAL, HSSFFont.COLOR_NORMAL, (short)10);//2：字体是否粗体，3：字体颜色，4：字体大小
				CellStyle cellStyle = PoiExcelUtil.createBorderCellStyle(wb, HSSFColor.BLACK.index, HSSFColor.WHITE.index, HSSFCellStyle.ALIGN_CENTER, font);
				HSSFRow row = PoiExcelUtil.createRow(sheet, 0, 250);//第一行
				HSSFCell cell = null;
				Font titleFont = PoiExcelUtil.createFont(wb, HSSFFont.BOLDWEIGHT_BOLD, HSSFFont.COLOR_NORMAL, (short)10);//2：字体是否粗体，3：字体颜色，4：字体大小
				CellStyle titleCellStyle = PoiExcelUtil.createBorderCellStyle(wb, HSSFColor.BLACK.index, HSSFColor.WHITE.index, HSSFCellStyle.ALIGN_CENTER, titleFont);
				for (int j=0;j<titleArray.length;j++){
					cell = PoiExcelUtil.createCell(row, j, titleCellStyle);
					cell.setCellValue(titleArray[j]);
					cell.setCellStyle(titleCellStyle);
				}
				for (int i=0;i<dateList.size();i++){
					row = PoiExcelUtil.createRow(sheet, i+1, 250);
					String [] dateArray = dateList.get(i);
					for (int j=0;j<dateArray.length;j++){
						cell = PoiExcelUtil.createCell(row, j, cellStyle);
						cell.setCellValue(dateArray[j]);
						cell.setCellStyle(cellStyle);
					}
				}
				String fileNameStr = path+"/"+dateStr+"/"+fileName+".xls";
				PoiExcelUtil.writeWorkbook(wb, fileNameStr);
				return true;
			}else{
				log.error("当前数据为空，不能导出数据");
				return false;
			}
		} catch (Exception e) {
			log.error("导出excel过程中发生异常："+e.getMessage(),e);
			return false;
		}
	}
}
