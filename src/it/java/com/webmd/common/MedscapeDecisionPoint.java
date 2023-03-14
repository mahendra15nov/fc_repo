package com.webmd.common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;

/**
 * Created on 21/10/2019 Version : V 1.0;
 * 
 * MDP Common functionality
 * 
 * @author amahendra Created on October 21st 2019
 */
public class MedscapeDecisionPoint extends MobileOptimizationCommon {

	By cancerTypeContainer = By.xpath("//div[@id='cancerList' and @class='dropdown']");
	By tumorListContainer = By.xpath("//div[@id='tumorList']");
	By back = By.xpath("//div[@id='column-left']//div[@class='breadcrumb']//a");

	/**
	 * This validates DPT KV pair and collapser functionality by interchanging
	 * Cancer type/ Tumor type on Decision Point page
	 *
	 * @param url
	 *            - the current url
	 * @param isLogin
	 *            - whether logged in or not
	 * @param pos
	 *            - the ad pos for which collapser code should be tested
	 */

	public void verifyDPTAndAdPos() {

		By cancerTypeContainer = By.xpath("//div[@id='cancerList' and @class='dropdown']");
		By tumorListContainer = By.xpath("//div[@id='tumorList']");
		int cancerTypeSize = 0;
		int tumorTypeSize = 0;
		int cancerTypeCount = 1;
		int tumorTypeCount = 1;

		do {
			getServer().newHar();
			waitForElement(cancerTypeContainer);
			getDriver().findElement(cancerTypeContainer).click();
			generateInfoReport("Selecting the value from Cancer Type dropdown");
			String cancerTypeList = "//ul[@id='cancerList']/li[" + cancerTypeCount + "]";
			cancerTypeSize = getDriver().findElements(By.xpath("//ul[@id='cancerList']/li")).size();
			getDriver().findElement(By.xpath(cancerTypeList)).click();
			waitForElement(tumorListContainer);
			getDriver().findElement(tumorListContainer).click();
			generateInfoReport("Selecting the value from Tumor Type dropdown");
			waitForElement(By.xpath("//ul[@id='tumorList']/li"));
			tumorTypeSize = getDriver().findElements(By.xpath("//ul[@id='tumorList']/li")).size();
			getDriver().findElement(By.xpath("//ul[@id='tumorList']/li[" + tumorTypeCount + "]")).click();
			generateInfoReport("Validating DPT key value pair");
			verifyDPT();
			verifySpecificPositionLoadedOnPage("101");
			tumorTypeCount++;
			if (tumorTypeCount > tumorTypeSize) {
				tumorTypeCount = 1;
				cancerTypeCount++;
			}
		} while (cancerTypeCount <= cancerTypeSize);
	}

	private void verifyDPT() {
		String urlValue = StringUtils.substringAfter(getDriver().getCurrentUrl(), "ctype=");
		urlValue = urlValue.replace("&ttype=", "%2C");
		String custParamsValue = getSpecificKeyFromSecurePubadCall("cust_params");
		generateInfoReport("CustParams: " + custParamsValue);
		custParamsValue = StringUtils.substringBetween(custParamsValue, "&dpt=", "&");
		try {
			Assert.assertEquals(custParamsValue, urlValue);
			generatePassReportWithNoScreenShot("DPT value tracked as expected");
		} catch (AssertionError e) {
			generateFailReport("Expected value is " + urlValue + " value tracked in cust_params is " + custParamsValue);
		}
	}

	protected void verifyAllAdPosLoadedOnPage(String[] aPos) {
		for (String pos : aPos) {
			verifySpecificPositionLoadedOnPage(pos);
		}
	}

	public void verifyAdhesiveFooterChanges() {

		getDivID();
		// Verify the Adhesive footer loaded or not
		isIframePresent = isAdhesiveFooterLoaded();

		// Adhesive Footer configuration verification
		adhesiveFooterConfig();

		onSCroll = getOnScroll();

		delpayInAdDisplay();

		// Adlable verification
		if (isVisibleInViewport(getDriver().findElement(By.id(adDiv)))) {
			generatePassReportWithNoScreenShot("pos 145/1145 Adhesive Footer ad is in view port.");
			// Verify Adlabel
			if (breakPoint.equals("4"))
				adLabel("145");
			if (breakPoint.equals("1"))
				adLabel("1145");

			// Background color verification
			backGroundColor = getBackGroundColor();

			backgroudColourVerification();
			// Verify the close button
			closButton = getCloseButton();

			if (closButton.equals("X")) {
				generatePassReportWithNoScreenShot("Default closeButton: \"X\" has been returned from ad server.");
			} else {
				generateInfoReport("closeButton: " + closButton + " has been returned from ad server.");
			}
			// TO-DO (Need to update the logic)
			closeButtonVerification();
			// Verify the styling update
			verifyAdhesivefooterOverlappingIssue();
			// closing functionality if close button exists
			closeButtonClosing();

		} else {
			generateInfoReport(
					"pos 145/1145 Adhesive Footer Ad is not in view port, ad server may not be returned the creative.");
		}
	}

	protected void adhesiveFooterConfig() {
		configData = getAdhesiveFooterConfig(adDiv);
		if (configData != null) {
			if (configData.isEmpty()) {
				generatePassReportWithNoScreenShot(
						"Adhesive Footer has been served with default configurations on the page for " + url);
			} else {
				generateInfoReport("custom config values are loaded on Optimized page");
			}

		} else {
			generateInfoReport(
					"Adhesive footer configurations are not returned from Ad server hence default configurations should be loaded.");
		}
	}

	/*
	 * Selects all possible values from the drop downs and validates the DPT Key
	 * value pair
	 */
	protected void workAroundDropdownAndValidateDPT() {

		int cancerTypeSize = 0;
		int tumorTypeSize = 0;
		int cancerTypeCount = 1;
		int tumorTypeCount = 1;

		do {
			getServer().newHar();
			waitForElement(cancerTypeContainer);
			getDriver().findElement(cancerTypeContainer).click();
			generateInfoReport("Selecting the value from Cancer Type dropdown");
			String cancerTypeList = "//ul[@id='cancerList']/li[" + cancerTypeCount + "]";
			cancerTypeSize = getDriver().findElements(By.xpath("//ul[@id='cancerList']/li")).size();
			getDriver().findElement(By.xpath(cancerTypeList)).click();
			waitForElement(tumorListContainer);
			getDriver().findElement(tumorListContainer).click();
			generateInfoReport("Selecting the value from Tumor Type dropdown");
			waitForElement(By.xpath("//ul[@id='tumorList']/li"));
			tumorTypeSize = getDriver().findElements(By.xpath("//ul[@id='tumorList']/li")).size();
			getDriver().findElement(By.xpath("//ul[@id='tumorList']/li[" + tumorTypeCount + "]")).click();
			generateInfoReport("Verifying DPT key value pair to have a value when selected values from dropdown");
			verifyDPTKeyValue();
			generateInfoReport("Verifying DPT Key value pair to have value when clicked on articles");
			workAroundRelatedArticlesAndValidateDPT();
			tumorTypeCount++;
			if (tumorTypeCount > tumorTypeSize) {
				tumorTypeCount = 1;
				cancerTypeCount++;
			}
		} while (cancerTypeCount <= cancerTypeSize);
	}

	private void verifyDPTKeyValue() {

		String urlValue = StringUtils.substringAfter(getDriver().getCurrentUrl(), "ctype=");
		urlValue = urlValue.replace("&ttype=", "%2C");
		String custParamsValue = getSpecificKeyFromSecurePubadCall("cust_params");
		generateInfoReport("CustParams: " + custParamsValue);
		custParamsValue = StringUtils.substringBetween(custParamsValue, "&dpt=", "&");
		try {
			Assert.assertEquals(custParamsValue, urlValue);
			generatePassReportWithNoScreenShot("DPT value tracked as expected");
		} catch (AssertionError e) {
			generateFailReport("Expected value is " + urlValue + " value tracked in cust_params is " + custParamsValue);
		}
	}

	/*
	 * Clicks on related articles and then clicks on Back to Decision Tree and
	 * validates the DPT key value pair
	 */
	private void workAroundRelatedArticlesAndValidateDPT() {

		try {
			getServer().newHar();
			List<WebElement> relatedDPLinks = getDriver()
					.findElements(By.xpath("//div[@class='dp-card']//div[@class='title']//a"));
			if (!relatedDPLinks.isEmpty()) {
				generateInfoReport(
						"We have got " + relatedDPLinks.size() + " number of related articles. Lets click on one");
				relatedDPLinks.get(0).click();
				verifyDPTKeyValue();
				generateInfoReport("Clicking on 'Back to Decision Tree' and verifying if DPT value is persisted");
				getServer().newHar();
				JavascriptExecutor executor = (JavascriptExecutor) getDriver();
				executor.executeScript("arguments[0].click();", getDriver().findElement(back));
				verifyDPTKeyValue();
			} else {
				generateInfoReport("Did not find related article pages");
			}

		} catch (NoSuchElementException e) {
			generateInfoReport("Unable to get the element " + e.getMessage());
		}

	}

	/*
	 * Checks DPT value to be zero
	 */
	protected void validateDptZero() {

		String dptValue = getSpecificKeyFromSecurePubadCall("cust_params");
		dptValue = StringUtils.substringBetween(dptValue, "&dpt=", "&");
		generateInfoReport("Verifying that DPT value should be 0");
		try {
			Assert.assertEquals(dptValue, "0");
			generatePassReport("DPT value is zero as expected on Decision Tree Page");
		} catch (AssertionError e) {
			generateFailReport("DPT value should be zero");
		}
	}
}
