package previousScripts;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.relevantcodes.extentreports.LogStatus;
import com.webmd.common.AdsCommon;
import com.webmd.general.common.ExtentTestManager;
import com.webmd.general.common.XlRead;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/*
 * PPE-152793: Update Proclivity mobile ad call for lazy loaded 1122's & prioritization of page loaded ads
 * This user story will enable proclivity ad call for lazy load ads on mobile
 */
public class UpdateProclivityMobileAdCallForLazyLoads extends AdsCommon{ 
	private JavascriptExecutor jse;
	String proclivityCall = "medscape.com/adpredictionservice/transform";

	static String adExpected = "1122";

	static String content, expectedSizes;
	static String [] pValues, pSizes = null;
	static Long count, height; 
	static boolean sizeMatched = false;
	static String actualAdSize;

	private boolean scroll(String adExpected){
		getServer().newHar();
		try{
			count = height/300;

			for(int i = 0; i<count;i++){
				height = height-300;
				jse.executeScript("window.scrollBy(0,300)");
				System.out.println("Scroll "+i);
				Thread.sleep(300);
				if(verifySpecificCallPresence("securepubads.g.doubleclick.net/gampad/ads?"))
					if(verifySpecificAdPresenceInSecurePubadCall(adExpected)){
						expectedSizes = getSizesForSpecificPositionFromAdCall("1122");
						return true;
					}			
			}
			System.out.println("Scrolled till end");
		}catch(Exception e){
			System.out.println("Problem in Scrolling the page");
			e.printStackTrace();
			ExtentTestManager.getTest().log(LogStatus.INFO, "Problem in Scrolling the page");
			return false;
		}
		return false;
	}


	/*
	 * 6.Verify whether this change is only for Mobile break point or not (To make sure no effect for lazy loads of desktop)
	 */
	@Test
	public void verifyDesktopForProclivityAdCall(){
		String URL = "https://emedicine.staging.medscape.com/article/320061-overview";
		getDriver().manage().window().maximize();
		getDriver().get(URL);

		jse = (JavascriptExecutor)driver;
		height = (Long)jse.executeScript("return document.body.scrollHeight");



		if(!scroll(adExpected)){
			ExtentTestManager.getTest().log(LogStatus.PASS, "Proclivity Ad Call not loaded for Desktop Break point, hence pass :"+URL);
		}else
			ExtentTestManager.getTest().log(LogStatus.FAIL, "Proclivity Ad Call loaded for desktop Breakpoint, hence fail :"+URL);

	}


	public String getDataFromProclivityAPICall(){
		String content = null;
		har= getServer().getHar();
		List<HarEntry> entries = har.getLog().getEntries();
		for (HarEntry entry : entries) {
			System.out.println(entry.getRequest().getUrl());
			if(entry.getRequest().getUrl().contains(proclivityCall)) {
				List<HarNameValuePair> queryParams = entry.getRequest().getQueryString();
				for (HarNameValuePair param : queryParams) {
					if(param.getName().contains("q")){//provide the key name
						content = param.getValue();
					}
				}
			}
		}
		return content;
	}

	/*
	 * In this method, checking of actual size vs size in priorities
	 */
	@Test 
	public void verifyPrioritiesInAPICall(){

		/*getDriver().get(URL);
		getServer().newHar();
		 */
		content = getResponseForSpecificCall(proclivityCall);
		String s = StringUtils.substringBetween(content, "pbr\":[", "]");
		s = "["+s+"]";
		/*s = "[{\"p\":1,\"w\":300,\"h\":600,\"a\":\"117a1ce7-636d-4443-ba1c-e7636d04432f\",\"pds\":\"33302391_300x600\","
				+ "\"pdp\":\"33302391_1150.00\",\"pdi\":33302391},{\"p\":2,\"w\":300,\"h\":250,\"a\":\"77dacbd9-3e3f-4d99-9acb-d93e3f1d9972\",\"pds\":\"33302391_300x250\",\"pdp\":"
				+ "\"33302391_1150.00\",\"pdi\":33302391},{\"p\":3,\"w\":728,\"h\":90,\"a\":\"12e3d816-3fd4-49ff-a3d8-163fd429ff59\",\"pds\":\"33302391_728x90\",\"pdp\":\"33302391_1150.00\",\"pdi\":33302391}]";
		 */
		int pbrLength;
		try {
			JSONArray pbrArray = new JSONArray(s);
			pbrLength = pbrArray.length();

			HashMap<String, String> pbrPriority = new HashMap<>();

			if(pbrLength!=0 && actualAdSize != null){

				for(int i =0; i < pbrLength; i++){

					String size = pbrArray.getJSONObject(i).get("w").toString()+"x"+pbrArray.getJSONObject(i).get("h").toString();
					String priority = pbrArray.getJSONObject(i).get("p").toString();
					pbrPriority.put(priority, size);
				}

				try{
					if(expectedSizes.contains(pbrPriority.get("1"))){
						Assert.assertTrue(actualAdSize.equals(pbrPriority.get("1")));
						ExtentTestManager.getTest().log(LogStatus.PASS, "Actual Ad size matched with P1 size");
					}else if(expectedSizes.contains(pbrPriority.get("2"))){
						Assert.assertTrue(actualAdSize.equals(pbrPriority.get("2")));
						ExtentTestManager.getTest().log(LogStatus.PASS, "Actual Ad size matched with P2 size");
					}else{
						Assert.assertTrue(actualAdSize.equals(pbrPriority.get("3")));
						ExtentTestManager.getTest().log(LogStatus.PASS, "Actual Ad size matched with P3 size");
					}
				}catch(AssertionError e){
					ExtentTestManager.getTest().log(LogStatus.FAIL, "Size loaded wrong");
				}catch(Exception e){
					try{
						Assert.assertTrue(expectedSizes.contains(actualAdSize));
						ExtentTestManager.getTest().log(LogStatus.PASS, "Priorities have no matched size, hence ad loaded with"
								+ "one of expected size :");
					}catch(AssertionError assertionError){
						ExtentTestManager.getTest().log(LogStatus.FAIL, "Size loaded wrong");
					}
				}
			}else if(pbrLength==0)
				ExtentTestManager.getTest().log(LogStatus.SKIP, "No PBR values loaded in the response");
			else
				ExtentTestManager.getTest().log(LogStatus.FAIL, "Actual size loaded NULL");

		} catch (JSONException e1) {
			e1.printStackTrace();
			sizeMatched = false;
		}

	}

	public String getCurrentAdsize(){
		List<WebElement> list = getDriver().findElements(By.xpath("//div[contains(@id,'ads-pos-1122')]"));
		WebElement actual = null;
		try{
			System.out.println("number of Ads: "+list.size());
			for(int i = 0; i< list.size(); i++){
				if(isVisibleInViewport(list.get(i))){
					System.out.println("Actual size noted");
					actual = list.get(i);
					System.out.println("Actual element noted");
					String w = actual.getAttribute("width");
					String h = actual.getAttribute("height");
					String size = w+"x"+h;
					return size;
				}
			}
		}catch(Exception e){
			ExtentTestManager.getTest().log(LogStatus.SKIP, "1122 not in view port");
		}
		return null;
	}

	/*
	 * Verify whether data loaded properly in lazy load Proclivity API call or not
	 * 
	 */

	@Test
	public void verifyAPICallData(){
		/*ExtentTestManager.getTest().log(LogStatus.INFO, "Test URL :");
		String content;
		getDriver().get("https://www.medscape.com/viewarticle/763530");
		getServer().newHar();
		//getDriver().findElement(By.xpath("//a[contains(text(),'Next')]")).click();
		 */

		content = getDataFromProclivityAPICall();
		System.out.println(content);

		compareUserSegvars(content);
		comparePageSegvars(content);

		//validate user details
		//validate ASID value
		//existance of PBR value and sizes
	}

	public boolean compareUserSegvars(String content){
		String pageSource = getDriver().getPageSource();
		try {
			JSONObject json = new JSONObject(content);
			JSONObject dfpData = new JSONObject(json.getString("dfpData"));
			JSONObject userSegvars = new JSONObject(dfpData.getString("userSegVars"));
			String dt = userSegvars.getString("dt");
			dt = "\"dt\":\""+dt;
			Assert.assertTrue(pageSource.contains(dt));

			String usp = userSegvars.getString("usp");
			usp = "\"usp\":\""+usp;
			Assert.assertTrue(pageSource.contains(usp));

			String pf = userSegvars.getString("pf");
			pf = "\"pf\":\""+pf;
			Assert.assertTrue(pageSource.contains(pf));
			ExtentTestManager.getTest().log(LogStatus.PASS, "UserSegvars matched with page source");
			return true;
		} catch (JSONException e) {
			e.printStackTrace();
			ExtentTestManager.getTest().log(LogStatus.SKIP, "JSON parsing error");
		}catch(AssertionError e){
			ExtentTestManager.getTest().log(LogStatus.FAIL, "Assertion error while comparing User segvars from API call with"
					+ "Page source");
		}
		return false;
	}

	public boolean comparePageSegvars(String content){
		String pageSource = getDriver().getPageSource();
		try {
			JSONObject json = new JSONObject(content);
			JSONObject dfpData = new JSONObject(json.getString("dfpData"));
			JSONObject pageSegvars = new JSONObject(dfpData.getString("pageSegVars"));

			String ssp = pageSegvars.getString("ssp");
			ssp = "\"ssp\":\""+ssp;
			Assert.assertTrue(pageSource.contains(ssp));

			String art = pageSegvars.getString("art");
			art = "\"art\":\""+art;
			Assert.assertTrue(pageSource.contains(art));

			String cg = pageSegvars.getString("cg");
			cg = "\"cg\":\""+cg;
			Assert.assertTrue(pageSource.contains(cg));

			String scg = pageSegvars.getString("scg");
			scg = "\"scg\":\""+scg;
			Assert.assertTrue(pageSource.contains(scg));

			String pub = pageSegvars.getString("pub");
			pub = "\"pub\":\""+pub;
			Assert.assertTrue(pageSource.contains(pub));

			ExtentTestManager.getTest().log(LogStatus.PASS, "PageSegvars matched with page source");

			return true;
		} catch (JSONException e) {
			e.printStackTrace();
			ExtentTestManager.getTest().log(LogStatus.SKIP, "JSON parsing error while comparing pageSegvars");
		}catch(AssertionError e){
			ExtentTestManager.getTest().log(LogStatus.FAIL, "Assertion error while comparing Page segvars from API call with"
					+ "Page source");
		}
		return false;
	}


	/*
	 * This is the main and only test case, below are the tests covered 
	 * 1. Verify whether Proclivity API call made for 1122 lazy load ad
	 * 2. Verify whether priorities loaded in API call
	 * 3. Verify whether creative loaded as per the size in the priorities, if no size in priority matched with available size ignore priority
	 * 4. Verify whether segvars loaded in API call matched with page source data or not
	 */
	@Test
	public void verifyProclivityAPICall(){

		String URL = "https://emedicine.qa01.medscape.com/article/1999835-overview";
		ExtentTestManager.getTest().log(LogStatus.INFO, "Test URL :"+URL);
		getDriver().get(URL);

		int iteration = 0;

		jse = (JavascriptExecutor)getDriver();
		height = (Long)jse.executeScript("return document.body.scrollHeight");

		while(height > 300){

			if(scroll(adExpected)){
				if(verifySpecificCallPresence(proclivityCall)){
					ExtentTestManager.getTest().log(LogStatus.PASS, "Proclivity API Call tracked for :"+URL);

					actualAdSize = getCurrentAdsize();

					verifyAPICallData();

					verifyPrioritiesInAPICall();
				}else
					ExtentTestManager.getTest().log(LogStatus.FAIL, "Proclivity API Call not tracked for :"+URL);
			}else
				ExtentTestManager.getTest().log(LogStatus.SKIP, "1122 Ad not loaded");


			//Making size matched as false for next Ad verification
			sizeMatched= false;
			iteration++;
		}
	}

	/*
	 * This method will return the number of entries in the har for particular call
	 */
	private int getCountOfCalls(String call){
		int count = 0;

		har= getServer().getHar();
		List<HarEntry> entries = har.getLog().getEntries();
		for (HarEntry entry : entries) {
			System.out.println(entry.getRequest().getUrl());
			if(entry.getRequest().getUrl().contains(call))
				count ++;
		}
		return count;
	}

	/*
	 * PPE-158212: Mobile> Verify whether proclivity API call made when 1122 lazy load ad flight on page
	 * This method is to match the count of "transform" calls and "ad" calls
	 */
	@Test(dataProvider = "dataProvider")
	public void countTransformCalls(String URL){
		generateInfoReport("Matching the count of Ad URLs and Transform URLs");
		//getDriver("1");
		login("proclivitytest@gmail.com","medscape");
		getDriver().get(URL);

		int actualCount, expectedCount;

		jse = (JavascriptExecutor)getDriver();
		height = (Long)jse.executeScript("return document.body.scrollHeight");

		getServer().newHar();

		count = height/300;

		for(int i = 0; i<count;i++){
			height = height-300;
			jse.executeScript("window.scrollBy(0,300)");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		expectedCount = getCountOfCalls("securepubads.g.doubleclick.net/gampad/ads?");
		actualCount = getCountOfCalls(proclivityCall);

		if(expectedCount==0 && actualCount == 0)
			generateSkipReport("No Transform/Ad calls observed in the page");
		else
			try{
				Assert.assertEquals(actualCount, expectedCount);
				generatePassReport("Transform calls count "+actualCount +"matched with Ads call count: "+expectedCount);
			}catch(AssertionError e){
				generateFailReport("Transform calls count not matched with Ads call count");
			}
	}

	@DataProvider
	public  String[][] dataProvider() {
		return XlRead.fetchDataExcludingFirstRow("TestData/ProclivityTestData.xls", "Sheet1");
		/*return new String[][]  {
			{"https://www.medscape.com/cardiology"}, 
			{"https://www.medscape.com/pediatrics"}
			};*/
	}

}
