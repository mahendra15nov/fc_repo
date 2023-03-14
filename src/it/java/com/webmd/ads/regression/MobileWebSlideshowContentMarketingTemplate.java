package com.webmd.ads.regression;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;

/**
 * Mobile Optimization - Side show content marketing template
 * 
 * @author amahendra
 *
 */
@Listeners(com.webmd.general.common.Listener.class)
public class MobileWebSlideshowContentMarketingTemplate extends AdsCommon {
	JavascriptExecutor jse;
	int totalSlids = 0;
	boolean contentMarketingTemEli = false;
	boolean blankAd = false;

	@BeforeClass(alwaysRun = true)
	public void beforeClass() {
		getDriver();
		login(getProperty("username"), getProperty("password"));
	}

	@AfterClass(alwaysRun = true)
	public void quitBrowser() {
		getDriver().quit();
		getServer().stop();
	}

	@Test(dataProvider = "medscapeurls", groups = { "ContentMarketingTemplate", "AdsRegression", "MobileWeb" })
	public void verifyMobileWeSlideshowContentMarketingTemplate(String url) throws InterruptedException {

		jse = (JavascriptExecutor) getDriver();
		// verify that whether the url contains ecd=conmkt
		getServer().newHar();
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD"))
			url = url.replace("medscape", env + "medscape");

		getURL(url);
		waitForAdCallFound();

		if (!is404(getDriver().getTitle())) {
			if (!isLoginPage()) {
				boolean b = false;
				boolean p = false;
				int cslide = 0;
				try {
					String csd = "";
					csd = getDriver()
							.findElement(By.xpath("//*[@id='slide-show-v2']//span[@class='crs-pagination_current']"))
							.getText();
					if (!csd.isEmpty())
						cslide = Integer.parseInt(csd);
					else
						cslide = 100;
					p = true;
				} catch (NoSuchElementException e) {

				}
				if (breakPoint.equals("1")) {
					try {
						b = getDriver().findElement(By.xpath("//section[@class='crs-header infinitescroll-only']"))
								.isDisplayed();
					} catch (NoSuchElementException e) {

					}
					if (url.contains("ecd=conmkt")
							&& (url.contains("slideshow") && (!url.contains("features")) && (!b))) {
						setDescription(
								"Verify that 300x50 adhesive footer ad (pos 1005) should be removed and 300x250 ad should be available at the bottom of the content (pos 1122) when 'ecd=conmkt' keys value pair exists in query string for slideshows");

						// verifying the site url after redirecting
						if (getDriver().getCurrentUrl().contains("ecd=conmkt")) {
							generateInfoReport("Content Marketing Template should applies.");
							contentMarketingTemEli = true;
							generateInfoReport("**** Slide 1 *****");
							verifyAdPos(false, false, cslide, "1122", "300x250", "1005");

							if (p) {
								navigateToNextSlideshow("1122", "300x250", "1005", url);
								navigateToBackSlideshow("1122", "300x250", "1005", url);
							}

						} else {
							if (isLoginPage())
								generateFailReport("Failed to login!!!");
							else
								generateFailReport(
										"ecd=conmkt key value pair was exists in url but after it reloading, it disappear in url.");
						}
					} else {
						if ((url.contains("slideshow") && b)) {

							if (isLoginPage()) {
								generateFailReport("Failed to login!!!");
							} else {
								if ((getDriver().getCurrentUrl().contains("slideshow") && b)) {
									generateInfoReport(
											"Infinite Slide shows, Content Marketing Template should not applies.");
									setDescription(
											"PPE-176934 Verify that current changes will not be applicable for infinite slideshows.");
									generateInfoReport("**** Slide 1 *****");
									verifyAdPos(true, false, cslide, "1005", "300x50", "1122");
									if (p) {
										navigateToNextSlideshow("1005", "300x50", "1122", url);
										navigateToBackSlideshow("1005", "300x50", "1122", url);
									}
								} else {
									generateFailReport(
											"ecd=conmkt key value pair was exists in url but after it reloading, it disappear in url.");
								}
							}
						} else {
							if (url.contains("ecd=conmkt")
									&& (url.contains("slideshow") && (url.contains("features")))) {
								if (isLoginPage()) {
									generateFailReport("Failed to login!!!");
								} else {
									if ((getDriver().getCurrentUrl().contains("ecd=conmkt")
											&& (getDriver().getCurrentUrl().contains("slideshow")
													&& (getDriver().getCurrentUrl().contains("features"))))) {
										generateInfoReport(
												"Normal Slide shows, Content Marketing Template should not applies.");
										setDescription(
												"PPE-176934 Verify that current changes will not be applicable for legacy slideshows.");
										generateInfoReport("**** Slide 1 *****");
										verifyAdPos(false, false, cslide, "1005", "300x50", "1122");
										if (p) {
											navigateToNextSlideshow("1005", "300x50", "1122", url);
											navigateToBackSlideshow("1005", "300x50", "1122", url);
										}
									} else {
										generateFailReport(
												"ecd=conmkt key value pair was exists in url but after it reloading, it disappear in url.");
									}
								}
							} else {
								setDescription(
										"PPE-176927 Verify that 300x50 adhesive footer ad (pos 1005) should be available when 'ecd=conmkt' key value pair does not exists in query string for slideshows");

								verifyAdPos(false, false, cslide, "1005", "300x50", "1122");
								if (p) {
									navigateToNextSlideshow("1005", "300x50", "1122", url);
									navigateToBackSlideshow("1005", "300x50", "1122", url);
								}
							}
						}
					}
				} else {
					generateInfoReport("Content Marketing Template is applicable only for MobileWeb Browser. ");
				}
			} else {
				generateWarningReport(url + " is required login.");
			}
		} else {
			generateWarningReport(url + " has a page not found.");
		}
	}

	private void verifyAdPos(boolean infinite, boolean interstitialadpb, int slide, String pos, String size,
			String npos) throws InterruptedException {

		int ncalls = getNumberOfCallsTrackedInNetwrok("securepubads.g.doubleclick.net/gampad/ads?");

		if (ncalls > 1) {
			if (infinite || interstitialadpb)
				generateInfoReport(ncalls
						+ " Ad calls has been found, but still we are not validating the Ad calls for infinite/interstitial slideshows.");
			else
				generateFailReport(
						ncalls + " 'securepubads.g.doubleclick.net/gampad/ads?' calls are tracked in network.");
		} else {
			if (ncalls == 1) {
				generatePassReportWithNoScreenShot(
						"Only one 'securepubads.g.doubleclick.net/gampad/ads?' calls are tracked");
			} else {
				generateFailReport("No 'securepubads.g.doubleclick.net/gampad/ads?' calls are tracked in network");
			}
		}

		if (verifySpecificAdPresenceInSecurePubadCall(pos)) {
			generatePassReport(pos + " position is found in Ad call");
			String rsize = getSizesForSpecificPositionFromAdCall(pos);
			if (rsize != null) {
				if (rsize.contains(size)) {
					generatePassReportWithNoScreenShot(
							"Expected " + size + " size is available in Ad call for " + pos + " position");

				} else {
					generateFailReport("Expected size " + size + " is not available for " + pos
							+ " Ad position, Available sizes are " + rsize);
				}
			} else {
				generateFailReport(pos + " ad position is not available in Ad call");
			}
		} else {
			generateFailReport(pos + " ad position is not available in Ad call");
		}
		// Add View Port Verification and Read More option
		if (contentMarketingTemEli) {
			if (slide == 1) {
				try {
					if (getDriver()
							.findElement(
									By.xpath("//div[@class='crs-slide rdmore slick-slide slick-current slick-active']"))
							.isDisplayed()) {
						generateFailReport("Read More button has applied in the first slide.");
					} else {
						generatePassReportWithNoScreenShot("Read More button has not applied in the first slide.");
					}
				} catch (NoSuchElementException e) {
					generatePassReportWithNoScreenShot("Read More button has not applied in the first slide.");
				}

				try {
					if (getDriver()
							.findElement(By
									.xpath("//div[@class='crs-slide first-slide slick-slide slick-current slick-active']"))
							.isDisplayed()) {
						generatePassReportWithNoScreenShot("First slide has been present at bottom of the content.");
					} else {
						generateFailReport("First slide has not been present at bottom of the content.");
					}
				} catch (NoSuchElementException e) {
					generateFailReport("First slide has not been present at bottom of the content.");
				}
				if (targetDevice.contains("android"))
					scrollingDownMobile(pos, npos, slide);
				else {
					if (pos.contains("1122"))
						scrollingDown(pos, npos, slide);
				}
			} else {
				if (slide > 1 && slide < totalSlids - 2) {
					if (targetDevice.contains("android"))
						scrollingDownMobile(pos, npos, slide);
					else {
						if (pos.contains("1122"))
							scrollingDown(pos, npos, slide);
					}

					try {
						StaticWait(3);
						if (getDriver()
								.findElement(By
										.xpath("//div[@class='crs-slide rdmore slick-slide slick-current slick-active']"))
								.isDisplayed()) {
							generatePassReportWithNoScreenShot("Read More button has applied in slide " + slide);
						} else {
							generateFailReport("Read More button has not applied in slide " + slide);
						}
					} catch (NoSuchElementException e) {
						try {
							if (getDriver()
									.findElement(By
											.xpath("//div[@class='crs-slide slick-slide rdmore slick-current slick-active']"))
									.isDisplayed()) {
								generatePassReportWithNoScreenShot("Read More button has applied in slide " + slide);
							} else {
								generateFailReport("Read More button has not applied in slide " + slide);
							}
						} catch (NoSuchElementException e1) {
							generateFailReport("Read More button has not applied in slide " + slide
									+ ", or xpath might have changed.");
						}
					}
					boolean readMore = false;
					int c = 0;
					do {
						readMore = false;
						try {
							StaticWait(2);
							// Click on Read More Option
							JavascriptExecutor executor = (JavascriptExecutor) getDriver();
							try {
								WebElement element = getDriver()
										.findElement(By.xpath("//*[@id='ad-wrapper-" + slide + "']/span"));
								executor.executeScript("arguments[0].click();", element);
								// Verify that 1122 should go down the page.
								Actions action = new Actions(getDriver());
								WebElement ele = getDriver().findElement(
										By.xpath("(//div[@class='crs-slide__copy has-content-ad'])[" + slide + "]/p"));
								action.dragAndDropBy(ele, ele.getLocation().getX(), ele.getLocation().getY() - 10)
										.build().perform();
								StaticWait(3);
							} catch (Exception e) {

							}
							try {
								if (getDriver()
										.findElement(By
												.xpath("//div[@class='crs-slide slick-slide rdmore-clicked slick-current slick-active']"))
										.isDisplayed()) {
									generatePassReportWithNoScreenShot(
											"1122 Ad has been moved to bottom of the slide show content");
								} else {
									if (c == 4)
										generateFailReport(
												"1122 Ad has not been moved to bottom of the slide show content");
									readMore = true;
								}

							} catch (NoSuchElementException e) {
								if (c == 4)
									generateFailReport(
											"1122 Ad has not been moved to bottom of the slide show content");
								readMore = true;
							}
							if (targetDevice.contains("android"))
								scrollingDownMobile(pos, npos, slide);
							else {
								if (pos.contains("1122"))
									scrollingDown(pos, npos, slide);
							}
						} catch (NoSuchElementException e) {
							readMore = true;
						}
						++c;
						if (c == 5)
							readMore = false;
					} while (readMore);

				} else {
					if (targetDevice.contains("android"))
						scrollingDownMobile(pos, npos, slide);
					else {
						if (pos.contains("1122"))
							scrollingDown(pos, npos, slide);
					}
				}
			}
		} else {
			if (targetDevice.contains("android"))
				scrollingDownMobile(pos, npos, slide);
			else {
				if (pos.contains("1122"))
					scrollingDown(pos, npos, slide);
			}
		}
	}

	private void scrollingDownMobile(String pos, String npos, int slide) throws InterruptedException {

		// More slides and for more information slides related code.
		try {

			WebElement moreslides = getDriver()
					.findElement(By.xpath("(//*[@id='slide-show-v2']//h3[@class='crs_related_heading'])[1]"));
			WebElement forMoreInformation = getDriver()
					.findElement(By.xpath("(//*[@id='slide-show-v2']//h3[@class='crs_related_heading'])[2]"));
			if (moreslides.isDisplayed() && moreslides.getText().contains("More Slideshows")) {
				generateInfoReport("This is the 'More Slides' related slide.");
			}
			if (forMoreInformation.isDisplayed() && forMoreInformation.getText().contains("For More Information")) {
				generateInfoReport("This is the 'For More Information' related slide.");
			}

		} catch (NoSuchElementException e) {

		}
		try {
			if (pos.contains("1122")) {
				getScreenshot(getDriver(), "", true);
			}
			// View Port Validation
			if (viewport(pos, npos, slide)) {
				generateFailReport("'ads-pos-" + pos + "' is not in view port");
			} else {
				generatePassReportWithNoScreenShot("'ads-pos-" + pos + "' is in view port");
			}

		} catch (NoSuchElementException e) {
			//
		}
	}

	private void scrollingDown(String pos, String npos, int slide) throws InterruptedException {

		int height = getDriver().manage().window().getSize().getHeight();
		int scroll = height / 50;
		int s = scroll;
		int max = 10;
		boolean adPresent = false;
		for (int i = 0; i < max; i++) {
			Thread.sleep(1000);

			try {
				// More slides and for more information slides related code.
				try {

					WebElement moreslides = getDriver()
							.findElement(By.xpath("(//*[@id='slide-show-v2']//h3[@class='crs_related_heading'])[1]"));
					WebElement forMoreInformation = getDriver()
							.findElement(By.xpath("(//*[@id='slide-show-v2']//h3[@class='crs_related_heading'])[2]"));
					if (moreslides.isDisplayed() && moreslides.getText().contains("More Slideshows")) {
						generateInfoReport("This is the 'More Slides' related slide.");
					}
					if (forMoreInformation.isDisplayed()
							&& forMoreInformation.getText().contains("For More Information")) {
						generateInfoReport("This is the 'For More Information' related slide.");
					}

				} catch (NoSuchElementException e) {

				}
				// View Port Validation
				if (viewport(pos, npos, slide)) {
					adPresent = true;
					break;
				}

			} catch (NoSuchElementException e) {
				//
			}
			if (pos.contains("1005")) {
				i = max - 1;
			}
			if (i == (max - 1) && (!adPresent)) {
				generateFailReport("'ads-pos-" + pos + "' is not in view port");
			}

			s = s + scroll;
			jse.executeScript("window.scrollBy(0," + s + ")");

		}
	}

	private boolean viewport(String pos, String npos, int slide) {
		blankAd = false;
		WebElement epos = null;
		if (pos.contains("1122")) {
			epos = getDriver().findElement(By.xpath("//div[contains(@id,'ads-pos-" + pos + "_" + slide + "')]"));

		} else {
			try {
				epos = getDriver().findElement(By.xpath("//div[contains(@id,'ads-pos-" + pos + "')]"));
				if (epos.getAttribute("class").equalsIgnoreCase("blank-ad")) {
					blankAd = true;
				}
			} catch (NoSuchElementException e) {
				epos = getDriver().findElement(By.xpath("//div[contains(@id,'ads-pos-" + pos + "')]"));
			}
		}
		if (!blankAd) {
			if (isVisibleInViewport(epos)) {
				generatePassReportWithNoScreenShot("'ads-pos-" + pos + "' is in view port");

				// Verifying that 1005 position should not be available

				if (verifySpecificAdPresenceInSecurePubadCall(npos)) {
					generateFailReport(npos + " position is found in Ad call");

				} else {
					generatePassReportWithNoScreenShot(npos + " ad position is not available in Ad call");
				}
				return true;
			} else {
				generateInfoReport("'ads-pos-" + pos + "' is not in view port");
			}
		} else {
			generatePassReportWithNoScreenShot("'ads-pos-" + pos + "' is served a blank Ad.");
			return true;
		}
		WebElement ne_pos = getDriver().findElement(By.xpath("//div[contains(@id,'ads-pos-" + npos + "')]"));
		if (isVisibleInViewport(ne_pos)) {
			generateFailReport("'ads-pos-" + npos
					+ "' should not be available when 'ecd=conmkt' keys value pair exists in the query string");

		}
		return false;
	}

	private void navigateToNextSlideshow(String pos, String size, String npos, String URL) throws InterruptedException {
		navigateSlideshow(true, pos, size, npos, URL);
	}

	private void navigateToBackSlideshow(String pos, String size, String npos, String URL) throws InterruptedException {
		navigateSlideshow(false, pos, size, npos, URL);
	}

	private void navigateSlideshow(boolean forward, String pos, String size, String npos, String URL)
			throws InterruptedException {
		boolean interstitialadpb = false;
		try {
			String totalSlides = getDriver()
					.findElement(By.xpath("(//*[@id='slide-show-v2']//span[@class='crs-pagination_total'])[1]"))
					.getText();

			totalSlids = Integer.parseInt(totalSlides);
			int currentSlids = Integer.parseInt(
					getDriver().findElement(By.xpath("//*[@id='slide-show-v2']//span[@class='crs-pagination_current']"))
							.getText());

			int slide = currentSlids;

			// Image size verification
			String width = "";
			try {
				width = getDriver().findElement(By.xpath("(//img[contains(@class,'crs-slide_image')])[" + slide + "]"))
						.getCssValue("max-width");
				generateInfoReport("Resized Image width is  -> " + width);
				imageSizeVerification(pos, width);
			} catch (Exception e) {

			}

			while (((slide + 1) < (totalSlids) && forward) || (1 < ((slide)) && (!forward))) {
				getServer().newHar();
				boolean nextb = false;

				try {
					if (forward) {
						scrollToObject(
								By.xpath(
										"//*[@id='navigation-arrows']/button[@class='crs_nav_arrow crs_nav_arrow--forward slick-arrow']"),
								"Top next slide button");
					} else {
						scrollToObject(
								By.xpath(
										"//*[@id='navigation-arrows']/button[@class='crs_nav_arrow crs_nav_arrow--back slick-arrow']"),
								"Top back slide button");
					}
				} catch (ElementNotVisibleException e) {
					nextb = false;
				} catch (NoSuchElementException e1) {
					nextb = false;
				}

				try {
					if (forward) {
						nextb = getDriver()
								.findElement(By
										.xpath("//*[@id='navigation-arrows']/button[@class='crs_nav_arrow crs_nav_arrow--forward slick-arrow']"))
								.isDisplayed();
					} else {
						nextb = getDriver()
								.findElement(By
										.xpath("//*[@id='navigation-arrows']/button[@class='crs_nav_arrow crs_nav_arrow--back slick-arrow']"))
								.isDisplayed();
					}
				} catch (ElementNotVisibleException e) {
					nextb = false;
				} catch (NoSuchElementException e1) {
					nextb = false;
				}
				if (nextb) {
					try {
						WebElement element = null;
						JavascriptExecutor executor = (JavascriptExecutor) getDriver();
						if (forward) {
							element = getDriver().findElement(By.xpath(
									"//*[@id='navigation-arrows']/button[@class='crs_nav_arrow crs_nav_arrow--forward slick-arrow']"));
						} else {
							element = getDriver().findElement(By.xpath(
									"//*[@id='navigation-arrows']/button[@class='crs_nav_arrow crs_nav_arrow--back slick-arrow']"));
						}
						executor.executeScript("arguments[0].click();", element);

						WebElement skipp = null;

						try {
							skipp = getDriver().findElement(By.xpath(
									"//*[@id='ss-interstitial']//a[@class='crs_nav_arrow crs_nav--button bg-blue slick-arrow inter-skip']"));

							if (skipp.isDisplayed()) {
								interstitialadpb = true;
								generateInfoReport("interstitial ad is present.");
							} else {
								interstitialadpb = false;
							}
						} catch (NoSuchElementException ee) {
							interstitialadpb = false;
						}
						if (interstitialadpb) {
							executor = (JavascriptExecutor) getDriver();
							executor.executeScript("arguments[0].click();", skipp);
							generateInfoReport("Click on interstitial ad.");
							Thread.sleep(2000);
							interstitialadpb = false;
						}

						try {
							skipp = getDriver().findElement(By.xpath(
									"//*[@id='ss-interstitial']//a[@class='crs_nav_arrow crs_nav--button bg-blue slick-arrow inter-skip']"));

							if (skipp.isDisplayed()) {
								interstitialadpb = true;
								generateInfoReport("interstitial ad is present.");
							} else {
								interstitialadpb = false;
							}
						} catch (NoSuchElementException ee) {
							interstitialadpb = false;
						}
						if (interstitialadpb) {
							executor = (JavascriptExecutor) getDriver();
							executor.executeScript("arguments[0].click();", skipp);
							generateInfoReport("Click on interstitial ad.");
							Thread.sleep(2000);
							interstitialadpb = false;
						}
					} catch (Exception e) {
					}

					// Image size verification
					if (forward) {
						slide = slide + 1;
					} else {
						slide = slide - 1;
					}
					if (slide > 1 && slide <= totalSlids - 2) {
						try {
							width = getDriver()
									.findElement(By.xpath("(//img[contains(@class,'crs-slide_image')])[" + slide + "]"))
									.getCssValue("max-width");
							generateInfoReport("Resized Image width is  -> " + width);
							imageSizeVerification(pos, width);
						} catch (Exception e) {

						}
					}
					StaticWait(5);
					generateInfoReport("***** Slide => " + slide + " *****");
					verifyAdPos(false, interstitialadpb, slide, pos, size, npos);
					StaticWait(5);
				}
			}
		} catch (Exception e) {
		}
	}

	public void imageSizeVerification(String pos, String width) {

		if (pos.contains("1122")) {
			if (width.contains("75")) {
				generatePassReport("Image size has been resized to 75% when ecd=conmkt value in query string.");
			} else {
				generateFailReport(
						"Image size is " + width + "% instead 75% even though ecd=conmkt value in query string.");
			}
		}
		if (pos.contains("1005")) {
			if (width.contains("75")) {
				generateFailReport(
						"Image size has been resized to 75% when there is no ecd=conmkt value in query string.");
			} else {
				generateFailReport(
						"Image size is " + width + "% instead 100% when there is no ecd=conmkt value in query string.");
			}
		}

	}

	@DataProvider
	public String[] medscapeurls() {
		return new String[] { "https://www.medscape.com/slideshow/match-day-6009702?ecd=conmkt",
				"https://www.medscape.com/slideshow/match-day-6009702",
				"https://www.medscape.com/features/slideshow/summer-injuries",
				"https://www.medscape.com/features/slideshow/summer-injuries?ecd=conmkt",
				"https://www.medscape.com/slideshow/2018-compensation-rheumatologist-6009674",
				"https://www.medscape.com/slideshow/2018-compensation-rheumatologist-6009674?ecd=conmkt",
				/*
				 * "https://reference.medscape.com/slideshow/food-poisoning-6009621?ecd=conmkt",
				 * "https://reference.medscape.com/slideshow/food-poisoning-6009621",
				 * "https://reference.medscape.com/slideshow/cerebrovascular-accident-6008993?ecd=conmkt",
				 * "https://reference.medscape.com/slideshow/tattoo-skin-reactions-6006290?ecd=conmkt",
				 * "https://reference.medscape.com/slideshow/complications-alcoholism-6000415?ecd=conmkt",
				 * "https://reference.medscape.com/slideshow/seasonal-affective-disorder-6007256?ecd=conmkt",
				 * "https://www.medscape.com/slideshow/2018-compensation-overview-6009667?ecd=conmkt",
				 * "https://www.medscape.com/slideshow/uk-doctors-satisfaction-survey-6009772?ecd=conmkt",
				 * "https://www.medscape.com/slideshow/uk-doctors-salary-report-6009730?ecd=conmkt",
				 * "https://www.medscape.com/slideshow/1918-influenza-6009604?ecd=conmkt",
				 * "https://www.medscape.com/slideshow/2018-isc-6009605?ecd=conmkt",
				 * "https://www.medscape.com/slideshow/2018-lifestyle-orthopedist-6009234?ecd=conmkt",
				 * "https://www.medscape.com/slideshow/2018-lifestyle-cardiologist-6009219?ecd=conmkt",
				 * "https://www.medscape.com/slideshow/2018-lifestyle-intensivist-6009220?ecd=conmkt",
				 * "https://www.medscape.com/slideshow/2018-lifestyle-emergency-medicine-6009223?ecd=conmkt",
				 * "https://www.medscape.com/slideshow/2018-lifestyle-family-physician-6009224?ecd=conmkt",
				 * "https://www.medscape.com/slideshow/2018-lifestyle-pulmonologist-6009240?ecd=conmkt",
				 * "https://www.medscape.com/slideshow/2018-lifestyle-rheumatologist-6009242?ecd=conmkt",
				 * "https://www.medscape.com/slideshow/2018-lifestyle-urologist-6009243?ecd=conmkt",
				 * "https://www.medscape.com/slideshow/2018-lifestyle-nephrologist-6009229?ecd=conmkt",
				 * "https://reference.medscape.com/features/slideshow/nail-diseases?ecd=conmkt",
				 * "https://reference.medscape.com/features/slideshow/infectious-skin-conditions?ecd=conmkt",
				 * "https://reference.medscape.com/features/slideshow/common-eye-conditions?ecd=conmkt",
				 * "https://www.medscape.com/slideshow/2018-acc-6009747?ecd=conmkt",
				 * "https://www.medscape.com/slideshow/2018-isc-6009605?ecd=conmkt",
				 * "https://www.medscape.com/slideshow/medical-speak-6009733?ecd=conmkt",
				 * "https://www.medscape.com/slideshow/2018-obesity-report-6009712?ecd=conmkt",
				 * "https://www.medscape.com/slideshow/pets-farm-animals-6009620?ecd=conmkt",
				 * "https://reference.medscape.com/slideshow/cerebrovascular-accident-6008993",
				 * "https://reference.medscape.com/slideshow/tattoo-skin-reactions-6006290",
				 * "https://reference.medscape.com/slideshow/complications-alcoholism-6000415",
				 * "https://reference.medscape.com/slideshow/seasonal-affective-disorder-6007256",
				 * "https://www.medscape.com/slideshow/2018-compensation-overview-6009667",
				 * "https://www.medscape.com/slideshow/uk-doctors-satisfaction-survey-6009772",
				 * "https://www.medscape.com/slideshow/uk-doctors-salary-report-6009730",
				 * "https://www.medscape.com/slideshow/1918-influenza-6009604",
				 * "https://www.medscape.com/slideshow/2018-isc-6009605",
				 * "https://www.medscape.com/slideshow/2018-lifestyle-orthopedist-6009234",
				 * "https://www.medscape.com/slideshow/2018-lifestyle-cardiologist-6009219",
				 * "https://www.medscape.com/slideshow/2018-lifestyle-intensivist-6009220",
				 * "https://www.medscape.com/slideshow/2018-lifestyle-emergency-medicine-6009223",
				 * "https://www.medscape.com/slideshow/2018-lifestyle-family-physician-6009224",
				 * "https://www.medscape.com/slideshow/2018-lifestyle-pulmonologist-6009240",
				 * "https://www.medscape.com/slideshow/2018-lifestyle-rheumatologist-6009242",
				 * "https://www.medscape.com/slideshow/2018-lifestyle-urologist-6009243",
				 * "https://www.medscape.com/slideshow/2018-lifestyle-nephrologist-6009229",
				 * "https://reference.medscape.com/features/slideshow/nail-diseases",
				 * "https://reference.medscape.com/features/slideshow/infectious-skin-conditions",
				 * "https://reference.medscape.com/features/slideshow/common-eye-conditions",
				 * "https://www.medscape.com/slideshow/2018-acc-6009747",
				 * "https://www.medscape.com/slideshow/2018-isc-6009605",
				 * "https://www.medscape.com/slideshow/medical-speak-6009733",
				 * "https://www.medscape.com/slideshow/pets-farm-animals-6009620"
				 */ };
	}
}
