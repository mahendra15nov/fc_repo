package com.webmd.ads;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;

@Listeners(com.webmd.general.common.Listener.class)
public class AdsFeatures extends AdsCommon {

	JavascriptExecutor jse;

	@BeforeClass
	public void beforeClass() {
		jse = (JavascriptExecutor) getDriver();
	}

	String URL = "";
	Long heightOfPage, currentScrollHeight;

	private boolean scrollTillNextLazyLoad() {
		Long count = 0L;
		Long height;
		height = (Long) jse.executeScript("return document.body.scrollHeight")
				- (Long) jse.executeScript("return scrollY");
		getServer().newHar();
		try {
			count = height / 300;
			for (int i = 0; i < count; i++) {
				height = height - 300;
				jse.executeScript("window.scrollBy(0,300)");
				Thread.sleep(300);
				if (verifySpecificCallPresence("securepubads.g.doubleclick.net"))
					return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	private boolean verifyIsPositionInViewPort(String pos) {
		List<WebElement> positions = getDriver().findElements(By.xpath("//div[contains[@id, 'ads-pos" + pos + "']]"));

		for (WebElement postion : positions) {
			if (isVisibleInViewport(postion))
				return true;
		}
		return false;
	}

	@DataProvider
	public String[][] dataProviderLazyLoad() {

		if (breakPoint.equalsIgnoreCase("1"))
			return XlRead.fetchDataExcludingFirstRow("TestData/AdsRegresssion.xls", "LazyLoadMobile");
		else
			return XlRead.fetchDataExcludingFirstRow("TestData/AdsRegresssion.xls", "LazyLoad");
		/*
		 * return new String[][] { {"https://www.medscape.com/cardiology"},
		 * {"https://www.medscape.com/pediatrics"} };
		 */
	}

	// This test is to validate whether specific lazy loaded position loaded or
	// not in ad call

	@Test(dataProvider = "dataProviderLazyLoad")
	public void verifyLazyLoad(String URL, String lazyLoadAdPosition) {
		getDriver().get(URL);
		// String lazyLoadAdPosition = "";
		boolean flag = false;
		boolean lazyLoadOnPage = false;
		try {
			heightOfPage = (Long) jse.executeScript("return document.body.scrollHeight");
			currentScrollHeight = (Long) jse.executeScript("return scrollY");
			flag = true;

		} catch (Exception e) {
			generateInfoReport("unable to get height of page");
		}
		if (flag) {
			while (heightOfPage - currentScrollHeight > 300) {
				if (scrollTillNextLazyLoad()) {
					try {
						if (verifySpecificAdPresenceInSecurePubadCall(lazyLoadAdPosition)) {
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							verifyIsPositionInViewPort(lazyLoadAdPosition);
							lazyLoadOnPage = true;
							generatePassReport(lazyLoadAdPosition + " shown on page");
						}
					} catch (AssertionError e) {
						generateFailReport("Lazyload functionality issue \n" + e);
					} catch (Exception e) {

					}
				}
				currentScrollHeight = (Long) jse.executeScript("return scrollY");
			}

		}
		if (!lazyLoadOnPage)
			generateInfoReport("There are No lazyload ads on page");

	}

	public void verifyNoAdPages() {
		getDriver().get(URL);
		generateInfoReport("Verifying whether Ads not loaded on Non Ad pages for: " + URL);

		if (verifySpecificCallPresence("securepubads.g.doubleclick.net"))
			generateFailReport("Ad call observed on No Ad pages for: " + URL);
		else {
			generatePassReport("No Ad call observed on page");
			if (scrollTillNextLazyLoad())
				generateFailReport("Ad call shown on page upon scrolling");
			else
				generatePassReport("Ad call not shown upon scrolling the page");
		}
	}

	// @Test(dataProvider = "dataProviderTest")
	public void test(String[] data) {
		System.out.println("From Test");
		String[][] input = XlRead.fetchData("TestData/AdsRegresssion.xls", "Sheet3");

		for (int i = 0; i < data.length; i++)
			// for (int j= 0; j<input[0].length; j++)
			System.out.println(" " + data[i] + " ");

	}

	@DataProvider
	public String[][] dataProviderTest() {

		return XlRead.fetchData("TestData/AdsRegresssion.xls", "Sheet3");
		/*
		 * return new String[][] { {"https://www.medscape.com/cardiology"},
		 * {"https://www.medscape.com/pediatrics"} };
		 */
	}

}