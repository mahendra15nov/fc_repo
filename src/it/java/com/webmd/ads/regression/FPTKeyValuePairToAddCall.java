package com.webmd.ads.regression;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;

/**
 * FPT key validation
 * 
 * @author amahendra
 *
 */
@Listeners(com.webmd.general.common.Listener.class)
public class FPTKeyValuePairToAddCall extends AdsCommon {

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() {
		getDriver();
	}

	@AfterMethod(alwaysRun = true)
	public void closeBrowser() {
		getDriver().close();
		getServer().stop();
	}

	@Test(dataProvider = "faforfpfarticles", groups = { "FPTKeyValue", "AdsRegression", "Desktop", "MobileWeb" })
	public void verifyFPTValue(String URL) {
		setDescription("Verify that if isFPFEligible = true/false/null, fpt=fpf/faf/0 is in cust_params in the ad");
		startServer();
		getServer().newHar();
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD"))
			URL = URL.replace("medscape", env + "medscape");
		getURL(URL);
		waitForAdCallFound();
		if (!is404(getDriver().getTitle())) {
			if (!is404(getDriver().getTitle())) {
				if (!isLoginPage()) {
					getURL(URL + "?faf=1");
					waitForAdCallFound();
				}
				if (!isLoginPage()) {

					generateInfoReport("isFPFEligible returned as " + getConsoleValue("isFPFEligible"));

					String custParams = getSpecificKeyFromSecurePubadCall("cust_params");
					if (custParams != null) {
						if (!custParams.isEmpty()) {
							if (custParams.contains("fpt=")) {
								String fpt = StringUtils.substringBetween(custParams, "fpt=", "&");
								if (fpt != null) {
									if (!fpt.isEmpty() && fpt != null) {
										if (fpt.equalsIgnoreCase("0")) {
											try {
												if (!getConsoleValue("isFPFEligible").isEmpty()) {
													if (getConsoleValue("isFPFEligible").equalsIgnoreCase("null")) {
														generatePassReportWithNoScreenShot(
																"Test successed => isFPFEligible = null and fpt = 0");
													} else {
														generateFailReport("Test failed => isFPFEligible = "
																+ getConsoleValue("isFPFEligible") + " and fpt = 0");
													}
												} else {
													generateInfoReport("isFPFEligible is not defined in the page.");
													generatePassReportWithNoScreenShot(
															"Test successed => isFPFEligible = null and fpt = 0");
												}
											} catch (NullPointerException e) {
												generatePassReportWithNoScreenShot(
														"Test successed => isFPFEligible = null and fpt = 0");
											}

										} else {
											if (fpt.equalsIgnoreCase("faf")) {
												if (!getConsoleValue("isFPFEligible").isEmpty()) {
													if (getConsoleValue("isFPFEligible").equalsIgnoreCase("false")) {
														generatePassReportWithNoScreenShot(
																"Test successed => isFPFEligible = false and fpt = faf");
													} else {
														generateFailReport("Test failed => isFPFEligible = "
																+ getConsoleValue("isFPFEligible") + " and fpt = faf");
													}
												} else {
													generateInfoReport("isFPFEligible is not defined in the page.");
													generateFailReport(
															"Test failed => isFPFEligible = null and fpt = faf");
												}
											} else {
												if (fpt.equalsIgnoreCase("fpf")) {
													if (!getConsoleValue("isFPFEligible").isEmpty()) {
														if (getConsoleValue("isFPFEligible").equalsIgnoreCase("true")) {
															generatePassReportWithNoScreenShot(
																	"Test successed => isFPFEligible = true and fpt = fpf");
														} else {
															generateFailReport("Test failed => isFPFEligible = "
																	+ getConsoleValue("isFPFEligible")
																	+ " and fpt = fpf");
														}
													} else {
														generateInfoReport("isFPFEligible is not defined in the page.");
														generateFailReport(
																"Test failed => isFPFEligible = null and fpt = fpf");
													}
												} else {
													generateFailReport("Test failed => isFPFEligible = "
															+ getConsoleValue("isFPFEligible") + " and fpt = " + fpt);
												}
											}
										}
									} else {
										generateFailReport("fpt value is not found in the cust_params");
									}
								} else {
									generateFailReport("fpt value is not found in the cust_params");
								}
							} else {
								generateFailReport("Could not find the 'fpt' values in cust_params in ad call");
							}
						} else {
							generateFailReport("cust_params has found empty in Ad call");
						}
					} else {
						generateFailReport("Ad call has not been tracked in Network calls");
					}
				} else {
					generateInfoReport(URL + " is a gated article hence need login.");
				}
			} else {
				generateInfoReport(URL + " has not fond.");
			}
		}
	}

	@DataProvider()
	public String[] faforfpfarticles() {
		return getURLs("AdsSanity.xls", "POS_SZS");
	}
}
