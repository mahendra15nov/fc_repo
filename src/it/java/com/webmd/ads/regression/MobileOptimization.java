package com.webmd.ads.regression;

import java.util.HashSet;

import org.openqa.selenium.JavascriptExecutor;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.webmd.common.MobileOptimizationCommon;

/**
 * PPE-204494 Mobile Optimization P1: Functionality Verification All mobile
 * optimization features are available inside this class.
 * 
 * @author amahendra Updated on 24/07/2019 for PPE-205915 [13/09/2019]
 *         -PPE-213097 - Mobile Opt Phase 1.5: Update DTM Targeting on Lower
 *         Environments
 */
@Listeners(com.webmd.general.common.Listener.class)
public class MobileOptimization extends MobileOptimizationCommon {

	HashSet<String> urls = new HashSet<>();

	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		getDriver().quit();
		getServer().stop();
	}

	@BeforeClass(alwaysRun = true)
	public void loginPerform() {
		login(getProperty("username"), getProperty("password"));
	}

	@BeforeMethod(alwaysRun = true)
	public void resetValues() {
		login(getProperty("username"), getProperty("password"));
		textDriverOptimized = false;
		configData = "";
		closButton = "X";
		onSCroll = "";
		backGroundColor = "#f0efef";
		start = System.currentTimeMillis();
		end = System.currentTimeMillis();
		adDiv = "";
		isIframePresent = false;
		isNoneResponsive = false;
	}

	/**
	 * Verify the DTM Flag
	 * 
	 * @param url
	 * @throws InterruptedException
	 */
	@Test(enabled = true, dataProvider = "medscapeurls", groups = { "MobileOptimization", "AdsRegression", "Desktop",
			"MobileWeb" })
	public void verifyDTMFlag(String url) {
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD")) {
			url = url.replace("medscape", env + "medscape");
		}
		if (url.contains("registration_ab.do") || url.contains("login")) {
			logout(getProperty("username"));
		}
		getServer().newHar();
		getURL(url);
		waitForAdCallFound();
		// Verify the default adhesive footer changes
		if (!is404(getDriver().getTitle())) {
			if (!isLoginPage()) {
				// textDriverOptimized flag verification
				verifytextDriverOptimizedFlag();
			} else {
				generateInfoReport(url + " has still required login though login performed aleardy.");
			}
		} else {
			generateSkipReport(url + " is not a valid URL.");
		}
	}

	/**
	 * All mobile optimization changes
	 * 
	 * @param url
	 * @throws InterruptedException
	 */

	@Test(enabled = true, dataProvider = "medscapeurls", groups = { "MobileOptimization", "AdsRegression", "Desktop",
			"MobileWeb" })
	public void verifyMobileOptimizationAndAdhesiveFooter(String url) throws InterruptedException {
		if (breakPoint.contains("1")) {
			jse = (JavascriptExecutor) getDriver();
			getServer().newHar();
			if (!env.isEmpty() && !env.equalsIgnoreCase("PROD")) {
				url = url.replace("medscape", env + "medscape");
			}
			getURL(url);
			waitForAdCallFound();
			// Verify the default adhesive footer changes
			if (!is404(getDriver().getTitle())) {
				if (!isLoginPage()) {
					// isEmd, isArticle and textDriverOptimized, isRD flags
					// verification.
					preRequisite();
					// textDriverOptimized flag verification
					verifytextDriverOptimizedFlag();
					// 1004 pos verification
					verifyPos1004IsIgnoredOrNot();
					verifyMovePos1520();
					// Drug monograph regression
					verifyOldAdhesiveFooterInDrugMonorgraphPages();
					// Recap articles regression
					verifyOldAdhesiveFooterInRecap();
					// Adhesive footer changes
					verifyNewAdhesiveFooterPosAndsApplicablesSizesInAdCall();
					// Ahesive footer update
					verifyAdhesiveFooterChanges();
					// pos 122 changes
					verifyMovePos1122ToOnePTagAhead();
					// pos 1420 changes
					verifyPos1420InPageLoadedAdCall();
					// verifySectionsMenuStickyness();
					// testSpacing();
					// validateRDValue();
					// testAdLabel();

				} else {
					generateInfoReport(url + " has still required login though login performed aleardy.");
				}
			} else {
				generateSkipReport(url + " is not a valid URL.");
			}
		} else {
			generateSkipReport("Its not a Mobile Break Point, test can be application for Mobile Breakpoint only.");
		}
	}

	@DataProvider
	public String[] medscapeurls() {
		return new String[] { "https://www.medscape.com/viewarticle/890715",
				"https://www.medscape.com/viewarticle/895121", "https://emedicine.medscape.com/article/211186-overview",
				"https://emedicine.medscape.com/article/180084-overview",
				"https://emedicine.medscape.com/article/174662-overview", "https://www.medscape.com/viewarticle/890267",
				"https://www.medscape.com/viewarticle/893766",
				"https://emedicine.medscape.com/article/329958-overview" };
	}
}
