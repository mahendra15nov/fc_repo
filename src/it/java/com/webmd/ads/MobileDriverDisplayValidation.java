package com.webmd.ads;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;


public class MobileDriverDisplayValidation extends MobileDriverOptimization{

	/*
	 * Get the flag value
	 * if flag value is false or not set: both the ads should be text driver
	 * If flag is true 1420 should be media ad, no pin lines shown, advertisement label should shown at bottom center
	 * If flag is true 1520 loads media: no pin lines shown, advertisement label should shown at bottom center
	 * If flag is true and 1520 is text driver: pin lines should show and no advertisement label
	 */



	private void validateAdPositionOnPage(WebElement pos){
		String cssValue = null;
		try{
			scrollToWebElement(pos);
			cssValue = pos.getCssValue("text-align");
			Assert.assertTrue(cssValue.equals("center"));
			generatePassReportWithNoScreenShot("Advertisement shown at center of page: "+pos.getAttribute("id"));
		}catch(AssertionError e){
			generateFailReport("Ad not shown at the center of the page: "+pos.getAttribute("id")+
					"Value from CSS is "+cssValue);
		}catch(Exception e){
			e.printStackTrace();
			generateFailReport("Un know exception\n"+e.toString());
		}

	}

	private void verifyMarginBottom(WebElement pos){
		String marginBottom = pos.getCssValue("margin-bottom").split("px")[0];
		int margin = Integer.parseInt(marginBottom);

		try{
			Assert.assertTrue(margin > 10 && margin < 25);
			generatePassReportWithNoScreenShot("Margin is proper, value of bottom margin is: "+marginBottom);
		}catch(AssertionError e){
			generateFailReport("Margin is not proper, value of bottom margin is: "+marginBottom);
		}
	}



	@Test (dataProvider = "dataProvider")
	public void testPageFunctionality(String URL){
		getDriver().get(URL);
		boolean hasNextPage = false;
		do{
			WebElement pos1420 = null, pos1520 = null;
			boolean is1420Loaded, is1520Loaded;
			String actualAdLoaded = null;
			try{
				pos1420 = getDriver().findElement(By.xpath("//div[@id='ads-pos-1420']"));
				generateInfoReport("1420 loaded on page");
				is1420Loaded = true;
			}catch(NoSuchElementException e){
				is1420Loaded = false;
				generateInfoReport("1420 not loaded on page");
			}

			try{
				pos1520 = getDriver().findElement(By.xpath("//div[@id='ads-pos-1520']"));
				generateInfoReport("1520 loaded on page");
				is1520Loaded = true;
			}catch(NoSuchElementException e){
				is1520Loaded = false;
				generateInfoReport("1520 not loaded on page");
			}


			if(isArticle()&&isTextDriverOptimized()&&is1520Loaded){
				generateInfoReport("Validating 1520 position on an article page with Flag = TRUE");
				actualAdLoaded = validateAdPosOnPage(pos1520);
				try{
					Assert.assertEquals(actualAdLoaded, "mediaAd");
					generatePassReportWithNoScreenShot("media Ad loaded for 1520");
				}catch(AssertionError e){
					generateFailReport("Media Ad not shown in position of 1520, loaded "+actualAdLoaded);
				}
				verifyMarginBottom(pos1520);
			}else if(isArticle()&&!isTextDriverOptimized()&&is1520Loaded){
				generateInfoReport("Validating 1520 position on an article page with Flag = FALSE");
				actualAdLoaded = validateAdPosOnPage(pos1520);
				try{
					Assert.assertTrue(actualAdLoaded.contains("textAd")||actualAdLoaded.contains("mediaAd"));
					generatePassReportWithNoScreenShot("Text ad at 1520 when flag is false/not set");
				}catch(AssertionError e){
					generateInfoReport("Text ad not loaded for 1520 while flag is not set, loaded "+actualAdLoaded);
				}
				verifyMarginBottom(pos1520);
			}

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			if(isArticle()&&isTextDriverOptimized()&&is1420Loaded){
				generateInfoReport("Validating 1420 position on an article page with Flag = TRUE");
				actualAdLoaded = validateAdPosOnPage(pos1420);
				try{
					Assert.assertTrue(actualAdLoaded.equals("mediaAd")||actualAdLoaded.equals("textAd"));
					generatePassReportWithNoScreenShot("Ad loaded properly for position 1420, loaded "+actualAdLoaded);
				}catch(AssertionError e){
					generateFailReport("Ad not loaded properly for position 1420");
				}
				verifyMarginBottom(pos1420);
			}else if(isArticle()&&!isTextDriverOptimized()&&is1420Loaded){
				generateInfoReport("Validating 1420 position on an article page with Flag = FALSE");
				actualAdLoaded = validateAdPosOnPage(pos1420);
				try{
					Assert.assertEquals(actualAdLoaded, "textAd");
					generatePassReportWithNoScreenShot("Text ad at 1420 when flag is false/not set");
				}catch(AssertionError e){
					generateInfoReport("Text ad not loaded for 1420 while flag is not set, loaded "+actualAdLoaded);
				}
				verifyMarginBottom(pos1420);
			}
			if(!is1420Loaded && !is1520Loaded)
				generateSkipReport("Both 1420 and 1520 not loaded on the page");
			
			
			try{
				getDriver().findElement(By.xpath("//div[@id='next-section']/a")).click();
				generateInfoReport("Next page available, validating next page");
				hasNextPage  = true;
			}catch(Exception e){
				generateInfoReport("No Next page available");
				hasNextPage = false;
			}
		}while(hasNextPage);

	}



	@Test(dataProvider = "dataProviderNonArticles")
	public void validateNonArticlePages(String URL){
		getDriver().get(URL);
		WebElement pos1420 = null, pos1520 = null;
		boolean is1420Loaded, is1520Loaded, isArticle, isFlagEnabled;
		String actualAdLoaded = null;
		try{
			pos1420 = getDriver().findElement(By.xpath("//div[@id='ads-pos-1420']"));
			generateInfoReport("1420 loaded on page");
			is1420Loaded = true;
		}catch(NoSuchElementException e){
			is1420Loaded = false;
			generateInfoReport("1420 not loaded on page");
		}

		try{
			pos1520 = getDriver().findElement(By.xpath("//div[@id='ads-pos-1520']"));
			generateInfoReport("1520 loaded on page");
			is1520Loaded = true;
		}catch(NoSuchElementException e){
			is1520Loaded = false;
			generateInfoReport("1520 not loaded on page");
		}

		isArticle = isArticle();
		isFlagEnabled = isTextDriverOptimized();

		if(is1420Loaded&&!isArticle&&!isFlagEnabled){
			actualAdLoaded = validateAdPosOnPage(pos1420);
			try{
				Assert.assertEquals(actualAdLoaded, "textAd");
				generateInfoReport("Text Ad loaded for 1420 position");
			}catch(AssertionError e){
				generateFailReport("1420 loaded "+actualAdLoaded);
			}
		}
		if(is1520Loaded&&!isArticle&&!isFlagEnabled){
			actualAdLoaded = validateAdPosOnPage(pos1420);
			try{
				Assert.assertEquals(actualAdLoaded, "textAd");
				generateInfoReport("Text Ad loaded for 1520 position");
			}catch(AssertionError e){
				generateFailReport("1520 loaded "+actualAdLoaded);
			}
		}
	}
}
