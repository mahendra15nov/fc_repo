package com.webmd.ads.regression;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.Base;


@Listeners(com.webmd.general.common.Listener.class)
public class TopLeaderBoardTest extends AdsCommon{
	static int count = 0;
	
	private JavascriptExecutor jse;
	
	private void scroll(JavascriptExecutor jse){
		try{
			System.out.println("Scrolling");
			jse.executeScript("window.scrollBy(0, 500)");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
	@Test//(dataProvider = "")
	public void test() throws InterruptedException{	
		jse = (JavascriptExecutor)getDriver();
		getServer().newHar();
		String URL ="https://www.medscape.com/viewarticle/901378?faf=1";
		String script = "window.location = \'"+URL+"\'";
		
		try{
			getDriver().manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
			getDriver().get(URL);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//jse.executeScript(script);
		
		do{
			System.out.println("No Ad call observed");
			Thread.sleep(500);
			scroll(jse);
		}while(!getDriver().findElement(By.id("adtagheader")).isDisplayed());
	}
}
