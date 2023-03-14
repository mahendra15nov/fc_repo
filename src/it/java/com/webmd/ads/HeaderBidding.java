package com.webmd.ads;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import com.webmd.common.AdsCommon;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;

@Listeners(com.webmd.general.common.Listener.class)
public class HeaderBidding extends AdsCommon {

	JavascriptExecutor jse;

	@Test(dataProvider = "medscapeurls", groups = { "HeaderBidding" })
	public void VerifyReCAPArtciles(String URL) throws InterruptedException {

		System.out.println(URL);
		// login(getProperty("username"), getProperty("password"));
		jse = (JavascriptExecutor) getDriver();

		getServer().newHar();
		long currentTime = System.currentTimeMillis();
		System.out.println("Time - Before Har Starts : " + currentTime);
		getDriver().get(URL);
		long currentTime1 = System.currentTimeMillis();
		Date harStartTime = null;
		// getServer().endHar();
		System.out.println("Elapsed Time : " + (currentTime1 - currentTime));

		Har har = getServer().getHar();
		List<HarEntry> res = har.getLog().getEntries();
		System.out.println(res);
		System.out.println("Har Entries start--");
		int firstcall = 0;
		Date harCurrentTime = null;
		Date targetTime = null;
		for (HarEntry harEntry : res) {
			// String url = harEntry.getRequest().getUrl();
			// if (harEntry.getTime() <= 500){
			// System.out.println((harEntry.getStartedDateTime().getTime() -
			// currentTime));
			// if ((harEntry.getStartedDateTime().getTime() - currentTime) <=
			// 10000) {

			if (firstcall == 0) {
				harStartTime = harEntry.getStartedDateTime();
				harCurrentTime = harEntry.getStartedDateTime();
				targetTime = DateUtils.addMilliseconds(harStartTime, 2000);
			} else {
				harCurrentTime = harEntry.getStartedDateTime();

			}
			if (harCurrentTime.compareTo(targetTime) <= 0) {
				System.out.println(harEntry.getRequest().getUrl());
				System.out.println("Status is " + harEntry.getResponse().getStatus());

			}
			++firstcall;

			// System.out.println(harEntry.getRequest().getUrl());
			// System.out.println(harEntry.getRequest().getHeaders());
			// System.out.println(harEntry.getResponse().getHeaders());
			// System.out.println(harEntry.getResponse().getHeadersSize());
			// System.out.println(harEntry.getTime());
			// System.out.println(harEntry.getStartedDateTime().getTime());
			// System.out.println(harEntry.getResponse().getStatus());
			// System.out.println(harEntry.getResponse().getHeaders().get);
			// System.out.println(harEntry.getResponse().getStatusText());
			// List<HarNameValuePair> harbamn =
			// harEntry.getResponse().getHeaders();
			// System.out.println(harbamn);
			// }

			/*
			 * for (HarNameValuePair harNameValuePair : harbamn) {
			 * System.out.println(harNameValuePair);
			 * //if(harNameValuePair.getName().contains("Date")) //
			 * harNameValuePair.getValue(); }
			 */
			// }

		}
	}

	public void reCAPfunctionality(String articleType) {
		if (numberOfAdCallsValidation()) {
		}
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
			if (numberOfAdCallsValidation()) {
			}
			if (footer != null) {
				if (isVisibleInViewport(footer)) {
					break;
				}
			}
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
			} else {
				generateInfoReport("No 'securepubads.g.doubleclick.net/gampad/ads?' calls are tracked in network");
			}
		}
		return false;
	}

	@DataProvider
	public String[] medscapeurls() {
		return new String[] {
				"https://www.medscape.com"/*
											 * , "https://www." + env +
											 * "medscape.com/viewarticle/895063",
											 * "https://reference." + env +
											 * "medscape.com/recap/724935",
											 * "https://reference." + env +
											 * "medscape.com/recap/897165"
											 */ };
	}

}
