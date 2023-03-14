package com.webmd.ads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import com.webmd.common.AdsCommon;
import com.webmd.common.AdsConstantns;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;

/**
 *
 * @author tnamburi
 * 
 *         This class has the common code to work around Headline ads
 */

public class HeadLineAdsCommon extends AdsCommon {

	public static final String ADPOS = "//li[@id='ads-pos-";

	By headLine = By.xpath("//div[@class='hp']");
	By headLineLabel = By.xpath("//span[@class='headline-ifi-label']");
	By headLineTitle = By.xpath("//div[@class='headline-title']");
	By headLinejobCode = By.xpath("//a[@class='headline-job-code-title']");
	By brandAlertClose = By.xpath("//*[@id=\"ck-editor-header\"]//div[@class='ba-close visible']");
	By brandAlertIFrame = By.xpath("//iframe[@id=\"brandalert\"]");

	List<String> adPlacementsOnPage = new ArrayList<>();

	public String getSizesForSpecificPos(String pos, String prevScp, String prevIuSzs) {
		HashMap<String, String> size = new HashMap<>();

		if (prevScp.contains(pos)) {
			String[] sizes = prevIuSzs.split(",");
			String[] positions = prevScp.split("\\|");
			try {
				Assert.assertEquals(sizes.length, positions.length);
				for (int i = 0; i < positions.length; i++) {
					size.put(positions[i], sizes[i]);
				}

				return size.get(pos);
			} catch (AssertionError e) {
				generateInfoReport("No of positions in prev_scp and prev_iu_szs are not equal");
			}
		}
		return null;
	}

	/*
	 * if ads should load in the ad call pass boolean as true, false otherwise
	 */
	public void verifyAdCallForPositions(boolean flag, List<String> adPlacementsOnPage) {
		generateInfoReport("Validating Ad call");
		Har har = getServer().getHar();
		har.getLog().getBrowser();
		List<HarEntry> res = har.getLog().getEntries();

		for (HarEntry harEntry : res) {
			String url = harEntry.getRequest().getUrl();
			if (url.contains(AdsConstantns.AD_CALL)) {
				String prevScp = null;
				String prerIUSzs = null;
				List<HarNameValuePair> queryParams = harEntry.getRequest().getQueryString();
				for (HarNameValuePair harNameValuePair : queryParams) {
					if (harNameValuePair.getName().equals("prev_scp")) {
						prevScp = harNameValuePair.getValue();
					} else if (harNameValuePair.getName().equals("prev_iu_szs")) {
						prerIUSzs = harNameValuePair.getValue();
					}
				}

				generateInfoReport("Value of PREV_SCP");
				for (String str : adPlacementsOnPage) {
					if (flag) {
						try {
							Assert.assertTrue(prevScp.contains(str));
							generatePassReportWithNoScreenShot("Ad position loaded in page loaded ad call");
							String sizes = getSizesForSpecificPos(str, prevScp, prerIUSzs);
							try {
								Assert.assertTrue(sizes.contains("2x9"));
								Assert.assertTrue(sizes.contains("320x50"));
								generatePassReportWithNoScreenShot("Sizes loaded properly");
							} catch (AssertionError e) {
								generateFailReport("Issue with sizes loaded for the position " + str
										+ " Sizes loaded in ad call are " + sizes);
							}
						} catch (AssertionError e) {
							generateFailReport(
									"Ad position is not loaded in page loaded ad call, prev_scp: " + prevScp);
						}
					} else {
						try {
							Assert.assertFalse(prevScp.contains(str));
							generatePassReportWithNoScreenShot("Ad position not loaded in other than page loaded call");
						} catch (AssertionError e) {
							generateFailReport(
									"Ad position is loaded in other than page loaded ad call, prev_scp: " + prevScp);
						}
					}

				}

			}
			break;
		}
	}

	/**
	 * This method is to validate advertisement label for particular position
	 *
	 * @param ad
	 *            position number
	 */

	public void validateAdvertisementLabel(String pos) {
		WebElement ele;
		String adLabel = null;

		try {
			ele = getDriver().findElement(By.xpath(ADPOS + pos + ")]"));
			generateInfoReport("Getting Ad label for " + pos + " id value is" + ele.getAttribute("id"));
			String adPosClassName = "nothing";
			try {
				adPosClassName = ele.getAttribute("class");
				Assert.assertTrue(adPosClassName.contains("adlabelblank"));
				generatePassReportWithNoScreenShot("Ad label blank loaded as part of class name");

				String adLabelPropertyValue = "nothing";
				try {
					adLabelPropertyValue = ele.getCssValue("background-image");
					Assert.assertEquals(adLabelPropertyValue, "none");
					generatePassReportWithNoScreenShot("Advertisement label not loaded as expected");
				} catch (AssertionError e) {
					generateFailReport("Expected property value is none, but actual is " + adLabelPropertyValue);
				}
			} catch (AssertionError e) {
				generateFailReport("Ad label blank not loaded in ad pos class name, class name is " + adPosClassName);
			}

		} catch (NoSuchElementException e) {
			generateSkipReport("Ad position " + pos + " is not shown on page");
		} catch (AssertionError e) {
		}
	}

	/**
	 * 
	 * @param pos
	 *            : Ad position which is to be tested
	 * @param name
	 *            : name of the property
	 * @param xpath
	 *            : xpath of the field to validate
	 * @param properties
	 *            : List of properites and expected values in format of
	 *            <propertyName,expectedValue>
	 */

	public void validateProperty(String pos, String propName, String xpath, String... properties) {
		generateBoldReport("Validating : " + propName);
		WebElement iFrame = getDriver().findElement(By.xpath(ADPOS + pos + "']//iframe"));
		String window = getDriver().getWindowHandle();
		try {
			getDriver().switchTo().frame(iFrame);
			String font = null;
			String fontSize = null;
			String lineHeight = null;
			String hex = null;
			String marginBottom = null;
			String padding = null;
			try {
				WebElement ele = getDriver().findElement(By.xpath(xpath));

				// getting the values
				for (String property : properties) {
					try {
						String propertyName = property.split("\\|")[0];
						String propertyValue = property.split("\\|")[1];
						switch (propertyName) {
						case "font":
							font = ele.getCssValue("font-family");
							compareTwoStrings(font, propertyValue, "font name");
							break;
						case "fontSize":
							fontSize = ele.getCssValue("font-size");
							compareTwoStrings(fontSize, propertyValue, "font-size");
							break;
						case "hex":
							hex = ele.getCssValue("color");
							compareTwoStrings(hex, propertyValue, "color");
							break;
						case "lineHeight":
							lineHeight = ele.getCssValue("line-height");
							compareTwoStrings(lineHeight, propertyValue, "line-height");
							break;
						case "marginBottom":
							marginBottom = ele.getCssValue("margin-bottom");
							compareTwoStrings(marginBottom, propertyValue, "margin-bottom");
							break;
						case "padding":
							padding = ele.getCssValue("padding");
							compareTwoStrings(padding, propertyValue, "padding");
							break;
						default:
							generateInfoReport("Property value passed wrong: " + property);
						}
					} catch (Exception e) {
						generateInfoReport("Exception while getting the property value for " + property);
					}
				}

			} catch (Exception e) {
				generateFailReport("Exception while validating Headline " + e.toString());
			}
		} catch (NoSuchElementException e) {
			generateSkipReport("iFrame is not shown for pos " + pos);
		}
		getDriver().switchTo().window(window);
	}

	/**
	 * This method is to verify and validate Headline which directs through
	 * micro-site, opens the micro-site within the same tab and which directs
	 * through Brand alert opens the alert within same tab upon clicking
	 *
	 * @param pos:
	 *            The ad pos
	 * @param element:
	 *            The element which has to be clicked (Headline label or title
	 *            or job code)
	 */
	public void validateClickOnHeadline(String pos, By element) {

		generateInfoReport("Verifying if Headline is available on page for position: " + pos);
		try {
			WebElement iFrame = getDriver().findElement(By.xpath(ADPOS + pos + "']//iframe"));
			getDriver().switchTo().frame(iFrame);
			String window = getDriver().getWindowHandle();
			boolean isHeadLinePresent = waitForElement(headLine);
			if (isHeadLinePresent) {
				generateInfoReport("Headline is present on the page: " + getDriver().getCurrentUrl());
				generateInfoReport("Clicking on Headline");
				getDriver().findElement(element).click();
				StaticWait(30);
				getDriver().switchTo().window(window);
				verifyWindowCount();
				if (isBrandAlert()) {
					getDriver().findElement(brandAlertClose).click();
				} else {
					getDriver().navigate().back();
				}
				getDriver().switchTo().defaultContent();

			}
		} catch (Exception e) {
			generateFailReport("Exception while clicking headline, " + e.toString());

		}
	}

	/**
	 * This method is to click on additional link ink custom template and check
	 * whether it is navigating properly or not, after validating it will go
	 * back to main window
	 * 
	 * @param number
	 *            of link to click
	 * @param window:
	 *            main window value
	 * @param URL:
	 *            Test URL
	 */
	private void checkCustomLinkClick(String number, String window, String URL) {

		WebElement link = getDriver().findElement(By.xpath("//span[@class='headline-second-link']/a[" + number + "]"));
		String url = link.getAttribute("href");
		url = StringUtils.substringAfter(url, "&adurl=");
		url = url.replace("http", "https");
		link.click();
		try {
			Assert.assertTrue(getDriver().getWindowHandles().size() == 1);
			generatePassReportWithNoScreenShot("Clicking on link opens same window");
			getDriver().switchTo().window(window);
			String currentURL = "";
			try {
				currentURL = getDriver().getCurrentUrl();
				Assert.assertTrue(currentURL.contains(url));
				generatePassReportWithNoScreenShot("Clicking on link navigating properly");
			} catch (AssertionError e) {
				generateFailReport("Expected URL to navigate is " + url + " But navigated to " + currentURL);
			}
		} catch (AssertionError e) {
			generateFailReport("Clicking on link opens a new window instead of opening in same window");
		}
		getDriver().switchTo().window(window);
		getDriver().get(URL);

	}

	/**
	 * This method is to validate clicks on custom links
	 *
	 * @param pos
	 */
	public void validateClickOnCustomLinks(String pos) {
		generateInfoReport("Validating click on Custom link for position " + pos);
		String url = getDriver().getCurrentUrl();

		int executionCount = 0;
		do {
			executionCount++;
			scrollToObject(By.xpath(ADPOS + pos + "']"), "Ad position " + pos);
			WebElement iFrame = getDriver().findElement(By.xpath(ADPOS + pos + "']//iframe"));
			String window = getDriver().getWindowHandle();

			try {
				getDriver().switchTo().frame(iFrame);
				List<WebElement> links = getDriver().findElements(By.xpath("//span[@class='headline-second-link']/a"));
				int count = links.size();

				if (count == 0) {
					// make sure no blank space
				} else if (count == 1) {
					try {
						Assert.assertFalse(getDriver().findElement(By.xpath("//span[@class='headline-second-link']"))
								.getText().contains("|"));
						generatePassReportWithNoScreenShot("| symbol not shown when there is only one link");
						if (executionCount < 2) {
							checkCustomLinkClick("1", window, url);
						}
					} catch (AssertionError e) {
						generateFailReport("| symbol shown when there is only one link");
					}
				} else if (count == 2) {
					if (executionCount < 2)
						checkCustomLinkClick("1", window, url);
					else
						checkCustomLinkClick("2", window, url);
				} else
					generateFailReport("Custom links count is " + count);
			} catch (NoSuchElementException e) {
				generateInfoReport("No links available in custom template");
			} catch (Exception e) {
				generateInfoReport("Exception while validating custom links" + e.toString());
			}
		} while (executionCount <= 2);
	}

	/**
	 * This method will add the ad positions to the List based on the type of
	 * Page whether Specialty Home Pages or News & Perspective Pages or
	 * Reference Article Pages etc.
	 *
	 * @param isSpeciality:
	 *            true if the page type is Specialty Home Page, false otherwise
	 */
	public void initiateAdPlacementsOnPage(boolean isSpeciality) {

		if (breakPoint.equals("1")) {

			adPlacementsOnPage = isSpeciality ? Arrays.asList("1622", "1722") : Arrays.asList("1622");
		} else {

			adPlacementsOnPage = isSpeciality ? Arrays.asList("622", "722") : Arrays.asList("622");
		}

	}

	/**
	 * This method will validate the properties loaded in the ad such as font,
	 * font-family, font size, color.
	 *
	 * @param actual
	 *            : the actual value of the property
	 * @param expected
	 *            : the expected value of the property
	 * @param type
	 *            : the property type whether font name, font-size, color,
	 *            line-height etc.
	 */
	public void compareTwoStrings(String actual, String expected, String type) {
		generateInfoReport("Validating " + type);
		try {
			Assert.assertEquals(actual.replaceAll("\"", ""), expected);
			generatePassReportWithNoScreenShot(type + " loaded as expected");
		} catch (AssertionError e) {
			generateFailReport("Validating " + type + ", Expected is " + expected + " but actual is " + actual);
		} catch (NullPointerException e) {
			generateSkipReport("Expected value is NULL");
		}
	}

	/**
	 * This method will verify the window count to be 1 upon clicking on
	 * Headline whether on Label, Title, Job Code, Additional Links to make sure
	 * the page is loaded in same window
	 */
	public void verifyWindowCount() {
		int windows = getDriver().getWindowHandles().size();
		if (windows == 1)
			generatePassReportWithNoScreenShot("Upon clicking on Headline, the page is loaded within the same page");
		else
			generateFailReport("URL opened in new window, no'of windows opened are " + windows);
	}

	/**
	 * This method verifies that the ad is loaded in the expected position on
	 * the page
	 *
	 *
	 * @param pos
	 *            : the ad position which is to be looked for placement
	 * @param adPosPath
	 *            : the path of the ad position
	 */
	public void verifyAdPlacementOnPage(String pos, By adPosPath) {
		String id = null;
		try {
			id = getDriver().findElement(adPosPath).getAttribute("id");
			Assert.assertTrue(id.contains(pos));
			generatePassReportWithNoScreenShot(pos + " position loaded in expected place");
		} catch (AssertionError e) {
			generateFailReport(pos + " not displayed as per the requirement, id loaded at position is " + id);
		} catch (NoSuchElementException e) {
			generateFailReport(pos + " not displayed as per the requirement");
		}
	}

	public boolean isBrandAlert() {

		boolean isBrandAlertFound = false;
		try {
			WebElement iFrameBrandAlert = getDriver().findElement(brandAlertIFrame);
			if (iFrameBrandAlert.isDisplayed()) {

				getDriver().switchTo().frame(iFrameBrandAlert);
				isBrandAlertFound = true;
			}

		} catch (Exception e) {
			generateInfoReport("Brand Alert is not found on the page");
			isBrandAlertFound = false;
		}
		return isBrandAlertFound;
	}
}
