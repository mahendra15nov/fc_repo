package com.webmd.ads;

import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import com.webmd.common.AdsCommon;
import com.webmd.general.common.UtilityMethods;

/*
 * PPE-164285: Create static page for Drug Monograph Announcement Ad Layer (MAN)
 */
/**
 * Drug Monograph MAN and Sticky layer verification,this is especially displays
 * in Jardiance drug pages.
 * 
 * @author amahendra Updated on 22/07/2019
 *
 */
@Listeners(com.webmd.general.common.Listener.class)
public class DrugMonograph extends AdsCommon {

	By layerAd = By.xpath("//div[@id='man-layer']");
	By layerAdCloseButton = By.xpath("//span[@class='man-close']");
	By nativeAdPosDesktop = By.xpath("//div[contains(@id,'421-sfp')]");
	By nativeAdPosMobile = By.xpath("//div[contains(@id,'1421-sfp')]");
	private By desktopFooterAd = By.xpath("//div[@id='ads-pos-145']");
	private By mobileFooterAd = By.xpath("//div[@id='ads-pos-1145']");
	private By footerAdOuterWrapper = By.xpath("//div[@id='stick-ad-outter-wrapper']");
	private By footerAdCloseButton = By.xpath("//span[@class='sticky-ad-close']");

	@BeforeMethod(groups = { "MANLayer", "AdsRegression", "Desktop", "MobileWeb" })
	public void beforeMethod() {
		login(getProperty("username"), getProperty("password"));
	}

	@AfterClass(groups = { "MANLayer", "AdsRegression", "Desktop", "MobileWeb" })
	public void closeBrowser() {
		getDriver().quit();
		getServer().stop();
	}

	/*
	 * This is the main test case which will call all the test cases related to
	 * layer Ad PPE-164541, PPE-164542, PPE-164544, PPE-164548
	 */

	@Test(dataProvider = "dataProvider", groups = { "MANLayer", "AdsRegression", "Desktop", "MobileWeb" })
	public void verifyDrugMonograph(String url) throws InterruptedException {

		getServer().newHar();
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD"))
			url = url.replace("medscape", env + "medscape");

		getURL(url);
		waitForAdCallFound();

		if (isTrue("thisIsDrug")) {
			// Verifying the Ad call
			if (numberOfAdCallsValidation()) {
				// MAN Layer verification
				verifyLayerAdPresence();
				// Gray Color verification
				verifyGreyAreaFunctionality();
				// Scrolling functionality
				verifyAutoscrolling();
				// Close button
				verifyCloseButtonFunctionality();
				// Page access after closing the MAN layer
				pageAcessAfterClosingAd();
				// Pos verificaiton in Ad call
				if (breakPoint.equalsIgnoreCase("4"))
					verifyDesktopFooterPosition();
				else
					verifyMobileFooterPosition();

				// Native driver position verification in Ad call
				verifyNativeDriverAdPosition();
				// Stickyness verification
				verifyStickyNessOFAd();
				// Close button functionality verification
				verifyCloseButtonFunctionalityForSticky();

				getDriver().switchTo().defaultContent();
			}
		} else {
			try {
				generateReport(!getDriver().findElement(layerAd).isDisplayed(),
						"Layer Ad not shown on Non Drugmonograph page: " + url,
						"Layer Ad shown on Non Drugmonograph page: " + url);

			} catch (Exception e) {
				generatePassReportWithNoScreenShot("MAN Layer Ad shown on Non Drugmonograph page: " + url);
			}
		}

	}

	/**
	 * Verifies the Native Driver Ad functionality
	 * 
	 * @throws InterruptedException
	 */
	private void verifyNativeDriverCaratLink() throws InterruptedException {
		getServer().newHar();
		Thread.sleep(1000);
		getDriver().findElement(By.xpath("//*[@id='str-inst-0']/child::div/child::div[@class='str-title']")).click();
		Thread.sleep(1000);
		verifyOmniture(
				getDriver().findElement(By.xpath("//*[@id='str-inst-0']/child::div/child::div[@class='str-title']")));
		String parant = getDriver().getWindowHandle();
		Set<String> child = getDriver().getWindowHandles();
		boolean page = false;
		for (String string : child) {
			if (!string.equalsIgnoreCase(parant)) {
				getDriver().switchTo().defaultContent();
				getDriver().switchTo().window(string);
				page = true;
				break;
			}
		}
		Thread.sleep(3000);
		if (!page) {
			generateFailReport("Child window has not found hence hyper could not have valid URL to open a window.");
		} else {

			if (getDriver().getTitle().contains("CGRP Preventive Migraine Treatment")) {
				generatePassReport("CGRP Preventive Migraine Treatment page is found");
			} else {
				generateFailReport("CGRP Preventive Migraine Treatment page doesnt not found");
			}
			getDriver().close();
		}
		getDriver().switchTo().window(parant);
		getDriver().switchTo().defaultContent();
		child.clear();
	}

	/**
	 * Close button functionality verification on Sticky footer Ad
	 */
	private void verifyCloseButtonFunctionalityForSticky() {
		try {
			Thread.sleep(1000);
			WebElement ele = getDriver().findElement(footerAdCloseButton);
			ele.click();
			Thread.sleep(1000);
			try {
				getDriver().findElement(footerAdOuterWrapper).isDisplayed();
			} catch (Exception e) {
				generatePassReport("Close button functinality Pass for " + getDriver().getCurrentUrl());
			} // Condition to check

		} catch (Exception e) {
			generateFailReport(UtilityMethods.getException(e));
		}
	}

	/**
	 * All External links verification
	 * 
	 * @throws InterruptedException
	 */
	public void verifyAllExternalLinks() throws InterruptedException {

		getDriver().switchTo().frame("google_ads_iframe_/4312434/profpromo/medscprefdesktop_4");
		getServer().newHar();
		getDriver().findElement(By.xpath("//*[@id='stkdsk_main_div']//*[@id='presc_info_line']")).click();
		Thread.sleep(3000);
		verifyOmniture(getDriver().findElement(By.xpath("//*[@id='stkdsk_main_div']//*[@id='presc_info_line']")));
		String parant = getDriver().getWindowHandle();
		Set<String> child = getDriver().getWindowHandles();
		boolean page = false;
		for (String string : child) {
			if (!string.equalsIgnoreCase(parant)) {
				getDriver().switchTo().defaultContent();
				getDriver().switchTo().window(string);
				page = true;
				break;
			}
		}
		Thread.sleep(3000);
		if (!page) {
			generateFailReport("Child window has not found hence hyper could not have valid URL to open a window.");
		} else {
			// jardiance.pdf document verification
			if (getDriver().findElement(By.xpath("//*[@id='plugin']")).isDisplayed()) {
				if (getDriver().findElement(By.xpath("//*[@id='plugin']")).getAttribute("src")
						.contains("jardiance.pdf")) {
					generatePassReport("jardiance.pdf document generated successfully!!");
					getDriver().close();
				} else {
					generateFailReport("Failed to generate jardiance.pdf document.");
				}

			} else {
				generateFailReport("Failed to generate jardiance.pdf document.");
			}
		}
		getDriver().switchTo().window(parant);
		child.clear();
		getServer().newHar();
		getDriver().switchTo().frame("google_ads_iframe_/4312434/profpromo/medscprefdesktop_4");
		getDriver().findElement(By.xpath("//*[@id='stkdsk_main_div']//*[@id='patient_info_line']")).click();
		verifyOmniture(getDriver().findElement(By.xpath("//*[@id='stkdsk_main_div']//*[@id='patient_info_line']")));
		Thread.sleep(3000);
		parant = getDriver().getWindowHandle();
		child = getDriver().getWindowHandles();
		page = false;
		for (String string : child) {
			if (!string.equalsIgnoreCase(parant)) {
				getDriver().switchTo().defaultContent();
				getDriver().switchTo().window(string);
				page = true;
				break;
			}
		}
		Thread.sleep(3000);
		if (!page) {
			generateFailReport("Child window has not found hence hyper could not have valid URL to open a window.");
		} else {
			// Jardiance Patient Information.pdf document verification
			if (getDriver().findElement(By.xpath("//*[@id='plugin']")).isDisplayed()) {
				if (getDriver().findElement(By.xpath("//*[@id='plugin']")).getAttribute("src")
						.contains("Jardiance%20Medication%20Guide")) {
					generatePassReport("Jardiance Patient Information.pdf document generated successfully!!");
					getDriver().close();
				} else {
					generateFailReport("Failed to generate Jardiance Patient Information.pdf document.");
				}
			} else {
				generateFailReport("Failed to generate Jardiance Patient Information.pdf document.");
			}
		}
		getDriver().switchTo().window(parant);
		child.clear();

		getDriver().switchTo().frame("google_ads_iframe_/4312434/profpromo/medscprefdesktop_4");
		// Verifing the http://care.diabetesjournals.org Ad
		getServer().newHar();
		getDriver().findElement(By.xpath("//img[@id='cta-bg']")).click();
		Thread.sleep(3000);
		verifyOmniture(getDriver().findElement(By.xpath("//img[@id='cta-bg']")));
		parant = getDriver().getWindowHandle();
		child = getDriver().getWindowHandles();
		page = false;
		for (String string : child) {
			if (!string.equalsIgnoreCase(parant)) {
				getDriver().switchTo().defaultContent();
				getDriver().switchTo().window(string);
				page = true;
				break;
			}
		}
		Thread.sleep(3000);
		if (!page) {
			generateFailReport("Child window has not found hence hyper could not have valid URL to open a window.");
		} else {

			if (getDriver().getTitle().contains(
					"Pharmacologic Approaches to Glycemic Treatment: Standards of Medical Care in Diabetes—2018 | Diabetes Car")) {
				generatePassReport(getDriver().getTitle() + " - page has opened successfully!!");
			} else {
				generateFailReport(
						"Expected page has not opened, expecting - Pharmacologic Approaches to Glycemic Treatment: Standards of Medical Care in Diabetes—2018 | Diabetes Care, but found : "
								+ getDriver().getTitle());
			}
			getDriver().close();
			getDriver().switchTo().window(parant);
		}
		child.clear();

		getDriver().switchTo().frame("google_ads_iframe_/4312434/profpromo/medscprefdesktop_4");
		WebElement element = getDriver().findElement(By.xpath("//*[@id='stkdsk_main_div']//span[@id='presc_info']"));
		((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true);", element);
		Thread.sleep(500);
		getServer().newHar();
		getDriver().findElement(By.xpath("//*[@id='stkdsk_main_div']//*[@id='presc_info_line']")).click();
		Thread.sleep(3000);
		verifyOmniture(getDriver().findElement(By.xpath("//*[@id='stkdsk_main_div']//*[@id='presc_info_line']")));
		parant = getDriver().getWindowHandle();
		child = getDriver().getWindowHandles();
		page = false;
		for (String string : child) {
			if (!string.equalsIgnoreCase(parant)) {
				getDriver().switchTo().defaultContent();
				getDriver().switchTo().window(string);
				page = true;
				break;
			}
		}
		Thread.sleep(3000);
		if (!page) {
			generateFailReport("Child window has not found hence hyper could not have valid URL to open a window.");
		} else {
			// jardiance.pdf document verification
			if (getDriver().findElement(By.xpath("//*[@id='plugin']")).isDisplayed()) {
				if (getDriver().findElement(By.xpath("//*[@id='plugin']")).getAttribute("src")
						.contains("jardiance.pdf")) {
					generatePassReport("jardiance.pdf document generated successfully!!");
					getDriver().close();
				} else {
					generateFailReport("Failed to generate jardiance.pdf document.");
				}

			} else {
				generateFailReport("Failed to generate jardiance.pdf document.");
			}
			getDriver().switchTo().window(parant);
		}
		child.clear();

		getDriver().switchTo().frame("google_ads_iframe_/4312434/profpromo/medscprefdesktop_4");
		getServer().newHar();
		getDriver().findElement(By.xpath("//*[@id='stkdsk_main_div']//*[@id='patient_info']")).click();
		Thread.sleep(3000);
		verifyOmniture(getDriver().findElement(By.xpath("//*[@id='stkdsk_main_div']//*[@id='patient_info']")));
		parant = getDriver().getWindowHandle();
		child = getDriver().getWindowHandles();
		page = false;
		for (String string : child) {
			if (!string.equalsIgnoreCase(parant)) {
				getDriver().switchTo().defaultContent();
				getDriver().switchTo().window(string);
				page = true;
				break;
			}
		}
		Thread.sleep(3000);
		if (!page) {
			generateFailReport("Child window has not found hence hyper could not have valid URL to open a window.");
		} else {
			// Jardiance Patient Information.pdf document verification
			if (getDriver().findElement(By.xpath("//*[@id='plugin']")).isDisplayed()) {
				if (getDriver().findElement(By.xpath("//*[@id='plugin']")).getAttribute("src")
						.contains("ardiance%20Patient%20Information.pdf")) {
					generatePassReport("Jardiance Patient Information.pdf document generated successfully!!");
					getDriver().close();
				} else {
					generateFailReport("Failed to generate Jardiance Patient Information.pdf document.");
				}

			} else {
				generateFailReport("Failed to generate Jardiance Patient Information.pdf document.");
			}
		}
		getDriver().switchTo().window(parant);
		child.clear();
	}

	/**
	 * Stickiness verification on MAN / Sticky leyer
	 */
	private void verifyStickyNessOFAd() {

		String position = getDriver().findElement(footerAdOuterWrapper).getCssValue("position");
		try {
			Assert.assertEquals(position, "fixed");
			generatePassReport("Ad is sticky");
		} catch (AssertionError e) {
			generateFailReport("Ad is not sticky, CSS Value of \"Position\" for Outer Layer of Ad is: " + position);
		}
	}

	/**
	 * Native driver position verification
	 */
	private void verifyNativeDriverAdPosition() {
		try {
			if (breakPoint.equalsIgnoreCase("4")) {
				if (getDriver().findElement(By.id("ads-pos-421")).isDisplayed())
					generatePassReport("421 position shown on desktop for Native driver");
				if (verifySpecificAdPresenceInSecurePubadCall("421"))
					generatePassReport("421 Ad position shown in Ad call");
			} else {
				if (getDriver().findElement(By.id("ads-pos-1421")).isDisplayed())
					generatePassReport("1421 position shown on desktop for Native driver");
				if (verifySpecificAdPresenceInSecurePubadCall("1421"))
					generatePassReport("1421 Ad position shown in Ad call");
			}
		} catch (Exception e) {
			generateFailReport("Un know exception \n" + UtilityMethods.getException(e));
		}
	}

	/**
	 * Mobile footer position verification
	 */
	private void verifyMobileFooterPosition() {
		try {
			if (getDriver().findElement(mobileFooterAd).isDisplayed())
				generatePassReport("1145 position shown on Mobile for " + getDriver().getCurrentUrl());
			if (verifySpecificAdPresenceInSecurePubadCall("1145"))
				generatePassReport("1145 Ad positino shown in Ad call");
			String expectedPos = "1145";
			String unExpSize = "";
			String unExpectedPos = "";
			String[] esizes = { "320x50", "320x80", "375x80", "320x52", "300x52", "2x7" };
			for (String size : esizes) {
				posAndSize(size, unExpSize, expectedPos, unExpectedPos);
			}
		} catch (Exception e) {
			generateFailReport("Unknown exception" + UtilityMethods.getException(e));
		}
	}

	/**
	 * Desktop footer position verification in Ad call
	 */
	private void verifyDesktopFooterPosition() {
		try {
			if (getDriver().findElement(desktopFooterAd).isDisplayed())
				generatePassReport("145 position shown on desktop for " + getDriver().getCurrentUrl());
			if (verifySpecificAdPresenceInSecurePubadCall("145")) {
				generatePassReport("145 Ad position shown in Ad call");
				String expectedPos = "145";
				String unExpSize = "";
				String unExpectedPos = "";
				String[] esizes = { "320x50", "728x100", "728x92", "2x7" };
				for (String size : esizes) {
					posAndSize(size, unExpSize, expectedPos, unExpectedPos);
				}
			}
		} catch (Exception e) {
			generateFailReport("Un know exception \n" + UtilityMethods.getException(e));
		}
	}

	/**
	 * 
	 * Method will verify that whether layer ad present or not
	 */
	public void verifyLayerAdPresence() {
		try {
			if (getDriver().findElement(layerAd).isDisplayed())
				generatePassReport("MAN Layer appears on the page");
		} catch (Exception e) {
			generateFailReport("MAN Layer not appears on the page");
		}
	}

	/**
	 * Method will verify MAN layer close button present or not
	 */
	public void verifyCloseButtonFunctionality() {
		try {
			if (getDriver().findElement(layerAdCloseButton).isDisplayed()) {
				generatePassReport("Close button is shown on Layer Ad");
				getServer().newHar();
				WebElement ele = getDriver().findElement(layerAdCloseButton);
				ele.click();
				// verifyOmniture(ele);
			}

		} catch (NoSuchElementException e) {

		}
		try {
			if (getDriver().findElement(layerAd).isDisplayed())
				generateFailReport("Layer Ad not closed after clicking on Close button");
		} catch (NoSuchElementException e) {
			generatePassReport("Layer Ad closed successfully after clicking on Close button");
		}
	}

	/**
	 * Method will verify whether omniture call present or not
	 * 
	 */
	public void verifyOmniture(WebElement element) {
		if (verifySpecificCallPresence("ssl")) {
			generatePassReport("Omniture call has been found for the following element : " + element);
		} else {
			generateFailReport("Omniture call has not been found for the following element : " + element);
		}
	}

	/**
	 * Color changes verification
	 */
	public void verifyGreyAreaFunctionality() {
		WebElement layer = getDriver().findElement(layerAd);
		String position = layer.getCssValue("position");
		String overflow = getDriver().findElement(By.xpath("//div[@class='man-whiteout-layer']"))
				.getCssValue("overflow");
		Double zIndexOfLayerAd = Double.parseDouble(layer.getCssValue("z-index"));
		Double zIndexOfWhiteLayer = Double.parseDouble(
				getDriver().findElement(By.xpath("//div[@class='man-whiteout-layer']")).getCssValue("z-index"));
		generateInfoReport("Values: " + "\n" + position + "\n" + overflow + "\n" + zIndexOfLayerAd);
		try {
			if (position.contains("fixed"))
				generatePassReport("Position value is fixed hence layer ad on page");
			if (overflow.contains("hidden"))
				generatePassReport("User unable to interact with page while layer ad shown on page");
			if (zIndexOfLayerAd > zIndexOfWhiteLayer)
				generatePassReport("Grey Area shown behind the Layer Ad");
		} catch (Exception e) {
			generateFailReport("CSS Properties of the Layer Ad are not As expected");
		}

	}

	/**
	 * Page access verification
	 */
	public void pageAcessAfterClosingAd() {
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		try {
			js.executeScript("window.scrollBy(0,150)");
			generatePassReport("Able to interact with the page after closing the Layer Ad");
		} catch (Exception e) {
			generateFailReport("Unable to interact with the page after closing the Layer Ad");
		}
	}

	/**
	 * Handle the child windows if there any when user clicks on external links
	 * 
	 * @param elementSouce
	 *            - Source element
	 * @param elementDest
	 *            - Child window element
	 * @param fileName
	 *            - File name which can be opened in child window
	 */
	private void verifyChildTabs(WebElement elementSouce, By elementDest, String fileName) {
		verifyOmniture(elementSouce);
		StaticWait(3);
		String parant = getDriver().getWindowHandle();
		Set<String> child = getDriver().getWindowHandles();
		boolean page = false;
		for (String string : child) {
			if (!string.equalsIgnoreCase(parant)) {
				getDriver().switchTo().window(string);
				page = true;
				break;
			}
		}
		StaticWait(3);
		if (page) {
			// jardiance.pdf document verification

			if (elementDest != null) {
				if (getDriver().findElement(elementDest).isDisplayed()) {
					boolean condition = getDriver().findElement(elementDest).getAttribute("src").contains(fileName);
					if (condition) {
						generatePassReport("Expected condition has been passed.");
						getDriver().close();
					} else {
						generateFailReport("Expected condition has been failed.");
					}

				} else {
					generateFailReport("Failed to generate jardiance.pdf document.");
				}
			} else {
				if (getDriver().getTitle().contains(
						"Pharmacologic Approaches to Glycemic Treatment: Standards of Medical Care in Diabetes—2018 | Diabetes Care")) {
					generatePassReport("Expected condition has been passed.");
					getDriver().close();
				} else {
					generateFailReport("Expected condition has been failed.");
				}
			}
		} else {
			generateFailReport("Child window has not found hence hyper could not have valid URL to open a window.");
		}
		getDriver().switchTo().window(parant);
		child.clear();
	}

	/**
	 * External links verification
	 * 
	 * @throws InterruptedException
	 */
	private void verifyAllExternalLinksonMAN() throws InterruptedException {
		getServer().newHar();
		Thread.sleep(1000);
		// verify Jardiance doc
		WebElement e1 = getDriver().findElement(By.xpath("(//*[@id='presc_info_line'])[1]"));
		e1.click();
		By by = By.xpath("//*[@id='plugin']");
		verifyChildTabs(e1, by, "jardiance.pdf");

		getServer().newHar();
		Thread.sleep(3000);
		e1 = getDriver().findElement(By.xpath("(//*[@id='patient_info_line'])[1]"));
		e1.click();
		by = By.xpath("//*[@id='plugin']");
		verifyChildTabs(e1, by, "ardiance%20Patient%20Information.pdf");

		getServer().newHar();
		Thread.sleep(3000);
		e1 = getDriver().findElement(By.xpath("//*[@id='mfg_footer']"));
		verifyChildTabs(e1, null,
				"Pharmacologic Approaches to Glycemic Treatment: Standards of Medical Care in Diabetes—2018 | Diabetes Care");

		e1 = getDriver().findElement(By.xpath("(//span[@id='presc_info'])[1]"));

		((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true);", e1);
		getServer().newHar();
		Thread.sleep(1000);
		e1.click();
		by = By.xpath("//*[@id='plugin']");
		verifyChildTabs(e1, by, "jardiance.pdf");

		getServer().newHar();
		Thread.sleep(3000);
		e1 = getDriver().findElement(By.xpath("(//*[@id='patient_info'])[1]"));
		e1.click();
		by = By.xpath("//*[@id='plugin']");
		verifyChildTabs(e1, by, "Jardiance%20Medication%20Guide");

	}

	/**
	 * Auto scrolling functionality verification
	 * 
	 * @throws InterruptedException
	 */
	private void verifyAutoscrolling() throws InterruptedException {
		boolean autoscrolling = false;
		for (int i = 1; i <= 10; i++) {
			if (getDriver().findElement(By.xpath("(//span[contains(.,'MOST COMMON ADVERSE REACTIONS')])[1]"))
					.isDisplayed()) {
				generatePassReport("Auto scrolling is being performed successfully on MAN!!");
				autoscrolling = true;
				break;
			} else {
				Thread.sleep(20000);
			}
		}
		if (!autoscrolling) {
			generateFailReport("Auto scrolling is being performed!!");
		}
	}

	@DataProvider
	public String[] dataProvider() {

		return new String[] { "https://reference.medscape.com/drug/jardiance-empagliflozin-999907",
				"https://www.medscape.com/viewarticle/914146" };

	}

}
