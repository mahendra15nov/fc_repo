package com.webmd.ads;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;

/**
 * @author sandeep.gowada
 * 
 *         PPE-220734: Add DFP Target Keys to Reference Q&A pages
 *
 */
public class VerifyDFPTKeysQAPages extends AdsCommon {

	private static final String SUBSTRING_START = "{\"reqHeaders\":";
	private static final String SUBSTRING_END = "; var userCampaign";

	@Test(dataProvider = "dataProvider")
	public void verifyKeysonQAPages(String url) {

		getDriver();
		login("infosession33", "medscape");
		getServer().newHar();
		getDriver().get(url);
		String questionPageSource = getDriver().getPageSource();
		String questionPageDFPTKeys = questionPageSource.substring(questionPageSource.indexOf(SUBSTRING_START),
				questionPageSource.indexOf(SUBSTRING_END));
		validateKeysOnQAPages(questionPageDFPTKeys);

	}

	private void validateKeysOnQAPages(String dfptKeys) {

		JSONObject jo = new JSONObject(dfptKeys);
		JSONObject webSegVarsTarget = jo.getJSONObject("webSegVars");
		JSONObject pageSegVarsTarget = jo.getJSONObject("pageSegVars");
		JSONObject userSegVarsTarget = jo.getJSONObject("userSegVars");
		JSONArray exclusionCategories = jo.getJSONArray("exclusionCategories");
		String exclCatQuestion = exclusionCategories.toString();
		List<WebElement> questionLinks = getDriver().findElements(By.xpath("//*[@id='content_qna-toc']//a"));
		if (questionLinks.size() > 5) {
			for (int i = 1; i <= 5; i++) {
				generateInfoReport("Clicking on the question link which redirects to an answer page");
				getDriver().findElements(By.xpath("//*[@id='content_qna-toc']//a")).get(i).click();
				waitForPageLoaded();
				String answerPagesource = getDriver().getPageSource();
				String answerPageDFPTKeys = answerPagesource.substring(answerPagesource.indexOf(SUBSTRING_START),
						answerPagesource.indexOf(SUBSTRING_END));
				JSONObject jsonObjectAnswer = new JSONObject(answerPageDFPTKeys);
				JSONObject webSegVarsAnswer = jsonObjectAnswer.getJSONObject("webSegVars");
				JSONObject pageSegVarsAnswer = jsonObjectAnswer.getJSONObject("pageSegVars");
				JSONObject userSegVarsAnswer = jsonObjectAnswer.getJSONObject("userSegVars");
				JSONArray exclusionCat = jsonObjectAnswer.getJSONArray("exclusionCategories");
				String exclCatAnswer = exclusionCat.toString();
				generateReport(exclCatQuestion.equals(exclCatAnswer),
						"The key 'exclusionCategories' is validated in Q&A Pages",
						"The key 'exclusionCategories' value is different in Q&A Pages");
				generateReport(userSegVarsTarget.getString("tc").equals(userSegVarsAnswer.getString("tc")),
						"The key 'tc' is validated in Q&A Pages", "The key 'tc' value is different in Q&A Pages");
				generateReport(userSegVarsTarget.getString("tar").equals(userSegVarsAnswer.getString("tar")),
						"The key 'tar' is validated in Q&A Pages", "The key 'tar' value is different in Q&A Pages");
				generateReport(webSegVarsTarget.getString("pc").equals(webSegVarsAnswer.getString("pc")),
						"The key 'pc' is validated in Q&A Pages", "The key 'pc' value is different in Q&A Pages");
				generateReport(pageSegVarsTarget.getString("ssp").equals(pageSegVarsAnswer.getString("ssp")),
						"The key 'ssp' is validated in Q&A Pages", "The key 'ssp' value is different in Q&A Pages");
				generateReport(pageSegVarsTarget.getString("ac").equals(pageSegVarsAnswer.getString("ac")),
						"The key 'ac' is validated in Q&A Pages", "The key 'ac' value is different in Q&A Pages");
				generateReport(pageSegVarsTarget.getString("acb").equals(pageSegVarsAnswer.getString("acb")),
						"The key 'acb' is validated in Q&A Pages", "The key 'acb' value is different in Q&A Pages");
				generateReport(pageSegVarsTarget.getString("art").equals(pageSegVarsAnswer.getString("art")),
						"The key 'art' is validated in Q&A Pages", "The key 'art' value is different in Q&A Pages");
				generateReport(pageSegVarsTarget.getString("as").equals(pageSegVarsAnswer.getString("as")),
						"The key 'as' is validated in Q&A Pages", "The key 'as' value is different in Q&A Pages");
				generateReport(pageSegVarsTarget.getString("cg").equals(pageSegVarsAnswer.getString("cg")),
						"The key 'cg' is validated in Q&A Pages", "The key 'cg' value is different in Q&A Pages");
				generateReport(pageSegVarsTarget.getString("scg").equals(pageSegVarsAnswer.getString("scg")),
						"The key 'scg' is validated in Q&A Pages", "The key 'scg' value is different in Q&A Pages");
				generateReport(pageSegVarsTarget.getString("ck").equals(pageSegVarsAnswer.getString("ck")),
						"The key 'ck' is validated in Q&A Pages", "The key 'ck' value is different in Q&A Pages");
				generateReport(pageSegVarsTarget.getString("pub").equals(pageSegVarsAnswer.getString("pub")),
						"The key 'pub' is validated in Q&A Pages", "The key 'pub' value is different in Q&A Pages");
				generateReport(pageSegVarsTarget.getString("ctype").equals(pageSegVarsAnswer.getString("ctype")),
						"The key 'ctype' is validated in Q&A Pages", "The key 'ctype' value is different in Q&A Pages");

				getDriver().navigate().back();
			}

		}

	}

	@DataProvider
	public String[][] dataProvider() {
		return new String[][] { { "https://emedicine.medscape.com/article/214100-questions-and-answers" } };
	}

}
