package com.webmd.ads;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;

/*
 * PPE-184584: Disable Media.net Refresh in Staging Environment
 */

public class DisableMediaNetRefreshForStaging extends AdsCommon{

	@Test (dataProvider = "dataProvider", groups = {"testMediaNetRefresh"})
	public void testMedianetRefresh(String type, String URL){
		login();
		generateInfoReport("Validating Page of type: "+type+"\nURL"+URL);
		getDriver().get(URL);
		scrollTillEnd();
		if(env.contains("staging"))
			if(isMediaNetRefreshHappened())
				generateFailReport("Media net refresh observed"+getDriver().getCurrentUrl());
			else
				generatePassReport("No Media net refresh observed on ");
		else
			if(isMediaNetRefreshHappened())
				generatePassReport("No Media net refresh observed on ");
			else
				generateFailReport("Media net refresh observed"+getDriver().getCurrentUrl());


		if(clickNextButton()){
			generateInfoReport("Validating next page");
			scrollTillEnd();
			if(env.contains("staging")){
				if(isMediaNetRefreshHappened())
					generateFailReport("Media net refresh observed"+getDriver().getCurrentUrl());
				else
					generatePassReport("No Media net refresh observed on ");
			}else
				if(isMediaNetRefreshHappened())
					generatePassReport("Media net refresh observed");
				else
					generateFailReport("Media net refresh not observed on "+getDriver().getCurrentUrl());
		}
	}

	@DataProvider
	public String[][] dataProvider() {
		// return XlRead.fetchDataExcludingFirstRow("TestData/iuPartsTest.xls",
		// "Sheet2");
		if (env.contains("qa01"))
			return XlRead.fetchDataExcludingFirstRow("TestData/iuPartsTest.xls", "QA01");
		else if (env.contains("qa00"))
			return XlRead.fetchDataExcludingFirstRow("TestData/iuPartsTest.xls", "QA00");
		else if (env.contains("dev01"))
			return XlRead.fetchDataExcludingFirstRow("TestData/iuPartsTest.xls", "DEV01");
		else if (env.contains("staging"))
			return XlRead.fetchDataExcludingFirstRow("TestData/ArticleURLs.xls", "Staging");
		else
			return XlRead.fetchDataExcludingFirstRow("TestData/ArticleURLs.xls", "PROD-ArticlePages");

	}

}
