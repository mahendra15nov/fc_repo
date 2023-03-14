package com.webmd.ads.smoke;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import com.webmd.common.AdsCommon;
import com.webmd.general.common.UtilityMethods;

/**
 * Ads Smoke test suite, which can be validate whether the Ads are loaded on the
 * page or not, Ad call is triggered or not, expected pos are encountered or not
 * 
 * @author amahendra Updated on 29/07/2019-merges the AdsCommonn changes and
 *         updated the few requirements ad per latest changes
 */
@Listeners(com.webmd.general.common.Listener.class)
public class AdsSanity extends AdsCommon {

	List<String> allAdsPos = new ArrayList<String>();
	String adCall = "securepubads.g.doubleclick.net/gampad/ads?";
	JavascriptExecutor jse;
	List<String> positionsFromAds2Ignore = null;
	String[] aadGroup = { "ad_bc", "ad_opt", "ad_ex0", "ad_ex1", "ad_ex2", "ad_ex3", "ad_ex4", "ad_ex5", "ad_ex6",
			"adEex7", "ad_ex8" };
	String[] aadH = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18",
			"19", "20", "21", "22", "23" };
	int clazyloadedCall = 0;
	HashMap<String, List<WebElement>> lazyLoadedE = new HashMap<>();
	HashMap<String, HashMap<String, ArrayList<String>>> pageDetails = new HashMap<>();
	ArrayList<HashMap<String, String>> pbrValuesList = new ArrayList<>();
	JSONObject reqHeadersTarget = null;
	JSONObject webSegVarsTarget = null;
	JSONObject pageSegVarsTarget = null;
	JSONObject userSegVarsTarget = null;
	String fileName = "AdsSanity.xls";
	// POS_SZS, Debug
	String sheetName = "POS_SZS";
	String procUserName = "";

	/**
	 * Login
	 */
	@BeforeClass(groups = { "AdsSmoke", "AdsRegression", "Desktop", "MobileWeb" })
	public void loginPerform() {
		startServer();
		// Preparation of test data
		pageDetails = getTestData(fileName, sheetName);

		login(getProperty("username"), getProperty("password"));
	}

	/**
	 * Cleaning up
	 */
	@AfterMethod(groups = { "AdsSmoke", "AdsRegression", "Desktop", "MobileWeb" })
	public void resetGValues() {
		if (positionsFromAds2Ignore != null)
			positionsFromAds2Ignore.clear();
		if (allAdsPos != null)
			allAdsPos.clear();
		clazyloadedCall = 0;
		if (lazyLoadedE != null)
			lazyLoadedE.clear();
		if (pbrValuesList != null)
			pbrValuesList.clear();
		super.element = null;
		getServer().newHar();

	}

	/**
	 * reset the har
	 */
	@BeforeMethod(groups = { "AdsSmoke", "AdsRegression", "Desktop", "MobileWeb" })
	public void resetV() {
		getServer().newHar();
		login(getProperty("username"), getProperty("password"));
	}

	/**
	 * Stopping the browserMob and closing the browsers
	 */
	@AfterClass(groups = { "AdsSmoke", "AdsRegression", "Desktop", "MobileWeb" })
	public void closeBrowser() {
		try {
			getDriver().quit();
			getServer().stop();
		} catch (Exception e) {
		}
	}

	/**
	 * Verification of Ads pos and sizes w.r.t input data from Ad call
	 * Verification of Ad whether it is laoded on the page or not Verification
	 * of ads2ignore list
	 * 
	 * @param url
	 */
	@Test(dataProvider = "dataProvider", groups = { "AdsSmoke", "AdsRegression", "Desktop", "MobileWeb" })
	public void adsSmokeTest(String url) {

		getDriver();
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD") && !env.equalsIgnoreCase("STAGING"))
			url = url.replace("medscape", env + "medscape").replace("staging.", "");
		if (url.contains("registration_ab.do") || url.contains("login")) {
			logout(getProperty("username"));
		}
		jse = (JavascriptExecutor) getDriver();
		if (pageDetails.get(url).get("PageType").get(0).contains("InfiniteSlideshow")) {
			generateSkipReport("Infinite slide shows will be handles in next version");
		} else {
			if (pageDetails.get(url).get("PageType").get(0).contains("ProclivityTestUrl")) {
				// Proclivity test
				logout(getProperty("username"));
				// proclivity login
				proclivityLogin();
				// verify proclivity data
				verifyProclivityDetails(url);
				logout(procUserName);
			} else {
				getURL(url);
				generateInfoReport("****** Ads pos and szs w.r.t expected and actual data verification ******");
				adsPosAndSzsValidation(url);
				generateInfoReport("****** Verification of Ads2ignore ******");
				verifyAds2IgnoreFunctionality();
			}
		}
		// throwErrorOnTestFailure();
	}

	/**
	 * Verification of Ad pos and sizes w.r.t input data from Ad call.
	 * 
	 * @param url
	 */
	public void adsPosAndSzsValidation(String url) {

		if (!is404(getDriver().getTitle())) {
			generateInfoReport("Page URL : " + url);
			generateInfoReport("Device : " + pageDetails.get(url).get("Device"));
			generateInfoReport("Event Type : " + pageDetails.get(url).get("EventType"));
			generateInfoReport("Position : " + pageDetails.get(url).get("Position"));
			generateInfoReport("Sizes : " + pageDetails.get(url).get("Sizes"));
			generateInfoReport("Page Type : " + pageDetails.get(url).get("PageType"));
			generateInfoReport("IUParts : " + pageDetails.get(url).get("IUParts"));
			boolean call = false;
			if (breakPoint.equals("4")) {
				call = isAdCallExpectedInDesktop(pageDetails.get(url).get("PageType").get(0));
			} else {
				call = isAdCallExpectedInMobile(pageDetails.get(url).get("PageType").get(0));
			}
			if (call) {
				waitForAdCallFound();
			}

			// Getting the ads2ignore list
			positionsFromAds2Ignore = new ArrayList<String>();
			// Gettign Ads2Ignore list
			if (getAdsToIgnorePositionsFromConsole() != null) {
				positionsFromAds2Ignore = Arrays.asList();
			} else {
				generateReport(!numberOfAdCallsValidation(), "ads2ignore has been returned null.",
						"ads2ignore has been returned null.");
			}
			try {
				// Page Load verification
				pageLoadVerification(url);
				getServer().newHar();
			} catch (Exception e) {

			}
			try {
				if (pageDetails.get(url).get("EventType").contains("LazyLoad")) {
					getServer().newHar();
					/// lazy load verification
					lazyLoadVerification(url);
				}
			} catch (Exception e) {

			}
		} else

		{
			generateInfoReport(url + " has not fond.");
		}
	}

	/**
	 * Smoke test in Page Loaded Ad call
	 * 
	 * @param url
	 */
	private void pageLoadVerification(String url) {
		// Verify IU Parts
		generateInfoReport("***** IU Parts Validation ******");
		verifyIuParts(pageDetails.get(url).get("IUParts").get(0));
		// verification of pass page number in Ad call
		generateInfoReport("***** Pass Page Number Validation ******");
		validatePassPageNumberInAdCall();
		// Adomik key value Ad_group and ad_h values verification
		generateInfoReport("***** Adomik Key values Validation ******");
		verifyadGroupAndadHValues();
		// Verification of Ad pos w.r.t to expected values and Ad call
		generateInfoReport("***** Pos and Sizes w.r.t input data Validation ******");
		verifyPosAndSzsInAdCallWithInputData(url, true);
		// Preparing the pos list
		prepareAdsPosList();
	}

	/**
	 * Smoke test in Lazy loaded Ad call
	 * 
	 * @param url
	 */
	private void lazyLoadVerification(String url) {
		generateInfoReport("Expected Lazy Load Ad call details : ");
		int index = pageDetails.get(url).get("EventType").indexOf("LazyLoad");
		generateInfoReport("Expected Pos : " + pageDetails.get(url).get("Position").get(index));
		generateInfoReport("Expected Szs : " + pageDetails.get(url).get("Sizes").get(index));

		for (String pos : Arrays.asList(pageDetails.get(url).get("Position").get(index))) {
			lazyLoadedE.put(pos,
					getlazyLoadedPos("//div[contains(@id,'ads-pos-" + pos + "') and contains(@class,'lazyloaded')]"));
		}
		int height = getDriver().manage().window().getSize().getHeight();
		int scroll = height / 50;
		int s = scroll;
		int max = 200;
		WebElement footer = null;
		boolean isAdCallFnd = false;
		try {
			footer = getDriver().findElement(By.xpath("//div[contains(@class,'footer_legal-text resp-container')]"));
		} catch (NoSuchElementException e) {
			max = 20;
		}
		for (int i = 0; i < max; i++) {
			boolean temp = false;
			getServer().newHar();
			StaticWait(2);
			s = s + scroll;
			jse.executeScript("window.scrollBy(0," + s + ")");
			if (numberOfAdCallsValidation()) {
				if (isVisibleInViewport(footer)) {
					StaticWait(5);
					temp = true;
				}
				// Verify IU Parts
				if (!pageDetails.get(url).get("IUParts").get(0).contains("NA"))
					verifyIuParts(pageDetails.get(url).get("IUParts").get(0));
				// verification of pass page number in Ad call
				validatePassPageNumberInAdCall();
				// Ad_group and ad_h values verification
				verifyadGroupAndadHValues();

				verifyPosAndSzsInAdCallWithInputData(url, false);
				isAdCallFnd = true;
				// Preparing the pos list
				prepareAdsPosList();
				clazyloadedCall++;
				if (temp)
					break;
			}
		}
		generateReport(isAdCallFnd, "Lazy Loaded Ad call has been found.", "Lazy Loaded Ad call has not been found.");
	}

	// Method to validate ad position numbers from ad call, it will check
	// Desktop should have 3 digits and other should have 4

	private void validateAdpositionIds(List<String> adPos) {
		generateInfoReport("Validating ad position length/size");
		if (adPos != null) {
			for (String pos : adPos) {
				if (breakPoint.equals("1")) {
					if (pos.length() != 4) {
						generateFailReport("Observed a non Mobile position in ad call" + adPos);
					}
				} else {
					if (pos.length() != 3) {
						generateFailReport("Observed a non desktop position in ad call: " + adPos);
					}
				}
			}
		}
	}

	/*
	 * Validate ads2Ignore with respect to on page ads
	 */
	private void validateAds2IgnoreWithRespectAdsLoadedOnPage(List<String> ads2Ignore) {
		generateInfoReport("Validating on page ads with Ads2Ignore");
		List<String> adsOnPage = getListOfLoadedAdsOnPage();
		boolean flag = true;

		for (String pos : ads2Ignore) {
			if (adsOnPage.contains(pos)) {
				generateFailReport(pos + " shown on page and also listed in ads2Ignore");
				flag = false;
			}
		}
		if (flag) {
			generatePassReport("All the ads on page not listed in Ads2Ignore");
		}
	}

	/**
	 * Gather the all Ad pos list
	 */
	private void prepareAdsPosList() {
		if (numberOfAdCallsValidation())
			allAdsPos.addAll(getPositionsFromPrevScp(getSpecificKeyFromSecurePubadCall("prev_scp")));
	}

	/*
	 * This is the test to validate whether all the sizes mentioned in
	 * ads2_ignore were not loaded in any ad call and page
	 */
	private void verifyAds2IgnoreFunctionality() {
		// Validating the pos length validation
		validateAdpositionIds(allAdsPos);
		// Validating the pos w.r.t ads2ignore list
		validateAds2IgnoreWithRespectAdsLoadedOnPage(positionsFromAds2Ignore);

		for (String pos : positionsFromAds2Ignore) {
			generateReport(!allAdsPos.contains(pos), "ads2ignore pos : " + pos + " is not appears in Ad call.",
					"ads2ignore pos : " + pos + " is appears in Ad call");
		}
	}

	/**
	 * Ads pos and sizes validation
	 * 
	 * @param url
	 * @param pageLoad
	 */
	private void verifyPosAndSzsInAdCallWithInputData(String url, boolean pageLoad) {

		ArrayList<String> pos = pageDetails.get(url).get("Position");
		ArrayList<String> szs = pageDetails.get(url).get("Sizes");
		ArrayList<String> eventType = pageDetails.get(url).get("EventType");

		// Getting the prev_scp from Ad call.
		String prevScp = getSpecificKeyFromSecurePubadCall("prev_scp");
		generateInfoReport("prev_scp : " + prevScp);
		if (!prevScp.isEmpty()) {
			if (!prevScp.isEmpty()) {

				int posSize = 0;
				int szsSize = 0;
				for (int k = 0; k < pos.size(); k++) {
					if (pageLoad) {
						if (eventType.get(k).contains("PageLoad")) {
							posSize++;
							szsSize++;
						}
					} else {
						if (eventType.get(k).contains("LazyLoad")) {
							posSize++;
							szsSize++;
						}
					}
				}

				String[] aPOs = prevScp.split("\\|");
				// Ad call pos count verification
				if (posSize == aPOs.length) {
					generatePassReportWithNoScreenShot("Expected pos's count - " + posSize
							+ " and Actual pos's count - " + aPOs.length + " are same.");
				} else {
					generateFailReport("Expected pos's count - " + posSize + " and Actual pos's count - " + aPOs.length
							+ " are different.");
				}
				// w.r.t to Excepted data
				for (int k = 0; k < pos.size(); k++) {
					if (pageLoad && eventType.get(k).contains("PageLoad")) {
						if (prevScp.contains(pos.get(k))) {
							generatePassReportWithNoScreenShot(
									"Page Loaded expected pos - " + pos.get(k) + " is found in Ad call data.");
						} else {
							generateFailReport(
									"Page Loaded expected pos - " + pos.get(k) + " is not found in Ad call data.");
						}
					} else {
						if (!pageLoad && eventType.get(k).contains("LazyLoad")) {
							if (prevScp.contains(pos.get(k))) {
								generatePassReportWithNoScreenShot(
										"Lazy Loaded expected pos - " + pos.get(k) + " is found in Ad call data.");
							} else {
								generateFailReport(
										"Lazy Loaded expected pos - " + pos.get(k) + " is not found in Ad call data.");
							}
						}
					}
				}
				// w.r.t to Ad call data
				for (int k = 0; k < aPOs.length; k++) {
					String actualPos = StringUtils.substringBetween(aPOs[k], "pos=", "&");

					WebElement ele = null;
					if (lazyLoadedE.get(actualPos) != null) {
						ele = lazyLoadedE.get(actualPos).get(clazyloadedCall);
					}

					// verify the pos loaded on the page or not
					verifySpecificPositionLoadedOnPage(actualPos, ele);

					try {
						if (pageLoad && eventType.get(k).contains("PageLoad")) {
							if (pos.contains(actualPos)) {
								generatePassReportWithNoScreenShot(
										"Page Loaded Ad call pos - " + actualPos + " is found in expected data list.");
							} else {
								generateFailReport("Page Loaded Ad call pos - " + actualPos
										+ " is not found in expected data list.");
							}
						} else {
							if (!pageLoad && eventType.get(k).contains("LazyLoad")) {
								if (pos.contains(actualPos)) {
									generatePassReportWithNoScreenShot("Lazy Loaded Ad call pos - " + actualPos
											+ " is found in expected data list.");
								} else {
									generateFailReport("Lazy Loaded Ad call pos - " + actualPos
											+ " is not found in expected data list.");
								}
							}
						}
					} catch (IndexOutOfBoundsException e) {
						generateFailReport("Miss match in expected pos list and actual ad call pos list.");
					}
				}

				// verify the applicable sizes
				String prevIUSzs = getSpecificKeyFromSecurePubadCall("prev_iu_szs");
				generateInfoReport("prev_iu_szs : " + prevIUSzs);
				if (!prevIUSzs.isEmpty()) {
					if (!prevIUSzs.isEmpty()) {
						// getting the applicable sizes from Ad call.
						String[] aSzs = prevIUSzs.split(",");
						// Preparing the pos and szs mapping list w.r.t Ad
						// call
						// and Exp data
						HashMap<String, String> mapAdcallPosAndSzs = new HashMap<>();
						for (int p = 0; p < aSzs.length; p++) {
							String temp = StringUtils.substringBetween(aPOs[p], "pos=", "&");
							mapAdcallPosAndSzs.put(temp, aSzs[p]);
						}

						HashMap<String, String> mapExpPosAndSzs = new HashMap<>();
						for (int p = 0; p < pos.size(); p++) {
							if (pageLoad && eventType.get(p).contains("PageLoad"))
								mapExpPosAndSzs.put(pos.get(p), szs.get(p));
							else if (!pageLoad && eventType.get(p).contains("LazyLoad"))
								mapExpPosAndSzs.put(pos.get(p), szs.get(p));
						}
						// Applicable sizes verification w.r.t Ad call data
						// and
						// Expected input data.
						if (aSzs.length == szsSize) {
							generatePassReportWithNoScreenShot("Expected applicable sizes's count - " + aSzs.length
									+ " and Actual applicable sizes's count - " + szsSize + " are same.");
						} else {
							generateFailReport("Expected applicable sizes's count - " + aSzs.length
									+ " and Actual applicable sizes's count - " + szsSize + " are different.");
						}

						// w.r.t to Excepted data
						if (posSize == aPOs.length) {
							generatePassReportWithNoScreenShot("Ad call pos's count - " + posSize
									+ " and Actual applicable sizes's count - " + aPOs.length + " are same.");
						} else {
							generateFailReport("Ad call pos's count - " + posSize + " and its size's count - "
									+ aPOs.length + " doesn not match.");
						}

						for (String adCallPos : mapAdcallPosAndSzs.keySet()) {
							generateInfoReport("Verifying the sizes data for pos -" + adCallPos);
							String[] aAdCallSzsList = null;
							String[] aExpSzsList = null;
							try {
								aAdCallSzsList = mapAdcallPosAndSzs.get(adCallPos).split("\\|");
								aExpSzsList = mapExpPosAndSzs.get(adCallPos).split("\\|");
							} catch (NullPointerException e) {

							}
							// Verify the Applicable sizes w.r.t Ad call
							// data
							// and Input data
							try {
								if (aAdCallSzsList != null && aExpSzsList != null) {
									if (aAdCallSzsList.length == aExpSzsList.length) {
										generatePassReportWithNoScreenShot("Ad call szs count - "
												+ aAdCallSzsList.length + " and Input data szs count - "
												+ aExpSzsList.length + " are same.");
									} else {
										generateFailReport("Ad call szs count - " + aAdCallSzsList.length
												+ " and Input data szs count - " + aExpSzsList.length
												+ " doesnt match.");
									}
								} else {
									generateFailReport("Missmatch in Expected date vs Ad call data.");
								}
							} catch (NullPointerException e) {
								generateFailReport("Miss match in expected sizes list and Ad call sizes list.");
							}
							// Applicable szs w.r.t input data
							if (aAdCallSzsList != null && aExpSzsList != null) {
								for (String inputSzs : aExpSzsList) {
									if (Arrays.toString(aAdCallSzsList).contains(inputSzs)) {
										generatePassReportWithNoScreenShot(
												"Expected " + inputSzs + " size is present in Ad call data.");
									} else {
										generateFailReport(
												"Expected " + inputSzs + " size is not present in Ad call data.");
									}
								}
							} else {
								generateFailReport(
										"aAdCallSzsList: " + aAdCallSzsList + " or aExpSzsList: " + aExpSzsList);
							}
						}

					} else {
						generateFailReport("prev_iu_szs is found with empty data in Ad call");
					}
				}

			} else {
				generateFailReport("prev_scp is found with empty data in Ad call");
			}
		} else {
			for (int i = 0; i < pos.size(); i++) {
				if (pos.get(i).contains("NA") && szs.get(i).contains("NA")) {
					generatePassReportWithNoScreenShot("Ad call is not present for " + url);
				} else {
					generateFailReport("We are missing some thing here w.r.t to Input data.");
				}
			}
		}
	}

	/**
	 * Validating the pass page number
	 */
	private void validatePassPageNumberInAdCall() {
		String custParams = getSpecificKeyFromSecurePubadCall("cust_params");
		// Validating page loaded ad call
		String pgValueURL = "";
		String pgValueAdCall = "";
		if (numberOfAdCallsValidation()) {
			pgValueURL = getPageNumberFromURL();
			pgValueAdCall = getPageNumberFromCustParams(custParams);
			generateReport(pgValueAdCall.equals(pgValueURL),
					"Actual pg value : " + pgValueAdCall + ", Expected pg value: " + pgValueURL
							+ " are loaded properly under cust_params in Ad call",
					"Actual pg value : " + pgValueAdCall + ", Expected pg value: " + pgValueURL
							+ " are not loaded properly under cust_params in Ad call");
		}
	}

	// This method is to get pagenumber from url
	private String getPageNumberFromURL() {
		String pageNumber = null;
		if (getDriver().getCurrentUrl().contains("_") && !getDriver().getCurrentUrl().contains("#"))
			pageNumber = getDriver().getCurrentUrl().split("_")[1];
		else
			pageNumber = "1";

		return pageNumber;
	}

	// This method is to get page number from cust params
	private String getPageNumberFromCustParams(String custParams) {
		String pageNumber = "";

		if (!custParams.contains("pg="))
			return pageNumber;
		else
			pageNumber = StringUtils.substringBetween(custParams, "&pg=", "&");

		return pageNumber;
	}

	/**
	 * Verification of IU Parts valeus
	 * 
	 * @param expected
	 */
	private void verifyIuParts(String expected) {
		String actual = null;
		if (numberOfAdCallsValidation()) {
			try {
				actual = getSpecificKeyFromSecurePubadCall("iu_parts");
				generateReport(actual.contains(expected),
						"Actual: " + actual + ",Expected: " + expected + " iu_parts tracked properly",
						"Actual: " + actual + ",Expected: " + expected + " iu_parts are different.");
			} catch (Exception e) {
				generateFailReport("Unknown exception \n" + UtilityMethods.getException(e));
			}
		} else {
			generateInfoReport("Ad call is not found for " + getDriver().getCurrentUrl());
		}
	}

	/**
	 * Verification of Adomik key values
	 */
	private void verifyadGroupAndadHValues() {

		String prevScp = getSpecificKeyFromSecurePubadCall("prev_scp");

		if (prevScp != null && (!prevScp.isEmpty())) {
			if (prevScp.contains("ad_group=") && prevScp.contains("ad_h=")) {
				generateInfoReport("prev_scp : " + prevScp);
				StringTokenizer str = new StringTokenizer(prevScp, "|");

				while (str.hasMoreTokens()) {
					String temp = "";
					String pos = str.nextToken();
					String adGroupValue = "";
					String adHValue = "";
					String posValue = StringUtils.substringBetween(pos, "pos=", "&");
					if (posValue == null || posValue.isEmpty()) {
						try {
							posValue = pos.substring(pos.indexOf("pos=") + 5);
						} catch (StringIndexOutOfBoundsException e) {

						}
					}
					if (!(posValue == null || posValue.isEmpty())) {
						temp = temp + "pos - " + posValue + " ==> ";
						if (pos.contains("ad_group")) {
							adGroupValue = StringUtils.substringBetween(pos, "ad_group=", "&");
							if (adGroupValue == null || adGroupValue.isEmpty()) {
								adGroupValue = pos.substring(pos.indexOf("ad_group=") + 9);
							}
							if (!(adGroupValue == null || adGroupValue.isEmpty())) {
								boolean adGroupPresent = false;
								for (String ad_group : aadGroup) {
									if (adGroupValue.equals(ad_group)) {
										temp = temp + adGroupValue + " ==> ";
										adGroupPresent = true;
									}
								}
								if (adGroupPresent) {
									generatePassReportWithNoScreenShot(
											"ad_group value has found within the range for position - " + posValue);
								} else {
									generatePassReportWithNoScreenShot(
											"ad_group value is not found within the range for pos - " + posValue
													+ " ad_group value is " + adGroupValue);
								}

							} else {
								generateFailReport("ad_group value is empty / null for position -  " + posValue);
							}

						} else {
							generateFailReport("ad_group key-value did not find for position - " + posValue);
						}

						if (pos.contains("ad_h")) {
							adHValue = StringUtils.substringBetween(pos, "ad_h=", "&");
							if (adHValue == null || adHValue.isEmpty()) {
								adHValue = pos.substring(pos.indexOf("ad_h=") + 5);
							}
							if (!(adHValue == null || adHValue.isEmpty())) {
								boolean ad_hPresent = false;
								for (String ad_h : aadH) {
									if (adHValue.equals(ad_h)) {
										temp = temp + adHValue + "";
										ad_hPresent = true;
									}
								}
								if (ad_hPresent) {
									generatePassReportWithNoScreenShot(
											"ad_h value has found within the range for position - " + posValue);
								} else {
									generatePassReportWithNoScreenShot(
											"ad_h value is not found within the range for pos - " + posValue
													+ " ad_h value is " + adHValue);
								}

							} else {
								generateFailReport("ad_h value is empty / null for position -  " + posValue);
							}
						} else {
							generateFailReport("ad_h key-value did not find for position - " + posValue);
						}
					} else {
						generateFailReport("Ad position is not available under pre_scp.");
					}
					generateInfoReport(temp);
				}

			} else {
				generateFailReport("Could not find the 'ad_group' and 'ad_h' values in cust_params in ad call");
			}
		}
	}

	/**
	 * Description :
	 * 
	 * @param URL
	 *            : Its medscape URL
	 * @throws InterruptedException
	 */
	private void verifyProclivityDetails(String url) {
		getURL(url);
		// Verify the default adhesive footer changes
		if (!is404(getDriver().getTitle())) {
			if (!isLoginPage()) {

				waitForAdCallFound();

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

					// Reading the request headers
					generateBoldReport("***** reqHeaders *******");
					// String requestHeaders =
					generateInfoReport("reqHeaders ==> " + reqHeadersTarget.toString());

					String[] aProcKeys = { "device", "domain", "domainCategory", "ep" };
					verifyProcKeys(aProcKeys, reqHeadersTarget, "reqHeaders");
					// reading the webSegVars
					generateBoldReport("***** webSegVars *******");
					generateInfoReport("webSegVars ==> " + webSegVarsTarget.toString());

					String[] aProcKeys1 = { "envp", "pc", "spon" };
					verifyProcKeys(aProcKeys1, webSegVarsTarget, "webSegVars");

					// reading the pageSegVars
					generateBoldReport("***** pageSegVars *******");
					generateInfoReport("pageSegVars ==> " + pageSegVarsTarget.toString());

					String[] aProcKeys2 = { "art", "cg", "pub", "scg", "ssp" };
					verifyProcKeys(aProcKeys2, pageSegVarsTarget, "pageSegVars");

					// reading the userSegVars
					generateBoldReport("***** userSegVars *******");
					String userSegVars = StringUtils.substringBetween(pageSource, "\"userSegVars\":{", ",\"lazyLoad\"");
					generateInfoReport("userSegVars ==> " + userSegVarsTarget.toString());
					// Getting the pbr data from user Segvars
					generateBoldReport("***** pbr Values *******");
					String pbrValues = StringUtils.substringBetween(userSegVars, "\"pbr\":[", "]");

					if (pbrValues != null && (!pbrValues.isEmpty())) {
						generatePassReportWithNoScreenShot("pbr value are present ==> " + pbrValues);
					} else {
						generateFailReport("pbr values are empty.");
					}
					Pattern pattern = Pattern.compile("\\{(.*?)\\}");
					Matcher m = pattern.matcher(pbrValues);

					while (m.find()) {
						generateInfoReport(m.group(1));
						pbrValuesList.add(getPbrValues(m.group(1)));
					}
					if (!pbrValues.isEmpty() && (pbrValues != null)) {
						userSegVars = userSegVars.replace("[" + pbrValues + "]", "\"pbr\"");
					}

					String[] aProcKeys3 = { "dt", "usp", "pf" };
					verifyProcKeys(aProcKeys3, userSegVarsTarget, "userSegVars");

					// verifyin the proclivity Ad to be loaded on the page
					verifyProfAds();

				} else {
					generateFailReport(url + " has still required login though login performed aleardy.");
				}
			}
		} else {
			generateSkipReport(url + " is not a valid URL.");
		}
		// throwErrorOnTestFailure();
	}

	/**
	 * Ads smoke test functionality
	 */
	private void verifyProfAds() {
		// Preparing the proc map from Ad call.
		for (HashMap<String, String> prPos : prepareProcMap()) {
			for (HashMap<String, String> pbrValues : pbrValuesList) {
				if (pbrValues.get("a").equals(prPos.get("asid"))) {
					generatePassReportWithNoScreenShot("pos = " + prPos.get("pos") + " asid = " + prPos.get("asid")
							+ " is matched with proclivities p=>" + pbrValues.get("p") + " asis = "
							+ pbrValues.get("a"));

					// verify the applicable sizes
					if (prPos.get("szs").contains(pbrValues.get("w") + "x" + pbrValues.get("h"))) {
						generatePassReportWithNoScreenShot("Proclivities P = " + pbrValues.get("p") + "'s width="
								+ pbrValues.get("w") + " and height=" + pbrValues.get("h") + " are available in pos = "
								+ prPos.get("pos") + "'s applicable sizes = " + prPos.get("szs") + " list.");
					} else {
						generateInfoReport("Proclivities P = " + pbrValues.get("p") + "'s width=" + pbrValues.get("w")
								+ " and height=" + pbrValues.get("h") + " are not available in pos = "
								+ prPos.get("pos") + "'s applicable sizes = " + prPos.get("szs") + " list.");

					}

				} else {
					generateInfoReport("pos = " + prPos.get("pos") + " asid = " + prPos.get("asid")
							+ " is not matched with proclivities p=>" + pbrValues.get("p") + " asis = "
							+ pbrValues.get("a"));
				}
			}
		}

	}

	/**
	 * Preparation of expected pos and sizes data
	 * 
	 * @return - ArrayList with pos data
	 */
	private ArrayList<HashMap<String, String>> prepareProcMap() {
		ArrayList<HashMap<String, String>> procPos1 = new ArrayList<>();
		String[] aPos = getSpecificKeyFromSecurePubadCall("prev_scp").split("\\|");
		String[] aSzs = getSpecificKeyFromSecurePubadCall("prev_iu_szs").split(",");
		for (int i = 0; i < aPos.length; i++) {
			if (aPos[i].contains("asid")) {
				procPos1.add(getProcPoss(aPos[i], aSzs[i]));
			}
		}
		return procPos1;
	}

	/**
	 * Getting the pos and szs data from Ad call
	 * 
	 * @param pos
	 *            - pos value
	 * @param szs
	 *            - its szs
	 * @return
	 */
	private HashMap<String, String> getProcPoss(String pos, String szs) {
		HashMap<String, String> procMap = new HashMap<>();
		procMap.put("pos", StringUtils.substringBetween(pos, "pos=", "&"));
		procMap.put("szs", szs);
		procMap.put("asid", StringUtils.substringBetween(pos, "asid=", "&"));
		return procMap;
	}

	/**
	 * Getting the Pbr values if it is proclivity user
	 * 
	 * @param pbr
	 * @return
	 */
	private HashMap<String, String> getPbrValues(String pbr) {
		HashMap<String, String> pbrMap = new HashMap<>();
		String[] aPbr = pbr.split(",");
		for (String string : aPbr) {
			String[] as = string.split(":");
			pbrMap.put(as[0].replace("\"", ""), as[1].replace("\"", ""));
		}
		return pbrMap;
	}

	/**
	 * Verification of Proc data
	 * 
	 * @param aProcKeys
	 * @param obj
	 * @param varKeysType
	 */
	private void verifyProcKeys(String[] aProcKeys, JSONObject obj, String varKeysType) {
		for (String key : aProcKeys)
			if (obj.keySet().contains(key)) {
				generatePassReportWithNoScreenShot("Key " + key + " is found in " + varKeysType + ".");
			} else {
				generateFailReport("Key " + key + " is not found in " + varKeysType + ".");
			}
	}

	private void proclivityLogin() {
		switch (env) {
		case "":
		case "PROD":
		case "staging":
		case "STAGING":
			login("proclivitytest@gmail.com", "medscape");
			procUserName = "proclivitytest@gmail.com";
			break;
		case "qa01":
		case "QA01":
			login("ykutsal", "medscape");
			procUserName = "ykutsal";
			break;
		case "qa00":
		case "QA00":
			login("proctest1@gmail.com", "medscape");
			procUserName = "proctest1@gmail.com";
			break;
		case "dev01":
		case "DEV01":
			login("proclivitytest@gmail.com", "medscape");
			procUserName = "proclivitytest@gmail.com";
			break;
		default:
			login("proclivitytest@gmail.com", "medscape");
			procUserName = "proclivitytest@gmail.com";
		}
	}

	@DataProvider
	public String[] dataProvider() {
		return getURLs(fileName, sheetName);
	}

}
