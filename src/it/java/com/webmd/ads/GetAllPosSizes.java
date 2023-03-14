package com.webmd.ads;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import com.webmd.common.AdsCommon;
import com.webmd.general.common.ExtentTestManager;
import com.webmd.general.common.XlRead;

@Listeners(com.webmd.general.common.Listener.class)
public class GetAllPosSizes extends AdsCommon {

	HashMap<String, String> pos_szs = new HashMap<>();

	@BeforeClass(groups = { "pos_szs" })
	public void beforeMethod() {
		login(getProperty("username"), getProperty("password"));
	}

	@AfterClass(groups = { "pos_szs" })
	public void closeBrowser() {
		System.out.println(pos_szs);
		getDriver().quit();
		getServer().stop();
	}

	@Test(dataProvider = "faforfpfarticles", groups = { "pos_szs" })
	public void getAllPosAndSizes(String URL) {
		getServer().newHar();
		getDriver().get(URL);
		if (!is404(getDriver().getTitle())) {
			JavascriptExecutor jse = (JavascriptExecutor) getDriver();
			if (!is404(getDriver().getTitle()) && !isLoginPage()) {
				posVsSzs(true);
				int height = getDriver().manage().window().getSize().getHeight();
				int scroll = height / 100;
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
					StaticWait(2);
					s = s + scroll;
					jse.executeScript("window.scrollBy(0," + s + ")");
					posVsSzs(false);
					if (footer != null) {
						if (isVisibleInViewport(footer)) {
							break;
						}
					}

				}
			} else {
				generateInfoReport(URL + " is a gated article hence need login.");
			}

		} else {
			generateInfoReport(URL + " has not fond.");
		}
	}

	public void posVsSzs(boolean pageLoad) {
		if (numberOfAdCallsValidation()) {
			String prev_scp = getSpecificKeyFromSecurePubadCall("prev_scp");
			if (prev_scp != null) {
				if (!prev_scp.isEmpty()) {
					String prev_iu_szs = "";
					if (pageLoad) {
						generateInfoReport("Page Load call pos and szs details.");
					} else {
						generateInfoReport("Lazy Load call pos and szs details.");
					}
					generateInfoReport("prev_scp : " + prev_scp);
					prev_iu_szs = getSpecificKeyFromSecurePubadCall("prev_iu_szs");
					generateInfoReport("prev_iu_szs : " + prev_iu_szs);

					String[] aPrev_scp = prev_scp.split("\\|");
					String[] aPrev_iu_szs = prev_iu_szs.split(",");
					if (aPrev_scp.length == aPrev_iu_szs.length) {
						for (int i = 0; i < aPrev_iu_szs.length; i++) {
							String pos = StringUtils.substringBetween(aPrev_scp[i], "pos=", "&");
							generateInfoReport("pos : " + pos + " == > " + aPrev_iu_szs[i]);
							if (pos_szs.keySet().contains(pos)) {
								if (!pos_szs.get(pos).contains(aPrev_iu_szs[i])) {
									pos_szs.put(pos, pos_szs.remove(pos) + "#" + aPrev_iu_szs[i]);
								}
							} else {
								pos_szs.put(pos, aPrev_iu_szs[i]);
							}
						}
					} else {
						generateFailReport(" prev_scp length :" + aPrev_scp.length + ", prev_iu_szs length: "
								+ aPrev_iu_szs.length);
					}
				} else {
					generateFailReport("prev_scp has found empty in Ad call");
				}
			} else {
				generateFailReport("Ad call has not been tracked in Network calls");
			}
			generateInfoReport(pos_szs.toString());
		}
	}

	@DataProvider()
	public String[][] faforfpfarticles() {
		// return XlRead.fetchDataExcludingFirstRow("AdsSanity.xls", "PROD");
		return new String[][] { { "https://reference.medscape.com/drug/carospir-aldactone-spironolactone-342407" } };

		/*
		 * if (env.contains("qa01")) return
		 * XlRead.fetchDataExcludingFirstRow("AdsSanity.xls", "QA01"); else if
		 * (env.contains("qa00")) return
		 * XlRead.fetchDataExcludingFirstRow("AdsSanity.xls", "QA00"); else if
		 * (env.contains("dev01")) return
		 * XlRead.fetchDataExcludingFirstRow("AdsSanity.xls", "DEV01"); else if
		 * (env.contains("staging")) return
		 * XlRead.fetchDataExcludingFirstRow("AdsSanity.xls", "Staging"); else
		 * return XlRead.fetchDataExcludingFirstRow("AdsSanity.xls", "PROD");
		 */

	}
}
