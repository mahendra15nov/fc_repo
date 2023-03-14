package com.webmd.ads;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.relevantcodes.extentreports.LogStatus;
import com.webmd.common.DFPTCommons;
import com.webmd.general.common.ExtentTestManager;

import io.restassured.response.Response;

/**
 * DFPT Keys validation
 * 
 * @author amahendra
 * 
 * 
 *         Version V 1.0 Updated on 28 Aug 2019
 *
 */
@Listeners(com.webmd.general.common.Listener.class)
public class DFPTRegression extends DFPTCommons {

	public DFPTRegression() throws IOException {
		super();
	}

	List<String> failedMsg = new ArrayList<>();
	HashMap<String, String> expectedMapAdCall = new HashMap<>();
	HashMap<String, String> expectedUserTacticsMap = new HashMap<>();
	Map<String, Object> targetvalues = new HashMap<>();
	String adpredictionServiceEnv = "qa01.";
	String fileName = "AdsSanity.xls";
	// POS_SZS, Debug
	String sheetName = "POS_SZS";
	HashMap<String, HashMap<String, ArrayList<String>>> testData;

	String call = "http://adpredictionservice-app-" + adpredictionServiceEnv
			+ "prf.ma1.medscape.com:8080/adpredictionservice/initialThenComplete";

	@BeforeClass(alwaysRun = true)
	public void beforeClass() {
		getDriver();
		login(getProperty("username"), getProperty("password"));
		testData = getTestData(fileName, sheetName);
	}

	/**
	 * Cleaning up
	 */
	@AfterMethod(alwaysRun = true)
	public void resetGValues() {
		failedMsg.clear();
		getServer().newHar();
		expectedMapAdCall.clear();
		expectedUserTacticsMap.clear();
		targetvalues.clear();
	}

	/**
	 * reset the har
	 */
	@BeforeMethod(alwaysRun = true)
	public void resetV() {
		getServer().newHar();
		login(getProperty("username"), getProperty("password"));
	}

	@Test(dataProvider = "dataProvider", groups = { "DFPT", "AdsRegression", "Desktop", "MobileWeb" })
	public void validateDFPTKeys(String url) throws IOException {

		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD"))
			url = url.replace("medscape", env + "medscape");

		if (isAdCallExpectedInDesktop(testData.get(url).get("PageType").get(0))) {
			if (url.contains("registration_ab.do") || url.contains("login")) {
				logout(getProperty("username"));
			}
			this.url = url;
			// Started the Har entries to track the Ad calls.
			getServer().newHar();

			// Navigating the desired url
			getURL(url);
			waitForAdCallFound();

			// Verifying the page not found
			if (!is404(getDriver().getTitle())) {
				// Getting page source
				String pageSource = getDriver().getPageSource();
				// Reading the user DFPT keys from source page
				String dfp = "";
				boolean dfptkeysfound = true;
				try {
					dfp = pageSource.substring(pageSource.indexOf("{\"reqHeaders\""),
							pageSource.indexOf("; var userCampaign"));
				} catch (StringIndexOutOfBoundsException e) {
					dfptkeysfound = false;
				}
				if (dfptkeysfound) {
					JSONObject jo = new JSONObject(dfp);
					JSONObject reqHeadersTarget = jo.getJSONObject("reqHeaders");
					JSONObject webSegVarsTarget = jo.getJSONObject("webSegVars");
					JSONObject pageSegVarsTarget = jo.getJSONObject("pageSegVars");
					JSONObject userSegVarsTarget = jo.getJSONObject("userSegVars");

					// Verifying the proclivity user or not
					try {
						if (!userSegVarsTarget.get("dt").toString().isEmpty()) {
							proclivityUser = true;
						} else {
							proclivityUser = false;
						}
					} catch (Exception e) {
						proclivityUser = false;
					}

					// Build the service call if it is the proclivity user
					if (proclivityUser) {
						// TO-DO
					}
					// prepare expected Map data
					prepareExpectedMapData();

					failedMsg.add("URL-" + url);

					// Getting the respective page PROD Seg Vars.
					// Note: PROD segvars are stored in properties file
					HashMap<String, List<String>> dfptKeys = new HashMap<>();
					if (false) {
						dfptKeys = getSegvers(testData.get(url).get("PageType").get(0));
					} else {
						dfptKeys = new HashMap<>();
						dfptKeys.put("reqHeaders", getListFromIterator(reqHeadersTarget.keys()));
						dfptKeys.put("webSegVars", getListFromIterator(webSegVarsTarget.keys()));
						dfptKeys.put("pageSegVars", getListFromIterator(pageSegVarsTarget.keys()));
						dfptKeys.put("userSegVars", getListFromIterator(userSegVarsTarget.keys()));
					}
					if (dfptKeys.size() != 0) {
						// Comparing the Cust_Param keys with PROD DFPT Keys
						ExtentTestManager.getTest().log(LogStatus.INFO,
								"Comparing the Cust_Param keys with PROD DFPT Keys.");
						ExtentTestManager.getTest().log(LogStatus.INFO,
								"Comparing the Cust_Param keys for reqHeaders.");
						if ((dfptKeys.get("reqHeaders") != null) && (dfptKeys.get("webSegVars") != null)
								&& (dfptKeys.get("pageSegVars") != null)) {

							// Managing the Proclivity user keys
							List<String> list = new ArrayList<>(dfptKeys.remove("userSegVars"));
							list = removeProclivityKeys(list);
							list = addProclivityData(list);

							dfptKeys.put("userSegVars", list);

							/*
							 * removing the ipAddress from request header
							 * segvars bcz there is know correct info about how
							 * ip will be assigning.
							 */
							list = new ArrayList<>(dfptKeys.remove("reqHeaders"));
							list.remove("ipAddress");
							dfptKeys.put("reqHeaders", list);

							StringBuilder mkeys = new StringBuilder();
							for (String key1 : dfptKeys.get("reqHeaders")) {

								if (!(key1.equals("domainCategory") || key1.equals("domain")
										|| key1.equals("enableDomain") || key1.equals("requestEnv")
										|| key1.equals("device") || key1.equals("ipAddress"))) {
									if (!expectedMapAdCall.containsKey(key1)) {
										mkeys.append(key1 + ", ");
									}
								}
							}
							if (!mkeys.toString().isEmpty()) {
								ExtentTestManager.getTest().log(LogStatus.FAIL,
										"Following reqHeader keys are missing in Ad call :-- "
												+ mkeys.substring(0, mkeys.length() - 2));
							} else {
								ExtentTestManager.getTest().log(LogStatus.PASS,
										"All reqHeader keys are found in Ad call.");
							}
							// Comparing the Cust_Param keys for webSegVars
							compareValues(expectedMapAdCall, dfptKeys.get("webSegVars"));
							// Comparing the Cust_Param keys for pageSegVars
							ExtentTestManager.getTest().log(LogStatus.INFO,
									"Comparing the Cust_Param keys for pageSegVars.");

							compareValues(expectedMapAdCall, dfptKeys.get("pageSegVars"));

							// Comparing the Cust_Param keys for userSegVars
							ExtentTestManager.getTest().log(LogStatus.INFO,
									"Comparing the Cust_Param keys for userSegVars.");
							compareValues(expectedMapAdCall, dfptKeys.get("userSegVars"));

						} else {
							ExtentTestManager.getTest().log(LogStatus.ERROR,
									"PROD DFPT Keys are not available for " + url + " in Segvars.properties file");
						}

						guid = (Long.parseLong(userSegVarsTarget.get("gd").toString()) / 27) + "";

						// Verifying the GUID
						String requestBody = "{\"initial\": {\"cp-override-locale\": \"string\",\"entry-page-referer\": true,\"extra-cookies\": {},\"guid\": "
								+ guid
								+ ",\"host\": \"string\",\"mednet-cookie\": \"string\",\"proxy-headers\": {},\"referer\": \"string\",\"remote-ip-address\": \"string\",\"requested-extra-objects\": [\"string\"],\"url\": \""
								+ url
								+ "\",\"user-agent\": \"string\",\"user-auth-channel\": 104},\"jsp-overrides\": {}}";

						// Getting the DFPT Keys and values from end point
						generateInfoReport("requestBody ==> " + requestBody + " and call ==> " + call);
						Response resp = apiCallPOST(call, requestBody);

						prepareExpectedRequestHeadersData(resp);

						prepareExpectedWebSegVarssData(resp);

						prepareExpectedPageSegVarssData(resp);

						prepareExpectedUserSegVarssData(resp);

						// verifying the reqHeaders end point keys with PROD
						// keys.
						verificationOfEndPointData(dfptKeys.get("reqHeaders"), expectedReqHeadersMap);
						// verifying the webSegVars end point keys with PROD
						// keys.
						verificationOfEndPointData(dfptKeys.get("webSegVars"), expectedWebSegVarsMap);
						// verifying the pageSegVars end point keys with
						// PROD
						// keys.
						verificationOfEndPointData(dfptKeys.get("pageSegVars"), expectedPageSegVarsMap);
						// verifying the userSegVars end point keys with
						// PROD
						// keys.
						verificationOfEndPointData(dfptKeys.get("userSegVars"), expectedUserSegVarsMap);
						// Preparing the lazyLoad map
						// Target page DFPT keys validation

						dfptKeysValidation(dfptKeys.get("reqHeaders"), reqHeadersTarget);

						dfptKeysValidation(dfptKeys.get("webSegVars"), webSegVarsTarget);

						dfptKeysValidation(dfptKeys.get("pageSegVars"), pageSegVarsTarget);

						dfptKeysValidation(dfptKeys.get("userSegVars"), userSegVarsTarget);

						// Preparing the Actual DFTP keys map
						// Preparing the actual reqHeaders map
						HashMap<String, Object> actualReqHeaders = verifyTargetKeys(dfptKeys.get("reqHeaders"),
								reqHeadersTarget);

						// Comparing the Expected map and Actual Map
						// comparing the reqHeaders map
						ExtentTestManager.getTest().log(LogStatus.INFO,
								"*** Comparing for reqHeaders w.r.t end point services ***");
						String reportMessage = comparingActualAndExpectedValues(expectedReqHeadersMap,
								actualReqHeaders);
						if (reportMessage.isEmpty()) {
							ExtentTestManager.getTest().log(LogStatus.PASS,
									"reqHeaders keys and values are met the expectation");
						} else {
							for (String msg : reportMessage.split("#")) {
								ExtentTestManager.getTest().log(LogStatus.FAIL,
										"Following reqHeaders values has mismatch : " + msg);
								failedMsg.add("Following reqHeaders values has mismatch : " + msg);
							}
						}
						HashMap<String, Object> actualUserSegVars = verifyTargetKeys(dfptKeys.get("userSegVars"),
								userSegVarsTarget);
						// comparing the userSegVars map
						ExtentTestManager.getTest().log(LogStatus.INFO,
								"*** Comparing for userSegVars w.r.t end point services ***");
						reportMessage = "";
						reportMessage = comparingActualAndExpectedValues(expectedUserSegVarsMap, actualUserSegVars);
						if (reportMessage.isEmpty()) {
							ExtentTestManager.getTest().log(LogStatus.PASS,
									"userSegVars keys and values are met the expectation");
						} else {
							for (String msg : reportMessage.split("#")) {
								ExtentTestManager.getTest().log(LogStatus.FAIL,
										"Following userSegVars values has mismatch : " + msg);
								failedMsg.add("Following userSegVars values has mismatch : " + msg);
							}
						}
						HashMap<String, Object> actualWebSegVars = verifyTargetKeys(dfptKeys.get("webSegVars"),
								webSegVarsTarget);
						// comparing the webSegVars map
						ExtentTestManager.getTest().log(LogStatus.INFO,
								"*** Comparing for webSegVars  w.r.t end point services ***");
						reportMessage = "";
						reportMessage = comparingActualAndExpectedValues(expectedWebSegVarsMap, actualWebSegVars);
						if (reportMessage.isEmpty()) {
							ExtentTestManager.getTest().log(LogStatus.PASS,
									"webSegVars keys and values are met the expectation");
						} else {
							for (String msg : reportMessage.split("#")) {
								ExtentTestManager.getTest().log(LogStatus.FAIL,
										"Following webSegVars values has mismatch : " + msg);
								failedMsg.add("Following webSegVars values has mismatch : " + msg);
							}
						}
						HashMap<String, Object> actualPageSegVars = verifyTargetKeys(dfptKeys.get("pageSegVars"),
								pageSegVarsTarget);
						// comparing the pageSegVars map
						ExtentTestManager.getTest().log(LogStatus.INFO,
								"*** Comparing for pageSegVars w.r.t end point services ***");

						reportMessage = "";
						reportMessage = comparingActualAndExpectedValues(expectedPageSegVarsMap, actualPageSegVars);
						if (reportMessage.isEmpty()) {
							ExtentTestManager.getTest().log(LogStatus.PASS,
									"pageSegVars keys and values are met the expectation");
						} else {
							for (String msg : reportMessage.split("#")) {
								ExtentTestManager.getTest().log(LogStatus.FAIL,
										"Following pageSegVars values has mismatch : " + msg);
								failedMsg.add("Following pageSegVars values has mismatch : " + msg);
							}
						}

						// Code for userTactics"
						String utc = null;

						utc = StringUtils.substringBetween(pageSource, "var userTactics =  \"", "\"");

						if (utc == null) {
							utc = StringUtils.substringBetween(pageSource, "var userTactics = \"", "\"");
						}

						if (utc != null) {
							utc = utc.trim();
							String userTact[] = utc.trim().split(",");
							map.put("userTactics", utc);
							boolean b = true;
							for (int m = 0; m < userTact.length; m++) {
								if (!ut.toString().contains(userTact[m])) {
									b = false;
									break;
								}
							}
							if (b) {
								expectedUserTacticsMap.put("userTactics", utc.trim());
							} else {
								expectedUserTacticsMap.put("userTactics", "userTactics is having different values");
							}
						}

						String userCampaign = StringUtils.substringBetween(pageSource, "var userCampaign =", ";");
						if (userCampaign != null) {
							userCampaign = userCampaign.trim();
							userCampaign = StringUtils.substringBetween(userCampaign, "\"", "\"").replace(" [", "");
						} else {
							userCampaign = "";
						}
						map.put("userCampaign", userCampaign);
						expectedUserTacticsMap.put("userCampaign", cidValue);

						// Code for ads2_ignore
						String adIgnore = StringUtils.substringBetween(pageSource, "var ads2_ignore =", ";");
						adIgnore = adIgnore.trim();
						generateInfoReport("adIgnore ==> " + adIgnore);
						JSONObject adsobj = new JSONObject(adIgnore);
						Iterator<String> adsiter = adsobj.keys();

						while (adsiter.hasNext()) {
							String key = adsiter.next();
							String value = adsobj.getString(key);
							map.put(key, value);
						}

						if (jspMap.containsKey("adSuppress") && jspMap.get("adSuppress").toString().length() > 1) {
							String ads = jspMap.get("adSuppress");
							if (ads.contains(",")) {
								String adsIgnore[] = ads.split(",");
								for (int l = 0; l < adsIgnore.length; l++) {
									expectedUserTacticsMap.put(adsIgnore[l], "true");
								}
							} else {
								expectedUserTacticsMap.put(ads, "true");
							}
						}

						// Getting the Segvars from Ads call
						// Comparing the Expected map and Actual Map
						// comparing the reqHeaders map
						ExtentTestManager.getTest().log(LogStatus.INFO,
								"*** Comparing for All the keys and values w.r.t Ad call ***");
						targetvalues.putAll(actualReqHeaders);
						targetvalues.putAll(actualPageSegVars);
						targetvalues.putAll(actualUserSegVars);
						targetvalues.putAll(actualWebSegVars);
						// Updating the expected map with missing key and values
						expectedMapAdCall.replace("enableDomain", targetvalues.get("enableDomain").toString());
						reportMessage = comparingActualAndExpectedValues(expectedMapAdCall, targetvalues);
						if (reportMessage.isEmpty()) {
							ExtentTestManager.getTest().log(LogStatus.PASS,
									" keys and values are met the expectation w.r.t Ad call");
							failedMsg.add("keys and values are met the expectation");
						} else {
							for (String msg : reportMessage.split("#")) {
								ExtentTestManager.getTest().log(LogStatus.FAIL,
										"Following Ad call values has mismatch : " + msg);
								failedMsg.add("Following Ad call values has mismatch : " + msg);
							}
						}
					} else {
						ExtentTestManager.getTest().log(LogStatus.ERROR,
								"Failed to fetch the PROD keys from segvars.properties file");
						failedMsg.add("Failed to fetch the PROD keys from segvars.properties file");
					}
				} else {
					ExtentTestManager.getTest().log(LogStatus.ERROR, "Could not find the DFPT  keys in page source.");
				}
			} else

			{
				ExtentTestManager.getTest().log(LogStatus.ERROR,
						url + " page is not available in " + configProp.getProperty("env") + " environment.");
			}
		}
	}

	private void prepareExpectedMapData() {
		// Reading the String Param values from Ad call
		String custParams = "";
		try {
			custParams = getSpecificKeyFromSecurePubadCall("cust_params");
		} catch (Exception e) {
			ExtentTestManager.getTest().log(LogStatus.ERROR,
					"Could not get the Ad call data, page might not be loaded.");
		}
		if (!custParams.isEmpty()) {
			for (String param : custParams.split("&")) {
				String[] keyValue = param.split("=");
				if (keyValue.length == 2)
					expectedMapAdCall.put(keyValue[0], keyValue[1]);
				else {
					expectedMapAdCall.put(keyValue[0], "");
				}
			}
		}
	}

	private List<String> removeProclivityKeys(List<String> list) {

		// remove the proclivity keys if it is not a
		// proclivity
		// user
		if (!proclivityUser && list.contains("dt"))
			list.remove("dt");
		if (!proclivityUser && list.contains("pbs"))
			list.remove("pbs");
		if (!proclivityUser && list.contains("pbr"))
			list.remove("pbr");
		if (!proclivityUser && list.contains("pdi"))
			list.remove("pdi");
		if (!proclivityUser && list.contains("pdp"))
			list.remove("pdp");
		if (!proclivityUser && list.contains("pds"))
			list.remove("pds");
		if (!proclivityUser && list.contains("pbp"))
			list.remove("pbp");
		if (!proclivityUser && list.contains("pb"))
			list.remove("pb");
		if (!proclivityUser && list.contains("masid"))
			list.remove("masid");
		return list;
	}

	private List<String> addProclivityData(List<String> list) {
		/*
		 * Add the proclivity keys if it is proclivity user and there are no
		 * keys
		 */
		if (proclivityUser && (!list.contains("dt")))
			list.add("dt");
		if (proclivityUser && (!list.contains("pbs")))
			list.add("pbs");
		if (proclivityUser && (!list.contains("pbr")))
			list.add("pbr");
		if (proclivityUser && (!list.contains("pdi")))
			list.add("pdi");
		if (proclivityUser && (!list.contains("pdp")))
			list.add("pdp");
		if (proclivityUser && (!list.contains("pds")))
			list.add("pds");
		if (proclivityUser && (!list.contains("pbp")))
			list.add("pbp");
		if (proclivityUser && (!list.contains("pb")))
			list.add("pb");
		if (proclivityUser && (!list.contains("masid")))
			list.add("masid");
		return list;
	}

	private void compareValues(HashMap<String, String> expectedMap, List<String> compareList) {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Comparing the Cust_Param keys for webSegVars.");
		StringBuffer mkeys = new StringBuffer();
		for (String key1 : compareList) {
			if (!expectedMap.containsKey(key1)) {
				mkeys.append(key1 + ", ");
			}
		}
		if (!mkeys.toString().isEmpty()) {
			ExtentTestManager.getTest().log(LogStatus.FAIL,
					"Following webSegVar keys are missing in Ad call :-- " + mkeys.substring(0, mkeys.length() - 2));
		} else {
			ExtentTestManager.getTest().log(LogStatus.PASS, "All webSegVar keys are found in Ad call.");
		}
	}

	private void prepareExpectedRequestHeadersData(Response resp) {
		String reqHeaders = "";
		try {
			reqHeaders = apiExtractValue(resp, "data.DFPTargetKeys.reqHeaders").get(0).toString().replace("{", "")
					.replace("}", "");
		} catch (Exception e) {

		}

		if (!reqHeaders.isEmpty()) {
			for (String s : reqHeaders.split(", ")) {
				String[] ss = s.trim().split("=");
				String value = "";
				try {
					value = ss[1];
				} catch (Exception e) {

				}
				expectedReqHeadersMap.put(ss[0], value);
			}
		}
		generateInfoReport("reqHeadersMap --> " + expectedReqHeadersMap);

	}

	private void prepareExpectedWebSegVarssData(Response resp) {
		String webSegVars = "";
		try {
			webSegVars = apiExtractValue(resp, "data.DFPTargetKeys.webSegVars").get(0).toString().replace("{", "")
					.replace("}", "");
		} catch (Exception e) {

		}

		if (!webSegVars.isEmpty()) {
			for (String s : webSegVars.split(", ")) {
				String[] ss = s.trim().split("=");
				String value = "";
				try {
					value = ss[1];
				} catch (Exception e) {

				}
				expectedWebSegVarsMap.put(ss[0], value);
			}
		}
		generateInfoReport("webSegVarsMap --> " + expectedWebSegVarsMap);

	}

	private void prepareExpectedPageSegVarssData(Response resp) {

		String pageSegVars = "";
		try {
			pageSegVars = apiExtractValue(resp, "data.DFPTargetKeys.pageSegVars").get(0).toString().replace("{", "")
					.replace("}", "");
		} catch (Exception e) {

		}

		if (!pageSegVars.isEmpty()) {
			for (String s : pageSegVars.split(", ")) {
				String[] ss = s.trim().split("=");
				try {
					expectedPageSegVarsMap.put(ss[0], ss[1]);
				} catch (Exception e) {
					expectedPageSegVarsMap.put(ss[0], "");
				}
			}
		}
		generateInfoReport("pageSegVars --> " + expectedPageSegVarsMap);

	}

	private void prepareExpectedUserSegVarssData(Response resp) {

		String userSegVars = "";
		try {
			userSegVars = apiExtractValue(resp, "data.DFPTargetKeys.userSegVars").get(0).toString().replace("{", "")
					.replace("}", "");
		} catch (Exception e) {

		}

		if (!userSegVars.isEmpty()) {
			for (String s : userSegVars.split(", ")) {
				String[] ss = s.trim().split("=");
				try {
					expectedUserSegVarsMap.put(ss[0], ss[1]);
				} catch (Exception e) {
					expectedUserSegVarsMap.put(ss[0], "");
				}
			}
		}
		generateInfoReport("userSegVars --> " + expectedUserSegVarsMap);

	}

	@DataProvider
	public Object[] dataProvider() {
		return getURLs(fileName, sheetName);
	}

	private void verificationOfEndPointData(List<String> endPointData, HashMap<String, String> expectedMap) {
		List<String> keys = null;
		if (endPointData != null && expectedMap != null) {
			keys = compareDFPTKeys(endPointData, new ArrayList<>(expectedMap.keySet()));
			if (keys.isEmpty()) {
				String temp = "";
				for (String key : keys) {
					switch (key) {
					case "device":
						expectedMapAdCall.put("device", expectedReqHeadersMap.get("device").toString());
						break;
					case "requestEnv":
						if (expectedReqHeadersMap.get("requestEnv") == null)
							expectedMapAdCall.put("requestEnv", "");
						else
							expectedMapAdCall.put("requestEnv", expectedReqHeadersMap.get("requestEnv").toString());
						break;
					case "enableDomain":
						expectedMapAdCall.put("enableDomain", map.get("enableDomain"));
						break;
					case "domainCategory":
						if (expectedMapAdCall.replace("domainCategory",
								expectedReqHeadersMap.get("domainCategory").toString()) == null)
							expectedMapAdCall.put("domainCategory",
									expectedReqHeadersMap.get("domainCategory").toString());
						break;
					case "domain":
						try {
							if (expectedMapAdCall.replace("domain",
									expectedReqHeadersMap.get("domain").toString()) == null) {
								expectedMapAdCall.put("domain", expectedReqHeadersMap.get("domain").toString());
							}
						} catch (NullPointerException e) {
							expectedMapAdCall.put("domain", "");
						}
						break;
					default:
						temp = temp + key + ", ";
					}

				}
				if (!temp.isEmpty()) {
					ExtentTestManager.getTest().log(LogStatus.FAIL,
							"Following keys are missing at end point : " + temp.substring(0, (temp.length() - 2)));
					failedMsg
							.add("Following keys are missing at end point : " + temp.substring(0, (temp.length() - 2)));
				}
			} else {
				ExtentTestManager.getTest().log(LogStatus.PASS, "Required all keys are available in End point");
			}

		} else {
			if (endPointData == null)
				ExtentTestManager.getTest().log(LogStatus.ERROR,
						"PROD keys are not available in Segvars.properties for " + url);
			else
				ExtentTestManager.getTest().log(LogStatus.ERROR,
						"PROD keys are not available in Segvars.properties for " + url);
		}
	}

	private void dfptKeysValidation(List<String> endPointData, JSONObject reqHeadersTarget) {
		if (endPointData != null) {
			if (reqHeadersTarget.length() != 0) {
				ExtentTestManager.getTest().log(LogStatus.PASS, "reqHeaders are fetched successfully from target");
			} else {
				ExtentTestManager.getTest().log(LogStatus.FAIL, "Could not find the reqHeaders in target");
				failedMsg.add("Could not find the reqHeaders in target");
			}
		} else {
			ExtentTestManager.getTest().log(LogStatus.ERROR, "reqHeaders keys are not available in target for " + url);
		}
		if (endPointData != null && reqHeadersTarget != null) {
			List<String> keys = compareDFPTKeys(endPointData, new ArrayList<>(reqHeadersTarget.keySet()));
			if (!keys.isEmpty()) {
				String temp = "";
				for (String key : keys) {
					temp = temp + key + ", ";
				}
				ExtentTestManager.getTest().log(LogStatus.FAIL,
						"Following reqHeaders are missing in target : " + temp.substring(0, temp.length() - 2));
				failedMsg.add("Following reqHeaders are missing in target : " + temp.substring(0, temp.length() - 2));
			} else {
				ExtentTestManager.getTest().log(LogStatus.PASS, "Required all keys are available in target");
			}
		} else {
			if (endPointData == null)
				ExtentTestManager.getTest().log(LogStatus.ERROR,
						"keys are not available in Segvars.properties for " + url);
			else
				ExtentTestManager.getTest().log(LogStatus.ERROR, "keys are not available in target for " + url);
		}
	}

	private <T> List<T> getListFromIterator(Iterator<T> iterator) {

		// Create an empty list
		List<T> list = new ArrayList<>();

		// Add each element of iterator to the List
		iterator.forEachRemaining(list::add);

		// Return the List
		return list;
	}

	private HashMap<String, Object> verifyTargetKeys(List<String> endPoint, JSONObject reqHeadersTarget) {
		HashMap<String, Object> actualReqHeaders = new HashMap<>();
		if (endPoint != null) {
			StringBuilder sb = new StringBuilder();
			for (String key : endPoint) {
				try {
					actualReqHeaders.put(key, reqHeadersTarget.get(key));
				} catch (JSONException ex) {
					sb.append(key + ",");
				}
			}
			if (sb.length() != 0) {
				ExtentTestManager.getTest().log(LogStatus.FAIL,
						"Following reqHeader keys are missing in target : " + sb.substring(0, sb.length() - 1));
			} else {
				ExtentTestManager.getTest().log(LogStatus.PASS, "All the required reqHeader keys are available.");
			}
		} else {
			ExtentTestManager.getTest().log(LogStatus.ERROR, "reqHeader keys are not available in target for " + url);
		}
		return actualReqHeaders;
	}
}
