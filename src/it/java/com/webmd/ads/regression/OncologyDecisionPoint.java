package com.webmd.ads.regression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.webmd.common.MedscapeDecisionPoint;

/**
 * Created on 20/08/2019 Version : V 1.0;
 * 
 * MDP Decision Tree Ad experience
 * 
 * @author amahendra Created on [20/08/2019] - PPE-197528 - MDP Decision Tree Ad
 *         Experience (Desktop) and PPE-197680 - MDP Decision Tree Ad Experience
 *         (MobileWeb)
 */
@Listeners(com.webmd.general.common.Listener.class)
public class OncologyDecisionPoint extends MedscapeDecisionPoint {

	boolean lazyLoad = false;
	String urlSL = " has not found.";

	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		getDriver().quit();
		getServer().stop();
	}

	@BeforeClass(alwaysRun = true)
	public void openBrowser() {
		login(getProperty("username"), getProperty("password"));
		jse = (JavascriptExecutor) getDriver();
	}

	@AfterMethod(alwaysRun = true)
	public void resetValues() {
		lazyLoad = false;
		urlSL = " has not found.";
	}

	/**
	 * PPE-213586 - MDP Overall functionality
	 * 
	 * @param url
	 */
	@Test(dataProvider = "medscapeurls", groups = { "MDP", "AdsSmoke", "AdsRegression", "MobileWeb" })
	public void verifyOncologyDecisionPoint(String url) {
		getServer().newHar();
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD"))
			url = url.replace("medscape", env + "medscape");
		getURL(url);
		waitForAdCallFound();
		StaticWait(10);
		if (!is404(getDriver().getTitle())) {
			if (!isLoginPage()) {
				if (numberOfAdCallsValidation()) {
					// ODP verification
					if (isTrue("_isODP")) {
						if (isTrue("_isAnArticle")) {
							String[] aAds = null;
							generateInfoReport(url + " is a ODP Article.");
							HashMap<String, ArrayList<String>> adPosSzs = new HashMap<>();

							if (breakPoint.equals("4")) {
								// verify the 122, 909 ans 141 pos
								adPosSzs.put("122",
										new ArrayList<String>(Arrays.asList("300x250", "300x600", "300x350")));
								adPosSzs.put("909", new ArrayList<String>(Arrays.asList("320x50", "2x5")));
								adPosSzs.put("141", new ArrayList<String>(Arrays.asList("728x90", "728x91")));
								aAds = new String[] { "122", "909", "141_1" };
							} else {
								if (breakPoint.equals("1")) {
									// verify the 1145, 1122 ans 1909 pos
									adPosSzs.put("1145", new ArrayList<String>(
											Arrays.asList("320x50", "320x80", "375x80", "320x52", "300x52", "2x7")));
									adPosSzs.put("1122", new ArrayList<String>(Arrays.asList("300x250", "300x400",
											"300x50", "300x51", "320x50", "320x51")));
									adPosSzs.put("1909", new ArrayList<String>(Arrays.asList("320x50", "2x5")));
									aAds = new String[] { "1122_f", "1909" };
								}
							}

							// Verify the Ad pos and szs
							verifySpecifiedAllAdPosAndSzs(adPosSzs, null);
							// Verify the Adiesive footer changes
							if (breakPoint.equals("1")) {
								verifyAdhesiveFooterChanges();
							}
							// Verify the Ads
							verifyAllAdPosLoadedOnPage(aAds);

							getServer().newHar();
							// Click on Related Guidelines tab
							getDriver().findElement(By.id("tab-guidelines")).click();
							StaticWait(2);
							generateReport(!numberOfAdCallsValidation(),
									"No Ad call fired on click Related Guidelines tab",
									"Ad call fired on click Related Guidelines tab");
							getServer().newHar();
							// Click on Read More option
							JavascriptExecutor executor = (JavascriptExecutor) getDriver();
							executor.executeScript("arguments[0].click();", getDriver().findElement(By.xpath(
									"//*[@id='dp-guidelines']/div[@class='guidelines-list']/child::div[@class='related-guideline']/span[@class='read-more']")));

							generateReport(!numberOfAdCallsValidation(),
									"No Ad call fired on click Read More option in Related Guidelines tab",
									"Ad call fired on click Read More option in Related Guidelines tab");
							getServer().newHar();
							// Click on Related Guidelines tab
							getDriver().findElement(By.id("tab-transcript")).click();
							StaticWait(2);
							generateReport(!numberOfAdCallsValidation(), "No Ad call fired on click Trascript tab",
									"Ad call fired on click Trascript tab");
							getServer().newHar();
							// Click on Read More option in Transcript tab
							executor.executeScript("arguments[0].click();", getDriver()
									.findElement(By.xpath("//*[@id='article-content']/span[@class='show-more']")));
							StaticWait(2);
							scrollTillEnd();
							adPosSzs.clear();
							String[] optionalPos = null;
							if (breakPoint.equals("4")) {
								// verify the 122, 909 ans 141 pos
								adPosSzs.put("520", new ArrayList<String>(Arrays.asList("2x3")));
								adPosSzs.put("141", new ArrayList<String>(Arrays.asList("728x90", "728x91")));
								adPosSzs.put("420", new ArrayList<String>(Arrays.asList("2x3")));
								optionalPos = new String[] { "141", "420" };
							} else {
								if (breakPoint.equals("1")) {
									// verify the 1145, 1122 ans 1909 pos
									adPosSzs.put("1122", new ArrayList<String>(Arrays.asList("300x250", "300x400",
											"300x50", "300x51", "320x50", "320x51")));
									adPosSzs.put("1520",
											new ArrayList<String>(Arrays.asList("320x50", "2x3", "1x12", "300x254")));
									adPosSzs.put("1420",
											new ArrayList<String>(Arrays.asList("320x50", "2x3", "1x12", "300x254")));
									optionalPos = new String[] { "1420", "1520" };
								}
							}
							// Verify the Ad pos and szs
							ArrayList<String> aList = verifySpecifiedAllAdPosAndSzs(adPosSzs, optionalPos);
							Object[] objArr = aList.toArray();
							String[] str = Arrays.copyOf(objArr, objArr.length, String[].class);
							// Verify the Ads
							verifyAllAdPosLoadedOnPage(str);

						} else {
							String[] aAds = null;
							generateInfoReport(url + " is a ODP Decision Tree page.");
							HashMap<String, ArrayList<String>> adPosSzs = new HashMap<>();
							if (breakPoint.equals("4")) {
								// 101 pos validation in Decision tree page
								adPosSzs.put("101",
										new ArrayList<String>(Arrays.asList("728x90", "970x250", "970x90")));
								aAds = new String[] { "101" };
							} else {
								if (breakPoint.equals("1")) {
									// verify the 1145, 1122 ans 1909 pos
									adPosSzs.put("1145", new ArrayList<String>(
											Arrays.asList("320x50", "320x80", "375x80", "320x52", "300x52", "2x7")));
									adPosSzs.put("1122", new ArrayList<String>(Arrays.asList("300x250", "300x400",
											"300x50", "300x51", "320x50", "320x51")));
									aAds = new String[] { "1122" };
								}
							}
							// Verify the Ad pos and szs
							verifySpecifiedAllAdPosAndSzs(adPosSzs, null);

							// Verify the Adiesive footer changes
							if (breakPoint.equals("1")) {
								verifyAdhesiveFooterChanges();
							}
							// Verify the Ads
							verifyAllAdPosLoadedOnPage(aAds);
							// verify the lazy loaded Ad call
							verifyNoLazyLoadedAdCall();
						}
					} else {
						generateInfoReport(url + " is not a ODP page.");
					}
				} else {
					generateInfoReport("Ad call has not been found in page load.");
				}
			}
		} else {
			generateSkipReport(getDriver().getCurrentUrl() + urlSL);
		}

	}

	@Test(groups = { "MDP", "AdsSmoke", "AdsRegression", "MobileWeb" })
	public void validateDPTKeyValuePair() {

		String url = "https://odp.env.medscape.com/dp/decision";

		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD"))
			url = url.replace("medscape", env + "medscape");
		setDescription("Verify the overall DPT key value pair occurence on Decision Tree Page & Article Pages");
		getDriver();
		login("infosession33", "medscape");
		getServer().newHar();
		getURL(url);
		waitForAdCallFound();
		validateDptZero();
		generateInfoReport(
				"Verifying DPT key value pair by working around the dropdowns and clicking on article pages");
		workAroundDropdownAndValidateDPT();

		url = "https://decisionpoint.medscape.com/oncology/viewarticle/897194";
		getDriver();
		getServer().newHar();
		getDriver().get(url);
		validateDptZero();
	}

	@DataProvider
	public String[] medscapeurls() {
		return new String[] {
				"https://decisionpoint.medscape.com/oncology/viewarticle/909125?ctype=Lung+Cancer&ttype=Mesothelioma&stage=undefined&biomarkers=undefined",
				"https://decisionpoint.medscape.com/oncology/viewarticle/909125?ctype=Lung+Cancer&ttype=Mesothelioma&stage=undefined&biomarkers=undefined#transcript",
				"https://decisionpoint.medscape.com/oncology/viewarticle/909125?ctype=Lung+Cancer&ttype=Mesothelioma&stage=undefined&biomarkers=undefined#guidelines" };
	}
}
