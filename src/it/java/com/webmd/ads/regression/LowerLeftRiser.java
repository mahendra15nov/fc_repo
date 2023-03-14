package com.webmd.ads.regression;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;

/**
 * Created on 29/10/2019 Version : V 1.0;
 * 
 * PPE-211363 - Create layer experience for "Lower Left Riser"
 * 
 * @author amahendra updated on 15/11/19 - for PPE-214881-LLR - Allow Product
 *         Type configuration to be passed from the ad server
 * 
 */
@Listeners(com.webmd.general.common.Listener.class)
public class LowerLeftRiser extends AdsCommon {

	JavascriptExecutor jse;

	String xpathLLRDivClass = "//div[contains(@class,'aggressive-driver')]";
	By llrDivClass = By.xpath(xpathLLRDivClass);
	By llrDivSpan = By.xpath("//div[contains(@class,'aggressive-driver')]/span[contains(@class,'close')]");
	By llrDivifi = By.xpath("//div[contains(@class,'aggressive-driver')]/span[@class='ifi']");

	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		getDriver().quit();
		getServer().stop();
	}

	@BeforeClass(alwaysRun = true)
	public void openBrowser() {
		login(getProperty("username"), getProperty("password"));
		jse = (JavascriptExecutor) getDriver();
	}

	/**
	 * PPE-211363 - Create layer experience for "Lower Left Riser"
	 * 
	 * @param url
	 */
	@Test(dataProvider = "medscapeurls", groups = { "LLR", "AdsSmoke", "AdsRegression", "MobileWeb" })
	public void verifyLowerLeftRaiser(String url) {
		getServer().newHar();
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD"))
			url = url.replace("medscape", env + "medscape");
		getURL(url);
		waitForAdCallFound();
		StaticWait(10);
		if (!is404(getDriver().getTitle())) {
			if (!isLoginPage()) {
				if (numberOfAdCallsValidation()) {
					if (breakPoint.equals("4")) {
						if (isTrue("s_responsive_design")
								&& getSpecificKeyFromSecurePubadCall("cust_params").contains("rd=1")) {
							generateInfoReport(getDriver().getCurrentUrl() + " is a responsive page.");
							// Verify the applicable size and pos
							posAndSize("1x2", "", "910", "");
							// Verification of LLR
							if (isLLRLaoded()) {
								// Validate the LLR properties
								isInnerContentLaodedInLLR();
								// Verify the properties
								verifyLLRProperties();
							}
						} else {
							generateInfoReport(getDriver().getCurrentUrl() + " is not a responsive page.");
						}
					}
				} else {
					generateInfoReport("Ad call has not been found in page load.");
				}
			}
		} else {
			generateSkipReport(getDriver().getCurrentUrl() + " is not working.");
		}
	}

	/**
	 * Verifies whether the LLR loaded on the page or not.
	 * 
	 * @return
	 */
	private boolean isLLRLaoded() {
		boolean aggressiveDriver = false;
		aggressiveDriver = getDriver().findElement(llrDivClass).isDisplayed();

		generateReport(aggressiveDriver, "LLR div has been loaded on the page.",
				"LLR div has not been loaded on the page.");
		if (aggressiveDriver) {
			aggressiveDriver = getDriver()
					.findElement(By
							.xpath("//div[contains(@class,'aggressive-driver')]//iframe[@id='aggressive-driver-frame']"))
					.isDisplayed();
			generateReport(aggressiveDriver, "LLR iframe has been loaded on the page.",
					"LLR iframe has not been loaded on the page.");
		}
		return aggressiveDriver;
	}

	/**
	 * Verify the whether inner content loaded or not in LLR
	 * 
	 * @return
	 */
	private boolean isInnerContentLaodedInLLR() {
		boolean isInnerContentLaoded = false;
		switchToFrame(By.xpath("//*[@id='aggressive-driver-frame']"), "Aggressive Driver LLR Iframe");
		if (!is404(getDriver().getTitle())) {
			generatePassReportWithNoScreenShot("LLR inner content has been loaded properly.");
			isInnerContentLaoded = true;
		} else {
			generateFailReport("LLR inner content has not been loaded properly, title is " + getDriver().getTitle());
		}
		getDriver().switchTo().defaultContent();
		return isInnerContentLaoded;
	}

	/**
	 * Verify the LLR CSS properties like close button, LLR layer, Medscape Logo
	 */
	private void verifyLLRProperties() {
		// Get the config vallues of LLR
		String configValues = getCreativeConfigValues("//*[@id='ads-pos-910']//iframe", "/html/body/div[2]/script[4]");

		String llrpage = StringUtils.substringBetween(configValues, "frameSrc: '", "',");
		generateInfoReport("Lower Left Raiser URL : " + llrpage);
		String productType = StringUtils.substringBetween(configValues, "productType: '", "',");
		generateInfoReport("Lower Left Raiser Product Type : " + productType);
		// Verification of LLR layer properties.
		if (productType.equals("LLR")) {
			generatePassReportWithNoScreenShot("Product Type is LLR");
		} else {
			generateFailReport("Product Type is not a LLR");
		}

		String display = getDriver().findElement(llrDivClass).getCssValue("display");
		String bottom = getDriver().findElement(llrDivClass).getCssValue("bottom");
		String left = getDriver().findElement(llrDivClass).getCssValue("left");
		String width = getDriver().findElement(llrDivClass).getCssValue("width");
		String height = getDriver().findElement(llrDivClass).getCssValue("height");
		generateReport(display.equals("block"), "LLR display property is block",
				"LLR display property is not block, its appears with " + display);
		generateReport(bottom.equals("0px"), "LLR bottom property is 0px",
				"LLR bottom property is not 0px, it is " + bottom);
		generateReport(left.equals("0px"), "LLR left property is 0px", "LLR left property is not 0px, it is " + left);
		generateReport(width.equals("320px"), "LLR width property is 320px",
				"LLR width property is not 320px, it is " + width);
		generateReport(height.equals("598px"), "LLR height property is 598px",
				"LLR height property is not 598px, it is " + height);

		// Verification of Close button properties
		String buttonClass = getDriver().findElement(llrDivSpan).getAttribute("class");

		String position = getDriver().findElement(llrDivSpan).getCssValue("position");
		generateReport(position.equals("absolute"), "LLR Close button position property is absolute",
				"LLR Close button position property is not absolute, its " + position);

		String top = getDriver().findElement(llrDivSpan).getCssValue("top");
		String right = getDriver().findElement(llrDivSpan).getCssValue("right");
		generateReport(right.equals("10px"), "LLR Close button right property is 10px",
				"LLR Close button right property is not 10px, its " + right);

		String cursor = getDriver().findElement(llrDivSpan).getCssValue("cursor");
		generateReport(cursor.equals("pointer"), "LLR Close button cursor property is pointer",
				"LLR Close button cursor property is not pointer, its " + cursor);

		if (buttonClass.equals("close close-x-image")) {
			String backgroundimage = getDriver().findElement(llrDivSpan).getCssValue("background-image");
			generateReport(backgroundimage.contains("/pi/global/icons/icon-close-x-black.svg"),
					"LLR Close button backgroundimage property is pi/global/icons/icon-close-x-black.svg",
					"LLR Close button backgroundimage property is not pi/global/icons/icon-close-x-black.svg, its "
							+ backgroundimage);
			generateReport(top.equals("6px"), "LLR Close button top property is 6px",
					"LLR Close button top property is not 6px, its " + top);
			width = getDriver()
					.findElement(
							By.xpath("//div[contains(@class,'aggressive-driver')]/span[@class='close close-x-image']"))
					.getCssValue("width");
			generateReport(width.equals("17px"), "LLR Close button width property is 17px",
					"LLR Close button width property is not 17px, its " + width);
			height = getDriver().findElement(llrDivSpan).getCssValue("height");
			generateReport(height.equals("18px"), "LLR Close button height property is 18px",
					"LLR Close button height property is not 18px, its " + height);
		} else {
			if (buttonClass.equals("close close-text")) {
				generateReport(top.equals("9px"), "LLR Close button top property is 9px",
						"LLR Close button top property is not 9px, its " + top);
				String colorRGBACode = getDriver().findElement(llrDivSpan).getCssValue("color");
				String color = getRGBAColorCodeInHexCode(colorRGBACode);
				generateReport(color.equals("#1a7abf"), "LLR Close button color property is #1a7abf",
						"LLR Close button color property is not #1a7abf, its " + color);
				String fontsize = getDriver().findElement(llrDivSpan).getCssValue("font-size");
				generateReport(fontsize.equals("12px"), "LLR Close button fontsize property is 12px",
						"LLR Close button fontsize property is not 12px, its " + fontsize);
				String textTransform = getDriver().findElement(llrDivSpan).getCssValue("text-transform");
				generateReport(textTransform.equals("uppercase"),
						"LLR Close button text-transform property is uppercase",
						"LLR Close button text-transform property is not uppercase, its " + textTransform);
			}
		}

		// Verification of Adlable Information from industry changes
		position = getDriver().findElement(llrDivifi).getCssValue("position");
		generateReport(position.equals("absolute"), "LLR adlable position property is absolute",
				"LLR adlable position property is not absolute, its " + position);
		top = getDriver().findElement(llrDivifi).getCssValue("top");
		generateReport(top.equals("8px"), "LLR adlable top property is 8px",
				"LLR adlable top property is not 8px, its " + position);
		left = getDriver().findElement(llrDivifi).getCssValue("left");
		generateReport(left.equals("120px"), "LLR adlable left property is 120px",
				"LLR adlable left property is not 120px, its " + position);
		String fontsize = getDriver().findElement(llrDivifi).getCssValue("font-size");
		generateReport(fontsize.equals("12px"), "LLR adlable fontsize property is 12px",
				"LLR adlable fontsize property is not 12px, its " + fontsize);
		String colorRGBACode = getDriver().findElement(llrDivifi).getCssValue("color");
		String color = getRGBAColorCodeInHexCode(colorRGBACode);
		generateReport(color.equals("#767674"), "LLR adlable color property is #767674",
				"LLR adlable color property is not #767674, its " + color);
	}

	@DataProvider
	public String[] medscapeurls() {
		return new String[] { "https://www.medscape.com/viewarticle/895054"/*
																			 * ,"https://www.qa01.medscape.com/viewarticle/894987",
																			 * "https://www.qa01.medscape.com/viewarticle/891310"
																			 */ };
	}
}
