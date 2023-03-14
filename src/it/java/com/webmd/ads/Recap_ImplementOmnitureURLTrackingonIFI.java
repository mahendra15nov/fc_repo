package com.webmd.ads;

import java.util.List;

import javax.swing.text.html.HTMLEditorKit.LinkController;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;


/*
 * PPE-184267: ReCAP | Implement Omniture URL Tracking on IFI
 */

public class Recap_ImplementOmnitureURLTrackingonIFI extends AdsCommon{

	By iframePos909 = By.xpath("//div[@id='ads-pos-909']//iframe");
	By iframePos1909 = By.xpath("//div[@id='ads-pos-1909']//iframe");

	private void checkOmniture(int linkNumber, String linkText){
		generateInfoReport("Validating Omniture upon clicking the link "+linkNumber);
		boolean flag = false;


		Har har = getServer().getHar();
		List<HarEntry> entries = har.getLog().getEntries();
		for (HarEntry entry : entries) {
			//generateInfoReport(entry.getRequest().getUrl());
			if (entry.getRequest().getUrl().contains("ssl.o.webmd.com/b/ss/webmddev")) {
				generatePassReportWithNoScreenShot("SSL call triggered upon clicking link from IFI");
				flag = true;
				List<HarNameValuePair> queryParams = entry.getRequest().getQueryString();
				for (HarNameValuePair harNameValuePair : queryParams) {
					switch(harNameValuePair.getName()){
					case "mmodule" : 
						try{
							Assert.assertTrue(harNameValuePair.getValue().contains("ares"));
							generatePassReportWithNoScreenShot("Module value tracked properly upon clicking link "+linkNumber);
						}catch(AssertionError e){
							generateFailReport("Module value tracked as "+harNameValuePair.getValue()
							+"instad of ares, for link "+linkNumber);
						}
						break;
					case "mlink" :
						try{
							Assert.assertTrue(harNameValuePair.getValue().contains("ifilink"+linkNumber));
							generatePassReportWithNoScreenShot("Link ID tracked properly upon clicking link "+linkNumber);
						}catch(AssertionError e){
							generateFailReport("Link ID tracked as "+harNameValuePair.getValue()+", for link "+linkNumber);
						}
						break;
					case "lnktxt" :
						String actualLinkText = null;
						try{
							actualLinkText = harNameValuePair.getValue().toLowerCase();
							Assert.assertTrue(actualLinkText.contains(linkText.toLowerCase()));
							generatePassReportWithNoScreenShot("Link Text tracked properly upon clicking link "+linkNumber);
						}catch(AssertionError e){
							generateFailReport("Link Text tracked as "+actualLinkText+", for link "+linkNumber
									+"Expected link is "+linkText);
						}
						break;
					case "exiturl" :
						try{
							switch (linkNumber){
							case 1 : Assert.assertTrue(harNameValuePair.getValue().contains("http://www.google.com"));
							break;
							case 2 : Assert.assertTrue(harNameValuePair.getValue().contains("http://www.yahoo.com"));
							break;
							case 3 : Assert.assertTrue(harNameValuePair.getValue().contains("http://www.medscape.com"));
							}
							generatePassReportWithNoScreenShot("Link Destination (exiturl) tracked properly upon clicking link "+linkNumber);
						}catch(AssertionError e){
							generateFailReport("Link Destination (exiturl) tracked as "+harNameValuePair.getValue()+", for link "+linkNumber);
						}
						break;

					}
				}
			}

		}
		try{
			Assert.assertTrue(flag);
			generatePassReport("Omniture call tracked and validated the attributes for link: "+linkNumber);
		}catch(AssertionError e){
			generateFailReport("No Omniture call getting tracked upon clicking the link "+linkNumber);
		}
	}

	//@Test (groups = {"recap"})
	public void testOmniture(){
		login();
		getDriver().get("https://reference.qa01.medscape.com/recap/896529?faf=1");
		String mainWindow;
		WebElement position;
		try{
			if(breakPoint.contains("1"))
				position = getDriver().findElement(By.xpath("//div[@id='ads-pos-1909']"));
			else
				position = getDriver().findElement(By.xpath("//div[@id='ads-pos-909']"));
		}catch(NoSuchElementException e){
			generateInfoReport("Ad not shown on page");
		}
		try{
			mainWindow = getDriver().getWindowHandle();
			if(breakPoint.contains("1")){
				waitForElement(iframePos1909);
				position = getDriver().findElement(By.xpath("//div[@id='ads-pos-1909']"));
				getDriver().switchTo().frame(getDriver().findElement(iframePos1909));
			}else{
				waitForElement(iframePos909);
				position = getDriver().findElement(By.xpath("//div[@id='ads-pos-909']"));
				getDriver().switchTo().frame(getDriver().findElement(iframePos909));
			}
			try{
				int linkCount = 1;
				generateInfoReport("Total Number of links available are "+getDriver().findElements(By.xpath("//ul/li/a")).size());
				for(WebElement ele : getDriver().findElements(By.xpath("//ul/li/a"))){
					System.out.println("Clicking the link"+ele.getText());
					getServer().newHar();
					try{
						ele.click();
						generateInfoReport("Number of windows "+getDriver().getWindowHandles().size());
					}catch(Exception e){
						System.out.println("error while clicking the link");
					}
					Thread.sleep(3000);
					checkOmniture(linkCount, "");
					linkCount++;
					//getStdParmValue("", "");

				}
			}catch(NoSuchElementException e){
				System.out.println("No Li shown");
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
			getDriver().switchTo().window(mainWindow);
		}catch(NoSuchElementException e){
			System.out.println("No iframe shown");
		}
	}

	@Test (groups = {"recap"})
	public void test() throws InterruptedException{
		login();
		getDriver().get("https://reference.qa01.medscape.com/recap/896529?faf=1");
		WebElement position, iframe;
		String adPos = null, mainWindow;
		String linkName;
		int linkNumber = 0;

		try{
			if(breakPoint.equals("1")){
				adPos = "//div[contains(@id,'ads-pos-1909')]";
				position = getDriver().findElement(By.xpath(adPos));
				iframe = getDriver().findElement(By.xpath(adPos+"//iframe"));
			}else{
				adPos = "//div[contains(@id,'ads-pos-909')]";
				position = getDriver().findElement(By.xpath(adPos));
				iframe = getDriver().findElement(By.xpath(adPos+"//iframe"));
			}
			
			mainWindow = getDriver().getWindowHandle();
			getDriver().switchTo().frame(iframe);
			
			for(WebElement link : getDriver().findElements(By.xpath("//ul/li/a"))){
				linkNumber++;
				linkName = link.getText();
				getServer().newHar();
				try{
					Actions actions = new Actions(getDriver());
					actions.moveToElement(link).click();
					actions.perform();
					Thread.sleep(2000);
					checkOmniture(linkNumber, linkName);
					
				}catch(Exception e){
					e.printStackTrace();
					generateFailReport("Unable to click the link "+linkName);
				}
				
				getDriver().switchTo().window(mainWindow);
				getDriver().switchTo().frame(iframe);
				
			}
		}catch(NoSuchElementException e){
			generateInfoReport("Ad position not loaded on page"+adPos);
		}

	}



}