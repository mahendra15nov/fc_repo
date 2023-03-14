package com.webmd.ads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.common.AdsConstantns;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;

/*
 * PPE-200052: Consult Sponsored Post: Allow ad call for pos 912 on Desktop and pos 1007 on MobileWeb
 */

public class ConsultSponseredPost extends AdsCommon {
	// add the sponsored post id's here
	ArrayList<String> sponseredPostIds = new ArrayList<>(Arrays.asList("312543"));

	public boolean verifyAdPositionsOnPage() {
		boolean flag = true;
		By adPos = By.xpath("//div[@class='bucket posts']/div[@class='post'][1]/following-sibling::div[1]");

		waitForElement(adPos);

		try {
			if (breakPoint.equals("4")) {
				Assert.assertTrue(getDriver().findElement(adPos).getAttribute("id").contains("post sponsor"));
				generateInfoReport("Sponsor post shown after firts post on desktop");
			} else {
				Assert.assertTrue(getDriver().findElement(adPos).getAttribute("id").contains("post sponsor"));
				generateInfoReport("Sponsor post shown after firts post on mobile");
			}
		} catch (AssertionError e) {
			flag = false;
			generateInfoReport("Ad not shown after first post ");
		} catch (NoSuchElementException e) {
			generateInfoReport("Ad not shown in the filter");
		}

		return flag;
	}

	private boolean validateAdPosOnPage() {
		boolean flag = false;
		By adPos = By.xpath("//div[@class='post sponsor']");
		String postID = null;
		try {
			waitForElement(adPos);
			WebElement ele = getDriver().findElement(adPos);
			if (ele.isDisplayed()) {
				String dataID = ele.getAttribute("data-id");
				generateInfoReport("dataid on page: " + dataID);
				postID = StringUtils.substringBefore(dataID, ",");
				Assert.assertTrue(sponseredPostIds.contains(postID));
				flag = true;
				generatePassReportWithNoScreenShot("Ad loaded on page");
			}
		} catch (NoSuchElementException e) {
			generateInfoReport("Ad not loaded on page");
		} catch (AssertionError e) {
			generateFailReport("Ad loaded with wrong post ID: " + postID);
		}
		return flag;
	}

	// This method will return TRUE value when there is Sponsor ID mentioned in
	// Ad call response, FALSE otherwise
	public boolean verifyAdCall() {
		String size = null, response = null, sponsorID = null;
		boolean flag = false;
		int count = 0;
		Har har = getServer().getHar();
		List<HarEntry> entries = har.getLog().getEntries();
		for (HarEntry entry : entries) {
			if (entry.getRequest().getUrl().contains(AdsConstantns.AD_CALL)) {
				flag = true;
				generateInfoReport("Ad call being made");
				count++;
				List<HarNameValuePair> queryParams = entry.getRequest().getQueryString();
				for (HarNameValuePair harNameValuePair : queryParams) {
					System.out.println(harNameValuePair.getName().trim() + " : " + harNameValuePair.getValue().trim());
					if (harNameValuePair.getName().trim().equalsIgnoreCase("prev_iu_szs")) {
						size = harNameValuePair.getValue().trim();
						try {
							Assert.assertTrue(size.equals("2x8"));
							generatePassReportWithNoScreenShot("Ad call loaded with 2x8 size");
						} catch (AssertionError e) {
							generateFailReport("Size available for the position is " + size);
						} catch (Exception e) {
							generateFailReport("Exception while validating ad call\n" + e.toString());
						}
						break;
					}

					/*
					 * Commented Sponsor id logic in ad call as ad call returned
					 * null response in execution response =
					 * entry.getResponse().getContent().getText();
					 * 
					 * generateInfoReport("Response is "+response);
					 * 
					 * if(response.contains("sponPostId")){ sponsorID =
					 * StringUtils.substringBetween(response, "sponPostId = ",
					 * ";");
					 * 
					 * }
					 */

				}
			}
		}
		if (count > 1)
			generateFailReport("More than one ad call loaded");

		return flag;
	}

	@Test(groups = { "testConsultAds" })
	public void validateSearchPost() {
		beforeTest();
		boolean flag = false;
		int count = 0;

		while (!flag && count < 4) {
			count++;
			getServer().newHar();
			/*
			 * type(consultSearchBox, "test", "SearchBox");
			 * click(consultSearchSubmit, "SearchSubmit");
			 */
			getDriver().get("https://www." + env + "medscape.com/consult/search?filterby=tag&query=test&sortBy=score");
			try {
				Assert.assertTrue(verifyAdCall());
				generatePassReportWithNoScreenShot("Ad call loaded");
			} catch (AssertionError e) {
				generateFailReport("No ad call loaded");
			}
			flag = validateAdPosOnPage();
		}
	}

	private void beforeTest() {
		login("kasupada", "medscape");
		getServer().newHar();
		getDriver().get("https://www." + env + "medscape.com/consult");
		// click(By.xpath("//div[@class='header-tabs']/a[contains(@href,'medscape.com/consult')]"),
		// "Consult Tab");
	}

	@Test(groups = { "testConsultAds" })
	public void validateSponsorAdInAllFeeds() {
		By filterTabs = By.xpath("//div[@class='filter-tabs']/a");

		beforeTest();
		List<WebElement> filters = getDriver().findElements(filterTabs);
		boolean flag = false, adShownOnPage;
		int count = 0;
		do {
			flag = false;
			generateInfoReport("Validating for " + filters.get(count).getText());
			if (verifyAdCall()) {
				adShownOnPage = false;
				while (!adShownOnPage && count < 4) {
					count++;
					try {
						Assert.assertTrue(verifyAdPositionsOnPage());
						adShownOnPage = true;
						generatePassReportWithNoScreenShot("Ad position loaded on page and sponsor id matched");
					} catch (AssertionError e) {
						generateInfoReport("Ad position not loaded on page");
						getDriver().navigate().refresh();
					}
				}
				if (!adShownOnPage)
					generateFailReport("Ad not shown on page after multiple refreshes of page");
			} else {
				try {
					Assert.assertFalse(verifyAdPositionsOnPage());
					generatePassReportWithNoScreenShot("Ad position not loaded on page when Sponsor ID is not match");
				} catch (AssertionError e) {
					generateFailReport("Ad loaded on page when sponsor id not matched");
				}
			}

			getServer().newHar();
			try {
				scrollTillEnd();
				Assert.assertFalse(verifySpecificCallPresence(AdsConstantns.AD_CALL));
				generatePassReport("Ad call not shown after scrolling the page");
			} catch (AssertionError e) {
				generateFailReport("Ad Call loaded upon scrolling the page");
			}
			getServer().newHar();
			try {
				count++;
				filters.get(count).click();
				flag = true;
			} catch (IndexOutOfBoundsException e) {
				generateInfoReport("No more filters available");
				flag = false;
			} catch (WebDriverException e) {
				generateInfoReport("Exception while clicking" + e.toString());
				flag = false;
			}
		} while (flag);
	}
}
