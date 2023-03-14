package com.webmd.ads;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.ReadProperties;
import com.webmd.general.common.XlRead;

//PPE-188486 -This file is related to resolving white space for right rail ads
/*
 * This feature is limited to Desktop
 * 
 */

public class RightRailWhiteSpaceValidation extends AdsCommon{
	By pageLoadedRightRailContainer = By.xpath("//div[contains(@id,'container') and contains(@id,'right')]");
	By pageLoadedRightRailAds = By.xpath("//div[contains(@id,'container') and contains(@id,'right')]//div[contains(@id,'ads-pos')]");
	By LazyLoadRightRailContainer = By.xpath("//div[@id='ll-container']");
	By LazyLoadRightRailAds = By.xpath("//div[@id='ll-container']/div[contains(@id,'ll-ads-container-')]/div[contains(@id,'ad')]");

	private int getConsolePageHeight(){
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		//String height = jse.executeScript("return document.body.scrollHeight").toString();
		String height = jse.executeScript("return $('#article-content').height();").toString();
		generateInfoReport("Left content height is "+height);
		if(height.contains("."))
			return Integer.parseInt(height.split(".")[0]);
		else if(height.contains("p"))
			return Integer.parseInt(height.split("p")[0]);
		else
			return Integer.parseInt(height);
	}

	private int getArticleContentHeight(){
		String height = getDriver().findElement(By.xpath("//div[@id='article-content' or @class='article-content']")).getCssValue("height");

		if(height.contains("."))
			return Integer.parseInt(height.split("\\.")[0]);
		return Integer.parseInt(height.split("p")[0]);
	}

	private void validatePageLoadRightRail(){
		try{
			Assert.assertTrue(getDriver().findElement(pageLoadedRightRailContainer).isDisplayed());
			generateInfoReport("Right rail container loaded on page");
			Assert.assertTrue(getDriver().findElements(pageLoadedRightRailAds).size() > 0);
			generateInfoReport("Right rail ads loaded in page load");
		}catch(AssertionError e){
			generateFailReport("Page loaded Right rail ads not loaded properly");
		}
	}

	private boolean validateLazyLoadRightRail(){
		try{
			getDriver().findElement(LazyLoadRightRailContainer).isDisplayed();
			generateInfoReport("Lazyload container is loaded on page");
			getDriver().findElement(LazyLoadRightRailAds).isDisplayed();
			generateInfoReport("Lazyload ads loaded on page");
			return true;
		}catch(NoSuchElementException e){
			generateInfoReport("Lazy load ads not loaded on page");
			return false;
		}
	}


	/*
	 * If page length is less than threshold only lazyload container should be there 
	 * else scroll the page lazyload right rail ads should also load
	 */

	@DataProvider
	public  String[][] dataProvider() {
		return XlRead.fetchDataExcludingFirstRow("TestData/rightRailSpace.xls", "Sheet1");
	}

	static HSSFWorkbook ouputWorkBook;
	static HSSFSheet outputSheet;
	static HSSFRow row;
	static FileOutputStream outFile;
	static File file = new File(ReadProperties.projectLocation+
			"/TestOutput/RightRailAdValidation_" + LocalDate.now() +"_"
			+LocalTime.now().format(DateTimeFormatter.ofPattern("HH_mm_ss"))+ ".xls");

	@BeforeClass
	public void beforeClass(){
		login("infosession50","medscape");
		ouputWorkBook = new HSSFWorkbook();
		outputSheet = ouputWorkBook.createSheet("Results");
		row = outputSheet.createRow(0);
		row.createCell(0).setCellValue("URL");
		row.createCell(1).setCellValue("QA Height");
		row.createCell(2).setCellValue("QA Has LL-Container?");
		row.createCell(3).setCellValue("QA ENV Has lazyload rightrail?");
		row.createCell(4).setCellValue("PROD Height");
		row.createCell(5).setCellValue("PROD Has LL-Container?");
		row.createCell(6).setCellValue("PROD Has lazyload rightrail?");
	}
	@AfterClass
	public static void writeIntoExcel() throws IOException {
		outFile = new FileOutputStream(file);
		ouputWorkBook.write(outFile);
		System.out.println("File Created");
	}

	@Test(dataProvider = "dataProvider")
	public void validateWithRespectToPROD(String URL){
		getDriver().get(URL);
		boolean hasNextPage;
		if(!getDriver().getTitle().contains("Page Not Found")){
			do{
				hasNextPage = false;
				scrollTillEnd();
				scrollBottomToTop();

				int rows = outputSheet.getPhysicalNumberOfRows();

				row = outputSheet.createRow(rows+1);
				row.createCell(0).setCellValue(getDriver().getCurrentUrl());
				row.createCell(1).setCellValue(getConsolePageHeight());

				if(checkPresenceOfLLContainer())
					row.createCell(2).setCellValue("YES");
				else
					row.createCell(2).setCellValue("NO");

				if(validateLazyLoadRightRail())
					row.createCell(3).setCellValue("YES");
				else
					row.createCell(3).setCellValue("NO");

				validateProd(row.createCell(4), row.createCell(5), row.createCell(6));
				getDriver().navigate().back();
				try{
					WebElement next = getDriver().findElement(By.xpath("//div[@id='next-section']/a"));
					scrollToWebElement(next);
					next.click();
					generateInfoReport("Article has Next page, clicked on Next page");
					hasNextPage = true;
				}catch(Exception e){
					hasNextPage = false;
					generateInfoReport("No Next page avaialable");
				}
			}while(hasNextPage);
		}

	}

	private void validateProd(HSSFCell cell1, HSSFCell cell2, HSSFCell cell3){
		String URL = getDriver().getCurrentUrl().replace(env, "");
		getDriver().get(URL);
		scrollTillEnd();
		scrollBottomToTop();
		cell1.setCellValue(getConsolePageHeight());

		if(checkPresenceOfLLContainer())
			cell2.setCellValue("YES");
		else
			cell2.setCellValue("NO");

		if(validateLazyLoadRightRail())
			cell3.setCellValue("YES");
		else
			cell3.setCellValue("NO");
	}

	private boolean checkPresenceOfLLContainer(){
		try{
			getDriver().findElement(LazyLoadRightRailContainer).isDisplayed();
			generateInfoReport("LL container shown on page");
			return true;
		}catch(NoSuchElementException e){
			generateInfoReport("LL container not loaded on page");
			return false;
		}
	}

	private void verifyMediaNetRefresh(boolean isHightLessThanThreslhold){
		generateInfoReport("Validating Media net refresh");
		scrollBottomToTop();
		boolean hasMediaNetRefresh = false; 
		int count = 0;
		do{
			getServer().newHar();
			count++;
			try{
				Thread.sleep(5000);
			}catch(Exception e){
				e.printStackTrace();
			}
			if(verifySpecificCallPresence(""))
				hasMediaNetRefresh = true;
		}while(count < 20 && !hasMediaNetRefresh);

		if(hasMediaNetRefresh && isHightLessThanThreslhold){
			try{
				//In this case isHightLessThanThreslhold is true and expecting checkPresenceOfLLContainer return false
				Assert.assertEquals(isHightLessThanThreslhold, !checkPresenceOfLLContainer());
				generatePassReportWithNoScreenShot("No changes applied in media net refresh");
			}catch(AssertionError e){
				generateFailReport("Media net refresh has lazyload container for less height page");
			}
		}else if(hasMediaNetRefresh && !isHightLessThanThreslhold){
			try{
				//In this case isHightLessThanThreslhold is false and expecting checkPresenceOfLLContainer return true
				Assert.assertEquals(!isHightLessThanThreslhold, checkPresenceOfLLContainer());
				generatePassReportWithNoScreenShot("No changes applied in media net refresh");
			}catch(AssertionError e){
				generateFailReport("Media net refresh has no lazyload container for more height page");
			}
		}else
			generateInfoReport("No media net refresh on page");
	}

	@Test (dataProvider= "dataProvider")
	public void consolidatedTest(String URL){//String URL
		getDriver().get(URL);
		boolean hasNextPage;
		do{
			hasNextPage = false;
			if(getConsolePageHeight()<1400){
				try{
					Assert.assertFalse(checkPresenceOfLLContainer());
					generatePassReportWithNoScreenShot("LL container not loaded as article height is less than 1400");
				}catch(AssertionError e){
					generateFailReport("LL container loaded on page even length is less than 1400");
				}
				try{
					getServer().newHar();
					scrollTillEnd();
					generateInfoReport("Validating after scrolling the page");
					Assert.assertFalse(validateLazyLoadRightRail());
					generatePassReportWithNoScreenShot("No Lazyload ads loaded on page load");
					try{
						Assert.assertFalse(verifySpecificAdPresenceInSecurePubadCall("122"));
						Assert.assertFalse(verifySpecificAdPresenceInSecurePubadCall("910"));
						generatePassReportWithNoScreenShot("No lazyload ad call made");
					}catch(AssertionError e){
						generateFailReport("Lazy load ad call made"+getSpecificKeyFromSecurePubadCall("prev_scp"));
					}
				}catch(AssertionError e){
					generateFailReport("Lazy load right rail ads loaded on page, height of page is "+getConsolePageHeight());
				}
				//Calling media net refresh method with true value, as this section is related to less height
				verifyMediaNetRefresh(true);
			}else{
				generateInfoReport("Article Content is "+getArticleContentHeight());
				try{
					Assert.assertTrue(checkPresenceOfLLContainer());
					generatePassReportWithNoScreenShot("LL container loaded as article height is more than 1400");
				}catch(AssertionError e){
					generateFailReport("LL container not loaded on page even length is more than 1400: "+getDriver().getCurrentUrl());
				}
				//Calling media net refresh method with false value, as this section is related to more height
				verifyMediaNetRefresh(false);
			}
			try{
				WebElement next = getDriver().findElement(By.xpath("//div[@id='next-section']/a"));
				scrollToWebElement(next);
				next.click();
				generateInfoReport("Article has Next page, clicked on Next page");
				hasNextPage = true;
			}catch(Exception e){
				hasNextPage = false;
				generateInfoReport("No Next page avaialable");
			}
		}while(hasNextPage);
	}//rightRailSpace



}
