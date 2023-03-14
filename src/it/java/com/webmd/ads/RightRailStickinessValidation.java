package com.webmd.ads;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;

public class RightRailStickinessValidation extends AdsCommon {

	By rightContainer = By.xpath("//div[@id='adcontainer_column-right']");
	String cssProperty = "position";
	String rightRailProperty = null;

	@Test(dataProvider = "dataProvider")
	public void stickyBehaviourValidation(String url) {

		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD")) {
			url = url.replace("medscape", env + "medscape");
		}
		getDriver();
		login();
		getDriver().get(url);
		validaterRightRailStickiness();
	}

	private void validaterRightRailStickiness() {

		scrollBottomToTop();

		try {
			getDriver().findElement(rightContainer).isDisplayed();

			try {
				rightRailProperty = getDriver().findElement(rightContainer).getCssValue(cssProperty);
				// validating property is static or not in the initial page load
				Assert.assertEquals(rightRailProperty.trim(), "static");
				generatePassReport(
						"The Right rail Property is " + rightRailProperty + " and expected is static on page load");
				generateInfoReport("Scrolling down the page to check the stickyness of the container");
				int count = 0;
				String headerboxHeight = getConsoleValue("$('#headerbox').outerHeight()");
				scrollByPixel(headerboxHeight);
				String height101 = getConsoleValue("$('#ads-pos-101').outerHeight()");
				scrollByPixel(height101);
				scrollByPixel("180");
				try {
					rightRailProperty = getDriver().findElement(rightContainer).getCssValue(cssProperty);
					Assert.assertEquals(rightRailProperty.trim(), "fixed");
					generatePassReport(
							"The Right rail Property is " + rightRailProperty + " and expected is fixed upon scroll");
				} catch (AssertionError e) {
					generateFailReport(
							"The right rail is not sticky after scrolled till it touches top of the browser, "
									+ "property Position Value is" + rightRailProperty);
				}
				String style = getDriver().findElement(rightContainer).getAttribute("style");
				while (count < 3) {
					scrollByPixel("100");
					style = getDriver().findElement(rightContainer).getAttribute("style");
					String topPx;
					count++;
					try {
						topPx = StringUtils.substringBetween(style, "top: ", "px;");
						if (!topPx.contains("0"))
							break;
					} catch (Exception e) {
						generateInfoReport("Style is " + style + " " + e.toString());
					}
				}
				try {
					Assert.assertFalse(style.contains("position: static; top: 0px"));
					generatePassReportWithNoScreenShot("Right rail moved after scroll");
				} catch (AssertionError e) {
					generateFailReport("Right rail is still sticky even scrolled 300px");
				}
			} catch (AssertionError e) {
				generateFailReport("Expected property value for position is Static and is " + rightRailProperty);
			}
		} catch (NoSuchElementException e) {
			generateInfoReport("Element no shown on page");
		}
	}

	private void scrollByPixel(String pixel) {
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		jse.executeScript("window.scrollBy(0, " + pixel + ")");
	}

	@DataProvider
	public String[][] dataProvider() {
		return new String[][] { { "https://www.medscape.com/viewarticle/894875" } };
	}
}
