package com.webmd.common;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.tls.HashAlgorithm;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;

/**
 * This is the Mobile Optimization changes
 * 
 * @author amahendra Created on October 21st 2019
 */
public class MobileOptimizationCommon extends AdsCommon {

	public JavascriptExecutor jse;
	public String isEmed = "isEmed";
	public boolean textDriverOptimized = false;
	public String isAnArticle = "_isAnArticle";
	public String url = "";
	public String adDiv = "";
	public boolean isIframePresent = false;
	public boolean isNoneResponsive = false;
	public String onSCroll = "";
	public String closButton = "X";
	public String backGroundColor = "#f0efef";
	public String configData = "";
	public long start = System.currentTimeMillis();
	public long end = System.currentTimeMillis();

	/**
	 * Verify textDriverOptimized flag is defined in article or not
	 */
	public void verifytextDriverOptimizedFlag() {
		boolean bSsp = false;
		String ssp = "";
		String pageType = "";
		String custParams = getSpecificKeyFromSecurePubadCall("cust_params");
		ssp = StringUtils.substringBetween(custParams, "ssp=", "&");
		String u = getDriver().getCurrentUrl();
		if (!(u.contains("deutsch") || u.contains("espanol") || u.contains("francais") || u.contains("portugues"))) {
			if (isTrue("_isAnArticle") || isTrue("isEmed")) {
				switch (ssp) {
				case "22":
					pageType = "Diabets";
					bSsp = true;
					break;
				case "7":
					pageType = "Hematology / Oncology";
					bSsp = true;
					break;
				case "26":
					pageType = "Neurology";
					bSsp = true;
					break;
				case "27":
					pageType = "Rheumatology";
					bSsp = true;
					break;
				default:
					bSsp = false;
					pageType = "Other";
				}
			} else {
				bSsp = false;
			}
		}
		if (isTrue("textDriverOptimized") && bSsp) {
			generatePassReportWithNoScreenShot(getDriver().getCurrentUrl() + " is " + pageType
					+ " url and its contains textDriverOptimized flag.");
		} else {
			if ((isTrue("textDriverOptimized") == true) && (bSsp == false)) {
				generateFailReport(getDriver().getCurrentUrl() + " is not a " + pageType
						+ " page but textDriverOptimized is defined in a article.");
			} else {
				if ((isTrue("textDriverOptimized") == false) && (bSsp == true)) {
					generateFailReport(getDriver().getCurrentUrl() + " is a " + pageType
							+ " page but textDriverOptimized flag does not defeined in the article.");
				}
			}
		}
	}

	/**
	 * pos 1520 / 1420 changes
	 * 
	 * @param pos
	 * @throws InterruptedException
	 */
	private void verifyStylingUpdates(String pos) throws InterruptedException {
		WebElement webElement = null;
		try {
			webElement = getDriver().findElement(By.xpath("//div[contains(@id,'ads-pos-" + pos + "')"));
		} catch (NoSuchElementException e) {
			generateInfoReport("No 1520/1420 position on " + url + "");
		}
		if (webElement != null) {
			// Verify the 1420 , 1520 pos szs
			try {

				if (verifySpecificPositionLoadedOnPage(pos)) {
					scrollToWebElement(webElement);
					StaticWait(3);
					verify1520And1420Changes(webElement, pos);
				} else {
					generateFailReport("pos " + pos + " Ad is not loaded on the Page.");
				}
			} catch (NoSuchElementException eee) {
				generateFailReport("Something went wrong during 1520/1420 Ad slot verification.");
			}
		}
	}

	/**
	 * changes of 1520 and 1420
	 * 
	 * @param webElement
	 * @param pos
	 */
	private void verify1520And1420Changes(WebElement webElement, String pos) {
		adcallvalidation(pos);
		if (textDriverOptimized) {
			// verify whether text ad / Media ad
			// loaded
			String className = webElement.getAttribute("class");
			if (className.contains("textAdClass")) {
				generateInfoReport(pos
						+ " Ad position has been loaded with Text Ads hence expected changes should not be applied.");
				verifyNoChangesEffected(webElement, pos);
			} else {
				generateInfoReport(
						pos + " Ad position has been loaded with Media Ads hence expected changes should be applied.");
				verifyNoChangesEffected(webElement, pos);
			}
		} else {
			verifyNoChangesEffected(webElement, pos);
		}
	}

	/**
	 * pos 1004 pos in Ad call
	 */
	public void verifyPos1004IsIgnoredOrNot() {
		if (numberOfAdCallsValidation()) {
			// Ad call verification w.r.t to pos 1004
			String prevScp = getSpecificKeyFromSecurePubadCall("prev_scp");
			generateInfoReport("prev_scp : " + prevScp);
			if (textDriverOptimized || isTrue("_isRecap"))
				generateReport(!prevScp.contains("1004"),
						"1004 pos has been removed from Ad call where DTM flag has applied.",
						"pos 1004 is exists in the Ad call.");
			else
				generateReport(prevScp.contains("1004"),
						"1004 pos has availabel in Ad call where DTM flag has not applied.",
						"pos 1004 is not in the Ad call.");
			// ads2_ignore set verification w.r.t to pos 1004
			if (textDriverOptimized || isTrue("_isRecap"))
				generateReport(isPosInAds2IgnoreList("1004"), "pos 1004 is not available under ads2_ignore list.",
						"pos 1004 is available under ads2_ignore list.");
			else
				generateReport(!isPosInAds2IgnoreList("1004"), "pos 1004 is not available under ads2_ignore list.",
						"pos 1004 is available under ads2_ignore list.");
		}

	}

	/**
	 * Section option sticky ness
	 */
	private void verifySectionsMenuStickyness() {
		if (isTrue(isEmed)) {
			if (textDriverOptimized) {
				generateInfoReport(
						"textDriverOptimized -> true hence 1004 pos will not be displayed on the page, so no Blank spance should be appear above the Sections menu.");
				try {
					String style = getDriver().findElement(By.id("dd_nav")).getAttribute("style");
					if (style.contains("top: 0px;")) {
						generatePassReportWithNoScreenShot("NO Blank space has been found above the sections menu.");
					} else {
						style = getDriver().findElement(By.id("dd_nav")).getAttribute("style");
						if (style.contains("top: 9px;")) {
							generateFailReport(
									"Blank space has been found above the sections menu or 1004 pos has been displayed in optmized emedicine article.");
						} else {
							generateFailReport("1004 pos has been displayed in optmized emedicine article.");
						}
					}
				} catch (NoSuchElementException e) {
					generateInfoReport("No Sections menu present in " + url);
				}
			} else {
				generateInfoReport("textDriverOptimized -> false hence 1004 pos will be displayed on the page.");
				StaticWait(7);
				try {
					String style = getDriver().findElement(By.id("dd_nav")).getAttribute("style");
					if (style.contains("top: 9px;")) {
						generatePassReportWithNoScreenShot("1004 Ad space has been found above the sections menu.");
					} else {
						style = getDriver().findElement(By.id("dd_nav")).getAttribute("style");
						if (style.contains("top: 0px;")) {
							generateFailReport("1004 pos has not been displayed in optmized emedicine article.");
						} else {
							generatePassReportWithNoScreenShot(
									"1004 pos has been displayed in optmized emedicine article.");
						}
					}
				} catch (NoSuchElementException e) {
					generateInfoReport("No Sections menu present in " + url);
				}
			}
		} else {
			generateInfoReport(url + " is not a Emedicine page.");
		}

	}

	/**
	 * pos 122 changes
	 */
	public void verifyMovePos1122ToOnePTagAhead() {

		if (isTrue(isAnArticle)) {
			if (textDriverOptimized) {
				// Ad call verification w.r.t to pos 1122
				String prevScp = getSpecificKeyFromSecurePubadCall("prev_scp");
				generateInfoReport("prev_scp : " + prevScp);
				if (prevScp.contains("1122")) {
					generatePassReportWithNoScreenShot("Page Load Ad call having 1122 pos.");
					int noOfPTags = getDriver().findElements(By.xpath("//div[@id='ads-pos-1122']/preceding-sibling::p"))
							.size();
					if (noOfPTags == 1) {
						generatePassReportWithNoScreenShot("Pos 1122 has been moved to after one P Content.");
					} else {
						generateFailReport("Pos 1122 has been exists after " + noOfPTags + " P Content.");
					}
				} else {
					generateFailReport("Page Load Ad call does not have 1122 pos.");
				}
			} else {
				// Ad call verification w.r.t to pos 1122
				String prevScp = "";
				prevScp = getSpecificKeyFromSecurePubadCall("prev_scp");
				generateInfoReport("prev_scp : " + prevScp);
				if (prevScp != null) {
					if (prevScp.contains("1122")) {
						generatePassReportWithNoScreenShot("Page Load Ad call having 1122 pos.");

						int noOfPTags = getDriver()
								.findElements(By.xpath("//div[@id='ads-pos-1122']/preceding-sibling::p")).size();
						if (noOfPTags == 2) {
							generatePassReportWithNoScreenShot("Pos 1122 has been moved to after two P Content.");
						} else {
							generateFailReport("Pos 1122 has been exists after " + noOfPTags + " P Content.");
						}
					} else {
						generateInfoReport("Page Load Ad call does not have 1122 pos.");
						int height = getDriver().manage().window().getSize().getHeight();
						int scroll = height / 50;
						int s = scroll;
						int max = 100;
						WebElement footer = null;
						try {
							footer = getDriver().findElement(By.xpath("// *[@id='footercontents']"));
						} catch (NoSuchElementException e) {
							max = 20;
						}
						for (int i = 0; i < max; i++) {
							getServer().newHar();
							StaticWait(1);
							s = s + scroll;
							jse.executeScript("window.scrollBy(0," + s + ")");
							prevScp = getSpecificKeyFromSecurePubadCall("prev_scp");
							generateInfoReport("prev_scp : " + prevScp);
							if (prevScp != null) {
								if (prevScp.contains("1122")) {
									generatePassReportWithNoScreenShot("Page Load Ad call having 1122 pos.");

									int noOfPTags = getDriver()
											.findElements(By.xpath("//div[@id='ads-pos-1122']/preceding-sibling::p"))
											.size();
									if (noOfPTags == 2) {
										generatePassReportWithNoScreenShot(
												"Pos 1122 has been moved to after two P Content.");
									} else {
										generateFailReport(
												"Pos 1122 has been exists after " + noOfPTags + " P Content.");
									}
									break;
								}
								if (footer != null && isVisibleInViewport(footer)) {
									break;
								}
							}
						}
					}
				} else {
					generateFailReport("prev_scp is found null.");
				}
			}
		} else {
			generateInfoReport(url + " is not a Emedicine / Viewarticle page.");
		}

	}

	/**
	 * Description : 1520 should be appear after three content tags of 1122 pos
	 * for Optimized
	 * 
	 * @param URL
	 *            : Its medscape URL
	 * @throws InterruptedException
	 */
	public void verifyMovePos1520() throws InterruptedException {
		if (isTrue(isAnArticle)) {
			if (textDriverOptimized) {
				// Ad call verification w.r.t to pos 1122
				String prevScp = getSpecificKeyFromSecurePubadCall("prev_scp");
				generateInfoReport("prev_scp : " + prevScp);
				if (prevScp.contains("1520")) {
					generatePassReportWithNoScreenShot("Page Load Ad call having 1520 pos.");

					// Verifying the styling updates
					verifyStylingUpdates("1520");
					int noOfPTagsAbive1520 = getDriver()
							.findElements(By.xpath("//div[@id='ads-pos-1520']/preceding-sibling::p")).size();
					int noOfPTagsAfter1122 = getDriver()
							.findElements(By.xpath("//div[@id='ads-pos-1122']/preceding-sibling::p")).size();

					if (noOfPTagsAbive1520 == 4 && noOfPTagsAfter1122 == 1) {
						generatePassReportWithNoScreenShot(
								"Pos 1520 has been moved to after three P Contents of 1122 pos.");
					} else {
						generateFailReport("Pos 1520 has not been moved to after three P Contents of 1122 pos.");
					}
				} else {
					generateFailReport("Page Load Ad call does not have 1520 pos.");
				}

			} else {
				// Ad call verification w.r.t to pos 1122
				String prevScp = "";
				prevScp = getSpecificKeyFromSecurePubadCall("prev_scp");
				generateInfoReport("prev_scp : " + prevScp);
				if (prevScp != null) {
					if (prevScp.contains("1520")) {
						generatePassReportWithNoScreenShot("Page Load Ad call having 1520 pos.");

						int noOfPTagsAbive1520 = getDriver()
								.findElements(By.xpath("//div[@id='ads-pos-1520']/preceding-sibling::p")).size();
						int noOfPTagsAfter1122 = getDriver()
								.findElements(By.xpath("//div[@id='ads-pos-1122']/preceding-sibling::p")).size();

						if (noOfPTagsAfter1122 == 2 && noOfPTagsAbive1520 >= 4) {
							generatePassReportWithNoScreenShot("Pos 1520 has been moved to after two P Content.");
						} else {
							generateFailReport("Pos 1520 has been exists after " + noOfPTagsAbive1520 + " P Content.");
						}
					} else {
						generateInfoReport("No 1520 pos in " + url);
					}
				} else {
					generateFailReport("prev_scp has returned null.");
				}
			}
		} else {
			generateInfoReport(url + " is not a Emedicine / viewarticle page.");
		}

	}

	/**
	 * Description : Pos 1420 should not be available in Ad call.
	 * 
	 * @param URL
	 *            : Its medscape URL
	 * @throws InterruptedException
	 */
	public void verifyPos1420InPageLoadedAdCall() throws InterruptedException {
		if (isTrue(isAnArticle)) {
			if (textDriverOptimized) {
				// Ad call verification w.r.t to pos 1420
				String prevScp = getSpecificKeyFromSecurePubadCall("prev_scp");
				generateInfoReport("prev_scp : " + prevScp);
				if (!prevScp.contains("1420")) {
					generatePassReportWithNoScreenShot(
							"1420 pos has been removed from Page Laoded Ad call where DTM flag has applied.");
				} else {
					generateFailReport("pos 1420 is exists in the Page Loaded  Ad call.");
				}
				// Verify 1420 pos in Lazy Loaded Ad call.
				// Lazy load call verification
				int height = getDriver().manage().window().getSize().getHeight();
				int scroll = height / 50;
				int s = scroll;
				int max = 50;
				WebElement footer = null;
				try {
					footer = getDriver().findElement(By.xpath("// *[@id='footercontents']"));
				} catch (NoSuchElementException e) {
					max = 20;
				}
				for (int i = 0; i < max; i++) {
					getServer().newHar();
					StaticWait(2);
					s = s + scroll;
					jse.executeScript("window.scrollBy(0," + s + ")");
					if (numberOfAdCallsValidation()) {
						prevScp = getSpecificKeyFromSecurePubadCall("prev_scp");
						generateInfoReport("prev_scp : " + prevScp);
						if (prevScp.contains("1420")) {
							generatePassReportWithNoScreenShot(
									"1420 pos has been found from lazy laoded Ad call where DTM flag has applied.");
							// Verifying the styling updates
							verifyStylingUpdates("1420");
						} else {
							generateInfoReport("pos 1420 does not exists in the lazy loaded  Ad call.");
						}
						break;
					}
					if (footer != null) {
						if (isVisibleInViewport(footer)) {
							break;
						}
					}
				}

			} else {
				// Ad call verification w.r.t to pos 1004
				String prevScp = getSpecificKeyFromSecurePubadCall("prev_scp");
				generateInfoReport("prev_scp : " + prevScp);
				if (prevScp != null) {
					if (prevScp.contains("1420")) {
						generatePassReportWithNoScreenShot(
								"1420 pos is available in Page Laoded Ad call where DTM flag has not applied.");
					} else {
						generateFailReport("pos 1420 is not in the Ad call.");
					}
				} else {
					generateFailReport("prev_scp has been returned null.");
				}
			}
		} else {
			generateInfoReport(url + " is not a emedicine / viewarticle page.");
		}

	}

	public void testSpacing() {
		int count = 0;
		boolean flag = true;
		String margin = null;
		scrollTillEnd();
		List<WebElement> lazyLoadAds = getDriver().findElements(By.xpath("//div[@class='inContentAd AdUnit']"));
		if (!lazyLoadAds.isEmpty()) {
			for (WebElement ad : lazyLoadAds) {
				count++;
				try {
					margin = ad.getCssValue("margin-bottom");
					Assert.assertEquals(margin, "15px");
					generatePassReport("Spacing is 15px for 1122 lazyload Ad position " + count);
				} catch (AssertionError e) {
					flag = false;
					generateFailReport("Spacing is " + margin);

				}
			}
		} else {
			generateInfoReport("No Lazyload Ads on Page");
		}
		if (!flag)
			generateFailReport("1122 lazyload ad spacing issue on URL" + url);

	}

	public void verifyAdhesiveFooterChanges() {

		getDivID();
		// Verify the Adhesive footer loaded or not
		isIframePresent = isAdhesiveFooterLoaded();

		verifyRulesOnAdhiesvefooterDisplay();

		// Adhesive Footer configuration verification
		adhesiveFooterConfig();

		if (isNoneResponsive && textDriverOptimized) {

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
	}

	protected void verifyRulesOnAdhiesvefooterDisplay() {
		if (isIframePresent) {
			// textDriverOptimized flag verification
			if (textDriverOptimized) {
				generatePassReportWithNoScreenShot("Adhesive footer has been returned on Optimized page.");
				if (isNoneResponsive) {
					generatePassReportWithNoScreenShot("Adhesive footer has been returned on none responsive page.");
				} else {
					generateFailReport(
							"Adhesive footer has been returned on responsive page, Default Adhesive footer should be displayed.");
				}
			} else {
				if (isTrue("thisIsDrug"))
					generatePassReportWithNoScreenShot("Adhesive footer has been returned on Non Optimized page.");
				else
					generateFailReport("Adhesive footer has been returned on Non Optimized page.");
			}
		} else {

			if (textDriverOptimized) {
				generateFailReport("Adhesive footer has not been returned on Optimized page.");
				if (isNoneResponsive) {
					generateFailReport("Adhesive footer has not been returned on none responsive page.");
				} else {
					generatePassReportWithNoScreenShot("Adhesive footer has not been returned on responsive page.");
				}
			} else {
				generatePassReportWithNoScreenShot("Adhesive footer has not been returned on non optimized page.");
			}
		}

	}

	protected String getOnScroll() {
		String onSCrollTemp = "";
		if (configData.contains("onScroll:")) {
			try {
				onSCrollTemp = StringUtils.substringBetween(configData, "onScroll:", ",").trim();
			} catch (Exception e) {
				onSCrollTemp = StringUtils.substringAfter(configData, "onScroll:").trim();
			}
			if (onSCrollTemp.length() > 10)
				onSCrollTemp = onSCrollTemp.substring(0, 6).trim();
			else
				onSCrollTemp = onSCrollTemp.trim();
		} else {
			generateInfoReport("onScroll data is not present in config file for Adhesive Footer.");
		}
		if (onSCrollTemp.equals("0") || onSCrollTemp.equals("")) {
			generatePassReportWithNoScreenShot("Default onScroll: \"0\" value has been returned from ad server.");
		} else {
			generateInfoReport("onScroll: " + onSCroll + " value has been returned from ad server.");
		}
		return onSCrollTemp;
	}

	public void adLabel(String pos) {
		String actual;
		String expected;
		actual = getLabelSrcFromAdPosition(pos);
		expected = expectedAdvertisementLabelSRC(getAdlabelForPosition(pos));
		if (actual.isEmpty() && pos.contains("145")) {
			try {
				Assert.assertTrue(actual.contains(expected));
				generatePassReportWithNoScreenShot("Adlabel shown properly for position " + pos);
			} catch (AssertionError e) {
				generateInfoReport("Expected : " + expected + " and Actual : " + actual);
				generateFailReport(
						"Adlabel not loaded properly for 145/1145, expected : " + expected + " and actual : " + actual);
			}
		}
	}

	protected void adhesiveFooterConfig() {
		configData = getAdhesiveFooterConfig(adDiv);
		if (configData != null) {
			if (configData.isEmpty()) {
				generatePassReportWithNoScreenShot(
						"Adhesive Footer has been served with default configurations on the page for " + url);
			} else {
				if (textDriverOptimized)
					generatePassReportWithNoScreenShot("custom config values are loaded on Optimized page");
				else
					generateFailReport("custom config values are loaded on Adhesive footer on Non Optimized.");
			}

		} else {
			generateInfoReport(
					"Adhesive footer configurations are not returned from Ad server hence default configurations should be loaded.");
		}
	}

	protected boolean isAdhesiveFooterLoaded() {
		boolean isIframe = false;
		try {
			if (getDriver().findElement(By.xpath("//div[@id='" + adDiv + "']//iframe")).isDisplayed()) {
				if (getDriver().findElement(By.xpath("//div[@id='" + adDiv + "']")).getAttribute("class")
						.contains("blank-ad")
						|| getDriver().findElement(By.xpath("//div[@id='" + adDiv + "']")).getAttribute("style")
								.contains("display: none;")) {
					generatePassReportWithNoScreenShot("Blank Adhesive footer has been loaded on the page.");
				} else {
					generatePassReportWithNoScreenShot("Adhesive footer has been loaded on the page.");
				}
				isIframe = true;
			}
		} catch (NoSuchElementException eee) {
			generateInfoReport("Creative configurations has not been returned from Ad server.");
		}
		return isIframe;
	}

	protected String getCloseButton() {
		String closeButtonTemp = "";
		if (configData.contains("closeButton:")) {
			if (configData.contains("closeButton: 'CLOSE'")) {
				closeButtonTemp = "CLOSE";
				generatePassReportWithNoScreenShot("Close button would be " + closeButtonTemp);
			} else {
				if (configData.contains("closeButton: false")) {
					closeButtonTemp = "false";
					generatePassReportWithNoScreenShot("Close button would be " + closeButtonTemp);

				} else {
					if (configData.contains("closeButton: 'X'")) {
						closeButtonTemp = "X";
						generatePassReportWithNoScreenShot("Close button would be " + closeButtonTemp);

					}
				}
			}
		} else {
			/*
			 * if close button configuration is not available then considered as
			 * true
			 */
			closeButtonTemp = "X";
			generateInfoReport("closeButton data is not present in congif data for Adhesive Footer.");
			generatePassReportWithNoScreenShot("default close button would be " + closeButtonTemp);

		}
		return closeButtonTemp;
	}

	protected String getBackGroundColor() {
		String bc = "";
		if (configData.contains("background: '")) {
			bc = StringUtils.substringBetween(configData, "background: '", "'").trim();
			generatePassReportWithNoScreenShot("Background color would be " + bc);
		} else {
			generateInfoReport("Background color is not present for Adhesive Footer.");
			generatePassReportWithNoScreenShot("Default background color #ffa500 would be applied.");
		}
		if (bc.equals("#f0efef")) {
			generatePassReportWithNoScreenShot(
					"Default background: \"#f0efef\" color has been returned from ad server.");
		} else {
			generateInfoReport("background: " + backGroundColor + "color has been returned from ad server.");
		}
		return bc;
	}

	public void verifyOldAdhesiveFooterInDrugMonorgraphPages() {

		if (isTrue("thisIsDrug")) {
			// getting the div ad
			getDivID();

			// Verify the default adhesive footer changes
			if (!is404(getDriver().getTitle())) {
				if (!isLoginPage() && numberOfAdCallsValidation()) {
					// verify the textDriverOptimized or not
					// Verify the textDriverOptimized value
					preRequisite();
					// Verify the old Adhesive Footer
					// Ad call verification
					adcallvalidation(true);

					String oldAdDiv = "";
					if (breakPoint.contains("4"))
						oldAdDiv = "ads-pos-145";
					if (breakPoint.contains("1"))
						oldAdDiv = "ads-pos-1145";
					try {
						if (verifySpecificPositionLoadedOnPage("1145")) {
							generatePassReportWithNoScreenShot(
									"Old Adhesive Footer has been exists in " + getDriver().getCurrentUrl());
						} else {
							generateInfoReport(
									"Old Adhesive Footer has been removed in " + getDriver().getCurrentUrl());
						}
					} catch (NoSuchElementException eee) {
						generateInfoReport("Old Adhesive Footer has been removed in " + getDriver().getCurrentUrl());
					}
					// Verifying the new adhesive footer
					try {
						if (getDriver().findElement(By.xpath("//div[@id='" + adDiv + "']")).isDisplayed()) {
							generateFailReport("New Adhesive Footer has been exists in " + getDriver().getCurrentUrl());
						} else {
							generatePassReportWithNoScreenShot(
									"New Adhesive Footer has not been exists in " + getDriver().getCurrentUrl());
						}
					} catch (NoSuchElementException eee) {
						generatePassReportWithNoScreenShot(
								"New Adhesive Footer has not been exists in " + getDriver().getCurrentUrl());
					}
				}
			}
		} else {
			generateInfoReport(url + " is not a Drug Monograph page.");
		}
	}

	public void verifyOldAdhesiveFooterInRecap() {

		if (isTrue("_isRecap")) {
			// getting the div ad
			getDivID();

			// Verify the default adhesive footer changes
			if (!is404(getDriver().getTitle())) {
				if (!isLoginPage() && numberOfAdCallsValidation()) {
					// verify the textDriverOptimized or not
					// Verify the textDriverOptimized value
					preRequisite();
					// Verify the old Adhesive Footer
					// Ad call verification
					adcallvalidation(true);

					String oldAdDiv = "";
					if (breakPoint.contains("4"))
						oldAdDiv = "ads-pos-145";
					if (breakPoint.contains("1"))
						oldAdDiv = "ads-pos-1145";
					try {
						if (verifySpecificPositionLoadedOnPage("1145")) {
							generatePassReportWithNoScreenShot(
									"Old Adhesive Footer has been exists in " + getDriver().getCurrentUrl());
						} else {
							generateFailReport(
									"Old Adhesive Footer has been removed in " + getDriver().getCurrentUrl());
						}
					} catch (NoSuchElementException eee) {
						generateFailReport("Old Adhesive Footer has been removed in " + getDriver().getCurrentUrl());
					}
					// Verifying the new adhesive footer
					try {
						if (getDriver().findElement(By.xpath("//div[@id='" + adDiv + "']")).isDisplayed()) {
							generateFailReport("New Adhesive Footer has been exists in " + getDriver().getCurrentUrl());
						} else {
							generatePassReportWithNoScreenShot(
									"New Adhesive Footer has not been exists in " + getDriver().getCurrentUrl());
						}
					} catch (NoSuchElementException eee) {
						generatePassReportWithNoScreenShot(
								"New Adhesive Footer has not been exists in " + getDriver().getCurrentUrl());
					}
				}
			}
		} else {
			generateInfoReport(url + " is not a Recap page.");
		}
	}

	public void verifyNewAdhesiveFooterPosAndsApplicablesSizesInAdCall() throws InterruptedException {

		if (textDriverOptimized) {
			adhesiveFooterUnit();
		}

	}

	/**
	 * Get the footer ad config data from creative div property
	 * 
	 * @return - config data
	 */
	public String getAdhesiveFooterConfig(String adDiv) {
		String htmlCode = "";
		// Switching to creative iframe
		String ifram = "";
		if (breakPoint.equals("1"))
			ifram = "/html/body/div[2]/div[2]/script[4]";
		if (breakPoint.equals("4"))
			ifram = "/html/body/div[1]/script[3]";

		htmlCode = getCreativeConfigValues("//div[@id='" + adDiv + "']//iframe", ifram);

		return htmlCode;
	}

	private void checkRDValue(String custParams, boolean flag) {
		try {
			if (!custParams.isEmpty()) {
				String rdValue = StringUtils.substringBetween(custParams, "rd=", "&");
				try {
					if (!rdValue.isEmpty()) {
						if (flag) {
							try {
								Assert.assertEquals(rdValue, "1");
								generatePassReportWithNoScreenShot("RD value tracked properly");
							} catch (AssertionError e) {
								generateFailReport("RD value expected 1 but not tracked, cust_params: " + custParams);
							}
						} else {
							try {
								Assert.assertEquals(rdValue, "0");
								generatePassReportWithNoScreenShot("RD value tracked properly");
							} catch (AssertionError e) {
								generateFailReport("RD value expected 0 but not tracked, cust_params: " + custParams);
							}
						}
					}
				} catch (NullPointerException e) {
					generateFailReport("RD not tracked under cust_params: " + custParams);
				}
			}
		} catch (NullPointerException e) {
			generateInfoReport("Cust_params is null");
		}
	}

	private boolean getDTMFlag() {
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		boolean flag = false;
		try {
			String value = jse.executeScript("return s_responsive_design").toString();
			if (value.contains("true"))
				flag = true;
		} catch (Exception e) {
			generateInfoReport("Exception while executing console command");
		}
		return flag;
	}

	private void validateMediaNetRefresh(boolean rdFlag) throws InterruptedException {
		generateInfoReport("Validating media net refresh");
		int count = 0;
		boolean flag = false;
		while (count < 8 && !flag) {
			getServer().newHar();
			count++;
			Thread.sleep(10000);
			String custParams = getSpecificKeyFromSecurePubadCall("cust_params");
			try {
				if (!custParams.isEmpty()) {
					flag = true;
					checkRDValue(custParams, rdFlag);
				}
			} catch (NullPointerException e) {
				generateInfoReport("No media net refresh observed");
			}

		}
		if (!flag)
			generateInfoReport("No Media net refresh observed");
	}

	public void validateRDValue() {
		boolean rdFlag = getDTMFlag();
		generateInfoReport("Validating page loaded ad call");
		checkRDValue(getSpecificKeyFromSecurePubadCall("cust_params"), rdFlag);
		getServer().newHar();
		scrollTillEnd();
		generateInfoReport("Validating lazyload ad call");
		checkRDValue(getSpecificKeyFromSecurePubadCall("cust_params"), rdFlag);
		scrollBottomToTop();
		try {
			getServer().newHar();
			generateInfoReport("Checking availability of next Functionality");
			getDriver()
					.findElement(By
							.xpath("//div[contains(@id,'next')]/a|//div[contains(@class,'next')]/a|//span[@class='toastArrow swipeRight']|//td[@class='next_slide']"))
					.click();
			generateInfoReport("clicked on next button");
			checkRDValue(getSpecificKeyFromSecurePubadCall("cust_params"), rdFlag);
		} catch (NoSuchElementException e) {
			generateInfoReport("No Next button available");
		} catch (WebDriverException e) {
			generateInfoReport("Exceptione while clickin on Next button");
		}
	}

	@DataProvider
	public String[][] dataProvider() {
		return XlRead.fetchDataExcludingFirstRow("TestData/dataProviderWithUserDetails.xls", "Dev01");
	}

	/**
	 * Verification of close button existance
	 */
	public void closeButtonVerification() {
		if (closButton.contains("false")) {
			boolean b = false;
			try {
				if (getDriver().findElement(By.xpath("//span[@class='footer-close-btn-text']")).isDisplayed()) {
					generateFailReport("Close button has been displayed with 'CLOSE'.");
					b = true;
				} else {
					generatePassReportWithNoScreenShot("Close button has not been displayed with 'CLOSE'");
				}
			} catch (NoSuchElementException ne) {
				generatePassReportWithNoScreenShot("Close button has not been displayed with 'CLOSE'");
			}
			try {
				if (getDriver().findElement(By.xpath("//span[@class='footer-close-btn']")).isDisplayed()) {
					generateFailReport("Close button has been displayed with 'X'.");
					b = true;
				} else {
					generatePassReportWithNoScreenShot("Close button has not been displayed with 'X'");
				}
			} catch (NoSuchElementException ne) {
				generatePassReportWithNoScreenShot("Close button has not been displayed with 'X'");
			}
			if (b) {
				generateFailReport("Adhesive Footer Ad having close button.");
			} else {
				generatePassReportWithNoScreenShot("Adhesive Footer Ad does not have close button, Close button.");
			}
		} else {
			if (closButton.contains("CLOSE")) {
				try {
					if (getDriver().findElement(By.xpath("//span[@class='footer-close-btn-text']")).isDisplayed()) {
						generatePassReportWithNoScreenShot("Close button has been displayed with 'CLOSE'.");
					} else {
						generateFailReport("Close button has not been displayed with 'CLOSE'");
					}
				} catch (NoSuchElementException ne) {
					generateFailReport("Close button has not been displayed with 'CLOSE'");
				}
			}
			if (closButton.contains("X")) {
				try {
					if (getDriver().findElement(By.xpath("//span[@class='footer-close-btn']")).isDisplayed()) {
						generatePassReportWithNoScreenShot("Close button has been displayed with 'X'.");
					} else {
						generateFailReport("Close button has not been displayed with 'X'");
					}
				} catch (NoSuchElementException ne) {
					generateFailReport("Close button has not been displayed with 'X'");
				}
			}
		}
	}

	public void closeButtonClosing() {
		if (!closButton.equals("false")) {
			if (closButton.equals("X") || closButton.equals("CLOSE")) {
				if (closButton.contains("CLOSE")) {
					try {
						if (getDriver().findElement(By.xpath("//span[@class='footer-close-btn-text']")).isDisplayed()) {
							getDriver().findElement(By.xpath("//span[@class='footer-close-btn-text']")).click();
						} else {
							generateFailReport("Close button has not been displayed with 'CLOSE'");
						}
					} catch (NoSuchElementException ne) {
						generateFailReport("Close button has not been displayed with 'CLOSE'");
					}
					try {
						if (!getDriver().findElement(By.xpath("//span[@class='footer-close-btn-text']"))
								.isDisplayed()) {
							generatePassReportWithNoScreenShot("Adhesive footer has been closed.");
						} else {
							generateFailReport("Adhesive footer has been closed.");
						}
					} catch (NoSuchElementException ne) {
						generatePassReportWithNoScreenShot("Adhesive footer has been closed.");
					}
				}
				if (closButton.contains("X")) {

					try {
						if (getDriver().findElement(By.xpath("//span[@class='footer-close-btn']")).isDisplayed()) {
							getDriver().findElement(By.xpath("//span[@class='footer-close-btn']")).click();
						} else {
							generateFailReport("Close button has not been displayed with 'X'");
						}
					} catch (NoSuchElementException ne) {
						generateFailReport("Close button has not been displayed with 'X'");
					}
					try {
						if (!getDriver().findElement(By.xpath("//span[@class='footer-close-btn']")).isDisplayed()) {
							generatePassReportWithNoScreenShot("Adhesive footer has been closed.");
						} else {
							generateFailReport("Adhesive footer has been closed.");
						}
					} catch (NoSuchElementException ne) {
						generatePassReportWithNoScreenShot("Adhesive footer has been closed.");
					}
				}

			} else {
				generateInfoReport("Adhesive Footer Ad does not have close button");
			}
		} else {
			generateInfoReport("Close button has " + closButton + " in config file.");
		}
	}

	/**
	 * Verification of background colour based on configuration value
	 */
	public void backgroudColourVerification() {
		String style = getDriver()
				.findElement(By.xpath("//*[@id='footercontents']/child::div[@class='adhesive-footer-wrapper']"))
				.getCssValue("background-color");
		if ((style != null) && (!style.isEmpty())) {
			String hex = "";
			if (backGroundColor.contains("#")) {
				hex = getRGBAColorCodeInHexCode(style);
			}
			if (hex.equalsIgnoreCase(backGroundColor)) {
				generatePassReportWithNoScreenShot("Expected-" + backGroundColor + " has been applied successfully.");
			} else {
				generateFailReport(
						"Expected-" + hex + " and Actual Background color - " + backGroundColor + " are different.");
			}
		}

	}

	/**
	 * Verification of delay in Adhesive Footer ad display
	 */
	public void delpayInAdDisplay() {
		if (onSCroll.equals("0") || onSCroll.equals("")) {
			if (isVisibleInViewport(getDriver().findElement(By.id(adDiv)))) {
				generatePassReportWithNoScreenShot("Adhesive Footer has been displayed without delay.");
			} else {
				generateFailReport("Ad has not been displayed at this moment, waiting for for Ad..");
			}
			boolean adDisplayed = isVisibleInViewport(getDriver().findElement(By.id(adDiv)));

			generateReport(adDisplayed, "Adhesive footer has been displayed", "Adhesive footer has not been displayed");

		} else {
			boolean adDisplayed = isVisibleInViewport(getDriver().findElement(By.id(adDiv)));
			int height = getDriver().manage().window().getSize().getHeight();
			height = height / 50;
			jse.executeScript("window.scrollBy(0," + height + ")");
			start = System.currentTimeMillis();
			while (!adDisplayed) {
				adDisplayed = isVisibleInViewport(getDriver().findElement(By.id(adDiv)));
				end = System.currentTimeMillis();
				if ((end - start) >= 60000) {
					break;
				}
			}
			int aScroll = Integer.parseInt(onSCroll);
			int ub = aScroll + 2000;
			int lb = aScroll - 1000;
			generateReport(((end - start) < ub) && ((end - start) > lb),
					"Adhesive footer displayed with expected : " + onSCroll,
					"Adhesive footer has not been displayed within the expected delay, expected delay is " + onSCroll
							+ ", actual delay is " + (end - start));

		}
	}

	public boolean numberOfAdCallsValidation() {
		int ncalls = getNumberOfCallsTrackedInNetwrok("securepubads.g.doubleclick.net/gampad/ads?");

		if (ncalls > 1) {
			generateFailReport(ncalls + " ads? calls are found in the page loaded session.");
			return true;
		} else {
			if (ncalls == 1) {
				generatePassReportWithNoScreenShot(
						"Only one 'securepubads.g.doubleclick.net/gampad/ads?' calls are tracked");
				return true;
			}
		}
		return false;
	}

	private void adhesiveFooterUnit() {
		if (numberOfAdCallsValidation()) {
			adcallvalidation(true);
		} else {
			if (!(getDriver().getCurrentUrl().contains("medscape.com/academy/business")
					|| getDriver().getCurrentUrl().contains("medscape.com/consult")
					|| getDriver().getCurrentUrl().contains("medscape.com/video")))
				generateFailReport("No Ad call has been found inn " + getDriver().getCurrentUrl());
			else
				generatePassReportWithNoScreenShot("No ad call has been found in academy/consult/video");
		}
	}

	/**
	 * Ad call validation, prev_scp and thir sizes.
	 * 
	 * @param pageLoad
	 *            - true - if it is page loaded ad call, false for lazy loaded
	 *            ad calls
	 */
	public void adcallvalidation(boolean pageLoad) {
		// number of secure peburd call verification
		// Verification of prev_scp and prev_iu_sizes
		String prevScp = getSpecificKeyFromSecurePubadCall("prev_scp");
		String prevIUSzs = getSpecificKeyFromSecurePubadCall("prev_iu_szs");
		generateInfoReport("prev_scp : " + prevScp);
		generateInfoReport("prev_iu_szs : " + prevIUSzs);
		if ((prevScp != null && (!prevScp.isEmpty())) && (prevIUSzs != null && (!prevIUSzs.isEmpty()))) {

			if (breakPoint.equalsIgnoreCase("4")) {
				// pos 145 verification in ad call
				if (StringUtils.countMatches(prevScp, "145") > 1) {
					generateFailReport("More than 1145/145 positions are found in Ad call, prev_scp : " + prevScp);
				} else {
					if (StringUtils.countMatches(prevScp, "145") == 1)
						generatePassReportWithNoScreenShot("Only one 1145/145 position has beeen found in Ad call.");
					else
						generateFailReport("No 1145/145 position has beeen found in Ad call.");
				}
				if (prevScp.contains("145")) {
					if (textDriverOptimized || isTrue("thisIsDrug") || isTrue("_isRecap")) {
						if (pageLoad) {
							if (isTrue("_isRecap")) {
								generateFailReport("pos 145 should not be available in Recap articles.");
							}
							generatePassReportWithNoScreenShot("Ad pos-145 has been found in ad call.");
							// applicable sizes verification
							String expectedPos = "145";
							String unExpSize = "";
							String expectedSize = "728x92";
							posAndSize(expectedSize, unExpSize, expectedPos, prevScp, prevIUSzs);
							expectedSize = "728x100";
							posAndSize(expectedSize, unExpSize, expectedPos, prevScp, prevIUSzs);
							expectedSize = "320x50";
							posAndSize(expectedSize, unExpSize, expectedPos, prevScp, prevIUSzs);
							expectedSize = "";
							unExpSize = "2x7";
							posAndSize(expectedSize, unExpSize, expectedPos, prevScp, prevIUSzs);

							// Verifying that whether duplicate sizes are exists
							String sizes = getSizesForSpecificPositionFromAdCall("145");
							if (sizes.isEmpty()) {
								generateFailReport("145 position is not available in Ad call.");
							} else {
								findDuplicateSizes(sizes);
							}

							// verification of extra sizes which are available
							// in the list
							String[] esizes = { "728x92", "728x100", "320x50" };
							notApplicableSizesValidation(expectedPos, esizes);

						} else {
							generateFailReport("pos 145 is found in Lazy Laoded ad call.");
						}
					} else {
						generateFailReport("pos 1145 should not be available for Non Optimized page.");
					}
				} else {
					if (pageLoad) {
						generateFailReport("Ad pos-145 has not been found in ad call.");
					}
					if (textDriverOptimized) {
						generateFailReport("pos 1145 should be available for Optimized page.");
					}
				}

			} else {
				if (breakPoint.equalsIgnoreCase("1")) {

					// pos 1145 verification in ad call
					if (prevScp.contains("1145")) {
						if (textDriverOptimized || isTrue("thisIsDrug") || isTrue("_isRecap")) {
							if (pageLoad) {
								generatePassReportWithNoScreenShot("Ad pos-1145 has been found in ad call.");
								// applicable sizes verification
								String expectedPos = "1145";
								String unExpSize = "";
								String expectedSize = "";
								expectedSize = "320x80";
								posAndSize(expectedSize, unExpSize, expectedPos, prevScp, prevIUSzs);
								expectedSize = "375x80";
								posAndSize(expectedSize, unExpSize, expectedPos, prevScp, prevIUSzs);
								expectedSize = "320x50";
								posAndSize(expectedSize, unExpSize, expectedPos, prevScp, prevIUSzs);

								if (isTrue("_isRecap")) {
									expectedSize = "300x50";
									posAndSize(expectedSize, unExpSize, expectedPos, prevScp, prevIUSzs);
								} else {
									expectedSize = "2x7";
									posAndSize(expectedSize, unExpSize, expectedPos, prevScp, prevIUSzs);
									expectedSize = "320x52";
									posAndSize(expectedSize, unExpSize, expectedPos, prevScp, prevIUSzs);
									expectedSize = "300x52";
									posAndSize(expectedSize, unExpSize, expectedPos, prevScp, prevIUSzs);
								}
								// Un expected positions verification
								expectedSize = "";
								unExpSize = "300x250";
								posAndSize(expectedSize, unExpSize, expectedPos, prevScp, prevIUSzs);
								unExpSize = "300x400";
								posAndSize(expectedSize, unExpSize, expectedPos, prevScp, prevIUSzs);
								if (!isTrue("_isRecap")) {
									unExpSize = "300x50";
									posAndSize(expectedSize, unExpSize, expectedPos, prevScp, prevIUSzs);
								}
								unExpSize = "300x51";
								posAndSize(expectedSize, unExpSize, expectedPos, prevScp, prevIUSzs);
								unExpSize = "320x51";
								posAndSize(expectedSize, unExpSize, expectedPos, prevScp, prevIUSzs);
								unExpSize = "2x3";
								posAndSize(expectedSize, unExpSize, expectedPos, prevScp, prevIUSzs);

								// Verifying the duplicate sizes
								String sizes = getSizesForSpecificPositionFromAdCall("1145");
								if (sizes.isEmpty()) {
									generateFailReport("1145 position is not available in Ad call.");
								} else {
									findDuplicateSizes(sizes);
								}

								// verification of extra sizes which are
								// available
								// in the list
								if (!isTrue("_isRecap")) {
									String[] esizes = { "320x52", "300x52", "320x80", "375x80", "320x50", "2x7" };

									notApplicableSizesValidation(expectedPos, esizes);
								}
							} else {
								generateFailReport("pos 1145 is found in Lazy Laoded ad call.");
							}
						} else {
							generateFailReport("pos 1145 should not be available for Non Optimized page.");
						}
					} else {
						if (pageLoad) {
							generateFailReport("Ad pos-1145 has not been found in ad call.");
						}
						if (textDriverOptimized) {
							generateFailReport("pos 1145 should be available for Optimized page.");
						}
					}

				}
			}

		} else {
			generateFailReport("prev_scp / prev_iu_szs has been found with null/empty in the Ad call.");
		}
	}

	public void testAdLabel() {
		int count = 0;
		boolean hasNextPage;
		String mainWindow = getDriver().getWindowHandle();
		do {
			hasNextPage = false;
			scrollTillEnd();
			scrollBottomToTop();
			List<String> posOnPage = getListOfLoadedAdsOnPage();
			generateInfoReport("Ads loaded on page" + posOnPage);

			for (String pos : posOnPage) {
				generateInfoReport("Validating " + pos);
				adLabel(pos);
			}
			try {
				getDriver().switchTo().window(mainWindow);
				getDriver().findElement(By.xpath("//div[@id='next-section']")).click();
				hasNextPage = true;
				generateInfoReport("Article has next page, clicked it");
			} catch (NoSuchElementException e) {
				generateInfoReport("Article has no next page");
			} catch (WebDriverException e) {
				generateInfoReport("Error while clicking the next page");

				count++;
				if (count == 1) {
					hasNextPage = true;
					getDriver().get(getDriver().getCurrentUrl() + "#vp2");
				}
			}
		} while (hasNextPage);
	}

	/**
	 * pos and applicable sizes validation.
	 * 
	 * @param expectedSize
	 *            - expected size
	 * @param unExpSize
	 *            - un expected size which should not be in the applicable sizes
	 *            list
	 * @param expectedPos
	 *            - expected position to be available in ad call.
	 * @param prev_scp
	 *            - prev_scp of ad call.
	 * @param prev_iu_szs
	 *            - ad call applicable sizes list.
	 */
	public void posAndSize(String expectedSize, String unExpSize, String expectedPos, String prev_scp,
			String prev_iu_szs) {
		StringTokenizer aPrev_scp = new StringTokenizer(prev_scp, "|");
		StringTokenizer aPrev_iu_szs = new StringTokenizer(prev_iu_szs, ",");

		if (aPrev_scp.countTokens() == aPrev_iu_szs.countTokens()) {

			while (aPrev_scp.hasMoreTokens()) {
				String position = "";
				String sizes = "";
				position = aPrev_scp.nextToken();
				sizes = aPrev_iu_szs.nextToken();
				generateInfoReport(position);
				generateInfoReport(sizes);
				if (position.contains(expectedPos))
					validatePositionAndSize(position, sizes, expectedPos, expectedSize, unExpSize);
			}
		} else {
			generateFailReport("prev_scp and prev_iu_szs counts are miss match, prev_scp count is "
					+ aPrev_scp.countTokens() + ", prev_iu_szs count is " + aPrev_iu_szs.countTokens());
		}
	}

	public void getDivID() {

		if (isTrue("thisIsDrug")) {
			if (breakPoint.contains("4"))
				adDiv = "ads-pos-145";
			if (breakPoint.contains("1"))
				adDiv = "ads-pos-1145";
		} else {
			if (breakPoint.contains("4"))
				adDiv = "ads-af-pos-145";
			if (breakPoint.contains("1"))
				adDiv = "ads-af-pos-1145";
		}
	}

	// This method will scroll the page till lazyloaded ad call observed
	private int scrollTillLazyLoad(JavascriptExecutor jse, int height) throws InterruptedException {
		boolean flag = false;
		do {
			jse.executeScript("window.scrollBy(0, 500)");
			height -= 500;

			Thread.sleep(500);

			flag = verifySpecificCallPresence("securepubads.g.doubleclick.net/gampad/ads?");
		} while (!flag && height > 500);

		return height;
	}

	/**
	 * Ads validation
	 */
	private void validateAllAdsLoadedOnPage() {
		List<WebElement> adPositions = null;
		try {
			adPositions = getDriver().findElements(By.xpath("//div[contains(@id,'ads-pos')]"));

		} catch (NoSuchElementException e) {

		}
		String position;
		for (WebElement pos : adPositions) {
			if (!pos.getAttribute("style").contains("display: none;")) {
				try {
					Assert.assertTrue(pos.isDisplayed());
					position = pos.getAttribute("id");
					generatePassReportWithNoScreenShot(position + " displayed on page");
				} catch (AssertionError e) {
					Actions actions = new Actions(getDriver());
					actions.moveToElement(pos);
					actions.perform();
					position = pos.getAttribute("id");
					generateFailReport(position + " not displayed on page");
				} catch (Exception e) {
					generateInfoReport("Un know error" + e.toString());
				}
			}
		}
	}

	/**
	 * DTM flag validation
	 * 
	 * @throws InterruptedException
	 */
	private void validateIfFlagTrue() throws InterruptedException {
		generateInfoReport("Flag value set to True, validating Conditions if Flag is True");
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();

		boolean isNextPageAvailable = false;

		int height = 0;

		do {
			// Validating pageloaded Ad call
			try {
				Assert.assertFalse(verifySpecificAdPresenceInSecurePubadCall("1420"));
				generatePassReportWithNoScreenShot("1420 is not shown in page loaded ad call");
			} catch (AssertionError e) {
				String prev_scp = getSpecificKeyFromSecurePubadCall("prev_scp");
				generateFailReport("1420 shown in pageloaded ad call: prev_scp = " + prev_scp);
			}

			// Validating Ad call loaded before ad loaded on page or not
			getServer().newHar();
			Actions actions = new Actions(getDriver());
			actions.moveToElement(
					getDriver().findElement(By.xpath("//div[@id='ads-pos-1520']/following-sibling::p[3]")));//// div[@id='ads-pos-1420']/preceding-sibling::p[1]
			actions.perform();
			try {
				Thread.sleep(3000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				Assert.assertTrue(verifySpecificCallPresence("securepubads.g.doubleclick.net/gampad/ads?"));
				generatePassReportWithNoScreenShot("Ad call loaded prior to Ad position loaded");
				try {
					Assert.assertTrue(verifySpecificAdPresenceInSecurePubadCall("1420"));
					generatePassReportWithNoScreenShot("1420 present in First lazyloaded ad call");

					// Validating lazy load parameter
					try {
						Assert.assertTrue(getSpecificKeyFromSecurePubadCall("cust_params").contains("ll=1"));
						generatePassReportWithNoScreenShot("Lazy load parameter loaded properly");
					} catch (AssertionError e) {
						generateFailReport("lazyload parameter not loaded under cust_params");
					}
				} catch (AssertionError e) {
					String prev_scp = getSpecificKeyFromSecurePubadCall("prev_scp");
					generateFailReport("1420 not present in first lazyloaded ad call: prev_scp = " + prev_scp);
				}

			} catch (AssertionError e) {
				generateFailReport("Ad call not loaded prior to the Ad position load");
			}

			// Validating First lazy loaded ad calls
			getServer().newHar();
			height = Integer.parseInt(jse.executeScript("return document.body.scrollHeight").toString());
			int scrolledHeight = Integer.parseInt(jse.executeScript("return window.pageYOffset").toString());
			height = height - scrolledHeight;

			// Validating Remaining lazyloaded ad calls
			while (height > 500) {
				getServer().newHar();
				height = scrollTillLazyLoad(jse, height);

				try {
					Assert.assertFalse(verifySpecificAdPresenceInSecurePubadCall("1420"));
					generatePassReportWithNoScreenShot("1420 is not present in Next lazyloaded ad call");
				} catch (AssertionError e) {
					String prev_scp = getSpecificKeyFromSecurePubadCall("prev_scp");
					generateFailReport("1420 is present in Next lazyloaded ad call: prev_scp = " + prev_scp);
				}
			}
			// call_Gallen("", "");
			try {
				getDriver().findElement(By.xpath("//div[@id='next-section']")).click();
				generateInfoReport("Validating next page");
				isNextPageAvailable = true;
			} catch (NoSuchElementException e) {
				generateInfoReport("Next page Not available");
			}
		} while (isNextPageAvailable);
	}

	/**
	 * Validation of textDriverOptimized flag
	 * 
	 * @throws InterruptedException
	 */
	private void validateIfFlagFalse() throws InterruptedException {
		generateInfoReport("Flag value set to False, validating Conditions if Flag is False");
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();

		boolean isNextPageAvailable = false;

		int height = 0;

		do {
			// Validating pageloaded Ad call
			try {
				Assert.assertTrue(verifySpecificAdPresenceInSecurePubadCall("1420"));
				generatePassReportWithNoScreenShot("1420 is shown in page loaded ad call for flag is False");
			} catch (AssertionError e) {
				String prev_scp = getSpecificKeyFromSecurePubadCall("prev_scp");
				generateFailReport(
						"1420 is not shown in page loaded ad call for flag is False, prev_scp = " + prev_scp);
			}

			// Validating First lazy loaded ad calls
			getServer().newHar();
			height = Integer.parseInt(jse.executeScript("return document.body.scrollHeight").toString());
			height = scrollTillLazyLoad(jse, height);

			try {
				Assert.assertFalse(verifySpecificAdPresenceInSecurePubadCall("1420"));
				generatePassReportWithNoScreenShot("1420 not present in First lazyloaded ad call");
			} catch (AssertionError e) {
				String prev_scp = getSpecificKeyFromSecurePubadCall("prev_scp");
				generateFailReport("1420 present in first lazyloaded ad call, prev_scp = " + prev_scp);
			}

			// Validating Remaining lazyloaded ad calls
			while (height > 500) {
				getServer().newHar();
				height = scrollTillLazyLoad(jse, height);

				try {
					Assert.assertFalse(verifySpecificAdPresenceInSecurePubadCall("1420"));
					generatePassReportWithNoScreenShot("1420 is not present in Next lazyloaded ad call");
				} catch (AssertionError e) {
					String prev_scp = getSpecificKeyFromSecurePubadCall("prev_scp");
					generateFailReport("1420 is present in Next lazyloaded ad call, prev_scp = " + prev_scp);
				}
			}

			try {
				getDriver().findElement(By.xpath("//div[@id='next-section']")).click();
				generateInfoReport("Validating next page");
				isNextPageAvailable = true;
			} catch (NoSuchElementException e) {
				generateInfoReport("Next page Not available");
			}
		} while (isNextPageAvailable);
	}

	/**
	 * Ad call validation, prev_scp and their sizes.
	 * 
	 * @param lazyLoad
	 *            - true - if it is lazy loaded ad call, false for page loaded
	 *            ad calls
	 */
	public void adcallvalidation(String pos) {
		// number of secure ads call verification
		// Verification of prev_scp and prev_iu_sizes
		String prevScp = getSpecificKeyFromSecurePubadCall("prev_scp");
		String prevIUSzs = getSpecificKeyFromSecurePubadCall("prev_iu_szs");
		generateInfoReport("prev_scp : " + prevScp);
		generateInfoReport("prev_iu_szs : " + prevIUSzs);
		if ((prevScp != null && (!prevScp.isEmpty())) && (prevIUSzs != null && (!prevIUSzs.isEmpty()))) {

			if (breakPoint.equalsIgnoreCase("1")) {

				// pos 1122 verification in ad call
				if (prevScp.contains(pos)) {
					generatePassReportWithNoScreenShot("Ad pos-" + pos + " has been found in ad call.");
					// applicable sizes verification
					String expectedPos = pos;
					String unExpSize = "";
					String expectedSize = "";
					String unExpectedPos = "";
					expectedSize = "320x50";
					posAndSize(expectedSize, unExpSize, expectedPos, unExpectedPos);
					expectedSize = "2x3";
					posAndSize(expectedSize, unExpSize, expectedPos, unExpectedPos);
					expectedSize = "1x12";
					posAndSize(expectedSize, unExpSize, expectedPos, unExpectedPos);
					expectedSize = "300x254";
					posAndSize(expectedSize, unExpSize, expectedPos, unExpectedPos);
					// Verifying the duplicate sizes
					String sizes = getSizesForSpecificPositionFromAdCall(pos);
					if (sizes.isEmpty()) {
						generateFailReport(pos + " position is not available in Ad call.");
					} else {
						findDuplicateSizes(sizes);
					}
				} else {
					generateFailReport("Ad pos-" + pos + " has not been found in ad call.");
				}

			} else {
				generateSkipReport("Break point 4 has been found, its not in scope of current user story.");
			}
		} else {
			generateFailReport("prev_scp / prev_iu_szs has been found with null/empty in the Ad call.");
		}
	}

	/**
	 * 
	 */
	private void verifyChangesEffected(WebElement webElement, String pos) {

		// Verifying the center alignment
		try {
			String textAlignment = webElement.getCssValue("text-align");
			if (textAlignment.equals("center")) {
				generatePassReportWithNoScreenShot(pos + " Ad has been aligned to center.");
			} else {
				generateFailReport(pos + " Ad has not been aligned to center, its aligned to " + textAlignment);
			}
		} catch (NoSuchElementException e) {
			generateFailReport(pos + " Ad has not been aligned to center.");
		}

		// Verifying the top pin lines
		try {
			String borderTop = webElement.getCssValue("border-top");
			if (borderTop.isEmpty() || borderTop.contains("0px")) {
				generatePassReportWithNoScreenShot(pos + " has no top pin line.");
			} else {
				generateFailReport(pos + " has top pin line, pin line is " + borderTop);
			}
		} catch (NoSuchElementException e) {
			generatePassReportWithNoScreenShot(pos + " has no top pin line.");
		}

		// Verifying the bottom pin lines
		try {
			String borderBottom = webElement.getCssValue("border-bottom");
			if (borderBottom.isEmpty() || borderBottom.contains("0px")) {
				generatePassReportWithNoScreenShot(pos + " has no bottom pin line.");
			} else {
				generateFailReport(pos + " has bottom pin line, pin line is " + borderBottom);
			}
		} catch (NoSuchElementException e) {
			generatePassReportWithNoScreenShot(pos + " has no bottom pin line.");
		}

		// Verification of Adlabel
		try {
			String adLabel = webElement.getCssValue("background-image");
			if (adLabel.equals("none")) {
				generateFailReport(pos + " has no Adlabel.");
			} else {
				generatePassReportWithNoScreenShot(pos + " has Adlabel.");
				if (isVisibleInViewport(webElement)) {
					generatePassReportWithNoScreenShot(pos + " Ad is in view port.");
					// Verify the AdLabel
					adLabel(pos);
				} else {
					generateInfoReport(pos
							+ " Adhesive Footer Ad is not in view port, ad server may not be returned the creative.");
				}
			}
		} catch (NoSuchElementException e) {
			generateFailReport(pos + " has no Adlabel.");
		}
	}

	private void verifyNoChangesEffected(WebElement webElement, String pos) {

		// Verifying the center alignment
		try {
			String textAlignment = webElement.getCssValue("text-align");
			if (textAlignment.equals("left")) {
				generatePassReportWithNoScreenShot(pos + " Ad has been aligned to left.");
			} else {
				generateFailReport(pos + " Ad has not been aligned to left, its aligned to " + textAlignment);
			}
		} catch (NoSuchElementException e) {
			generateFailReport(pos + " Ad has not been aligned to left.");
		}

		// Verifying the top pin lines
		try {
			String borderTop = webElement.getCssValue("border-top");
			if ((!borderTop.isEmpty()) || borderTop.contains("1px")) {
				generatePassReportWithNoScreenShot(pos + " has top pin line.");
			} else {
				generateFailReport(pos + " has top no pin line, pin line is " + borderTop);
			}
		} catch (NoSuchElementException e) {
			generateFailReport(pos + " has top pin line.");
		}

		// Verifying the bottom pin lines
		try {
			String borderBottom = webElement.getCssValue("border-bottom");
			if ((!borderBottom.isEmpty()) || borderBottom.contains("1px")) {
				generatePassReportWithNoScreenShot(pos + " has bottom pin line.");
			} else {
				generateFailReport(pos + " has no bottom pin line, pin line is " + borderBottom);
			}
		} catch (NoSuchElementException e) {
			generateFailReport(pos + " has no bottom pin line.");
		}

		// Verification of Adlabel
		try {
			String adLabel = webElement.getCssValue("background-image");
			if (adLabel.equals("none")) {
				generatePassReportWithNoScreenShot(pos + " has no Adlabel.");
			} else {
				generateFailReport(pos + " has Adlabel.");
				if (isVisibleInViewport(webElement)) {
					generatePassReportWithNoScreenShot(pos + " Ad is in view port.");
					// Verify the AdLabel
					adLabel(pos);
				} else {
					generateInfoReport(pos + "Ad is not in view port, ad server may not be returned the creative.");
				}
			}
		} catch (NoSuchElementException e) {
			generatePassReportWithNoScreenShot(pos + " has no Adlabel.");
		}
	}

	public void preRequisite() {

		if (isTrue(isEmed)) {
			generateInfoReport(url + " is a Emedicine URL.");
		} else {
			generateInfoReport(url + " is not a Emedicine URL.");
		}
		if (isTrue(isAnArticle)) {
			generateInfoReport(url + " is an Viewarticle URL.");
		} else {
			generateInfoReport(url + " is not an Viewarticle URL.");
		}
		// Verify the textDriverOptimized value
		if (isTrue("textDriverOptimized")) {
			generateInfoReport("DTM Flag has been set hence expected changes should be appear on page.");
			textDriverOptimized = true;
		} else {
			generateInfoReport("DTM Flag has not been set hence expected changes should not be appear on page.");
		}
		if (isNoneResponsivePage()) {
			isNoneResponsive = true;
			generateInfoReport(
					url + " is a none responsive(rd=1) page hence Adhesive footer should be served on this page. ");
		} else {
			generateInfoReport(
					url + " is a responsive page(rd=0) hence Adhesive footer should not be served on this page. ");
		}
	}

	/**
	 * Styling changes validation
	 */
	protected void verifyAdhesivefooterOverlappingIssue() {
		generateReport(
				getDriver()
						.findElement(
								By.xpath("//div[@id='footercontents']/child::div[@class='adhesive-footer-wrapper']"))
						.getCssValue("z-index").equals("5000000"),
				"Adhesive footer z-index has been set as '5000000'",
				"Adhesive footer z-index has not been set as '5000000'");
	}
}
