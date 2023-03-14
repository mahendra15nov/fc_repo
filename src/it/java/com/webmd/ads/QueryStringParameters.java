package com.webmd.ads;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class QueryStringParameters extends AdsCommon {
	static List<String> keysSet = new ArrayList();
	static FileOutputStream outFile;
	static File file = new File(ReadProperties.projectLocation + "/TestOutput/AdCallValidationResults_"
			+ LocalDate.now() + "_" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH_mm_ss")) + ".xls");

	static Map<String, String> expectedMap = new LinkedHashMap<String, String>();
	static Map<String, String> actualMap = new LinkedHashMap<String, String>();
	static List<String> expectedKeys;

	static HSSFWorkbook ouputWorkBook;

	static HSSFSheet outputSheet;
	static HSSFRow rowActualValues, rowExpectedValues, row;

	JavascriptExecutor jse;

	@BeforeClass(groups = { "QueryString" })
	public void beforeClass() {
		String input[][] = XlRead.fetchData("TestData/Test.xls", "Sheet4");
		ouputWorkBook = new HSSFWorkbook();
		outputSheet = ouputWorkBook.createSheet("Results");
		row = outputSheet.createRow(0);

		for (int i = 0; i < input[0].length; i++) {
			row.createCell(i).setCellValue(input[0][i]);
			keysSet.add(input[0][i]);
		}
		jse = (JavascriptExecutor) getDriver();
	}

	/*
	 * Writing output to the file
	 */
	public static void writeIntoExcel() throws IOException {
		outFile = new FileOutputStream(file);
		ouputWorkBook.write(outFile);
		System.out.println("File Created");
	}

	@DataProvider
	public String[][] dataProvider() {
		return XlRead.fetchDataExcludingFirstRow("TestData/Test.xls", "Sheet4");
	}

	public void prepareActualMap() {
		actualMap.clear();
		actualMap.put("URL", expectedMap.get("URL"));
		actualMap.put("TriggerEvent", expectedMap.get("TriggerEvent"));
		actualMap.put("DeviceType", expectedMap.get("DeviceType"));
		actualMap.put("User", expectedMap.get("User"));
		System.out.println("Actual MAP");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Har har = getServer().getHar();
		har.getLog().getBrowser();
		List<HarEntry> res = har.getLog().getEntries();

		for (HarEntry harEntry : res) {
			String url = harEntry.getRequest().getUrl();
			System.out.println(url);
			if (url.contains("securepubads.g.doubleclick.net/gampad/ads?")) {
				List<HarNameValuePair> queryParams = harEntry.getRequest().getQueryString();
				for (HarNameValuePair harNameValuePair : queryParams) {
					actualMap.put(harNameValuePair.getName(), harNameValuePair.getValue());
				}
				break;
			}
		}
	}



	@Test(dataProvider = "dataProvider", groups = { "QueryString" })
	public void test(String[] input) {
		if (input[2].equalsIgnoreCase("d"))
			// getDriver("4");
			// else if(input[2].equalsIgnoreCase("m"))
			// getDriver("1");

			// Login based on the user given in input
			// adsLogin(input[3]);

			if (input[1].equalsIgnoreCase("lazyloadblb")) {
			getDriver().get(input[0]);
			getServer().newHar();
			jse.executeScript("window.scrollBy(0, document.body.scrollHeight)");
			} else if (input[1].equalsIgnoreCase("pageload")) {
			getServer().newHar();
			System.out.println("Opening " + input[0]);
			getDriver().get(input[0]);
			}
		prepareActualMap();

		int rows = outputSheet.getPhysicalNumberOfRows(), columnCount = 0;
		rowExpectedValues = outputSheet.createRow(rows + 1);
		rowActualValues = outputSheet.createRow(rows + 2);

		for (String key : expectedMap.keySet()) {
			try {
				System.out.println("expected key set: " + key);
				rowExpectedValues.createCell(columnCount).setCellValue(expectedMap.get(key));
				System.out.println("comparing " + actualMap.get(key) + " " + expectedMap.get(key));
				try {
					Assert.assertTrue(actualMap.get(key).equalsIgnoreCase(expectedMap.get(key)));
					rowActualValues.createCell(columnCount).setCellValue(actualMap.get(key));
					generatePassReport("Passed for the Key " + key);
				} catch (AssertionError e) {
					// generateFailReport("Failed for the Key "+key+"Expected
					// Value is "+expectedMap.get(key)
					// +", Actual value is "+actualMap.get(key));
				}
				columnCount++;
			} catch (Exception e) {
			}
		}

	}

	@AfterClass(groups = { "QueryString" })
	public void afterClass() throws IOException {
		writeIntoExcel();
	}

}
