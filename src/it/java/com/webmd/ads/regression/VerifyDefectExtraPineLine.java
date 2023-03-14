package com.webmd.ads.regression;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;

/**
 * @author sandeep.gowada
 *
 *         PPE-198506: Extra pin line is getting appended to upper pin line when
 *         we click on sub sections on drug monograph page.
 */
public class VerifyDefectExtraPineLine extends AdsCommon {

	By close = By.xpath("//*[contains(@id,'man-layer')]//span[@class='man-close']");
	By stickyAdClose = By.xpath("//span[@class= 'sticky-ad-close']");
	By drugMenu = By.xpath("//div[@class = 'sections-nav']/ul/li[not(contains(@style,'display: none;'))]/a");
	By div = By.xpath("//div[contains(@id,'ads-pos-421')]//div[contains(@id,'str-inst')]");
	By firstNext = By.xpath("//div[contains(@id,'content_0')]//div[contains(@class,'next_btn_drug')]//a");
	By currentContent = By.xpath("//div[@class='currentContent']//a[contains(text(),'Next')]");
	By next = By.xpath("//a[contains(text(),'Next')]");
	String property = "border-top";
	String value = "0px none rgb(0, 0, 0)";

	@Test(dataProvider = "dataProvider", groups = { "testExtraPineLine" })
	public void verifyNoExtraPineLineClickMenu(String url) {
		setDescription(
				"Verify that only one upper and one bottom pine lines are displayed when clicked on sub sections on drug monograph page.");
		closeAds(url);
		try {
			List<WebElement> drugMenuElements = getDriver().findElements(drugMenu);
			generateInfoReport("Verifying that no extra pine line is appeared when the page is loaded");
			Assert.assertEquals(getDriver().findElement(div).getCssValue(property), value);
			for (WebElement menu : drugMenuElements) {
				if (!menu.getAttribute("class").contains("selected")) {
					generateInfoReport("Clicking on " + menu.getText() + " in the page to verify the pineline");
					menu.click();
					Assert.assertEquals(getDriver().findElement(div).getCssValue(property), value);
					generatePassReport("Extra Pine line is not displayed when clicked on " + menu.getText());
				}
			}

		} catch (Exception e) {
			generateFailReport("Execption occured: " + e.getMessage());
		}

	}

	@Test(dataProvider = "dataProvider", groups = { "testExtraPineLine" })
	public void verifyNoExtraPineLineClickNext(String url) {
		setDescription(
				"Verify that only one upper and one bottom pine lines are displayed when clicked on next page on drug monograph page.");
		closeAds(url);
		generateInfoReport("Verifying that no extra pine line is appeared when the page is loaded");
		int c = 1;
		WebElement ele = null;
		boolean flag = false;
		boolean isFound = false;
		do {
			generateInfoReport("Verifying whether no extra pine line is displayed on the page");
			Assert.assertEquals(getDriver().findElement(div).getCssValue(property), value);
			if (c == 1) {
				ele = getDriver().findElement(firstNext);
				scrollToWebElement(ele);
				generateInfoReport("Clicking on " + ele.getText() + " on the page to verify the pineline");
				ele.click();
				flag = true;
			} else {
				try {
					isFound = waitForElement(currentContent);
					if (isFound && !getDriver().findElement(currentContent).isDisplayed()) {
						break;
					}
				} catch (NoSuchElementException ee) {
					generateFailReport("Exception occured :" + ee.getMessage());
				}
				if (isFound) {
					ele = getDriver().findElement(currentContent);
					scrollToWebElement(ele);
					generateInfoReport("Clicking on " + ele.getText() + " on the page to verify the pineline");
					ele.click();
				} else {
					flag = false;
				}

			}
			c++;
			generatePassReport("Extra Pine line is not displayed when clicked on next button");
		} while (flag);
	}

	private void closeAds(String url) {
		url = url.replace("env.", env);
		getDriver();
		getDriver().manage().deleteAllCookies();
		login("infosession33", "medscape");
		getServer().newHar();
		getDriver().get(url);
		try {
			boolean isClosePresent = waitForElement(close);
			if (isClosePresent) {
				WebElement closeElement = getDriver().findElement(close);
				if (closeElement != null && closeElement.isDisplayed()) {
					closeElement.click();
				}
			}
			boolean isPresent = waitForElement(stickyAdClose);
			if (isPresent) {
				WebElement stickyAdCloseElement = getDriver().findElement(stickyAdClose);

				if (stickyAdCloseElement != null && stickyAdCloseElement.isDisplayed()) {
					stickyAdCloseElement.click();
				}
			}
		} catch (Exception e) {
			generateInfoReport("Exception occured: " + e.getMessage());
		}
	}

	@DataProvider
	public String[][] dataProvider() {
		return new String[][] { { "https://reference.env.medscape.com/drug/jardiance-empagliflozin-999907" } };
	}
}