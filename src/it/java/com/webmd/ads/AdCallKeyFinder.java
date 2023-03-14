package com.webmd.ads;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.ReadProperties;
import com.webmd.general.common.XlRead;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;

/*
 * Class to get keys from ad calls of different kind of pages
 */
@Listeners(com.webmd.general.common.Listener.class)
public class AdCallKeyFinder extends AdsCommon {

	static List<String> keysSet = new LinkedList();
	static FileOutputStream outFile;
	static File file = new File(ReadProperties.projectLocation+
			"/TestOutput/AdCallValidationResults_" + LocalDate.now() +"_"
			+LocalTime.now().format(DateTimeFormatter.ofPattern("HH_mm_ss"))+ ".xls");

	static Map<String, String> expectedMap = new LinkedHashMap<String, String>();
	static Map<String, String> actualMap = new LinkedHashMap<String, String>();
	static List<String> expectedKeys;

	static HSSFWorkbook ouputWorkBook;

	static HSSFSheet outputSheet;
	static HSSFRow rowValues, rowKeys, row;

	JavascriptExecutor jse;

	/*
	 * Setting the Style to RED , For the Unexpected Values
	 */
	public static void errorStyle(HSSFCell cell) {	
		CellStyle style = ouputWorkBook.createCellStyle();
		style = ouputWorkBook.createCellStyle();
		style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		cell.setCellStyle(style);
	}

	@BeforeClass
	public void beforeClass(){
		login();
		getDriver();
		String input[][] = XlRead.fetchData("TestData/keysSet.xls", "Keys");
		ouputWorkBook = new HSSFWorkbook();
		outputSheet = ouputWorkBook.createSheet("Results");
		row = outputSheet.createRow(0);

		for(int i=0; i<input[0].length; i++){
			row.createCell(i).setCellValue(input[0][i]);
			keysSet.add(input[0][i]);
		}
		//jse = (JavascriptExecutor)getDriver();
	}

	/*
	 * Writing output to the file
	 */
	public static void writeIntoExcel() throws IOException {
		outFile = new FileOutputStream(file);
		ouputWorkBook.write(outFile);
		System.out.println("File Created");
	}

	@Test (dataProvider = "dataProviderIUPartsForAllTypeOfPages")
	public void getAdCallKeys(String type, String URL){
		getServer().newHar();
		getDriver().get(URL+"?faf=1");//+"?faf=1"

		Har har =getServer().getHar();
		har.getLog().getBrowser();
		List<HarEntry> res = har.getLog().getEntries();

		for (HarEntry harEntry : res) {
			String url = harEntry.getRequest().getUrl();
			if(url.contains("securepubads.g.doubleclick.net/gampad/ads?")){
				List<HarNameValuePair> queryParams = harEntry.getRequest().getQueryString();
				for (HarNameValuePair harNameValuePair : queryParams) {
					actualMap.put(harNameValuePair.getName(), harNameValuePair.getValue());
				}
				break;
			}
		}

		int rows = outputSheet.getPhysicalNumberOfRows(), columnCount=0;

		rowKeys = outputSheet.createRow(rows+1);
		rowValues = outputSheet.createRow(rows + 2);
		row = outputSheet.createRow(rows+3);
		HSSFCell cell = null;

		/*rowKeys.createCell(0).setCellValue(type);
		rowValues.createCell(0).setCellValue(URL);*/

		for(String key:keysSet){
			/*cell = rowKeys.createCell(columnCount);
			cell.setCellValue(key);
			try{

				Assert.assertTrue(actualMap.keySet().contains(key));
			}catch(AssertionError e){
				try{
					Assert.assertTrue(actualMap.get("cust_params").contains(key+"="));
				}catch(AssertionError e1){
					cell.setCellValue(key);
					errorStyle(cell);
				}
			}*/

			cell = rowValues.createCell(columnCount);
			String value;

			value = actualMap.get(key);
			if(value!=null)
				cell.setCellValue(value);
			else if(actualMap.get("cust_params").contains(key+"=")){
				value = StringUtils.substringBetween(actualMap.get("cust_params"),key+"=","&");
				cell.setCellValue(value);
			}else{
				cell.setCellValue("NA");
				errorStyle(cell);
			}

			columnCount++;
		}
		row.createCell(0).setCellValue("Done");

	}


	@Test (dataProvider = "dataProviderIUPartsForAllTypeOfPages")
	public void getValuesFromCustParams(String type, String URL){//String type, String URL
		getServer().newHar();
		getDriver().get(URL);//+"?faf=1"
		//getDriver().get("https://www.medscape.com/viewarticle/910121");
		String custParams = null;
		String[] params;
		int rows = outputSheet.getPhysicalNumberOfRows();

		

		int callCount = 1;

		scrollTillEnd();

		Har har =getServer().getHar();
		har.getLog().getBrowser();
		List<HarEntry> res = har.getLog().getEntries();

		for (HarEntry harEntry : res) {
			String url = harEntry.getRequest().getUrl();
			if(url.contains("securepubads.g.doubleclick.net/gampad/ads?")){
				custParams = getSpecificKeyFromSecurePubadCall("cust_params");
				HSSFCell cell = null;
				rowKeys.createCell(0).setCellValue(getDriver().getCurrentUrl());
				rowValues.createCell(0).setCellValue("Ad call Count ="+callCount);
				params = custParams.split("&");
				int count = 1;
				
				rowKeys = outputSheet.createRow(rows+1);
				rowValues = outputSheet.createRow(rows + 2);
				row = outputSheet.createRow(rows+3);

				for(String param : params){
					cell = rowKeys.createCell(count);
					cell.setCellValue(param.split("=")[0]);
					cell = rowValues.createCell(count);
					try{
						cell.setCellValue(param.split("=")[1]);
					}catch(ArrayIndexOutOfBoundsException e){
						cell.setCellValue("Blank");
					}
					count++;
				}
				
				row.createCell(0).setCellValue("Done");
				callCount++;
			}
			
		}




	}


	@DataProvider
	public  String[][] dataProviderIUPartsForAllTypeOfPages() {
		return XlRead.fetchDataExcludingFirstRow("TestData/iuPartsTest.xls", "Prod");
		/*return new String[][]  {
			{"https://www.medscape.com/cardiology"}, 
			{"https://www.medscape.com/pediatrics"}
			};*/
	}

	@AfterClass
	public void afterClass() throws IOException{
		writeIntoExcel();
	}

}
