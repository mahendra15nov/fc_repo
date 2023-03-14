package com.webmd.ads.regression;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.html5.RemoteLocalStorage;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.relevantcodes.extentreports.LogStatus;
import com.webmd.common.AdsCommon;
import com.webmd.general.common.ExtentTestManager;
import java.util.Base64;

/*
 * This is to test DMD pixel implementation. This feature is for anon user. 
 * DMD call will triggered when the user is anon and DMD will identifies the NPI number of the user 
 * 
 * All the test cases written in different methods and to avoid multiple session all the methods were called in a single
 * method "mainTest"
 */
@Listeners(com.webmd.general.common.Listener.class)
public class ImplementDMDPixel extends AdsCommon {

	public ImplementDMDPixel() {
		if (env == null) {
			env = "";
		}
	}

	public static final String dmdCallURL = "medtargetsystem.com/beacon/process/";
	RemoteLocalStorage local;
	public static final String consoleCommand = "AIM.tag(10)";
	private static String userNPI;
	String URL = "https://www.medscape.com";
	String article = "https://www.medscape.com/viewarticle/857139";
	String otherLangEdition = "https://deutsch.medscape.com/";
	String otherLangEdition1 = "https://espanol.medscape.com/";

	@AfterMethod(groups = { "AdsRegression", "Desktop", "MobileWeb" })
	public void closeBrowser() {
		getDriver().quit();
		getServer().stop();
	}

	@BeforeMethod(groups = { "AdsRegression", "Desktop", "MobileWeb" })
	public void openBrowser() {
		startServer();
	}

	// PPE-151388 : To verify DMD call made or not for anon user first page
	// access
	@Test(priority = 1, groups = { "AdsRegression", "Desktop", "MobileWeb" })
	public void verifyDMDCall() {
		getServer().newHar();
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD"))
			URL = URL.replace("medscape", env + "medscape");
		getURL(URL);
		waitForAdCallFound();
		if (!is404(getDriver().getTitle()) && !isLoginPage()) {
			if (verifySpecificCallPresence(dmdCallURL))
				generatePassReportWithNoScreenShot("DMD call made for :" + URL);
			else
				generateFailReport("DMD call not made for :" + URL);
		}
	}

	// PPE-151390 : To Verify whether no call made to DMD in logged in session.
	@Test(priority = 7, groups = { "AdsRegression", "Desktop", "MobileWeb" })
	public void verifyNoCallMadeForLoggedInAndLoggedOutSessions() {
		login(getProperty("username"), getProperty("password"));
		getServer().newHar();
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD"))
			article = article.replace("medscape", env + "medscape");

		getURL(article);
		waitForAdCallFound();
		if (!is404(getDriver().getTitle()) && !isLoginPage()) {
			if (!verifySpecificCallPresence(dmdCallURL))
				generatePassReportWithNoScreenShot("DMD call not made for Logged-In Session");
			else
				generateFailReport("DMD call made for Logged-In Session, hence failed");
			getServer().newHar();
			getURL(URL);
			waitForAdCallFound();
			if (!verifySpecificCallPresence(dmdCallURL))
				generatePassReportWithNoScreenShot("DMD call not made for Logged-out Session");
			else
				generateFailReport("DMD call made for Logged-out Session, hence failed");

		}
	}

	// PPE-151392 : To verify whether DMD identifies valid user or not
	@Test(priority = 2, groups = { "AdsRegression", "Desktop", "MobileWeb" })
	public void verifyDMDIdentifiesValidUser() {
		String NPI = null;
		try {
			System.out.println(getConsoleValue("AIM.tag(10)"));
		} catch (WebDriverException e) {
			e.printStackTrace();
		}

		getServer().newHar();
		getDriver().navigate().refresh();
		String debugInfo = "";
		try {
			debugInfo = getConsoleValue("AIM.debug()");
		} catch (WebDriverException e) {
			e.printStackTrace();
		}
		System.out.println("Debug Info \n" + debugInfo);
		// Code to validate the response from above command

		String processCallData = getResponseForSpecificCall("process");
		if (processCallData != null) {
			try {
				Assert.assertTrue(processCallData.contains("npi_number"));
				generatePassReportWithNoScreenShot("NPI number tracked under DMD call, Hence DMD identifies the user");
				try {
					userNPI = StringUtils.substringBetween(processCallData, "npi_number\":", ",");
					Assert.assertTrue(!userNPI.isEmpty());
					generatePassReportWithNoScreenShot("NPI number tracked under DMD call");
				} catch (AssertionError e) {
					generateFailReport("NPI number not matched with Test user," + "NPI number tracked is " + NPI);
				}
			} catch (AssertionError e) {
				generateFailReport("NPI number not tracked under DMD call, " + "Hence DMD didn't identify the user");
			}
		}
	}

	// PPE-PPE-151394 : To Verify whether no call made to other language
	// editions
	@Test(dataProvider = "otherLanguageData", priority = 8, groups = { "AdsRegression", "Desktop", "MobileWeb" })
	public void verifyNoCallMadeToOtherLanguageEditions(String URL) {
		getDriver().manage().deleteAllCookies();
		StaticWait(3);
		getServer().newHar();
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD"))
			URL = URL.replace("medscape", env + "medscape");

		getURL(URL);
		waitForAdCallFound();
		if (!is404(getDriver().getTitle()) && !isLoginPage()) {
			if (verifySpecificCallPresence(dmdCallURL))
				generatePassReportWithNoScreenShot("DMD call not made for Other Language Edition " + URL);
			else
				generateFailReport("DMD call made for Other Language editions " + URL);
		}
	}

	@DataProvider
	public String[] otherLanguageData() {
		return new String[] { "https://deutsch." + env + "medscape.com/", "https://espanol." + env + "medscape.com/" };
	}

	// PPE-151398 : To verify DMD call not made for subsequent page access
	@Test(priority = 5, groups = { "AdsRegression", "Desktop", "MobileWeb" })
	public void verifyNoCallMadeToSubSequentPageAcess() {
		getServer().newHar();
		String temp = article + "?faf=1";
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD"))
			temp = temp.replace("medscape", env + "medscape");

		getURL(temp);
		waitForAdCallFound();
		if (!is404(getDriver().getTitle()) && !isLoginPage()) {
			if (!verifySpecificCallPresence(dmdCallURL))
				generatePassReportWithNoScreenShot("DMD call not made for Sub-sequent Page access");
			else
				generateFailReport("DMD call made for Sub-sequent Page access");
		}
	}

	// PPE-151401 : To verify the Data recorded for DMD
	@Test(priority = 4, groups = { "AdsRegression", "Desktop", "MobileWeb" })
	public void verifyDataRecorded() {
		String processCallData = "";
		if (dmdCallURL != null && (!dmdCallURL.isEmpty()))
			processCallData = getResponseForSpecificCall(dmdCallURL);
		try {
			String ip_id = StringUtils.substringBetween(processCallData, "ip_id\":", ",");
			String mac_id = StringUtils.substringBetween(processCallData, "mac_id\":", ",");
			String aim_id = StringUtils.substringBetween(processCallData, "aim_id\":", ",");
			String profile_id = StringUtils.substringBetween(processCallData, "profile_id\":", ",");
			String organization_id = StringUtils.substringBetween(processCallData, "organization_id\":", ",");
			String property_id = StringUtils.substringBetween(processCallData, "property_id\":", ",");
			String signal = StringUtils.substringBetween(processCallData, "signal\":", "}") + "}";
			generatePassReportWithNoScreenShot("DMD call response loaded properly" + "\nIP_ID: " + ip_id + "\nMAC_ID: "
					+ mac_id + "\nAIM_ID" + aim_id + "\nProfile_ID" + profile_id + "\nOrganization ID: "
					+ organization_id + "\nProperty ID: " + property_id + "\nSignal: " + signal);

		} catch (NullPointerException e) {
			generateFailReport("Some of the details Missed in DMD call, " + processCallData);
		}

	}

	// PPE-151414 : To verify whether NPI value tracked in Local storage or not

	@Test(priority = 3, groups = { "AdsRegression", "Desktop", "MobileWeb" })
	public void verifyNPIDetailsInLocalStorage() {
		String NPI = "";
		try {
			NPI = getConsoleValue("window.localStorage.getItem('dmd_did')");
		} catch (WebDriverException e) {

		} catch (NullPointerException ee) {

		}
		String encodedNPI = "";
		try {
			encodedNPI = Base64.getEncoder().encodeToString(userNPI.getBytes());
		} catch (NullPointerException e) {

		}
		try {
			Assert.assertEquals(NPI, encodedNPI);
			generatePassReportWithNoScreenShot("NPI value tracked properly under local storage");
		} catch (AssertionError e) {
			generateFailReport("NPI value wrongly tracked under local storage," + "value is " + NPI);
		}
	}

	@Test(priority = 6, groups = { "AdsRegression", "Desktop", "MobileWeb" })
	public void verifyDIDUnderAdCall() {
		getServer().newHar();
		String didValue = null;
		String temp = article + "?faf=1";
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD"))
			temp = temp.replace("medscape", env + "medscape");

		getURL(temp);
		waitForAdCallFound();
		if (!is404(getDriver().getTitle()) && !isLoginPage() && (userNPI != null)) {
			try {
				String custParams = getSpecificKeyFromSecurePubadCall("cust_params");
				didValue = StringUtils.substringBetween(custParams, "did=", "&");
				Assert.assertTrue(didValue.equalsIgnoreCase(userNPI));
				generatePassReportWithNoScreenShot(
						"NPI value tracked properly under Ads call in subseqent page access");
			} catch (AssertionError e) {
				generateFailReport("NPI value not tracked properly under Ads call in subseqent page access"
						+ ", Value Tracked is " + didValue);
			} catch (Exception e) {
				generateFailReport("Issue while verfying NPI value in Ad call" + e);
				e.printStackTrace();
			}
		} else {
			if ((userNPI == null)) {
				generateFailReport("Issue while verfying NPI value, NPI value did not find.");
			}
		}
	}

	@Test(enabled = false)
	public void mainTest() {

		// verify the DMD call made or not
		verifyDMDCall();

		// Verifying whether DMD identifies user and validating user details
		verifyDMDIdentifiesValidUser();

		// Verifying the Local Storage Value
		verifyNPIDetailsInLocalStorage();

		// Verify whether data recorded in DMD call correct or not
		verifyDataRecorded();

		// Verifying whether no call made in subsequent page access
		verifyNoCallMadeToSubSequentPageAcess();

		// Verifying DID value under Ad call in subsequent page access
		verifyDIDUnderAdCall();

		// Verifying whether no call made after logged out session
		verifyNoCallMadeForLoggedInAndLoggedOutSessions();

		// Verifying call made to DMD in other language editions
		verifyNoCallMadeToOtherLanguageEditions(otherLangEdition);
		verifyNoCallMadeToOtherLanguageEditions(otherLangEdition1);
	}
}