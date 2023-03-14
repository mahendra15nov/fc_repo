package com.webmd.ads;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.ReadProperties;
import com.webmd.general.common.XlRead;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;

public class AdCallValidationDemo extends AdsCommon{
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
	static HSSFRow rowActualValues, rowExpectedValues, row;

	JavascriptExecutor jse;

	@BeforeClass
	public void beforeClass(){
		String input[][] = XlRead.fetchData("TestData/iuPartsTest.xls", "Sheet4");
		ouputWorkBook = new HSSFWorkbook();
		outputSheet = ouputWorkBook.createSheet("Results");
		row = outputSheet.createRow(0);

		for(int i=0; i<input[0].length; i++){
			row.createCell(i).setCellValue(input[0][i]);
			keysSet.add(input[0][i]);
		}
	}

	/*
	 * Writing output to the file
	 */
	public static void writeIntoExcel() throws IOException {
		outFile = new FileOutputStream(file);
		ouputWorkBook.write(outFile);
		System.out.println("File Created");
	}

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


	@BeforeMethod
	public void beforeMethod(){
		expectedMap.clear();
		actualMap.clear();
	}

	@DataProvider
	public String[][] dataProvider(){
		return XlRead.fetchDataExcludingFirstRow("TestData/iuPartsTest.xls", "Sheet4");
		//return XlRead.fetchDataExcludingFirstRow("TestData/Test.xls", "test");
	}

	public void prepareExpectedMAP(String[] input){
		int i = 0;
		//System.out.println("Expected MAP");

		for(String key: keysSet){
			//System.out.println(key+ " : "+input[i]);
			expectedMap.put(key, input[i]);
			i++;
		}
		System.out.println("expected map prepared");
	}

	public void prepareActualMap(){

		System.out.println("Actual MAP");
		Map<String, String> locallMap = new LinkedHashMap<String, String>();
		boolean flag = false;
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Har har =getServer().getHar();
		har.getLog().getBrowser();
		List<HarEntry> res = har.getLog().getEntries();

		for (HarEntry harEntry : res) {
			String url = harEntry.getRequest().getUrl();
			if(url.contains("securepubads.g.doubleclick.net/gampad/ads?")){
				flag = true;
				List<HarNameValuePair> queryParams = harEntry.getRequest().getQueryString();
				for (HarNameValuePair harNameValuePair : queryParams) {
					locallMap.put(harNameValuePair.getName(), harNameValuePair.getValue());
				}
				break;
			}
		}

		if(flag){
			actualMap.clear();

			for(String key: keysSet){
				switch(key){
				case "TestURL" : actualMap.put("TestURL", expectedMap.get("TestURL"));
				break;
				case "TriggerEvent" : actualMap.put("TriggerEvent", expectedMap.get("TriggerEvent"));
				break;
				case "DeviceType": actualMap.put("DeviceType", expectedMap.get("DeviceType"));
				break;
				case "User": actualMap.put("User", expectedMap.get("User"));
				break;
				case "PageType" : actualMap.put("PageType", expectedMap.get("PageType"));
				break;
				default:
					if(locallMap.get(key)!=null){
						actualMap.put(key, locallMap.get(key));
					}else if(locallMap.get("cust_params").contains(key+"=")){
						actualMap.put(key, StringUtils.substringBetween
								(locallMap.get("cust_params"), "&"+key+"=", "&"));
					}else
						actualMap.put(key, "NA");
				}
			}

		}else{
			generateInfoReport("No Ad call observed");
		}
	}

	private String adsLogin(String details, String url){
		if(details.contains(",")){
			String[] user = details.split(",");
			login(user[0],user[1]);
		}else if(details.equalsIgnoreCase("anon")){
			login("infosession33","medscape");
			getDriver().manage().deleteAllCookies();
			url = url+"?faf=1";
		}
		
		return url;
	}
	
	private void scroll(){
		jse = (JavascriptExecutor)getDriver();
		int height = Integer.parseInt(jse.executeScript("return document.body.scrollHeight").toString());
		while(height > 300){
			jse.executeScript("window.scrollBy(0, 300)");
			height -= 300;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}


	@Test(dataProvider = "dataProvider")
	public void test(String[] input){
		prepareExpectedMAP(input);
		if(input[2].equalsIgnoreCase("d")){
		
		}
		//	getDriver("4");
		else if(input[2].equalsIgnoreCase("m")){
			
		}
		//	getDriver("1");

		//Login based on the user given in input
		String URL = adsLogin(input[3], input[0]);
		

		if(input[1].equalsIgnoreCase("lazyload blb")){
			getDriver().get(URL);
			getServer().newHar();
			scroll();
		}else if(input[1].equalsIgnoreCase("page load")){
			getServer().newHar();
			getDriver().get(URL);
		}
		prepareActualMap();	


		int rows = outputSheet.getPhysicalNumberOfRows(), columnCount=0;
		rowExpectedValues = outputSheet.createRow(rows+2);
		rowActualValues = outputSheet.createRow(rows + 3);
		HSSFCell cell = null;

		for (String key : keysSet) {
			try{

				rowExpectedValues.createCell(columnCount).setCellValue(expectedMap.get(key));
				System.out.println("comparing "+key+":"+actualMap.get(key)+" "+expectedMap.get(key));
				try{
					cell = rowActualValues.createCell(columnCount);
					cell.setCellValue(actualMap.get(key));
					Assert.assertTrue(actualMap.get(key).equalsIgnoreCase(expectedMap.get(key)));
					//rowActualValues.createCell(columnCount).setCellValue(actualMap.get(key));
					generatePassReport("Passed for the Key "+key);
				}catch(AssertionError e){
					errorStyle(cell);
					//generateFailReport("Failed for the Key "+key+"Expected Value is "+expectedMap.get(key)
					//+", Actual value is "+actualMap.get(key));
				}
				columnCount++;
			}catch(Exception e){
				errorStyle(cell);
			}
		}
	}
	//@AfterMethod
	public void afterMethod(){
		getDriver().close();
	}

	@AfterClass
	public void afterClass() throws IOException{
		writeIntoExcel();
	}

}
