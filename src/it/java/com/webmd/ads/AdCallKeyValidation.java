package com.webmd.ads;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.common.AdsConstantns;
import com.webmd.general.common.XlRead;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;

/**
 * 
 * @author Triandh Namburi
 * 
 *         This Class is to perform regression on Ad call keys It will have
 *         methods to get keys from config file/excel sheet/medscape url as
 *         Expected values get Actual Keys list from Medscape URL and compare
 *         both
 *
 */

public class AdCallKeyValidation extends AdsCommon {

	/*
	 * To get ad call possible ways URL - access URL and get ad call Trigger
	 * Event - perform the trigger event and get ad call any location - get the
	 * keys from the file mentioned in location
	 */

	public static List<String> expectedQueryStringParameterKeys = new ArrayList<String>();
	public static List<String> actualQueryStringParameterKeys = new ArrayList<String>();

	public static List<String> expectedCustParamKeys = new ArrayList<String>();
	public static List<String> actualCustParamKeys = new ArrayList<String>();

	/**
	 * This method is to get keys list from cust_params
	 * 
	 * @param cust_params
	 * @return List of Keys From Cust_Params of Ad call
	 */
	public List getKeysFromCustParams(String cust_params) {
		List<String> keys = new ArrayList<String>();
		String[] keysList = cust_params.split("&");
		for (String key : keysList) {
			keys.add(StringUtils.substringBefore(key, "="));
		}
		return keys;
	}

	/**
	 * This method is to get list of query string params from given URL
	 * 
	 * @param URL
	 * @return List of keys from Query String Parameters of Ad call
	 */
	public void getQueryStringKeysFromPageLoadEvent(String URL, String event) {
		getDriver();
		getServer().newHar();
		getDriver().get(URL);
		waitForPageLoaded();
		Har har = getServer().getHar();
		har.getLog().getBrowser();
		List<HarEntry> res = har.getLog().getEntries();

		for (HarEntry harEntry : res) {
			String url = harEntry.getRequest().getUrl();
			if (url.contains(AdsConstantns.AD_CALL)) {
				List<HarNameValuePair> queryParams = harEntry.getRequest().getQueryString();
				for (HarNameValuePair harNameValuePair : queryParams) {
					if (event.equals("expected"))
						expectedQueryStringParameterKeys.add(harNameValuePair.getName());
					else if (event.equals("actual"))
						actualQueryStringParameterKeys.add(harNameValuePair.getName());

					if (harNameValuePair.getName().contains("cust_params")) {
						if (event.contains("expected"))
							expectedCustParamKeys = getKeysFromCustParams(harNameValuePair.getValue());
						else if (event.contains("actual"))
							actualCustParamKeys = getKeysFromCustParams(harNameValuePair.getValue());
					}
				}
				break;
			}
		}
	}

	/**
	 * This method is to get list of query string params from given URL
	 * 
	 * @param Trigger
	 *            Event (Ex: lazyload, Next click)
	 * @return List of keys from Query String Parameters of Ad call
	 */
	public void getQueryStringKeysAfterTriggerEvent(String event, String type) {
		getDriver();
		getServer().newHar();
		List<String> keys = new ArrayList<String>();
		try {
			if (event.toLowerCase().contains("lazyload"))
				scrollTillAdCall();
			else if (event.toLowerCase().contains("next"))
				clickNextButton();
			else
				getDriver().findElement(By.xpath(event)).click();
		} catch (Exception e) {
			generateSkipReport("Exceptione while performing event " + event + " : " + e.toString());
		}

		Har har = getServer().getHar();
		har.getLog().getBrowser();
		List<HarEntry> res = har.getLog().getEntries();

		for (HarEntry harEntry : res) {
			String url = harEntry.getRequest().getUrl();
			if (url.contains(AdsConstantns.AD_CALL)) {
				List<HarNameValuePair> queryParams = harEntry.getRequest().getQueryString();
				for (HarNameValuePair harNameValuePair : queryParams) {
					if (event.equals("expected"))
						expectedQueryStringParameterKeys.add(harNameValuePair.getName());
					else if (event.equals("actual"))
						actualQueryStringParameterKeys.add(harNameValuePair.getName());

					if (harNameValuePair.getName().contains("cust_params")) {
						if (type.contains("expected"))
							expectedCustParamKeys = getKeysFromCustParams(harNameValuePair.getValue());
						else if (type.contains("actual"))
							actualCustParamKeys = getKeysFromCustParams(harNameValuePair.getValue());
					}
				}
				break;
			}
		}
	}

	/**
	 * Method to get Har with lazyload ad call
	 * 
	 * @return Har with Ad call, if there is no lazyloaded ad call, it will
	 *         return null
	 */
	public Har scrollTillAdCall() {
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		int height = Integer.parseInt(jse.executeScript("return document.body.scrollHeight").toString());
		height -= 2000;
		try {
			WebElement footer = getDriver().findElement(By.xpath("//div[@class='page-footer']"));
		} catch (NoSuchElementException e) {
			generateBoldReport("");
			return null;
		}

		do {
			getServer().newHar();
			jse.executeScript("window.scrollBy(0, 500)");
			height -= 500;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Har har = getServer().getHar();
			har.getLog().getBrowser();
			List<HarEntry> res = har.getLog().getEntries();

			for (HarEntry harEntry : res) {
				String url = harEntry.getRequest().getUrl();
				if (url.contains(AdsConstantns.AD_CALL))
					return har;
			}
		} while (height > 100);

		return null;
	}

	private String getProdURLForCurrentURL(String URL) {
		String prodURL = URL.replace(env, "");

		return prodURL;
	}

	/**
	 * 
	 * @param expectedKeys:
	 *            Base list
	 * @param actualKeys:
	 *            current test data list compare both or equal or not, if equal
	 *            it generates report if not check additional and missed keys
	 *            from the actual list
	 */

	private void compareTwoLists(List expectedKeys, List actualKeys) {
		try {
			if (expectedKeys.equals(actualKeys))
				generateBoldReport("Both the Keys list are equal");
			else {
				List<String> temp = new ArrayList<String>();
				;
				temp.addAll(expectedKeys);
				// temp = expectedKeys;
				temp.removeAll(actualKeys);
				if (temp.size() > 0) {
					generateBoldReport("Below is the list of Missed keys in actual Keys");
					generateInfoReport(temp.toString());
				} else
					generateBoldReport("There are no missed keys when compared");
				temp.clear();
				temp.addAll(actualKeys);
				// temp = actualKeys;
				temp.removeAll(expectedKeys);
				if (temp.size() > 0) {
					generateBoldReport("Below is the list of Additional keys :");
					generateInfoReport(temp.toString());
				} else
					generateBoldReport("There are no additional keys");

			}
		} catch (Exception e) {
			generateSkipReport("Exception while comparing the lists " + e.toString());
		}
	}

	/**
	 * This method is to compare both expected and actual, it will compare both
	 * query string parameters and cust_params
	 */
	private void compareBothTheKeysList() {
		generateBoldReport("Validating Query String Paramters");
		compareTwoLists(expectedQueryStringParameterKeys, actualQueryStringParameterKeys);
		generateBoldReport("Validating Cust_Params keys");
		compareTwoLists(expectedCustParamKeys, actualCustParamKeys);

	}

	private void getKeysFromExcelSheet() {
		generateBoldReport("Getting Keys list from Excel Sheet");
		String[][] queryParams = XlRead.fetchDataExcludingFirstRow("TestData/adCallKeys.xls", "queryStringKeys");
		String[][] custParams = XlRead.fetchDataExcludingFirstRow("TestData/adCallKeys.xls", "custParamKeys");
		for (int i = 0; i < queryParams.length; i++)
			expectedQueryStringParameterKeys.add(queryParams[i][0]);
		for (int i = 0; i < custParams.length; i++)
			expectedCustParamKeys.add(custParams[i][0]);
	}

	/**
	 * 
	 * @param URL
	 *            : Test URL
	 * @param event
	 *            : pageload, lazyload, next
	 * @param compareWith
	 *            : Compare the ad call keys with PROD/Conifg
	 */
	@Test(dataProvider = "dataProvider", groups = { "testDrugMonograph" })
	public void test(String URL, String event, String compareWith) {
		getDriver();
		URL = URL.replace("env.", env);
		switch (event) {
		case "pageload":
			generateInfoReport("Validating Page Load event");
			getQueryStringKeysFromPageLoadEvent(URL, "actual");
			if (compareWith.contains("PROD"))
				getQueryStringKeysFromPageLoadEvent(getProdURLForCurrentURL(URL), "expected");
			else
				getKeysFromExcelSheet();
			compareBothTheKeysList();
			break;
		case "lazyload":
			getDriver().get(URL);
			waitForPageLoaded();
			generateInfoReport("Validating Lazy Load event");
			getQueryStringKeysAfterTriggerEvent(event, "actual");
			if (compareWith.contains("PROD"))
				getQueryStringKeysAfterTriggerEvent(event, "expected");
			else
				getKeysFromExcelSheet();
			compareBothTheKeysList();
			break;
		case "next":
			getDriver().get(URL);
			generateInfoReport("Validating Lazy Load event");
			getQueryStringKeysAfterTriggerEvent("next", "actual");
			if (compareWith.contains("PROD"))
				getQueryStringKeysAfterTriggerEvent(getProdURLForCurrentURL(URL), "expected");
			else
				getKeysFromExcelSheet();
			compareBothTheKeysList();
		}
	}

	@DataProvider
	public String[][] dataProvider() {
		return XlRead.fetchDataExcludingFirstRow("TestData/adCallValiationTestdata.xls", "Sheet1");
	}
}
