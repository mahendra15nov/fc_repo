package com.webmd.ads;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;

public class ProclivityVerification extends AdsCommon {

	By searchButtonMobile = By.xpath("//a[@class='mobile-search-button']/span");
	By serachBoxMobile = By.xpath("//input[@id='layer-search-input']");
	By searchSubmitButton = By.xpath("//div[@class ='searchContainer']//button[@type='submit']");
	By searchResultFirstItem = By.xpath("//p[@class='searchResultTitle']");

	@BeforeMethod(groups = { "proclivityProd" })
	public void beforeMethod() {
		login("proclivitytest@gmail.com", "medscape");
	}

	private String getASIDFromPageSource() {
		return StringUtils.substringBetween(getDriver().getPageSource().toString(), "masid\":\"", "\",");
	}

	private void scrollTillAdCall() {
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		boolean flag = false;

		do {
			jse.executeScript("window.scrollBy(0, 500)");
			try {
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Har har = getServer().getHar();
			har.getLog().getBrowser();
			List<HarEntry> res = har.getLog().getEntries();

			for (HarEntry harEntry : res) {
				String url = harEntry.getRequest().getUrl();
				if (url.contains("securepubads.g.doubleclick.net/gampad/ads?")) {
					flag = true;
				}
			}
		} while (!flag);
	}

	private void verifyLazyload() {
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		getServer().newHar();
		int height = Integer.parseInt(jse.executeScript("return document.body.scrollHeight").toString());

		boolean flag = false;
		int adCallCount = 0, transformCallCount = 0;

		// jse.executeScript("window.scrollBy(0,"+height/2+")");
		scrollTillEnd();
		try {
			Thread.sleep(500);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Har har = getServer().getHar();
		har.getLog().getBrowser();
		List<HarEntry> res = har.getLog().getEntries();
		String prev_scp = null, response = null;

		// The below logic can be removed if priority logic implemented

		for (HarEntry harEntry : res) {
			String url = harEntry.getRequest().getUrl();
			if (url.contains("securepubads.g.doubleclick.net/gampad/ads?"))
				adCallCount++;
			else if (url.contains("api.medscape.com/adpredictionservice/transform"))
				transformCallCount++;
		}

		try {
			Assert.assertEquals(adCallCount, transformCallCount);
			generatePassReport("Count of Lazyload ads and transform calls matched");
		} catch (AssertionError e) {
			generateFailReport("Count of Lazyload Ad calls is : " + adCallCount + " and count of transform calls is "
					+ transformCallCount);
		}

		// Commenting the ASID validation in transform call as priority logic
		// not yet implemented

		/*
		 * for (HarEntry harEntry : res) { String url =
		 * harEntry.getRequest().getUrl();
		 * if(url.contains("securepubads.g.doubleclick.net/gampad/ads?")){
		 * adCallCount++; System.out.println(url); generateInfoReport(
		 * "Lazyload ad call observed");
		 * 
		 * List<HarNameValuePair> queryParams =
		 * harEntry.getRequest().getQueryString(); for (HarNameValuePair
		 * harNameValuePair : queryParams) {
		 * System.out.println(harNameValuePair.getName().trim() + " : " +
		 * harNameValuePair.getValue().trim()); if
		 * (harNameValuePair.getName().trim().equalsIgnoreCase("prev_scp"))
		 * prev_scp += harNameValuePair.getValue();
		 * 
		 * }
		 * 
		 * }else
		 * if(url.contains("api.medscape.com/adpredictionservice/transform")){
		 * transformCallCount++; flag = true; //generatePassReport(
		 * "Transform call made"); response +=
		 * harEntry.getResponse().getContent().getText(); } }
		 * 
		 * if(flag){ generatePassReport("Transform call tracked on page");
		 * 
		 * try{ Assert.assertEquals(adCallCount, transformCallCount);
		 * generatePassReport(
		 * "Count of Lazyload ads and transform calls matched");
		 * }catch(AssertionError e){ generateFailReport(
		 * "Count of Lazyload Ad calls is : "+adCallCount+
		 * " and count of transform calls is "+transformCallCount); }
		 * 
		 * System.out.println("Response is "+response); String[]
		 * asidFromTransform = StringUtils.substringsBetween(response,
		 * "\"a\":\"", "\",");
		 * 
		 * for(String asid: asidFromTransform){ try{
		 * Assert.assertTrue(prev_scp.contains(asid)); generatePassReport(
		 * "ASID match from transform call and Ads call"); }catch(AssertionError
		 * e){ generateFailReport("Trying to search "+asidFromTransform+" in "
		 * +prev_scp); } } }else generateFailReport("No Transform call Observed"
		 * );
		 */
	}

	@Test(dataProvider = "dataProvider", groups = { "proclivityProd"})
	public void test(String searchText) {
		getDriver().findElement(searchButtonMobile).click();
		getDriver().findElement(serachBoxMobile).sendKeys(searchText);

		getDriver().findElement(searchSubmitButton).click();
		getServer().newHar();
		getDriver().findElement(searchResultFirstItem).click();
		String asid = getASIDFromPageSource();
		String prev_scp = getSpecificKeyFromSecurePubadCall("prev_scp");

		if (asid.length() > 0)
			generatePassReport("ASID tracked under pagesource");
		else
			generateFailReport("ASID not tracked in page source");

		if (prev_scp.contains("asid"))
			generatePassReport("ASID tracked in Ad call");
		else
			generateFailReport("ASID not tracked in Ad call");

		/*
		 * try{ Assert.assertTrue(prev_scp.contains(asid)); generatePassReport(
		 * "Page load ad call has ASID match from pagesource");
		 * }catch(AssertionError e){ generateFailReport(
		 * "Trying to check availability of "+asid+" in "+prev_scp); }
		 */
		generateInfoReport("validating lazyload ad");
		verifyLazyload();
		/*
		 * getServer().newHar();
		 * 
		 * scrollTillAdCall(); String response; try{ verifySpecificCallPresence(
		 * "api.medscape.com/adpredictionservice/transform"); response =
		 * getResponseForSpecificCall("transform"); asid =
		 * StringUtils.substringBetween(response, "\"a\":\"", "\","); prev_scp =
		 * getSpecificKeyFromSecurePubadCall("prev_scp"); try{
		 * Assert.assertTrue(prev_scp.contains(asid)); generatePassReport(
		 * "ASID match from transform call to AD call"); }catch(AssertionError
		 * e){ generateFailReport("Trying to Search "+asid+" in "+prev_scp); }
		 * }catch(Exception e){ generateFailReport(
		 * "No Transform call observed for lazyload ad call"); }
		 */

	}

	@DataProvider
	public String[][] dataProvider() {
		// return new String[][] { { "A New Consequence of the Opioid Epidemic"
		// } };
		return new String[][] { { "https://reference.medscape.com/drug/carospir-aldactone-spironolactone-342407" } };
	}

}
