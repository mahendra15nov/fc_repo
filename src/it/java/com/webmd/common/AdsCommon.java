package com.webmd.common;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;

import com.webmd.general.common.ActionType;
import com.webmd.general.common.ReadProperties;

import jxl.Sheet;
import jxl.Workbook;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;

public class AdsCommon extends ActionType {
	HashMap<String, String> params = new HashMap<>();
	public WebElement element = null;
	public JavascriptExecutor jse = null;

	public enum QueryStringParamENUM {

		GDFP_REQ("gdfp_req"), PVSID("pvsid"), CORRELATOR("correlator"), OUTPUT("output"), CALLBACK("callback"), IMPL(
				"impl"), IU_PARTS("iu_parts"), ENC_PREV_IUS("enc_prev_ius"), PREV_SCP("prev_scp"), PREV_IU_SZS(
						"prev_iu_szs"), CUST_PARAMS("cust_params"), COOKIE("cookie"), URL("url");

		private String key;

		QueryStringParamENUM(String key) {
			this.key = key;
		}

		public String value() {
			return key;
		}
	}

	public enum AdPosENUM {

		POS_1005("1005"), POS_1122("1122"), POS_101("101"), POS_1145("1145"), POS_800("800"), POS_1909("1909"), POS_122(
				"122"), POS_909(
						"909"), POS_141("141"), POS_520("520"), POS_420("420"), POS_1420("1420"), POS_1520("1520");

		private String key;

		AdPosENUM(String key) {
			this.key = key;
		}

		public String value() {
			return key;
		}
	}

	/*
	 * This method will return true/false based on the availability of requested
	 * call in the network traffic. URL/Part of URL is input of this method
	 */

	public boolean verifySpecificCallPresence(String callName) {
		Har har = getServer().getHar();
		try {
			List<HarEntry> entries = har.getLog().getEntries();
			for (HarEntry entry : entries) {
				if (entry.getRequest().getUrl().contains(callName)) {
					return true;
				}
			}
		} catch (Exception e) {
			generateInfoReport("No calls recorded in HAR");
		}
		return false;
	}

	/*
	 * This is the method to return true or false based on the Ad availability
	 * in the Securepubad call. Ad position id is the input to this method
	 * [Mahendra] : updated on 11 Sep 2019
	 */
	public boolean verifySpecificAdPresenceInSecurePubadCall(String ad) {
		boolean flag = false;
		List<String> aPos = getPositionsFromPrevScp(getSpecificKeyFromSecurePubadCall("prev_scp"));
		try {
			if (aPos.contains(ad))
				flag = true;
		} catch (NullPointerException e) {
		}
		return flag;
	}

	/*
	 * This is the method to return required value for the key from Securepubad
	 * call Key name is the input to this method Note: If there are multiple Ad
	 * calls, this method will append the values
	 */
	public String getSpecificKeyFromSecurePubadCall(String key) {
		HashSet<String> temp = new HashSet<>();
		params.clear();
		String value = "";
		Har har = getServer().getHar();
		List<HarEntry> entries = har.getLog().getEntries();
		for (HarEntry entry : entries) {
			if (entry.getRequest().getUrl().contains(AdsConstantns.AD_CALL)) {
				List<HarNameValuePair> queryParams = entry.getRequest().getQueryString();
				for (HarNameValuePair harNameValuePair : queryParams) {
					if (harNameValuePair.getName().trim().equalsIgnoreCase(key)) {
						switch (key) {
						case "prev_scp":
							// Currently media net refresh calls not
							// considering
							if (!harNameValuePair.getValue().trim().contains("mnrf")) {
								value = appendAdCall(value, harNameValuePair.getValue().trim());
							}
							break;
						case "prev_iu_szs":
							String[] aPos = null;
							String[] aSzs = null;
							boolean isMR = false;
							for (HarNameValuePair harNameValuePair1 : queryParams) {
								if (harNameValuePair1.getName().trim().equalsIgnoreCase("prev_scp")) {
									// Currently media net refresh calls not
									// considering
									if (!harNameValuePair1.getValue().trim().contains("mnrf")) {
										aPos = StringUtils.substringsBetween(harNameValuePair1.getValue().trim(),
												"pos=", "&");
									} else {
										isMR = true;
									}
								}
							}
							if (!isMR) {
								aSzs = harNameValuePair.getValue().trim().split(",");
								for (int i = 0; i < aSzs.length; i++) {
									if (temp.add(aPos[i])) {
										if (value.isEmpty()) {
											value = aSzs[i];
										} else {
											value = value + "," + aSzs[i];
										}
									}
								}
								isMR = false;
							}
							break;
						default:
							value = harNameValuePair.getValue().trim();
						}
					}
				}
			}
		}
		return value;
	}

	private String appendAdCall(String previousKey, String currentKey) {
		String value = "";
		if (previousKey.isEmpty()) {
			value = currentKey;
		} else {
			value = previousKey;
			String[] aPos = currentKey.split("\\|");
			for (String pos : aPos) {
				String p = StringUtils.substringBetween(pos, "pos=", "&");
				if (!previousKey.contains(p)) {
					value = value + "|" + pos;
				}
			}

		}
		return value;
	}

	/*
	 * This method is to get available size values for a particular position in
	 * the Ad call Need to send position value as input and it will return
	 * available sizes if that position loaded in the call otherwise it will
	 * return NULL
	 */
	public String getSizesForSpecificPositionFromAdCall(String pos) {
		String prevIuSzs = getSpecificKeyFromSecurePubadCall("prev_iu_szs");
		String prevScp = getSpecificKeyFromSecurePubadCall("prev_scp");

		if (prevScp.contains(pos)) {
			String[] sizes = prevIuSzs.split(",");
			String[] positions = prevScp.split("\\|");

			for (int i = 0; i < positions.length; i++) {
				if (positions[i].contains(pos))
					return sizes[i];
			}
		}
		return null;
	}

	public String getCurrentPageType() {
		String url = getDriver().getCurrentUrl();
		String type = null;
		if (url.contains("vercolecao") || url.contains("verartigo") || url.contains("viewarticle")
				|| url.contains("viewcollection") || url.contains("article") || url.contains("verarticulo")
				|| url.contains("vercoleccion") || url.contains("artikelansicht") || url.contains("uebersicht")
				|| url.contains("voirarticle") || url.contains("voircollection")
				|| (url.contains("emedicine") && url.contains("article"))
				|| (url.contains("reference") && url.contains("drug"))) {
			type = "content";
		} else if (url.split("/").length == 3 || url.split("/").length == 2) {
			type = "homePage";
		} else if (url.contains("index")) {
			type = "indexPage";
		} else {
			type = "0";
		}
		return type;
	}

	/*
	 * This method is to get Response of any specific call
	 */

	public String getResponseForSpecificCall(String callName) {
		params.clear();
		String response = null;
		Har har = getServer().getHar();
		List<HarEntry> entries = har.getLog().getEntries();
		for (HarEntry entry : entries) {
			if (entry.getRequest().getUrl().contains(callName)) {
				response = entry.getResponse().getContent().getText();
				return response;
			}
		}
		return response;
	}

	public boolean verifySpecificPositionLoadedOnPage(String pos, WebElement lazyLE) {
		if (lazyLE != null) {
			element = lazyLE;
		}
		return verifySpecificPositionLoadedOnPage(pos);
	}

	/*
	 * This method will verify the ad presence on page and return "true" if ad
	 * shown on page even as blank Otherwise it will return "false"
	 */
	public boolean verifySpecificPositionLoadedOnPage(String pos) {

		String locator = "//div[contains(@id,'ads-pos-" + pos + "') or contains(@id,'ads-af-pos-" + pos + "')]";

		generateInfoReport("locator=>" + locator);
		return verifySpecificPositionLoadedOnPage(pos, locator);
	}

	/**
	 * 
	 * @param pos
	 *            - Ad position
	 * @param locator
	 * @return
	 */
	public boolean verifySpecificPositionLoadedOnPage(String pos, String locator) {

		boolean flag = false;

		int height = 0;
		int width = 0;

		// Verify the lazy load element in map
		if (element == null) {
			try {
				element = getDriver().findElement(By.xpath(locator));
			} catch (NoSuchElementException e) {
				generateFailReport(locator + " has not found in the page.");
			}
		}
		if (element != null) {
			String styleAdTag = "";
			try {
				WebElement adTagHeader = getDriver().findElement(By.xpath("//div[@id='adtagheader']"));
				styleAdTag = adTagHeader.getAttribute("style");
			} catch (NoSuchElementException e) {
			}
			String className = element.getAttribute("class");
			String style = "";
			try {
				style = element.getAttribute("style");
			} catch (NoSuchElementException e) {

			}
			if (className.equalsIgnoreCase("adunit")) {
				if (style.equalsIgnoreCase("display: none;") || styleAdTag.equalsIgnoreCase("display: none;")) {
					generatePassReportWithNoScreenShot(pos + " has been loaded with style=\"display: none;\" ");
					// Verify the Ad collapser
					flag = isCollapserServed(pos);
				} else {
					WebElement iframe = null;
					locator = locator + "//iframe";
					try {
						iframe = getDriver().findElement(By.xpath(locator));
					} catch (NoSuchElementException e) {

					}

					if (iframe != null) {
						if (!iframe.getAttribute("width").isEmpty() && (!iframe.getAttribute("height").isEmpty())) {
							width = Integer.parseInt(iframe.getAttribute("width"));
							height = Integer.parseInt(iframe.getAttribute("height"));
						}
						if (width == 0 && height == 0) {
							locator = locator + "//preceding-sibling::div";
							width = Integer.parseInt(StringUtils.substringBefore(
									getDriver().findElement(By.xpath(locator)).getCssValue("width"), "px"));
							height = Integer.parseInt(StringUtils.substringBefore(
									getDriver().findElement(By.xpath(locator)).getCssValue("height"), "px"));
						}
						// Verifies the adlabel
						adLabel(pos);

						generateInfoReport("Width is : " + width + " and Height is : " + height);
						if (pos.contains("_"))
							pos = pos.split("_")[0];
						String sizes = getSizesForSpecificPositionFromAdCall(pos);
						// text ad actual size and Ad call size will be
						// different
						if (pos.contains("420") || pos.contains("520") || pos.contains("145")) {
							generateInfoReport(
									"Curremtly serving a text Ad with width :" + width + ", height:" + height);
							width = 2;
							if (pos.contains("145"))
								height = 7;
							else
								height = 3;
						}
						if (!sizes.contains(width + "x" + height)) {
							String[] aRTPos = new String[] { "122", "910", "909" };
							if (Arrays.toString(aRTPos).contains(pos)) {
								(new Actions(getDriver())).moveToElement(getDriver().findElement(By.xpath(locator)));
								StaticWait(3);
								if (!iframe.getAttribute("width").isEmpty()
										&& (!iframe.getAttribute("height").isEmpty())) {
									width = Integer.parseInt(iframe.getAttribute("width"));
									height = Integer.parseInt(iframe.getAttribute("height"));
								}
								if (width == 0 && height == 0) {
									locator = locator + "//preceding-sibling::div";
									width = Integer.parseInt(StringUtils.substringBefore(
											getDriver().findElement(By.xpath(locator)).getCssValue("width"), "px"));
									height = Integer.parseInt(StringUtils.substringBefore(
											getDriver().findElement(By.xpath(locator)).getCssValue("height"), "px"));
								}
							}
						}
						generateReport(sizes.contains(width + "x" + height),
								width + "x" + height + " is appears in applicable sizes list : " + sizes,
								width + "x" + height + " does not appears in applicable sizes list : " + sizes);
						boolean b = width > 10 && height > 10;

						if (pos.contains("420") || pos.contains("520") || pos.contains("145")) {
							if (width == 2)
								b = true;
							else
								b = width > 10;
						}
						// when size 1x2 i.e. width and height are less than
						// 10px and
						// those are expected
						if (sizes.contains(width + "x" + height))
							b = true;

						generateReport(b, "height: " + height + " and width: " + width + " are greater than 10px",
								"height: " + height + " and width: " + width + " are not greater than 10px");
						if (width > 10 && height > 10) {
							element = null;
							return true;
						}
					} else {
						generateFailReport("Creative has not been laoded on the page.");
					}
				}
			} else {
				try {
					style = element.getAttribute("style");
				} catch (NoSuchElementException e) {

				}
				if (style.contains("display: none;")) {
					generatePassReportWithNoScreenShot(pos + " has been loaded with style=\"display: none;\" ");
					flag = true;
				} else {

					if (className.equalsIgnoreCase("blank-ad")) {
						WebElement iframe = null;
						locator = locator + "//iframe";
						try {
							iframe = getDriver().findElement(By.xpath(locator));
						} catch (NoSuchElementException e) {

						}
						if (iframe != null) {
							width = Integer.parseInt(iframe.getAttribute("width"));
							height = Integer.parseInt(iframe.getAttribute("height"));
							if (width == 0 && height == 0) {
								locator = locator + "//preceding-sibling::div";
								width = Integer.parseInt(StringUtils.substringBefore(
										getDriver().findElement(By.xpath(locator)).getCssValue("width"), "px"));
								height = Integer.parseInt(StringUtils.substringBefore(
										iframe.findElement(By.xpath(locator)).getCssValue("height"), "px"));
							}
							generateInfoReport("Width is : " + width + " and Height is : " + height);
							boolean b = (width < 10 && height < 10);

							if (pos.contains("420") || pos.contains("520") || pos.contains("145")) {
								b = width < 10;
							}
							generateReport(b,
									"Width:" + width + " and Height:" + height + " shown as expected for Blank Ad",
									"Missmatch in width:" + width + " and height:" + height + " for blank ad.");
							// Verify the collapser
							flag = isCollapserServed(pos);

						} else {
							generateFailReport("Creative has not been laoded on the page.");
						}
					} else {
						// [Mahendra-17/07/2019]- Validate text Ads
						if (className.equalsIgnoreCase("textDriver")) {
							width = Integer.parseInt(StringUtils.substringBetween(
									getDriver().findElement(By.xpath(locator + "//iframe")).getAttribute("style"),
									"width:", "px;").trim());
							generateInfoReport("Width is: " + width);

							generateReport(width > 10, "width:" + width + " is expected",
									"Missmatch in width:" + width + "");
							if (width > 10)
								flag = true;
						}
					}

				}
			}
		}
		element = null;
		return flag;
	}

	public int getNumberOfCallsTrackedInNetwrok(String call) {
		Har har = getServer().getHar();
		har.getLog().getBrowser();
		List<HarEntry> res = har.getLog().getEntries();
		int n = 0;
		ArrayList<String> prevScp = new ArrayList<>();
		for (HarEntry harEntry : res) {
			String url = "";
			try {
				url = harEntry.getRequest().getUrl();
			} catch (NullPointerException e) {

			}
			if (url.contains(call)) {

				List<HarNameValuePair> queryParams = harEntry.getRequest().getQueryString();
				for (HarNameValuePair harNameValuePair : queryParams) {
					if (harNameValuePair.getName().trim().equalsIgnoreCase("prev_scp")) {
						if (!harNameValuePair.getValue().trim().contains("mnrf")) {
							ArrayList<String> aCurrentPos = new ArrayList(
									getPositionsFromPrevScp(harNameValuePair.getValue().trim()));
							if (n == 0) {
								n++;
								prevScp.addAll(aCurrentPos);
							} else {
								if (prevScp.containsAll(aCurrentPos)) {
									generateFailReport("Duplicate Ad call is fired.");
									n++;
								} else {
									for (String p : aCurrentPos) {
										if (prevScp.contains(p)) {
											generateFailReport("Duplicate Ad pos : " + p + " is appear in ad call.");
										} else {
											prevScp.add(p);
										}
									}

								}
							}
							if (n != 1) {
								aCurrentPos.retainAll(prevScp);
								for (String pos : aCurrentPos) {
									generateFailReport(pos + " has found duplicate pos in consecutive Ad call.");
								}
							}
						} else {
							generateInfoReport(
									"Medianet refresh call has been found --> " + harNameValuePair.getValue().trim());
						}
					}
				}

			}
		}
		return n;

	}

	public void scrollTillEnd() {
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		int height = Integer.parseInt(jse.executeScript("return document.body.scrollHeight").toString());
		while (height > 100) {
			jse.executeScript("window.scrollBy(0, 100)");
			height -= 100;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// This method is to scroll to a web element, WebElement should be pass to
	// it
	public void scrollToWebElement(WebElement pos) {
		Actions actions = new Actions(getDriver());
		actions.moveToElement(pos);
		actions.perform();
	}

	public void scrollBottomToTop() {
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		jse.executeScript("window.scrollBy(0, -document.body.scrollHeight)");
	}

	/*
	 * public void throwErrorOnTestFailure() throws Error { try { if
	 * (getProperty("closeBrowserAfterEveryMethod").equals("true")) {
	 * getDriver().quit(); getServer().stop(); } } catch (Exception e) {
	 * e.printStackTrace(); } String status =
	 * ExtentTestManager.getTest().getRunStatus().toString().toLowerCase(); if
	 * (status.equals("fail")) { addScreenShotToReport(); throw new Error(
	 * "Test Case Failed, please go through the above test steps for more information"
	 * ); } }
	 */

	/*
	 * This method will check whether advertisement label shown or not, if shown
	 * whether it displayed bottom center or not Return true if advertisement
	 * label shown, false otherwise
	 */
	public boolean validateAdvertisementLabel(WebElement pos) {
		String labelPosition = null;
		String labelImageLocation = null;
		String labelImageExpected = "https://img.medscapestatic.com/pi/global/text/text_advertisement_top.gif";
		try {
			scrollToWebElement(pos);
			labelPosition = pos.getCssValue("background-position");
			labelImageLocation = pos.getCssValue("background-image");
			if (labelPosition.equals("bottom") && labelImageLocation.contains(labelImageExpected)) {
				generateInfoReport(
						"Advertisement label shown at the bottom of the position: " + pos.getAttribute("id"));
				return true;
			} else {
				generateInfoReport(
						"Advertisement label not shown at the bottom center of the Ad pos: " + pos.getAttribute("id"));
				return false;
			}
		} catch (Exception e) {
			generateInfoReport(
					"Advertisement label not shown at the bottom center of the Ad pos: " + pos.getAttribute("id"));
			return false;
		}
	}

	/*
	 * This method will provide the alignment of ad position on page
	 */

	public String validatePhysicalPositionOfAdOnPage(WebElement pos) {
		String adPositionOnPage = null;
		scrollToWebElement(pos);
		adPositionOnPage = pos.getCssValue("text-align");
		generateInfoReport(pos.getAttribute("id") + " : shwon " + adPositionOnPage + " of the page");
		return adPositionOnPage;

	}

	// This method will check whether pin lines shown or not, if pin lines shown
	// return true otherwise false
	public boolean checkPinLinesDisplay(WebElement pos) {
		String topBorder;
		String bottomBorder;
		try {
			topBorder = pos.getCssValue("border-top");
			bottomBorder = pos.getCssValue("border-bottom");
			generateInfoReport(topBorder + " " + bottomBorder);
			Assert.assertTrue(topBorder.contains("1px solid"));
			Assert.assertTrue(bottomBorder.contains("1px solid"));
			generateInfoReport("Pin lines shown for ad position: " + pos.getAttribute("id"));
			return true;
		} catch (AssertionError e) {
			e.printStackTrace();
			generateInfoReport("Pin lines not shown for ad position: " + pos.getAttribute("id"));
			return false;
		}
	}

	/**
	 * Validation of specified size is available for given position, also verify
	 * unexpected size to be removed from applicable sizes for given pos
	 * 
	 * @param position
	 *            - prev_scp
	 * @param sizes
	 *            - prev_iu_szs
	 * @param expectedPos
	 *            - expected pos
	 * @param expectedSize
	 *            - expected size
	 * @param unExpSize
	 *            - unexpected size
	 */
	public void validatePositionAndSize(String position, String sizes, String expectedPos, String expectedSize,
			String unExpSize) {

		// Expected position
		if (position.contains(expectedPos)) {
			generatePassReportWithNoScreenShot("Position " + expectedPos + " has been found in Ad call.");
			if (!expectedSize.isEmpty()) {
				if (sizes.contains(expectedSize)) {
					generatePassReportWithNoScreenShot("Applicable size " + expectedSize + " has been found.");
				} else {
					generateFailReport("Applicable size " + expectedSize + " has not been found.");
				}
			}

			// Verifying w.r.t unexpected position.
			if (!unExpSize.isEmpty()) {
				if (sizes.contains(unExpSize)) {
					generateFailReport("Applicable size " + unExpSize + " has been found.");
				} else {
					generatePassReportWithNoScreenShot("Applicable size " + unExpSize + " has not been found.");
				}
			}
		} else {
			generateFailReport("Position " + expectedPos + " has not been found in Ad call.");
		}
	}

	// This method is to get the physical size of the ad position page
	public int getSizeOfAdPosition(WebElement pos) {
		Actions actions = new Actions(getDriver());
		actions.moveToElement(pos);
		actions.perform();
		generateInfoReport(pos.getCssValue("height"));
		int height = Integer.parseInt(pos.getCssValue("height").split("p")[0]);
		generateInfoReport("height is " + height);
		return height;
	}

	// This method is to make sure whether specific ad position is Text or not,
	// it returns true if text driver, false otherwise
	public boolean checkWetherTextAd(String pos) {
		WebElement iFrameLocator = getDriver().findElement(By.xpath("//div[@id='ads-pos-" + pos + "']/div/iframe"));
		String mainWindow = getDriver().getWindowHandle();
		boolean flag;
		try {
			getDriver().switchTo().frame(iFrameLocator);
			getDriver().findElement(By.xpath("//div[@id='textDriverRespnsve']")).isDisplayed();
			flag = true;
			generateInfoReport(pos + " is a text driver Ad");
		} catch (NoSuchElementException e) {
			flag = false;
			generateInfoReport(pos + " is a media Ad");
		}
		getDriver().switchTo().window(mainWindow);
		return flag;
	}

	/*
	 * This method will validate ad position on page, check whether blank ad, if
	 * yes, validate size check whether text ad, if yes, check pin lines and no
	 * advertisement label check whether media ad, if yes, no pin lines and
	 * advertisement label
	 * 
	 * Possible return values ################### blankAd, mediaAd, textAd
	 */
	public String validateAdPosOnPage(WebElement pos) {
		String result = null;
		String position = pos.getAttribute("id").substring(7);
		int height = 0;
		int adPosLength;

		if (breakPoint.equals("1"))
			adPosLength = 4;
		else
			adPosLength = 3;

		boolean isTextDriver = checkWetherTextAd(StringUtils.substring(pos.getAttribute("id"), 8, 8 + adPosLength));

		try {
			scrollToWebElement(pos);
			if (pos.getAttribute("class").contains("blank-ad")
					|| pos.getAttribute("style").contains("display: none;")) {
				result = "blankAd";
				try {
					height = getSizeOfAdPosition(pos);
					Assert.assertTrue(height < 20);
					generatePassReportWithNoScreenShot("Blank ad loaded properly for ad position: " + position);
				} catch (AssertionError e) {
					generateFailReport(
							position + "Supposed to be blank-ad. But it shown on page, Height of the Ad is" + height);
				}
			} else if (!isTextDriver) {// condition for media ad
				result = "mediaAd";
				// Validating advertisement label
				try {
					Assert.assertTrue(validateAdvertisementLabel(pos));
					generatePassReportWithNoScreenShot(
							"Advertisement label shown at bottom middle of the ad position: " + position);
				} catch (AssertionError e) {
					generateInfoReport("Advertisement label not shown at the middle of the page");
				}
				String physicalPositionOfAdOnPage = validatePhysicalPositionOfAdOnPage(pos);
				// Validating physical position
				try {
					Assert.assertTrue(physicalPositionOfAdOnPage.equals("center"));
					generatePassReportWithNoScreenShot(position + " Ad loaded to the center of the page");
				} catch (AssertionError e) {
					generateFailReport(
							"Expected: Ad should load center of the page. But it loaded " + physicalPositionOfAdOnPage);
				}
				// Validating No pin lines shown
				try {
					Assert.assertFalse(checkPinLinesDisplay(pos));
					generatePassReportWithNoScreenShot("No Pin lines shown for media ad");
				} catch (AssertionError e) {
					generateFailReport("Pin lines shown for media ad");
				}
				// Validating size
				try {
					height = getSizeOfAdPosition(pos);
					Assert.assertTrue(height > 30);
					generatePassReportWithNoScreenShot("Ad loaded with height: " + height);
				} catch (AssertionError e) {
					generateFailReport("Media Ad loaded with Height: " + height);
				}

			} else {// condition for text ad
				result = "textAd";
				// Validating advertisement label
				try {
					Assert.assertFalse(validateAdvertisementLabel(pos));
					generatePassReportWithNoScreenShot(
							"Advertisement label not shown at bottom middle of the ad position: " + position);
				} catch (AssertionError e) {
					generateInfoReport("Advertisement label shown for the position for text Ad");
				}
				String physicalPositionOfAdOnPage = validatePhysicalPositionOfAdOnPage(pos);
				// Validating physical position
				try {
					Assert.assertTrue(physicalPositionOfAdOnPage.equals("left"));
					generatePassReportWithNoScreenShot(
							position + " Ad loaded to the left of the page as it is text ad");
				} catch (AssertionError e) {
					generateFailReport(
							"Expected: Ad should load left of the page. But it loaded " + physicalPositionOfAdOnPage);
				}
				// Validating No pin lines shown
				try {
					Assert.assertTrue(checkPinLinesDisplay(pos));
					generatePassReportWithNoScreenShot("Pin lines shown for text ad");
				} catch (AssertionError e) {
					generateFailReport("Pin lines shown for text ad");
				}
				// Validating size
				try {
					height = getSizeOfAdPosition(pos);
					Assert.assertTrue(height > 30);
					generatePassReportWithNoScreenShot("Ad loaded with height: " + height);
				} catch (AssertionError e) {
					generateFailReport("Text Ad loaded with Height: " + height);
				}
			}
		} catch (Exception e) {
			generateInfoReport("Error while validating " + position);
		}
		return result;
	}

	/**
	 * Created on 03/12/2019, version : 1.0
	 * 
	 * Verify that the applicable sizes for specific position if there any extra
	 * not applicable.
	 * 
	 * @param pos
	 *            - expected position
	 * @param sizes
	 *            - expected sizes for given position
	 */
	public void notApplicableSizesValidation(String pos, String[] sizes) {
		String appSizes = getSizesForSpecificPositionFromAdCall(pos);
		String[] availableSizes = appSizes.split("\\|");
		if (availableSizes.length == sizes.length) {
			generatePassReportWithNoScreenShot(
					"Applicable sizes count and expected sizes count is same for " + pos + ".");
			for (String asize : availableSizes) {
				boolean available = false;
				for (String esize : sizes) {
					if (asize.equals(esize)) {
						available = true;
						break;
					}
				}
				if (available) {
					generatePassReportWithNoScreenShot(
							"Available size-" + asize + " is expected in the list for pos " + pos);
				} else {
					generateFailReport("Available size-" + asize + " is not expected size in the list for pos " + pos);
				}
			}
		} else {
			generateFailReport("Available sizes count and expected sizes count is not same for " + pos
					+ ", Available sizes count is " + availableSizes.length + ", expected sizes count is "
					+ sizes.length);
		}
	}

	/**
	 * It will return the number of ad call tracked for particular session.
	 * 
	 * @return
	 */
	public boolean numberOfAdCallsValidation() {
		return numberOfAdCallsValidation(false);
	}

	/**
	 * It will return the number of ad call tracked for particular session.
	 * 
	 * @return
	 */
	public boolean numberOfAdCallsValidation(boolean noOFAdCallIssueValidation) {
		boolean adFlag = false;
		int ncalls = getNumberOfCallsTrackedInNetwrok(AdsConstantns.AD_CALL);

		if (ncalls > 1) {
			generateFailReport(ncalls + " ads? calls are found in the page loaded session.");
			adFlag = true;
		} else {
			if (ncalls == 1) {
				generatePassReportWithNoScreenShot(
						"Only one " + AdsConstantns.AD_CALL + " calls are tracked on page load");
				adFlag = true;
			}
		}
		if (noOFAdCallIssueValidation) {
			generateInfoReport("Checking if there are any mutilple unviewbale ad calls when scrolled down & up");
			JavascriptExecutor jsExecutor = (JavascriptExecutor) getDriver();
			jsExecutor.executeScript("window.scrollBy(0,300)");
			scrollBottomToTop();
			int adCalls = getNumberOfCallsTrackedInNetwrok(AdsConstantns.AD_CALL);
			generateInfoReport("Number of ads loaded: " + adCalls);
			if (adCalls > 3) {
				generateFailReport("More number of ad calls recorded: " + adCalls);
			}
		}
		return adFlag;
	}

	/**
	 * it will find the duplicate sizes in the applicable sizes list
	 * 
	 * @param sizes
	 *            - takes the sizes
	 */
	public void findDuplicateSizes(String sizes) {
		String[] aSizes = sizes.split("\\|");
		for (int i = 0; i < aSizes.length; i++) {
			for (int k = i + 1; k < aSizes.length; k++) {
				if (aSizes[i].equals(aSizes[k])) {
					generateFailReport(aSizes[i] + " has been duplcate in the applicable sizes list");
				}
			}
		}
	}

	/*
	 * This is method is to get list of positions id's loaded on page
	 */

	public List<String> getListOfLoadedAdsOnPage() {
		List<String> adPos = new ArrayList<>();
		String position;
		int height;
		int count = 0;
		for (WebElement pos : getDriver().findElements(By.xpath("//div[contains(@id,'ads-')]"))) {

			if (pos.getAttribute("style").contains("display: none;")
					|| pos.getAttribute("class").contains("blank-ad")) {
				generateInfoReport(pos.getAttribute("id") + " is blank ad");
			} else {

				try {
					Actions actions = new Actions(getDriver());
					actions.moveToElement(pos);
					actions.perform();
					position = pos.getAttribute("id");
					height = Integer.parseInt(pos.getCssValue("height").split("p")[0]);
					Assert.assertTrue(height > 20);
					adPos.add(position);
					count++;
					generateInfoReport(position + " displayed on page and added to list");
				} catch (AssertionError e) {
					position = pos.getAttribute("id");
					generateInfoReport(position + " not displayed on page");
				} catch (Exception e) {
					generateInfoReport("Un know error" + e.toString());
				}

			}

		}
		return adPos;
	}

	// This is the method to get ads2_ingore list from current opened page, it
	// will returns the list of positions
	public String[] getAdsToIgnorePositionsFromConsole() {
		String[] positions = null;
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		try {
			String pos = jse.executeScript("return ads2_ignore").toString().replace("{", "").replaceAll("}", "").trim();
			positions = pos.split(",");
			for (int i = 0; i < positions.length; i++) {
				positions[i] = positions[i].replaceAll("=true", "").trim();
			}
		} catch (Exception e) {
			generateInfoReport("Error while getting ads2igore from console");
		}
		generateInfoReport("Got ads2igore from console");
		return positions;
	}

	/*
	 * This is the method to get list of ad positions from prev_scp, input of
	 * this method is String(prev_scp) it will return all the positions from
	 * given prev_scp in form of list
	 */
	public List<String> getPositionsFromPrevScp(String prevScp) {
		List<String> positions = new ArrayList<>();
		try {
			positions = Arrays.asList(StringUtils.substringsBetween(prevScp, "pos=", "&"));
		} catch (Exception e) {
			generateFailReport("Failed to get the desired pos's from given prev_scp");
		}
		return positions;
	}

	/**
	 * Verify whether the page is a login page
	 * 
	 * @return true - if it is a login , false - if it is not a login page
	 */
	public boolean isLoginPage() {
		boolean isLoginPage = false;
		if (getDriver().getTitle().contains("Medscape Deutschland Anmelden")
				|| getDriver().getTitle().contains("Medscape Log In")
				|| getDriver().getTitle().contains("Medscape.com Iniciar Sesión")
				|| getDriver().getTitle().contains(
						"Medscape France - Informations & Ressources médicales pour médecins | Medscape France")
				|| getDriver().getTitle().contains("Medscape.com Entre")) {
			isLoginPage = true;
		}
		return isLoginPage;

	}

	/**
	 * If page goes to 404 then return true, other wise returns false it
	 * considers below titles are page not found; Page Not Found Seite Nicht
	 * Gefunden Página no encontrada Page Introuvable Página não encontrada page
	 * not found
	 * 
	 * @param windowTitle
	 * @return
	 */
	public boolean is404(String windowTitle) {

		if (super.is404(windowTitle)) {
			try {
				WebElement ele = getDriver().findElement(By.xpath("//*[@id='main-message']//span"));
				if (ele.getText().contains("This page isn’t working") && ele.isDisplayed()) {
					return false;
				}
			} catch (NoSuchElementException e) {
				return true;
			}
		}
		return false;
	}

	/*
	 * This method is to login with provided info from test case, if user is
	 * anon, it will delete all cookies
	 */

	public void adsLogin(String login) {
		if (login.contains("anon")) {
			generateInfoReport("User is ANON, clearing the cookies");
			getDriver().manage().deleteAllCookies();
		} else if (login.contains(",")) {
			String user = login.split(",")[0];
			String pwd = login.split(",")[1];
			generateInfoReport("Logging in with " + user);
			login(user, pwd);
		} else {
			login(login, "medscape");
		}
	}

	// Method to get adlabel value from dom, div tag
	public String getAdlabelForPosition(String pos) {
		String adLabel = null;
		String mainWindow = null;
		try {
			String location = "//div[@id='ads-pos-" + pos + "' or @id='ads-af-pos-" + pos + "']";
			generateInfoReport("Getting Ad label for " + pos + " id value is"
					+ getDriver().findElement(By.xpath(location)).getAttribute("id"));
			mainWindow = getDriver().getWindowHandle();
			String iframe = location + "//iframe";

			getDriver().switchTo().frame(getDriver().findElement(By.xpath(iframe)));
			try {
				JavascriptExecutor jse = (JavascriptExecutor) getDriver();
				WebElement element = getDriver()
						.findElement(By.xpath("//script[@type='text/javascript' and contains(text(),'adlabel')]"));
				adLabel = jse.executeScript("return arguments[0].innerHTML;", element).toString();
				generateInfoReport("Ad label for position " + pos + " is " + adLabel);
			} catch (Exception e) {
				generateInfoReport(pos + " has no adlabel value, hence default value will be ADVERTISEMENT");
				adLabel = "";
			}
			getDriver().switchTo().window(mainWindow);
		} catch (NoSuchElementException e) {
			generateInfoReport("Ad position " + pos + " not shown on page");
		}
		return adLabel;
	}

	// method to get src of advertisement label
	public String getLabelSrcFromAdPosition(String pos) {
		String adPos = "//div[@id='ads-pos-" + pos + "' or @id='ads-af-pos-" + pos + "']";
		String src = null;
		try {
			generateInfoReport("Getting SRC of Ad label for pos " + pos + " id value is "
					+ getDriver().findElement(By.xpath(adPos)).getAttribute("id"));

			src = getDriver().findElement(By.xpath(adPos)).getCssValue("background-image");
		} catch (NoSuchElementException e) {
			generateInfoReport(pos + " position not loaded on page");
		}

		return src;
	}

	// Method to provide expected src of advertisement label based on the input
	// provided
	public String expectedAdvertisementLabelSRC(String adLabelFromDom) {
		String label = null;

		try {

			adLabelFromDom = StringUtils.substringBetween(adLabelFromDom, "adlabel = \"", "\"").toLowerCase();
		} catch (NullPointerException e) {
			adLabelFromDom = "";
		}

		switch (adLabelFromDom) {
		case "":
			if (getDriver().getCurrentUrl().contains("portugues"))
				label = "/pt/pi/global/text/text_advertisement_top_pt.png";
			else if (getDriver().getCurrentUrl().contains("francais"))
				label = "/fr/pi/global/text/text_advertisement_top_fr.gif";
			else if (getDriver().getCurrentUrl().contains("deutsch"))
				label = "/de/pi/global/text/text_advertisement_top.gif";
			else if (getDriver().getCurrentUrl().contains("espanol"))
				label = "/es/pi/global/text/text_advertisement_top_es.png";
			else
				label = "pi/global/text/text_advertisement_top.gif";
			break;
		case "ifi":
			label = "pi/global/text/txt-ifi-top.gif";
			break;
		case "ifg":
			label = "pi/global/text/txt-ifg-top.gif";
			break;
		case "blank":
			label = "none";
			break;
		default:
			label = "pi/global/text/text_advertisement_top.gif";
		}

		return label;
	}

	/**
	 * Verifies the adlabel based on pos
	 * 
	 * @param pos
	 */
	public void adLabel(String pos) {
		String actual;
		String expected;
		boolean b = false;
		actual = getLabelSrcFromAdPosition(pos);
		expected = expectedAdvertisementLabelSRC(getAdlabelForPosition(pos));
		// Currently no ad label will be applied for 420, 520
		if (pos.contains("420") || pos.contains("520"))
			expected = "none";
		if (actual != null && expected != null)
			b = actual.contains(expected);
		generateReport(b, "Adlabel shown properly for position " + pos,
				"Expected : " + expected + " and Actual : " + actual);
	}

	/*
	 * Method to click on next button will return true of clicked on it, other
	 * wise false
	 */
	public boolean clickNextButton() {
		List<WebElement> elements = // getDriver().findElements(By.cssSelector(".next_btn
									// a span"));
				getDriver().findElements(By.xpath(
						"//div[contains(@id,'next')]/a|//div[contains(@class,'next')]/a/span|//span[@class='toastArrow swipeRight']|//td[@class='next_slide']|//a[@role='button' and contains(text(),'Next')]"));
		for (WebElement ele : elements) {
			try {
				generateInfoReport(ele.getText());
				ele.isDisplayed();
				Actions actions = new Actions(getDriver());
				actions.moveToElement(ele).click();
				actions.perform();
				generateInfoReport("Clicked on next button");
				return true;
			} catch (NoSuchElementException e) {
				generateInfoReport("Exception while click on Next");
			} catch (WebDriverException e) {
				generateInfoReport("Next button not clickable");
			} catch (Exception e) {
				generateInfoReport("Next is not displayed");
			}
		}
		return false;
	}

	/*
	 * This is the method to validate whether media net refresh happen or not it
	 * will return true if refresh happen, false otherwise Call the method after
	 * loading the page/Required event done
	 */

	public boolean isMediaNetRefreshHappened() {
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		int count = 0;
		int currentScrollHeight;

		List<WebElement> adPositions = getDriver().findElements(By.xpath("//div[contains(@id,'ads-')]"));
		for (WebElement pos : adPositions) {
			try {
				if (getSizeOfAdPosition(pos) > 20) {
					generateInfoReport("Validating media net for position: " + pos.getAttribute("id"));

					currentScrollHeight = Integer.parseInt(jse.executeScript("return window.pageYOffset").toString());
					while (count < 7) {
						getServer().newHar();
						count++;
						currentScrollHeight += 100;
						jse.executeScript("window.scrollBy(0, " + currentScrollHeight + ")");
						Thread.sleep(5000);
						currentScrollHeight -= 200;
						jse.executeScript("window.scrollBy(0, " + currentScrollHeight + ")");
						Thread.sleep(5000);
						if (verifySpecificCallPresence(AdsConstantns.AD_CALL))
							return true;
						generateInfoReport("No media net refresh observed " + count);
					}
					generateInfoReport("No Media net refresh observed even after waiting for 70 Sec");
					return false;
				}
			} catch (Exception e) {
				generateInfoReport("Exception while checking " + pos.getAttribute("id"));
			}
		}
		generateInfoReport("No Ad is physically loaded on page");
		try {
			scrollBottomToTop();
			currentScrollHeight = Integer.parseInt(jse.executeScript("return window.pageYOffset").toString());
			while (count < 7) {
				getServer().newHar();
				count++;
				currentScrollHeight += 300;
				jse.executeScript("window.scrollBy(0, " + currentScrollHeight + ")");
				Thread.sleep(5000);
				currentScrollHeight -= 200;
				jse.executeScript("window.scrollBy(0, " + currentScrollHeight + ")");
				Thread.sleep(5000);
				if (verifySpecificCallPresence(AdsConstantns.AD_CALL))
					return true;
				generateInfoReport("No media net refresh observed " + count);
			}

		} catch (Exception e) {
			generateInfoReport("Exception " + e.toString());
		}
		return false;
	}

	/**
	 * Method will be useful to get the console variable values which can return
	 * the boolean types
	 * 
	 * @param varType
	 *            - Variable (_isArticle, isEmd..etc)
	 * @return - true / false
	 */
	public boolean isTrue(String varType) {
		boolean varTypeValue = false;
		String value = getConsoleValue(varType);
		if (!value.isEmpty()) {
			varTypeValue = Boolean.parseBoolean(value);
		} else {
			generateInfoReport(varType + " has not been defined in " + getDriver().getCurrentUrl());
		}
		return varTypeValue;
	}

	/**
	 * Method will be useful to get the console variable values which can return
	 * the actual value
	 * 
	 * @param varType
	 *            - Variable (_isArticle, isEmd..etc)
	 * @return - string
	 */
	public String getConsoleValue(String varType) {
		Object b = null;
		String varTypeValue = "";
		try {
			b = ((JavascriptExecutor) getDriver()).executeScript("return " + varType);
		} catch (org.openqa.selenium.WebDriverException e) {
			b = null;
		}
		if (b != null) {
			varTypeValue = b.toString();
		} else {
			generateInfoReport(varType + " has not been defined in " + getDriver().getCurrentUrl());
		}
		return varTypeValue;
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
	 */
	public void posAndSize(String expectedSize, String unExpSize, String expectedPos, String unExpectedPos) {
		String prevScp = getSpecificKeyFromSecurePubadCall("prev_scp");
		String prevIUSzs = getSpecificKeyFromSecurePubadCall("prev_iu_szs");
		StringTokenizer aPrevScp = new StringTokenizer(prevScp, "|");
		StringTokenizer aPrevIUSzs = new StringTokenizer(prevIUSzs, ",");

		if (aPrevScp.countTokens() == aPrevIUSzs.countTokens()) {

			while (aPrevScp.hasMoreTokens()) {
				String position = "";
				String sizes = "";
				position = aPrevScp.nextToken();
				sizes = aPrevIUSzs.nextToken();
				generateInfoReport(position);
				generateInfoReport(sizes);
				if (!expectedPos.isEmpty() && position.contains(expectedPos))
					validatePositionAndSize(position, sizes, expectedPos, expectedSize, unExpSize);
				if (!unExpectedPos.isEmpty() && position.contains(unExpectedPos))
					generateFailReport(unExpectedPos + " has been found in ad call in " + getDriver().getCurrentUrl());
			}
		} else {
			generateFailReport("prev_scp and prev_iu_szs counts are miss match, prev_scp count is "
					+ aPrevScp.countTokens() + ", prev_iu_szs count is " + aPrevIUSzs.countTokens());
		}
	}

	/**
	 * Method will return whether the page is responsive or not
	 * 
	 * @return true /false
	 */
	public boolean isNoneResponsivePage() {
		boolean isRD = false;
		if (numberOfAdCallsValidation()) {
			String custParams = getSpecificKeyFromSecurePubadCall("cust_params");
			if (custParams != null) {
				if (!custParams.isEmpty()) {
					if (custParams.contains("rd=1"))
						isRD = true;
				} else {
					generateFailReport("cust_params value is found empty, cust_params value is " + custParams);
				}
			} else {
				generateFailReport("cust_params value is not found in Ad call.");
			}
		} else {
			generateInfoReport("Ad call is not found in " + getDriver().getCurrentUrl());
		}
		return isRD;
	}

	/**
	 * Method will verify whether expected pos is available in ads2Ignore list
	 * 
	 * @param pos
	 * @return true / false
	 */
	public boolean isPosInAds2IgnoreList(String pos) {
		boolean isPos = false;
		String[] ads2Ignore = getAdsToIgnorePositionsFromConsole();
		generateInfoReport("ads2_ignore list : " + Arrays.toString(ads2Ignore));
		for (String p : ads2Ignore) {
			if (p.contains(pos)) {
				isPos = true;
				break;
			}
		}
		return isPos;
	}

	/*
	 * Scrolls down the page till next lazy loaded ad call is triggered.
	 *
	 * @return true - when the lazy-loaded ad call is triggered false - when
	 * lazy-loaded ad call is not triggered
	 */

	public boolean scrollTillNextLazyLoadFound() {
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		boolean isLazyLoadRecorded = false;
		Long count = 0L;
		Long height;
		height = (Long) jse.executeScript("return document.body.scrollHeight")
				- (Long) jse.executeScript("return scrollY");
		getServer().newHar();
		generateInfoReport("Scrolling down page till next lazy load call is recorded");
		try {
			count = height / 100;
			for (int i = 0; i < count; i++) {
				jse.executeScript("window.scrollBy(0,100)");
				Thread.sleep(300);
				if (verifySpecificCallPresence(AdsConstantns.AD_CALL)) {
					isLazyLoadRecorded = true;
					break;
				}
			}
		} catch (Exception e) {
			return isLazyLoadRecorded;
		}
		if (!isLazyLoadRecorded) {
			generateInfoReport("Lazy Load Ad call not recorded for url: " + getDriver().getCurrentUrl());

		}
		return isLazyLoadRecorded;
	}

	private boolean noAdCall(String pagType) {
		boolean adCall = true;
		switch (pagType) {
		case "No_Ad_Page":
		case "RegistrationPage":
		case "ORGTownhall":
		case "DE_LoginPage":
		case "ES_LoginPage":
		case "FR_LoginPage":
		case "PO_LoginPage":
		case "ORGArticle":
		case "DE_ORGArticle":
		case "ES_ORGArticle":
		case "FR_ORGArticle":
		case "PO_ORGArticle":
		case "ProfilePage":
		case "SettingsPage":
		case "FAQPage":
		case "PostDetailsPage":
		case "ORGArticlePage":
		case "ODP_HomePage":
		case "ODP_ArticlePage":
			adCall = false;
			break;
		}
		return adCall;
	}

	/**
	 * Method will be return whether the Ad call should fired or not
	 * 
	 * @param pagType
	 *            - Type of page
	 * @return - return false/ true
	 */
	public boolean isAdCallExpectedInDesktop(String pagType) {
		boolean adCall = true;
		switch (pagType) {
		case "ORGClinicalAdvances":
		case "ORGPersonalisedLearning":
		case "ORGLearningCenter":
			adCall = false;
			break;
		default:
			adCall = noAdCall(pagType);
			break;
		}
		return adCall;
	}

	/**
	 * Method will be return whether the Ad call should fired or not
	 * 
	 * @param pagType
	 *            - Type of page
	 * @return - return false/ true
	 */
	public boolean isAdCallExpectedInMobile(String pagType) {
		boolean adCall = true;
		switch (pagType) {
		case "LoginPage":
			adCall = false;
			break;
		default:
			adCall = noAdCall(pagType);
			break;
		}
		return adCall;
	}

	/**
	 * Logout
	 * 
	 * @param user
	 * @return
	 */
	public boolean logout(String user) {
		getURL("https://login." + env.replace("staging.", "")
				+ "medscape.com/login/sso/logout?RememberMe=No&logoutbtn=Confirm+Log+Out");
		try {
			getCookie.remove(user);
		} catch (Exception e) {
		}
		return true;
	}

	/**
	 * get the lazy load positions
	 * 
	 * @param locator
	 * @return
	 */
	public List<WebElement> getlazyLoadedPos(String locator) {
		List<WebElement> posList = null;
		try {
			posList = getDriver().findElements(By.xpath(locator));
		} catch (NoSuchElementException e) {
		}

		return posList;
	}

	/**
	 * Getting the Page pos and szs details from input data
	 * 
	 * @param row
	 * @param pageRow
	 * @return
	 */
	private HashMap<String, ArrayList<String>> getPageValues(int row, int pageRow, Sheet sh) {

		ArrayList<String> pageTypeList = new ArrayList<>();
		ArrayList<String> deviceTypeList = new ArrayList<>();
		ArrayList<String> eventTypeList = new ArrayList<>();
		ArrayList<String> posList = new ArrayList<>();
		ArrayList<String> sizesList = new ArrayList<>();
		ArrayList<String> iuParts = new ArrayList<>();
		HashMap<String, ArrayList<String>> details = new HashMap<>();

		for (int p = row; p <= pageRow; p++) {
			if (p == row)
				pageTypeList.add(sh.getCell(1, p).getContents().trim());

			if (breakPoint.equalsIgnoreCase("4") && sh.getCell(2, p).getContents().trim().contains("Desktop")) {
				deviceTypeList.add(sh.getCell(2, p).getContents().trim());
				eventTypeList.add(sh.getCell(3, p).getContents().trim());
				posList.add(sh.getCell(4, p).getContents().trim());
				sizesList.add(sh.getCell(5, p).getContents().trim());
				if (!sh.getCell(6, p).getContents().trim().isEmpty())
					iuParts.add(sh.getCell(6, p).getContents().trim());
			}
			if (breakPoint.equalsIgnoreCase("1") && sh.getCell(2, p).getContents().trim().contains("MobileWeb")) {
				deviceTypeList.add(sh.getCell(2, p).getContents().trim());
				eventTypeList.add(sh.getCell(3, p).getContents().trim());
				posList.add(sh.getCell(4, p).getContents().trim());
				sizesList.add(sh.getCell(5, p).getContents().trim());
				if (!sh.getCell(6, p).getContents().trim().isEmpty())
					iuParts.add(sh.getCell(6, p).getContents().trim());
			}
		}
		details.put("PageType", pageTypeList);
		details.put("Device", deviceTypeList);
		details.put("EventType", eventTypeList);
		details.put("Position", posList);
		details.put("Sizes", sizesList);
		details.put("IUParts", iuParts);
		return details;
	}

	/**
	 * Data row range
	 * 
	 * @param startRow
	 * @return
	 */
	private int getRowRange(int startRow, Sheet sh) {
		int k = startRow + 1;
		for (; k < (startRow + 100); k++) {
			try {
				if (sh != null) {
					if (sh.getCell(0, k).getContents().trim().contains("medscape")
							|| sh.getCell(2, k).getContents().trim().isEmpty()) {
						break;
					}
				} else {
					generateFailReport("Sheet reference is null.");
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}
		}
		return k - 1;
	}

	/**
	 * get the sheet object
	 * 
	 * @param fileName
	 * @param sheetName
	 * @return
	 */
	private Sheet getSheet(String fileName, String sheetName) {
		Workbook wb = null;
		try {
			// "AdsSanity.xls"
			File f = new File(ReadProperties.projectLocation + fileName);
			wb = Workbook.getWorkbook(f);
		} catch (Exception e) {
			generateFailReport("Encountered an error while opening a AdsSanity.xls file.");
		}
		// TO get the access to the sheet
		// POS_SZS , Debug
		Sheet sh = null;
		if (wb != null)
			sh = wb.getSheet(sheetName);
		return sh;
	}

	/**
	 * Method will return the test data
	 * 
	 * @param fileName
	 *            - file name
	 * @param sheetName
	 *            - sheet name
	 * @return - Map object
	 */
	public HashMap<String, HashMap<String, ArrayList<String>>> getTestData(String fileName, String sheetName) {
		HashMap<String, HashMap<String, ArrayList<String>>> pageDetails = new HashMap<>();
		// To get the number of rows present in sheet
		Sheet sh = getSheet(fileName, sheetName);
		int totalNoOfRows = sh.getRows();
		// To get the number of columns present in sheet
		String url = "";
		for (int i = 1; i < totalNoOfRows; i++) {
			int pageRow = getRowRange(i, sh);
			if (!sh.getCell(0, i).getContents().trim().isEmpty()) {
				if (!env.isEmpty() && !env.equalsIgnoreCase("PROD")) {
					url = sh.getCell(0, i).getContents().trim();
					url = url.replace("medscape", env + "medscape");
				}
				pageDetails.put(url, getPageValues(i, pageRow, sh));

			}
			i = pageRow;
		}
		return pageDetails;
	}

	/**
	 * Method will return the list of urls
	 * 
	 * @param fileName
	 * @param sheetName
	 * @return
	 */
	public String[] getURLs(String fileName, String sheetName) {
		ArrayList<String> urls = new ArrayList<>();
		Sheet sh = getSheet(fileName, sheetName);
		int totalNoOfRows = 0;
		if (sh != null) {
			totalNoOfRows = sh.getRows();
		}
		// To get the number of columns present in sheet
		for (int i = 1; i < totalNoOfRows; i++) {
			int pageRow = getRowRange(i, sh);
			if (!sh.getCell(0, i).getContents().trim().isEmpty())
				urls.add(sh.getCell(0, i).getContents().trim());
			i = pageRow;
		}
		return urls.toArray(new String[urls.size()]);
	}

	/**
	 * wait for till ad call fired
	 */
	public void waitForAdCallFound() {
		int c = 0;
		while (!numberOfAdCallsValidation()) {
			waitForPageLoad(10);
			c++;
			if (c == 50)
				break;
		}
	}

	/**
	 * Verify the lazy loaded Ad call
	 */
	public void verifyNoLazyLoadedAdCall() {
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		int height = getDriver().manage().window().getSize().getHeight();
		int scroll = height / 50;
		int s = scroll;
		int max = 200;
		WebElement footer = null;
		boolean isAdCallFnd = false;
		try {
			footer = getDriver().findElement(By.xpath("//div[contains(@class,'footer_legal-text resp-container')]"));
		} catch (NoSuchElementException e) {
			max = 100;
		}
		for (int i = 0; i < max; i++) {
			getServer().newHar();
			StaticWait(2);
			s = s + scroll;
			jse.executeScript("window.scrollBy(0," + s + ")");
			if (isVisibleInViewport(footer)) {
				break;
			}
			if (numberOfAdCallsValidation()) {
				isAdCallFnd = true;
			}
		}
		generateReport(!isAdCallFnd, "Lazy Loaded Ad call has not been found.", "Lazy Loaded Ad call has been found.");
	}

	/**
	 * 
	 * @param esizes
	 * @param expectedPos
	 * @param unExpSize
	 * @param unExpectedPos
	 */
	public void verifyApplicableSizes(String[] esizes, String expectedPos, String unExpSize, String unExpectedPos) {
		for (String size : esizes) {
			posAndSize(size, unExpSize, expectedPos, unExpectedPos);
		}

		// Verifying the duplicate sizes
		String sizes = getSizesForSpecificPositionFromAdCall(expectedPos);
		if (sizes.isEmpty()) {
			generateFailReport(expectedPos + " position is not available in Ad call.");
		} else {
			findDuplicateSizes(sizes);
		}
		/*
		 * verification of extra sizes which are available in the list
		 */
		notApplicableSizesValidation(expectedPos, esizes);
	}

	/**
	 * All the Ad pos's and their sizes will be verified
	 * 
	 * @param adPosSzs
	 *            - key - Position value , value - Array list - Sizes list
	 * @param optionalPos
	 *            - null - if there are no optional pos in the list, String[] -
	 *            if there are any optional list
	 * @return - ArrayList<String> - available position values on the page
	 */
	public ArrayList<String> verifySpecifiedAllAdPosAndSzs(HashMap<String, ArrayList<String>> adPosSzs,
			String[] optionalPos) {
		ArrayList<String> availablePos = new ArrayList<>();
		List<String> aPos = getPositionsFromPrevScp(
				getSpecificKeyFromSecurePubadCall(QueryStringParamENUM.PREV_SCP.toString()));
		int optional = 0;
		if (optionalPos != null)
			optional = optionalPos.length;

		if (optional > 0) {
			generateReport(aPos.size() <= adPosSzs.size(),
					"Expected Ad pos " + adPosSzs.size() + " and Actual Ad pos " + aPos.size() + " are different.",
					"Expected Ad pos " + adPosSzs.size() + " and Actual Ad pos " + aPos.size() + " are different.");

		} else {
			generateReport(aPos.size() == adPosSzs.size(),
					"Expected Ad pos " + adPosSzs.size() + " and Actual Ad pos " + aPos.size() + " are different.",
					"Expected Ad pos " + adPosSzs.size() + " and Actual Ad pos " + aPos.size() + " are different.");
		}

		for (String pos : adPosSzs.keySet()) {
			if (aPos.contains(pos)) {
				generatePassReportWithNoScreenShot("Pos " + pos + " is appear in Ad call.");
				Object[] objArr = adPosSzs.get(pos).toArray();
				String[] str = Arrays.copyOf(objArr, objArr.length, String[].class);
				verifyApplicableSizes(str, pos, "", "");
				availablePos.add(pos);
			} else {
				if (optional == 0) {
					generateFailReport(pos + " is not appear in Ad call.");
				} else {
					if (Arrays.asList(optionalPos).contains(pos))
						generateInfoReport(pos + " is not appear in Ad call.");
					else
						generateFailReport(pos + " is not appear in Ad call.");
				}
			}
		}
		return availablePos;
	}

	/*
	 * Method to check if collapser script is served from ad server
	 *
	 * @return true if collapser script is served
	 */
	public boolean isCollapserServed(String pos) {

		boolean collapserServed = false;
		try {
			generateInfoReport("Checking if collpaser is served from ad server to pos " + pos);
			WebElement iFrame = getDriver().findElement(By.xpath("//div[@id='ads-pos-" + pos + "']//iframe"));
			getDriver().switchTo().frame(iFrame);
			getDriver().findElement(
					By.xpath("//script[contains(@src,'//img.medscapestatic.com/pi/scripts/ads/dfp/collapse-ad.js')]"));
			generateInfoReport("Collapser loaded, hence Ad shouldn't show in front end");
			collapserServed = true;
		} catch (NoSuchElementException e) {
			generateInfoReport("Collapser is not loaded for ad position: " + pos);
		} catch (Exception e) {
			generateInfoReport(e.getMessage());
		}
		getDriver().switchTo().parentFrame();
		return collapserServed;
	}

	/**
	 * Converting RGBA color code to Hex Color code
	 * 
	 * @param colorRGBACode
	 * @return
	 */
	public String getRGBAColorCodeInHexCode(String colorRGBACode) {
		String rgb = "";
		String rgba = "";
		String hex = "";
		if (colorRGBACode.contains("rgba"))
			rgba = StringUtils.substringBetween(colorRGBACode, "rgba(", ")").trim();
		else
			rgb = StringUtils.substringBetween(colorRGBACode, "rgb(", ")").trim();
		String[] rgbA = null;
		if (!rgba.isEmpty())
			rgbA = rgba.split(",");
		if (!rgb.isEmpty())
			rgbA = rgb.split(",");
		int r = 0;
		int g = 0;
		int b = 0;
		if (rgbA != null && rgbA.length >= 3) {
			r = Integer.parseInt(rgbA[0].trim());
			g = Integer.parseInt(rgbA[1].trim());
			b = Integer.parseInt(rgbA[2].trim());
		}
		int a = 0;
		if (!rgba.isEmpty() && rgbA != null)
			a = Integer.parseInt(rgbA[3].trim());
		Color c = null;
		if (!rgba.isEmpty())
			c = new Color(r, g, b, a);
		if (!rgb.isEmpty())
			c = new Color(r, g, b);
		if (c != null)
			hex = "#" + Integer.toHexString(c.getRGB()).replace("f", "");
		return hex;
	}

	/**
	 * Get inner script which is related to config data
	 * 
	 * @return - config data
	 */
	public String getCreativeConfigValues(String iframXapth, String innerScriptPath) {
		String htmlCode = "";
		try {
			// Switching to creative iframe
			switchToFrame(By.xpath(iframXapth), "Switching to Ad pos iframe");

			WebElement script = getDriver().findElement(By.xpath(innerScriptPath));

			htmlCode = (String) ((JavascriptExecutor) getDriver()).executeScript("return arguments[0].innerHTML;",
					script);
			generateInfoReport("Config values == > " + htmlCode);
			if (!htmlCode.contains("config")) {
				htmlCode = "";
			} else {
				htmlCode = StringUtils.substringBetween(htmlCode, "config = {", "}");
				generateInfoReport("Config Data : " + htmlCode);
			}
			// come out from iframe
			getDriver().switchTo().defaultContent();
		} catch (Exception e) {
			getDriver().switchTo().defaultContent();
		}
		return htmlCode;
	}

}