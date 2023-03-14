package com.webmd.ads.regression;

import static com.webmd.general.common.UtilityMethods.getException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.common.AdsConstantns;
import com.webmd.general.common.XlRead;
import com.webmd.general.objectrepo.LoginPageObjects;

import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;

/**
 * 
 * @author tnamburi
 *
 *         PPE-199113: LiveRamp - Pass values from Passback URL to the DFP Ad
 *         Call
 * 
 *         Verify whether values stored in the cookie properly or not. Is it in
 *         scope? Verify whether user details tracked in all the ad calls or not
 *         (not mandatory for first ad call on the session) Verify whether user
 *         details are consistent throughout the session or not(Scope: Lazyload,
 *         Next click, Next page access, media net refresh) Verify whether no
 *         values tracked in ad call once user logged in Verify whether no
 *         values tracked in ad call for logged-out user
 */

public class LiveRampFeature extends AdsCommon {

	@BeforeClass(alwaysRun = true)
	public void clearingCookies() {
		generateInfoReport("Launching fresh browser before LiveRampExecution");
		getDriver().quit();
		getDriver();
	}

	public static String[] actualKeys = { "egd", "tar", "tc", "lmid" },
			expectedKeys = { "Global_USER_ID_ENC", "CAMPAIGN_LIST", "CP_TACTIC_LIST", "LIST_MATCH_LIST" };
	public static HashMap<String, String> expected = new HashMap<String, String>();
	public static HashMap<String, String> actual = new HashMap<String, String>();
	public static int count = 0;

	// method to get the expected values
	public void getExpectedValues(String user) {
		if (env.contains("qa01") || env.contains("dev01"))
			getDriver().get("https://di.rlcdn.com/709366.gif?m=" + user);
		else if (env.contains("staging"))
			getDriver().get("https://di.rlcdn.com/709722.gif?m=" + user);
		else
			getDriver().get("https://di.rlcdn.com/709762.gif?m=" + user);
		String pageSrc = getDriver().getPageSource();
		generateInfoReport("PageSource: " + pageSrc);
		String value;
		generateInfoReport("Below are the Expected values from LiveRamp");
		for (String key : expectedKeys) {
			value = getEachExpecteValue(pageSrc, key);
			expected.put(key, value);
			generateInfoReport(key + " : " + value);
		}
	}

	public void getActualValues(String cust_params) {
		actual.clear();
		for (String key : actualKeys) {
			actual.put(key, getValueFromCustParams(cust_params, key));
		}
	}

	public String getEachExpecteValue(String pageSource, String key) {
		String value = "0";
		if (pageSource.contains(key))
			value = StringUtils.substringBetween(pageSource, key + "\": \"", "\"");
		return value;
	}

	// This method is to get the specific key's value from cust_params
	public String getValueFromCustParams(String cust_params, String key) {

		String value = StringUtils.substringBetween(cust_params, "&" + key + "=", "&");
		if (value == null || value.equals(""))
			value = "0";
		return value;
	}

	// This method is to validate all the required keys under cust_params
	public boolean compareActualAndExpected() {
		String actualValue = null;
		String exepectedValue = null;

		boolean flag = true;
		for (int i = 0; i < actualKeys.length; i++) {
			try {
				actualValue = actual.get(actualKeys[i]).toString();
				actualValue = URLDecoder.decode(actualValue, StandardCharsets.UTF_8.toString());

				try {
					Assert.assertNotNull(actualValue);
					if (actualValue.equals(""))
						actualValue = "0";
				} catch (NullPointerException e) {
					actualValue = "0";
				} catch (AssertionError e) {
					actualValue = "0";
				}

				if (actualKeys[i].equals("tar") || actualKeys[i].equals("tc")) {
					exepectedValue = StringUtils.substringBetween(getDriver().getPageSource(), actualKeys[i] + "\":\"",
							"\",");
					if (exepectedValue.equals("0"))
						exepectedValue = expected.get(expectedKeys[i]);
				} else
					exepectedValue = expected.get(expectedKeys[i]);

				Assert.assertEquals(exepectedValue, actualValue);
				generatePassReport("Validating " + expectedKeys[i] + " : " + actualKeys[i] + " and values are same");
			} catch (AssertionError e) {
				generateFailReport("Failed to compare the keys " + expectedKeys[i] + " : " + actualKeys[i]
						+ "Values are ***" + exepectedValue + "***" + actualValue);
				flag = false;
			} catch (UnsupportedEncodingException e) {
				generateFailReport("Failed while decrypting the Actual value");
			} catch (Exception e) {
				generateFailReport("Exception while comparing atual vs expected" + e.toString());
			}
		}
		return flag;
	}

	// This method is to make sure whether each key loaded once
	public void verifyCountEachKey(String cust_params) {
		for (int i = 0; i < actualKeys.length; i++) {
			int count = 0;
			try {
				count = cust_params.split(actualKeys[i]).length;
				Assert.assertEquals(count, 2);
				generatePassReportWithNoScreenShot(actualKeys[i] + " loaded once under cust params");
			} catch (AssertionError e) {
				generateFailReport(actualKeys[i] + " loaded " + (count - 1) + " times in cust_params: " + cust_params);
			}
		}
	}

	public void verifyAdCall(List<HarEntry> entries) {
		int count = 0;
		boolean flag = false;
		for (HarEntry entry : entries) {
			if (entry.getRequest().getUrl().contains(AdsConstantns.AD_CALL)) {
				List<HarNameValuePair> queryParams = entry.getRequest().getQueryString();
				for (HarNameValuePair harNameValuePair : queryParams) {
					if (harNameValuePair.getName().trim().equalsIgnoreCase("cust_params")) {
						flag = true;
						count++;
						generateInfoReport("Validating Ad call number" + count);
						String cust_params = harNameValuePair.getValue().trim();
						getActualValues(cust_params);
						verifyCountEachKey(cust_params);
						try {
							Assert.assertTrue(compareActualAndExpected());
							generatePassReportWithNoScreenShot("All keys tracked properly in ad call");
						} catch (AssertionError e) {
							generateFailReport("Mismatch in keys observed for ad call" + cust_params);
						}
					}
				}
			}
		}
		if (!flag)
			generateInfoReport("No Ad call observed after the Event");
	}

	public void loginHere(String user, String pwd) {
		getURLThrowsError("https://login." + env.replace("staging.", "") + "medscape.com/login/sso/getlogin?ac=401");

		waitForElement(LoginPageObjects.loginButton, 120);
		type(LoginPageObjects.userName, user, "Username");
		type(LoginPageObjects.password, pwd, "Password");

		try {
			getDriver().findElement(LoginPageObjects.password).submit();
			generateInfoReport("Submit login perfomed");
		} catch (Exception e) {
			generateInfoReport("Submit login not perfomed <br>" + getException(e));
		}
	}

	@DataProvider
	public String[][] dataProvider() {
		String[][] users = { { "infosession33", "medscape" } };
		/*
		 * 
		 */
		return users;
	}

	private boolean omniture(List<HarEntry> entries) {
		int count = 0;
		boolean flag = false;
		String guid = expected.get("Global_USER_ID_ENC");
		generateInfoReport("Searching for guid : " + guid);
		for (HarEntry entry : entries) {
			List<HarNameValuePair> queryParams = entry.getRequest().getQueryString();
			for (HarNameValuePair param : queryParams) {
				if (param.getValue().equals(guid)) {
					generateInfoReport(param.getName() + " : " + param.getValue());
					generateInfoReport("GUID is observed in : " + entry.getRequest().getUrl());
					count++;
					break;
				}
			}
		}
		if (count > 0) {
			flag = true;
			generateBoldReport("GUID displayed " + count + " times after the event performed");
		}
		return flag;
	}

	public String[] urls() {
		return new String[] { "https://www.env.medscape.com/cardiology",
				"https://www.env.medscape.com/viewarticle/895088?faf=1",
				"https://www.env.medscape.com/viewarticle/894435", "https://reference.env.medscape.com/",
				"https://reference.env.medscape.com/drugs", "https://www.env.medscape.org/",
				"http://www.env.medscape.org/viewarticle/895766?faf=1", "https://emedicine.env.medscape.com/",
				"https://emedicine.env.medscape.com/article/2500076-overview?faf=1",
				"https://reference.env.medscape.com/drug/abreva-docosanol-topical-343510?faf=1" };
	}

	private String getLiverampCallValue() {
		if (env.startsWith("q"))
			return "di.rlcdn.com/709366";
		else if (env.contains("staging"))
			return "di.rlcdn.com/709722";
		else
			return "di.rlcdn.com/709762";
	}

	public void verifyOmniture(List<HarEntry> entries, boolean flag) {
		generateBoldReport("Validating Omniture");
		try {
			if (flag) {
				Assert.assertTrue(omniture(entries));
				generatePassReportWithNoScreenShot("Omnitre expected to load and is loaded");
			} else {
				Assert.assertFalse(omniture(entries));
				generatePassReportWithNoScreenShot("Omnitre expected not to load and is not loaded");
			}
		} catch (AssertionError e) {
			if (flag)
				generateFailReport("Omnitre expected to load and is not loaded");
			else
				generateFailReport("Omnitre expected not to load and is loaded");
		}

	}

	// Method to check call made on each pillar or not for first access
	private void verifyPixelOnAllPillars() {
		String[] URLs = { "https://www.env.medscape.com/cardiology", "https://reference.env.medscape.com/",
				"https://emedicine.env.medscape.com/", "https://www.env.medscape.org/" };
		for (String URL : URLs) {
			generateInfoReport("Validating the pillar with URL: " + URL);
			getServer().newHar();
			getDriver().get(URL.replace("env.", env));
			waitForPageLoaded();
			List<HarEntry> entries = getServer().getHar().getLog().getEntries();

			verifyPixelMadeInitial(entries);
			generateBoldReport("Validating Omniture in intial page access");
			verifyOmniture(entries, true);
			verifyAdCall(entries);
		}
	}

	private void verifyNoPixelMadeLater(List<HarEntry> entries) {
		boolean flag = false;
		for (HarEntry entry : entries) {
			if (entry.getRequest().getUrl().contains(getLiverampCallValue())) {
				generateFailReport("Liveramp pixel call made in the later session");
				flag = true;
				break;
			}
		}
		if (!flag)
			generatePassReportWithNoScreenShot("Liveramp pixel call not made in later session");
	}

	private void verifyPixelMadeInitial(List<HarEntry> entries) {
		boolean flag = false;
		int count = 0;
		String liveRampValue = getLiverampCallValue();
		for (HarEntry entry : entries) {
			if (entry.getRequest().getUrl().contains(liveRampValue)) {
				count++;
				flag = true;
			}
		}
		if (flag) {
			if (count == 1)
				generatePassReportWithNoScreenShot("Liveramp pixel call made in earlier session");
			else
				generateInfoReport("LiveRamp pixel tracked" + count + " times");
		} else
			generateInfoReport("LiveRamp pixel call not made in earlier session");
	}

	private String getLiveRampUserData() {
		String user = null;
		String[][] data = XlRead.fetchDataExcludingFirstRow("AdsRegression.xls", "LiveRamp");
		Random rand = new Random();
		int userNumber = rand.nextInt(100);
		user = data[userNumber][0];
		return user;
	}
	/*
	 * Access liveramp url save expected values per session have boolean values
	 * for each pillar Make sure for each pillar first page access might not
	 * have values and liveramp will trigger its call Get ad call and check all
	 * the values get ssl calls and check the values
	 */

	@Test(groups = { "AdsRegression", "Desktop", "MobileWeb", "liveRampFeature" })
	public void validateLiveRampTotalFeature() {
		String user = getLiveRampUserData();
		getDriver();
		getExpectedValues(user);
		generateBoldReport("Validating all pillars making liveramp call in the first access");
		verifyPixelOnAllPillars();
		boolean isLoggedIn = false;
		do {
			for (String URL : urls()) {
				URL = URL.replace("env.", env);
				generateInfoReport("Validating the URL " + URL);
				getServer().newHar();
				getURL(URL);
				waitForPageLoaded();
				List<HarEntry> entries = getServer().getHar().getLog().getEntries();
				generateBoldReport("Validating Omniture in later page access");
				verifyOmniture(entries, false);
				verifyAdCall(entries);
				verifyNoPixelMadeLater(entries);
			}
			if (!isLoggedIn) {
				loginHere("infosession33", "medscape");
				generateBoldReport("Performing Login to validate");
				isLoggedIn = true;
			} else {
				logout("infosession33");
				generateBoldReport("Performing Logout to validate");
				isLoggedIn = false;
			}
		} while (isLoggedIn);
	}

}
