package com.webmd.ads;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.relevantcodes.extentreports.LogStatus;
import com.webmd.common.AdsCommon;
import com.webmd.general.common.ExtentTestManager;

/*
 * PPE-169640: Fast 5 Quiz - Right Rail Ad Adhesion
 */
public class FastFiveQuizRightRailMedianetrefresh extends AdsCommon {

	By sponseredPoll = By.className("sponsored-poll qna-render rightAd ready sticky show-questions");
	By contentFooter = By.id("footercontents");
	By relatedLinks = By.className("related-links");

	JavascriptExecutor jse;

	private boolean isQuizPollDisplayed() {
		if (getDriver().findElement(sponseredPoll).isDisplayed())
			return true;
		return false;
	}

	@DataProvider
	public String[][] dataProvider() {
		String[][] urls = { { "https://reference.staging.medscape.com/viewarticle/879782_2" },
				/*
				 * {
				 * "https://reference.staging.medscape.com/viewarticle/879782_3"
				 * }, {
				 * "https://reference.staging.medscape.com/viewarticle/879782_4"
				 * }, {
				 * "https://reference.staging.medscape.com/viewarticle/879782_5"
				 * }, {
				 * "https://reference.staging.medscape.com/viewarticle/879782_6"
				 * }
				 */ };

		return urls;
	}

	// PPE-172071: Verify whether right rail ad poll sticky or not while user
	// scroll the page till content footer
	@Test(enabled = true, dataProvider = "dataProvider")
	public void verifyQiuzPollDupAppend(String URL) throws InterruptedException {
		generateInfoReport(
				"Verify whether right rail ad poll sticky or not while user scroll the page till content footer");
		login();
		getDriver().get(URL);
		verifyQiuzPollAppend();

	}

	public boolean isVisibleInViewport(WebElement element) {

		return (Boolean) ((JavascriptExecutor) getDriver()).executeScript(
				"var elem = arguments[0],                 " + "  box = elem.getBoundingClientRect(),    "
						+ "  cx = box.left + box.width / 2,         " + "  cy = box.top + box.height / 2,         "
						+ "  e = document.elementFromPoint(cx, cy); " + "for (; e; e = e.parentElement) {         "
						+ "  if (e === elem)                        " + "    return true;                         "
						+ "}                                        " + "return false;                            ",
				element);
	}

	// PPE-172074: Verify the Right rail ad poll sticky on all browsers and
	// break points except mobile

	@Test(enabled = false, dataProvider = "dataProvider")
	public void verifyAllOtherBreakpoints(String URL) throws InterruptedException {
		generateInfoReport("Verify the Right rail ad poll sticky on all browsers and break points except mobile");
		//getDriver("2");
		login();
		generateInfoReport("Verifying on Breakpoint 2");
		getDriver().get(URL);
		verifyQiuzPollAppend();
		//getDriver("3");
		login();
		generateInfoReport("Verifying on Breakpoint 3");
		getDriver().get(URL);
		verifyQiuzPollAppend();
	}

	// PPE-172075: Verify whether there is no impact on mobile ad poll or not
	// due to sticky ad poll on desktop
	@Test(enabled = false, dataProvider = "dataProvider")
	public void verifyQuizPollStickinessOnMobile(String URL) throws InterruptedException {
		generateInfoReport(
				"Verify whether there is no impact on mobile ad poll or not due to sticky ad poll on desktop");
		//getDriver("1");
		login();
		getDriver().get(URL);
		verifyQiuzPollAppend();
	}

	public void verifyQiuzPollAppend() throws InterruptedException {
		boolean mrf = false;
		List<WebElement> elmts = getDriver().findElements(By.xpath("//div[@id='column-right']//section"));

		if (elmts.size() == 0) {
			ExtentTestManager.getTest().log(LogStatus.FAIL, "No Quiz poll found in the page");
		} else {

			jse = (JavascriptExecutor) getDriver();
			int height = getDriver().manage().window().getSize().getHeight();
			int scroll = height / 50;

			for (int mr = 0; mr <= 50; mr++) {
				getServer().newHar();
				Thread.sleep(10000);
				jse.executeScript("window.scrollBy(0," + scroll + ")");
				Thread.sleep(1000);
				jse.executeScript("window.scrollBy(0," + (-scroll) + ")");

				if (verifySpecificCallPresence("securepubads.g.doubleclick.net/gampad/ads?")) {
					generatePassReport("Media.net refresh call has been found");
					mrf = true;
					break;
				} else {
					generateInfoReport("Media.net refresh call has not been found");
				}
			}
			if (!mrf)
				ExtentTestManager.getTest().log(LogStatus.FAIL, "Media.net refresh call has not been found");
		}

		elmts = getDriver().findElements(By.xpath("//div[@id='column-right']//section"));

		if (elmts.size() > 1) {
			if (mrf) {
				ExtentTestManager.getTest().log(LogStatus.FAIL,
						"Two Quiz polls has been found after medaia net refresh.");
				verifyPoll_In_ViewPort(elmts);
			} else {
				ExtentTestManager.getTest().log(LogStatus.FAIL,
						"Two Quiz polls has been found even through medaia net refresh not happens");
				verifyPoll_In_ViewPort(elmts);
			}
		} else {
			if (elmts.size() == 1) {
				if (mrf) {
					generatePassReport("No Quiz polls has been appended after Media.net refresh.");
					verifyPoll_In_ViewPort(elmts);
				}
			}
		}

	}

	public void verifyPoll_In_ViewPort(List<WebElement> elmts) throws InterruptedException {
		jse = (JavascriptExecutor) getDriver();
		int height = getDriver().manage().window().getSize().getHeight();
		int scroll = height / 50;
		int s = scroll;

		int max = 30;
		for (int i = 0; i < max; i++) {
			Thread.sleep(1000);
			s = s + scroll;
			jse.executeScript("window.scrollBy(0," + s + ")");

			try {
				for (int k = 0; k < elmts.size(); k++) {
					if (isVisibleInViewport(elmts.get(k))) {
						generatePassReport(k + " - Fast 5 quiz is in view port");
					} else {
						generateInfoReport(k + " - Fast 5 quiz is not in view port");
					}
				}

			} catch (NoSuchElementException e) {
				generateInfoReport("Fast 5 quiz is not in view port");
			}
			try {
				if (isVisibleInViewport(getDriver().findElement(By.xpath("(//*[@class='rel-links-title'])[1]")))) {
					generatePassReport("Quiz stick end point has come.");
				} else {
					generateInfoReport("Quiz stick end point has not in view port");
				}
			} catch (NoSuchElementException e) {
				generateInfoReport("Quiz stick end point has not in view port");
			}

		}
	}
}
