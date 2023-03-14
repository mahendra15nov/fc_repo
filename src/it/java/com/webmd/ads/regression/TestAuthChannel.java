package com.webmd.ads.regression;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;

@Listeners(com.webmd.general.common.Listener.class)
/**
 * Description : Verication of Auth channel value
 * 
 * Created Date : 19/08/2019
 * 
 * Version : V1.0
 * 
 * @author amahendra
 *
 */
public class TestAuthChannel extends AdsCommon {

	JavascriptExecutor jse;
	ArrayList<HashMap<String, String>> pbrValuesList = new ArrayList<>();
	JSONObject reqHeadersTarget = null;
	JSONObject webSegVarsTarget = null;
	JSONObject pageSegVarsTarget = null;
	JSONObject userSegVarsTarget = null;

	/**
	 * Description : Method will quit the driver and also stops the BrowserMob
	 * service
	 */
	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		// logout("proclivitytest@gmail.com");
		getDriver().quit();
		getServer().stop();
	}

	/**
	 * Description :
	 * 
	 * @param URL
	 *            : Its medscape URL
	 * @throws InterruptedException
	 */
	@Test(dataProvider = "medscapeurls", groups = { "auth", "AdsSmoke", "AdsRegression", "Desktop",
			"MobileWeb" }, priority = 1)
	public void verifyAuthValueforAnonUser(String URL) {
		jse = (JavascriptExecutor) getDriver();

		getDriver();

		getURL(URL);
		// Verify the default adhesive footer changes
		if (!is404(getDriver().getTitle())) {
			if (!isLoginPage()) {
				int c = 0;
				while (!numberOfAdCallsValidation()) {
					waitForPageLoad(10);
					c++;
					if (c == 50)
						break;
				}

				String pageSource = getDriver().getPageSource();
				// get the proclivity pos from Ad call.

				String DFP = "";
				boolean dfptkeysfound = true;
				try {
					DFP = pageSource.substring(pageSource.indexOf("{\"reqHeaders\""),
							pageSource.indexOf("; var userCampaign"));
				} catch (StringIndexOutOfBoundsException e) {
					dfptkeysfound = false;
				}
				if (dfptkeysfound) {
					JSONObject jo = new JSONObject(DFP);
					reqHeadersTarget = jo.getJSONObject("reqHeaders");
					webSegVarsTarget = jo.getJSONObject("webSegVars");
					pageSegVarsTarget = jo.getJSONObject("pageSegVars");
					userSegVarsTarget = jo.getJSONObject("userSegVars");
					generateInfoReport(webSegVarsTarget.toString());
					String authValue = StringUtils.substringBetween(webSegVarsTarget.toString(), "\"auth\":\"", "\",");
					if (authValue.equals("0")) {
						generatePassReportWithNoScreenShot("Auth value is " + authValue + " for Anonymos user.");
					} else {
						generateFailReport("Auth value is " + authValue + " for Anonymos user.");
					}

				} else {
					generateFailReport(URL + " has still required login though login performed aleardy.");
				}
			}
		} else {
			generateSkipReport(URL + " is not a valid URL.");
		}
		// throwErrorOnTestFailure();
	}

	@Test(priority = 2, dataProvider = "medscapeurls", groups = { "auth", "AdsSmoke", "AdsRegression", "Desktop",
			"MobileWeb" })
	public void verifyAuthValueforLoogedInUser(String URL) {
		jse = (JavascriptExecutor) getDriver();

		getDriver();

		login(getProperty("username"), getProperty("password"));

		getURL(URL);
		// Verify the default adhesive footer changes
		if (!is404(getDriver().getTitle())) {
			if (!isLoginPage()) {
				int c = 0;
				while (!numberOfAdCallsValidation()) {
					waitForPageLoad(10);
					c++;
					if (c == 50)
						break;
				}

				String pageSource = getDriver().getPageSource();
				// get the proclivity pos from Ad call.

				String DFP = "";
				boolean dfptkeysfound = true;
				try {
					DFP = pageSource.substring(pageSource.indexOf("{\"reqHeaders\""),
							pageSource.indexOf("; var userCampaign"));
				} catch (StringIndexOutOfBoundsException e) {
					dfptkeysfound = false;
				}
				if (dfptkeysfound) {
					JSONObject jo = new JSONObject(DFP);
					reqHeadersTarget = jo.getJSONObject("reqHeaders");
					webSegVarsTarget = jo.getJSONObject("webSegVars");
					pageSegVarsTarget = jo.getJSONObject("pageSegVars");
					userSegVarsTarget = jo.getJSONObject("userSegVars");
					generateInfoReport(webSegVarsTarget.toString());
					String authValue = StringUtils.substringBetween(webSegVarsTarget.toString(), "\"auth\":\"", "\",");
					if (authValue.equals("1")) {
						generatePassReportWithNoScreenShot("Auth value is " + authValue + " for Loogedin user.");
					} else {
						generateFailReport("Auth value is " + authValue + " for Loggedin user.");
					}

				} else {
					generateFailReport(URL + " has still required login though login performed aleardy.");
				}
			}
		} else {
			generateSkipReport(URL + " is not a valid URL.");
		}
	}

	@DataProvider
	public String[] medscapeurls() {
		return getURLs("AdsSanity.xls", "POS_SZS");
	}
}
