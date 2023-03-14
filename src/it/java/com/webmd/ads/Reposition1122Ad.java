package com.webmd.ads;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;

public class Reposition1122Ad extends AdsCommon{

	@BeforeClass
	public void beforeClass(){
		getDriver();
		login("infosession33", "medscape");
	}

	By validate1122 = By.xpath("//div[@id='article-content']/p[1]/following-sibling::*[position()=1][name()='div']/following-sibling::*[position()=1][name()='p']");
	By pos1122 = By.xpath("//div[@id='ads-pos-1122']");
	By preceding1122 = By.xpath("//div[@id='ads-pos-1122']/preceding-sibling::*[position()=1]");
	By following1122 = By.xpath("//div[@id='ads-pos-1122']/following-sibling::*[position()=1]");

	public String executeConsoleCommand(String command){
		String response = null;

		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		try{
			response = jse.executeScript("return "+command).toString();
		}catch(WebDriverException e){
			e.printStackTrace();
			response = command+" is not defined";
		}
		return response;
	}

	private boolean validateWithRespectToProd(){
		String qaPrecdingTag, qafollowingTag, prodPrecedingTag, prodFollowingTag, prodURL;

		try{
			qaPrecdingTag = getDriver().findElement(preceding1122).getTagName();
			qafollowingTag = getDriver().findElement(following1122).getTagName();
		}catch(NoSuchElementException e){
			generateInfoReport("There is no 1122 loaded on page");
			return true;
		}

		prodURL = getDriver().getCurrentUrl().replace(env, "")+"?faf=1";

		getDriver().get(prodURL);

		prodPrecedingTag = getDriver().findElement(preceding1122).getTagName();
		prodFollowingTag = getDriver().findElement(following1122).getTagName();

		generateInfoReport("Tag Preceding to 1122 in PROD: "+prodPrecedingTag+
				"\nTag Preceding to 1122 in QA is: "+qaPrecdingTag+
				"\nTag Following to 1122 in PROD is:"+prodFollowingTag+
				"\nTag Following to 1122 in QA is: "+qafollowingTag);

		if(qaPrecdingTag.equals(prodPrecedingTag)&&qafollowingTag.equals(prodFollowingTag))
			return true;
		else
			return false;
	}
	@Test (dataProvider = "dataProvider")
	public void validateDTMFlag(String URL){//String URL (dataProvider = "viewArticles")
		getDriver().get(URL);
		String response = executeConsoleCommand("textDriverOptimized");
		String is_article = executeConsoleCommand("_isAnArticle");
		boolean flag = false;
		System.out.println(is_article);
		String pageSource = getDriver().getPageSource();

		if((pageSource.contains("ssp=2;")|pageSource.contains("ssp\":\"2\""))&&is_article.contains("true")){
			generateInfoReport("Page is arcticle and comes under Cardiology");
			try{
				Assert.assertTrue(response.equalsIgnoreCase("true")||response.equalsIgnoreCase("false"));
				generateInfoReport("Flag output from console is "+response);
				//In this case 1122 can be after first content tag, hence pass if after first contnet tag
				try{
					getDriver().findElement(validate1122).isDisplayed();
					generatePassReportWithNoScreenShot("1122 loaded after the first content tag in the page");
				}catch(NoSuchElementException e){
					generateInfoReport("Validating with respect to PROD");
					try{
						Assert.assertTrue(validateWithRespectToProd());
						generatePassReportWithNoScreenShot("Conditon similar in PROD and QA");
					}catch(AssertionError e1){
						generateFailReport("Condition not similar to PROD and QA");
					}
				}
			}catch(AssertionError e){
				generateFailReport("Expected output is true/false. Actual output is"+response);
			}
		}else{
			generateInfoReport("page is not related to cardiology/page is not article");
			try{
				Assert.assertTrue(response.contains("is not defined"));
				generateInfoReport("Flag output from console is "+response);
				//Here 1122 shouldn't be there after first p tag hence if webelement displayed then fail
				try{
					getDriver().findElement(validate1122).isDisplayed();
					generateFailReport("1122 loaded after the first content tag in the page");
				}catch(NoSuchElementException e){
					generatePassReportWithNoScreenShot("1122 not loaded after the first content tag");
					try{
						Assert.assertTrue(validateWithRespectToProd());
						generatePassReportWithNoScreenShot("Conditon similar in PROD and QA");
					}catch(AssertionError e1){
						generateFailReport("Condition not similar to PROD and QA");
					}
				}
			}catch(AssertionError e){
				generateFailReport("Expected output is 'textDriverOptimed is not defined'. Actual output is"+response);
			}
		}
	}

	@DataProvider
	public  String[][] dataProvider() {
		return XlRead.fetchDataExcludingFirstRow("TestData/DTMTestData.xls", "Sheet1");
	}
	
	@DataProvider
	public  String[][] dataProviderForMediaNetRefresh() {
		//return XlRead.fetchDataExcludingFirstRow("TestData/DTMTestData.xls", "Sheet1");
		return new String[][]  {
		{"https://reference.qa01.medscape.com/viewarticle/842254"}, 
		{"https://www.qa01.medscape.com/viewarticle/891148"},
		{"https://www.qa01.medscape.com/viewarticle/895129"},
		{"https://www.qa01.medscape.com/viewarticle/858206"}
		};
	}
	
	@Test (dataProvider = "dataProviderForMediaNetRefresh")
	public void verifyMedianetRefresh(String URL){
		getDriver().get(URL);
		String response = executeConsoleCommand("textDriverOptimized");
		String is_article = executeConsoleCommand("_isAnArticle");
		boolean flag = false;
		System.out.println(is_article);
		String pageSource = getDriver().getPageSource();
		int count = 0;

		if((pageSource.contains("ssp=2;")|pageSource.contains("ssp\":\"2\""))&&is_article.contains("true")){
			generateInfoReport("Page is arcticle and comes under Cardiology");
			try{
				Assert.assertTrue(response.equalsIgnoreCase("true")||response.equalsIgnoreCase("false"));
				generateInfoReport("Flag output from console is "+response);
				//In this case 1122 can be after first content tag, hence pass if after first contnet tag
				try{
					getDriver().findElement(validate1122).isDisplayed();
					generatePassReportWithNoScreenShot("1122 loaded after the first content tag in the page");
					scrollToWebElement(getDriver().findElement(By.xpath("//div[@id='ads-pos-1122']")));
					do{
						getServer().newHar();
						try{
							Thread.sleep(5000);
							count++;
						}catch(Exception e){
							e.printStackTrace();
						}
					}while(!verifySpecificCallPresence("securepubads.g.doubleclick.net/gampad/ads?") && count < 15);
					if(count == 15)
						generateSkipReport("No media net refresh happen");
					else{
						Assert.assertTrue(getDriver().findElement(validate1122).isDisplayed());
						generatePassReportWithNoScreenShot("Ad position has not changed in media net refresh");
					}
				}catch(NoSuchElementException e){
					generateSkipReport("1122 not repositioned for this article");
					generatePassReportWithNoScreenShot("Position of 1122 has not changed in media net refresh");
				}catch(AssertionError e){
					generateFailReport("Position of 1122 has changed in media net refresh");
				}
			}catch(AssertionError e){
				generateInfoReport("Flag output from console is "+response);
			}

		}else
			generateSkipReport("Article page didnthave Text Driver Optimized");
	}
}
