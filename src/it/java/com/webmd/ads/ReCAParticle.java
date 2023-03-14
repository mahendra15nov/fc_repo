package com.webmd.ads;

import java.util.StringTokenizer;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import com.webmd.common.AdsCommon;

/*
Verify that pos 101 should not be exists in Desktop ad calls.
Verify that pos 909 with ad size "Fluid" (320x50) and 2x5 should be exists in Desktop ad calls.
Verify that lazy loaded ad calls do not exist for Desktop
Verify that pos 1004 should not be exists in MobileWeb ad call.
Verify that pos 1909 with ad size "Fluid" (320x50) should be exists in MobileWeb ad calls.
Verify that Adhesive Footer pos 1145 with ad sizes "375x80, 320x80, 320x50, 300x50" should be exists for MobileWeb ad calls.
Verify that Adhesive Footer ad should not contain the close button.
Verify that there is no impact on regular viewarticle pages.
Verify that medianet refresh calls should have the same ad call data as per ReCap article changes.
Verify that the 1122 and the 1909 should be added right before the "Recommendations" widget if there is no 1122 in page loaded ad call.
*/
@Listeners(com.webmd.general.common.Listener.class)
public class ReCAParticle extends AdsCommon {

	JavascriptExecutor jse;

	@BeforeTest(groups = { "ReCAP" })
	public void openBrowser() {
		getDriver();
	}

	@AfterTest(groups = { "ReCAP" })
	public void closeBrowser() {
		getDriver().quit();
	}

	@Test(dataProvider = "medscapeurls", groups = { "ReCAP" })
	public void VerifyReCAPArtciles(String URL) throws InterruptedException {

		System.out.println(URL);
		login(getProperty("username"), getProperty("password"));
		jse = (JavascriptExecutor) getDriver();

		getServer().newHar();

		getDriver().get(URL);

		if (!is404(getDriver().getTitle())) {
			if (!(getDriver().getTitle().contains("Medscape Log In ")
					|| getDriver().getTitle().contains("Medscape Log In"))) {

				if (getDriver().getCurrentUrl().toLowerCase().contains("recap")) {

					Object b = null;
					try {
						b = jse.executeScript("return _isRecap");
					} catch (org.openqa.selenium.WebDriverException e) {
						b = null;
					}
					System.out.println(b);
					if (b != null && b.toString().equals("true")) {
						String pc = StringUtils.substringBetween(getDriver().getPageSource(), "\"pc\":\"", "\",");
						if (pc != null && (!pc.isEmpty())) {
							if (pc.equalsIgnoreCase("recap")) {
								reCAPfunctionality("recap");
							} else {
								generateFailReport("pc: value in page is not a repcap, its a " + pc);
							}
						} else {
							generateFailReport("pc: value in page is found - " + pc);
						}
					} else {
						generateFailReport(
								"_isRecap value has been returned " + b.toString() + ", though it recap article.");
					}
				} else {
					generateInfoReport("Given " + URL + " is not a ReCAP Article.");
					if (getDriver().getCurrentUrl().toLowerCase().contains("viewarticle")) {
						reCAPfunctionality("viewarticle");
					}
				}
			} else {
				generateInfoReport("Login required for " + URL);
			}
		}
	}

	public void reCAPfunctionality(String articleType) {
		if (numberOfAdCallsValidation()) {
			adcallvalidation(articleType);
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
				if (breakPoint.equals("4")) {
					generateFailReport("Lazy loaded calls should not be tracked in Desktop");
				}
				if (breakPoint.equals("1")) {
					adcallvalidation(articleType);
				}
			}
			if (footer != null) {
				if (isVisibleInViewport(footer)) {
					break;
				}
			}
		}
	}

	public void adcallvalidation(String articleType) {
		// number of secure peburd call verification
		// Verification of prev_scp and prev_iu_sizes
		String prev_scp = getSpecificKeyFromSecurePubadCall("prev_scp");
		String prev_iu_szs = getSpecificKeyFromSecurePubadCall("prev_iu_szs");
		generateInfoReport("prev_scp : " + prev_scp);
		generateInfoReport("prev_iu_szs : " + prev_iu_szs);
		if ((prev_scp != null && (!prev_scp.isEmpty())) && (prev_iu_szs != null && (!prev_iu_szs.isEmpty()))) {

			if (breakPoint.equalsIgnoreCase("4")) {
				// pos 101 verification in ad call
				if (articleType.equals("recap")) {
					if (!prev_scp.contains("101")) {
						generatePassReportWithNoScreenShot("Ad pos-101 has not been found in ad call.");
					} else {
						generateFailReport("Ad pos-101 has been found in ad call.");
					}
				} else {
					if (articleType.equals("viewarticle")) {
						if (prev_scp.contains("101")) {
							generatePassReportWithNoScreenShot("Ad pos-101 has been found in ad call.");
						} else {
							generateFailReport("Ad pos-101 has not been found in ad call.");
						}
					}
				}
				// pos 910 verification in ad call
				if (articleType.equals("recap")) {
					if (prev_scp.contains("910")) {
						generatePassReportWithNoScreenShot("Ad pos-910 has been found in ad call.");
					} else {
						generateInfoReport("Ad pos-910 has not been found in ad call.");
					}
				} else {
					if (articleType.equals("viewarticle")) {
						if (prev_scp.contains("910")) {
							generatePassReportWithNoScreenShot("Ad pos-910 has been found in ad call.");
						} else {
							generateFailReport("Ad pos-910 has not been found in ad call.");
						}
					}
				}
				// pos 122 verification in ad call
				if (articleType.equals("recap")) {
					if (prev_scp.contains("122")) {
						generatePassReportWithNoScreenShot("Ad pos-122 has been found in ad call.");
					} else {
						generateInfoReport("Ad pos-122 has not been found in ad call.");
					}
				} else {
					if (articleType.equals("viewarticle")) {
						if (prev_scp.contains("122")) {
							generatePassReportWithNoScreenShot("Ad pos-122 has been found in ad call.");
						} else {
							generateFailReport("Ad pos-122 has not been found in ad call.");
						}
					}
				}
				// pos 909 verification in ad call
				if (articleType.equals("recap")) {
					if (prev_scp.contains("909")) {
						generatePassReportWithNoScreenShot("Ad pos-909 has been found in ad call.");
						// applicable sizes verification
						posAndSize(prev_scp, prev_iu_szs);

						// Verify the ad placeholder in page
						verifyAdPositionOnPage("909");
					} else {
						generateFailReport("Ad pos-909 has not been found in ad call.");
					}
				} else {
					if (articleType.equals("viewarticle")) {
						if (!prev_scp.contains("909")) {
							generatePassReportWithNoScreenShot("Ad pos-909 has not been found in ad call.");
						} else {
							generateFailReport("Ad pos-909 has been found in ad call.");
						}
					}
				}

			} else {
				if (breakPoint.equalsIgnoreCase("1")) {
					if (articleType.equals("recap")) {
						// verification of 1004 pos unavailability in ad call
						if (!prev_scp.contains("1004")) {
							generatePassReportWithNoScreenShot("Ad pos-1004 has not been found in ad call.");
						} else {
							generateFailReport("Ad pos-1004 has been found in ad call.");
						}
					} else {
						if (articleType.equals("viewarticle")) {
							if (prev_scp.contains("1004")) {
								generatePassReportWithNoScreenShot("Ad pos-1004 has been found in ad call.");
							} else {
								generateFailReport("Ad pos-1004 has not been found in ad call.");
							}
						}
					}

					// pos 1909 verification in ad call
					if (articleType.equals("recap")) {

						if (prev_scp.contains("1909")) {
							generatePassReportWithNoScreenShot("Ad pos-1909 has been found in ad call.");
							// applicable sizes verification
							posAndSize(prev_scp, prev_iu_szs);
							// Verify the ad position on page
							verifyAdPositionOnPage("1909");
						} else {
							generateFailReport("Ad pos-1909 has not been found in ad call.");
						}
					} else {
						if (articleType.equals("viewarticle")) {
							if (!prev_scp.contains("1909")) {
								generatePassReportWithNoScreenShot("Ad pos-1909 has not been found in ad call.");
							} else {
								generateFailReport("Ad pos-1909 has been found in ad call.");
							}
						}
					}
					// pos 1145 verification in ad call
					if (articleType.equals("recap")) {
						if (prev_scp.contains("1145")) {
							generatePassReportWithNoScreenShot("Ad pos-1145 has been found in ad call.");
							// applicable sizes verification
							posAndSize(prev_scp, prev_iu_szs);
							verifyAdPositionOnPage("1145");
						} else {
							generateFailReport("Ad pos-1145 has not been found in ad call.");
						}
					} else {
						if (articleType.equals("viewarticle")) {
							if (!prev_scp.contains("1145")) {
								generatePassReportWithNoScreenShot("Ad pos-1145 has not been found in ad call.");
							} else {
								generateFailReport("Ad pos-1145 has been found in ad call.");
							}
						}
					}

				}
			}

		} else {
			generateFailReport("prev_scp / prev_iu_szs has been found with null/empty in the Ad call.");
		}
	}

	private void verifyAdPositionOnPage(String pos) {
		WebElement element = getDriver().findElement(By.id("ads-pos-" + pos));

		String className = element.getAttribute("class");
		if (className.contains("blank-ad")) {
			generateInfoReport("blank ad has been served for pos " + pos);
		} else {

			if (element.getAttribute("style").contains("display: none;")) {
				generateInfoReport("pos " + pos + " is not in view port in the page.");
			} else {
				if (isVisibleInViewport(element)) {
					generatePassReportWithNoScreenShot("pos " + pos + " is in view port.");
				} else {
					generateFailReport("pos " + pos + " is not in view port.");
				}
			}
		}
	}

	public void posAndSize(String prev_scp, String prev_iu_szs) {
		StringTokenizer aPrev_scp = new StringTokenizer(prev_scp, "|");
		StringTokenizer aPrev_iu_szs = new StringTokenizer(prev_iu_szs, ",");

		if (aPrev_scp.countTokens() == aPrev_iu_szs.countTokens()) {

			while (aPrev_scp.hasMoreTokens()) {
				String position = "";
				String sizes = "";
				String expectedPos = "";
				String expectedSize = "";
				String unExpSize = "";
				position = aPrev_scp.nextToken();
				sizes = aPrev_iu_szs.nextToken();
				generateInfoReport(position);
				generateInfoReport(sizes);
				if (breakPoint.equals("1")) {
					if (position.contains("1909")) {
						expectedPos = "1909";
					}
				}
				if (breakPoint.equals("4")) {
					if (position.contains("909")) {
						expectedPos = "909";
					}
				}
				if (expectedPos.contains("909")) {
					expectedSize = "320x50";
					validatePositionAndSize(position, sizes, expectedPos, expectedSize, unExpSize);
					expectedSize = "2x5";
					validatePositionAndSize(position, sizes, expectedPos, expectedSize, unExpSize);
				}
				if (breakPoint.equals("1") && position.contains("1145")) {
					expectedSize = "375x80";
					expectedPos = "1145";
					validatePositionAndSize(position, sizes, expectedPos, expectedSize, unExpSize);
					expectedSize = "320x80";
					validatePositionAndSize(position, sizes, expectedPos, expectedSize, unExpSize);
					expectedSize = "320x50";
					validatePositionAndSize(position, sizes, expectedPos, expectedSize, unExpSize);
					expectedSize = "300x50";
					validatePositionAndSize(position, sizes, expectedPos, expectedSize, unExpSize);
				}
			}
		} else {
			generateFailReport("prev_scp and prev_iu_szs counts are miss match, prev_scp count is "
					+ aPrev_scp.countTokens() + ", prev_iu_szs count is " + aPrev_iu_szs.countTokens());
		}
	}

	public void validatePositionAndSize(String position, String sizes, String expectedPos, String expectedSize,
			String unExpSize) {

		if (position.contains(expectedPos)) {
			generatePassReportWithNoScreenShot("Position " + expectedPos + " has been found in Ad call.");
			if (sizes.contains(expectedSize)) {
				generatePassReportWithNoScreenShot("Applicable size " + expectedSize + " has been found.");
			} else {
				generateFailReport("Applicable size " + expectedSize + " has not been found.");
			}
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
		return new String[] { /*
								 * "https://reference." + env +
								 * "medscape.com/recap/897165", "https://www." +
								 * env + "medscape.com/recap/896895",
								 * "https://www." + env +
								 * "medscape.com/viewarticle/894993",
								 * "https://www." + env +
								 * "medscape.com/viewarticle/895063",
								 */
				"https://reference.qa01.medscape.com/recap/896529" };
	}

}
