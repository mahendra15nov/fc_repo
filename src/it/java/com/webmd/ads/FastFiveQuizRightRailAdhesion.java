package com.webmd.ads;

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

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.LogStatus;
import com.webmd.common.AdsCommon;
import com.webmd.general.common.ExtentTestManager;

/*
 * PPE-169640: Fast 5 Quiz - Right Rail Ad Adhesion
 */
public class FastFiveQuizRightRailAdhesion extends AdsCommon {

	By sponseredPoll = By.className("sponsored-poll qna-render rightAd ready sticky show-questions");
	By contentFooter = By.id("footercontents");
	By relatedLinks = By.className("related-links");

	JavascriptExecutor jse;

	private boolean isQuizPollDisplayed() {
		if (getDriver().findElement(sponseredPoll).isDisplayed())
			return true;
		return false;
	}

	public void scrollByPixels(int number) {
		generateInfoReport("Scrolling the page by "+number+" px");
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		jse.executeScript("window.scrollBy(0, "+number+")");
		StaticWait(1);
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
	public void verifyQiuzPollStickiness(String URL) throws InterruptedException {
		generateInfoReport(
				"Verify whether right rail ad poll sticky or not while user scroll the page till content footer");
		login();
		getDriver().get(URL);
		verifyQiuzPollStickiness();

	}



	private boolean isWebElementVisible(WebElement w) {
		Dimension weD = w.getSize();
		Point weP = w.getLocation();
		Dimension d = driver.manage().window().getSize();

		int x = d.getWidth();
		int y = d.getHeight();
		int x2 = weD.getWidth() + weP.getX();
		int y2 = weD.getHeight() + weP.getY();

		return x2 <= x && y2 <= y;
	}
	// PPE-172074: Verify the Right rail ad poll sticky on all browsers and
	// break points except mobile

	@Test(enabled = true, dataProvider = "dataProvider")
	public void verifyAllOtherBreakpoints(String URL) throws InterruptedException {
		generateInfoReport("Verify the Right rail ad poll sticky on all browsers and break points except mobile");
		//getDriver("2");
		login();
		generateInfoReport("Verifying on Breakpoint 2");
		getDriver().get(URL);
		verifyQiuzPollStickiness();
		//getDriver("3");
		login();
		generateInfoReport("Verifying on Breakpoint 3");
		getDriver().get(URL);
		verifyQiuzPollStickiness();
	}

	// PPE-172075: Verify whether there is no impact on mobile ad poll or not
	// due to sticky ad poll on desktop
	@Test(enabled = true, dataProvider = "dataProvider")
	public void verifyQuizPollStickinessOnMobile(String URL) throws InterruptedException {
		generateInfoReport(
				"Verify whether there is no impact on mobile ad poll or not due to sticky ad poll on desktop");
		//getDriver("1");
		login();
		getDriver().get(URL);
		verifyQiuzPollStickiness();
	}

	// PPE-172078: Verify whether right rail lazy loading is disabled when
	// sticky poll is displayed (122 and 910)
	@Test(enabled = false, dataProvider = "dataProvider")
	public void verifyNoLazyload(String URL) throws InterruptedException {
		//getDriver("4");
		login();
		getDriver().get(URL);
		getServer().newHar();
		verifyQiuzPollStickiness();
		if (verifySpecificAdPresenceInSecurePubadCall("securepubads.g.doubleclick.net/gampad/ads?")) {
			generateFailReport("Lazy load Ad call has been found");
		} else {
			generatePassReport("lazy load call has not been found");
		}
	}

	public void verifyQiuzPollStickiness() throws InterruptedException {
		jse = (JavascriptExecutor) getDriver();
		int height = getDriver().manage().window().getSize().getHeight();
		int scroll = height / 50;
		int s = scroll;
		boolean quiz = false;
		boolean linkt = false;
		boolean quizend = false;
		int max = 30;
		for (int i = 0; i < max; i++) {
			Thread.sleep(1000);
			s = s + scroll;
			jse.executeScript("window.scrollBy(0," + s + ")");
			try {

				if (isVisibleInViewport(getDriver().findElement(By.xpath("//div[@id='column-right']//section")))) {
					// if
					// (getDriver().findElement(By.xpath("//div[@id='column-right']//section")).isDisplayed())
					// {
					quiz = true;
					generatePassReport("Fast 5 quiz is in view port");
				} else {
					quiz = false;
					generateInfoReport("Fast 5 quiz is not in view port");
				}
			} catch (NoSuchElementException e) {
				generateInfoReport("Fast 5 quiz is not in view port");
				quiz = false;
			}
			try {

				if (isVisibleInViewport(getDriver().findElement(By.xpath("(//*[@class='rel-links-title'])[1]")))) {
					// if
					// (getDriver().findElement(By.xpath("(//*[@class='rel-links-title'])[1]")).isDisplayed())
					// {
					generatePassReport("Quiz stick end point has come.");
					linkt = true;
				} else {
					linkt = false;
					generateInfoReport("Quiz stick end point has not in view port");
				}
			} catch (NoSuchElementException e) {
				generateInfoReport("Quiz stick end point has not in view port");
				linkt = false;
			}
			if (quiz && linkt) {
				generatePassReport("Quiz stick end point has come, now quiz poll should scroll up along with page.");
				quizend = true;
			}
			if (quizend) {
				if (i == max - 1) {
					if ((!quiz && !linkt)) {
						generatePassReport("Quiz poll has scrolled along with page");
						break;
					} else {
						ExtentTestManager.getTest().log(LogStatus.FAIL,
								"Quiz poll has not scrolled, it has completely stick on page.");
						break;

					}
				}
			} else {
				if (i == max - 1) {
					ExtentTestManager.getTest().log(LogStatus.FAIL,
							"Quiz poll has scrolled along with page, it was not sticky till footer end");
					break;
				}
			}
		}
	}
	
	private void checkpoll(By poll, String event){
		try{
			generateInfoReport("checking whether poll is in view port or not after the event "+event);
			//Assert.assertTrue(isVisibleInViewport(getDriver().findElement(poll)));
			Assert.assertTrue(getDriver().findElement(poll).isDisplayed());
			generatePassReport("Poll is in viewport");
		}catch(AssertionError e){
			generateFailReport("Poll is disappeared");
		}catch(NoSuchElementException e){
			generateFailReport("Poll is not loaded on the page");
		}
	}
	@Test
	public void checkPollNotDisappear(){
		login();
		getDriver().get("https://reference."+env+"medscape.com/viewarticle/909548_6?google_preview=Kdqa_QEWlMkYrcrE6AUwreb57wWIAYCAgKDH4uStLQ&iu=4312434&gdfp_req=1&lineItemId=5050791255&creativeId=138268847538");
		By poll = By.xpath("//div[contains(@class,'sponsored-poll')]");
		waitForPageLoad(30);
		/*call_Gallen("TestInput/GalenSpecs/FFQPollDisappearValidation.gspec", "Poll");
		LogStatus status = ExtentTestManager.getTest().getRunStatus();
		System.out.println("Status is: "+status);
		
		scrollToObject(poll, "Poll");
		call_Gallen("TestInput/GalenSpecs/FFQPollDisappearValidation.gspec", "Poll");
		status = ExtentTestManager.getTest().getRunStatus();
		System.out.println("Status is: "+status);

		scrollByPixels(900);
		call_Gallen("TestInput/GalenSpecs/FFQPollDisappearValidation.gspec", "Poll");
		status = ExtentTestManager.getTest().getRunStatus();
		System.out.println("Status is: "+status);*/
		checkpoll(poll, "page load");
		getScreenshot(getDriver(), "PageLoad");
		scrollToObject(poll, "Poll");
		checkpoll(poll, "scroll to poll");
		getScreenshot(getDriver(), "Scrolled to object");
		scrollByPixels(300);
		getScreenshot(getDriver(), "scrolled 300px");
		checkpoll(poll, "scroll 300px");
		
		scrollByPixels(600);
		getScreenshot(getDriver(), "scrolled 600px");
		checkpoll(poll, "scroll 300px");

	}
}
