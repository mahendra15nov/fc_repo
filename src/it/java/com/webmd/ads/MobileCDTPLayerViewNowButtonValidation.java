package com.webmd.ads;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;


/*
 * PPE-180445: Mobile CDTP view now CTA does not work
 * Requirements: The user should be able to click the "View Now" CTA to open the program
 * 
 * This Feature is specific to Mobile Device also update config.properties file as below, as CDTP layer will be loaded before page load completely
 * # 
Available Options - NONE, NORMAL
PageLoadStrategy =NONE

#Seconds
waitForElementTimeOut = 30
waitForPageLoadTimeOut =25
 */

public class MobileCDTPLayerViewNowButtonValidation extends AdsCommon{
	
	private boolean checkPresenceOfCDTPLayer(){
		try{
			waitForElement(By.xpath("//div[@class='sd-layer']"));
			getDriver().findElement(By.xpath("//div[@class='sd-layer']")).isDisplayed();
			generateInfoReport("CDTP layer shown");
			return true;
		}catch(NoSuchElementException e){
			generateInfoReport("CDTP layer not shown");
			return false;
		}
	}
	
	private void clickViewNowButton(){
		String titleBeforeClick = null, titleAfterClick = null;
		try{
			titleBeforeClick = getDriver().getTitle();
			getDriver().findElement(By.xpath("//a[@class='btn-link btn-link-blue']")).click();
			generateInfoReport("Clicked on View Now Button");
			waitForPageLoaded();
			titleAfterClick = getDriver().getTitle();
			Assert.assertNotEquals(titleBeforeClick, titleAfterClick);
			generateInfoReport("Navigated to third party upon clicking View Now"+titleAfterClick);
		}catch(NoSuchElementException e){
			generateInfoReport("View Now button not available");
		}catch(AssertionError e){
			generateFailReport("Not navigated to third party after clicking on view now, Title is "+titleAfterClick);
		}
		catch(Exception e){
			e.printStackTrace();
			generateFailReport("View Now button not available");
		}
	}
	
	private void closeCDTPLayer(){
		String titleBeforeClick = null, titleAfterClick = null;
		try{
			titleBeforeClick = getDriver().getTitle();
			getDriver().findElement(By.xpath("//div[@class='close']/a[contains(text(),'Close')]")).click();
			titleAfterClick = getDriver().getTitle();
			Assert.assertEquals(titleBeforeClick, titleAfterClick);
			generatePassReportWithNoScreenShot("CDTP layer closed upon clicking the Close button");
		}catch(AssertionError e){
			generateInfoReport("Title after clicking view button is "+titleBeforeClick);
		}catch(NoSuchElementException e){
			generateFailReport("Close button not shown on CDTP layer");
		}
	}
	
	private void leaveLayerTillClose(){
		String titleBeforeClick = null, titleAfterClick = null;
		try{
			titleBeforeClick = getDriver().getTitle();
			try{
				Thread.sleep(5000);
			}catch(Exception e){
				e.printStackTrace();
			}
			generateInfoReport("Waited for 5 seconds");
			waitForPageLoaded();
			titleAfterClick = getDriver().getTitle();
			Assert.assertNotEquals(titleBeforeClick, titleAfterClick);
			generateInfoReport("Navigated to third party after CDTP layer getting closed by itself"+titleAfterClick);
		}catch(AssertionError e){
			generateFailReport("Not navigated to third party after layer getting closed itself "+titleAfterClick);
		}
	}
	
	@BeforeClass(alwaysRun = true)
	public void beforeClass(){
		login("infosession50","medscape");
	}
	
	@Test(dataProvider = "dataProvider")
	public void validateViewNowButoon(String URL){
		getDriver().get(URL);
		int count = 0;
		boolean cdtpLayer = false;
		do{
			count++;
		if(checkPresenceOfCDTPLayer()){
			clickViewNowButton();
			cdtpLayer = true;
		}else
			getDriver().navigate().refresh();
		}while(!cdtpLayer && count < 5);
		
		if(cdtpLayer)
			generateInfoReport("CDTP layer shown");
		else
			generateBoldReport("CDTP layer not shown");
	}
	
	@Test(dataProvider = "dataProvider")
	public void validateCloseCDTP(String URL){
		getDriver().get(URL);
		int count = 0;
		boolean cdtpLayer = false;
		
		
		do{
			count++;
		if(checkPresenceOfCDTPLayer()){
			closeCDTPLayer();
			cdtpLayer = true;
		}else
			getDriver().navigate().refresh();
		}while(!cdtpLayer && count < 5);
		if(!cdtpLayer)
			generateSkipReport("CDTP Layer not shown");
	}
	
	@Test(dataProvider = "dataProvider")
	public void validateLeavingCDTPLayer(String URL){
		getDriver().get(URL);
		int count = 0;
		boolean cdtpLayer = false;
		
		
		do{
			count++;
		if(checkPresenceOfCDTPLayer()){
			leaveLayerTillClose();
			cdtpLayer = true;
		}else
			getDriver().navigate().refresh();
		}while(!cdtpLayer && count < 5);
		if(!cdtpLayer)
			generateSkipReport("CDTP Layer not shown");
	}
	
	@Test(dataProvider = "dataProvider")
	public void navigateBackToMedscape(String URL){
		String titleBeforeClick = null, titleAfterClick = null;
		getDriver().get(URL);
		titleBeforeClick = getDriver().getCurrentUrl();
		int count = 0;
		boolean cdtpLayer = false;
		
		
		do{
			count++;
		if(checkPresenceOfCDTPLayer()){
			cdtpLayer = true;
			clickViewNowButton();
			waitForPageLoaded();
			try{
				By returnLink = By.xpath("//a[contains(text(),'Return to Medscape Content') and @class='return-link']");
				waitForElement(returnLink);
				getDriver().findElement(returnLink).click();
				waitForPageLoaded();
				titleAfterClick = getDriver().getTitle();
				Assert.assertEquals(titleAfterClick, titleBeforeClick);
				generatePassReportWithNoScreenShot("Navigated back to article page from third party site");
			}catch(AssertionError e){
				generateFailReport("Not navigated back to article page from third party site, current page is "+titleAfterClick);
			}catch(NoSuchElementException e){
				generateFailReport("There is no option to navigate back to Medscape");
			}
		}else
			getDriver().navigate().refresh();
		}while(!cdtpLayer && count < 5);
		if(!cdtpLayer)
			generateSkipReport("CDTP Layer not shown");
	}
	
	@DataProvider
	public  String[][] dataProvider() {
		/*if(env.contains("staging"))
			return XlRead.fetchDataExcludingFirstRow("AdsSanity.xls", "Staging");
		else if(env.contains("qa01"))
			return XlRead.fetchDataExcludingFirstRow("AdsSanity.xls", "QA01");
		else
			return XlRead.fetchDataExcludingFirstRow("AdsSanity.xls", "PROD");*/
		return new String[][]  {
			//{"https://www.dev01.medscape.com/viewarticle/895141?faf=1"}, 
			//{"https://www.dev01.medscape.com/viewarticle/894884?faf=1"},
			{"https://www.dev01.medscape.com/viewarticle/894519?faf=1"}
			};
	}

}
