package com.webmd.ads;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;

/**
 * 
 *  @author tnamburi
 * Adobe AAM - Pass Segment to DFP - Medscape
 * 	1. Pass the segment values from the AAM cookie to the DFP key value pair DMP
	2. Multiple values should be comma deliminated. Note: commas will be encoded in the ad call
	3. We should not impact the Lotame values being passed to DMP
	
	PPE-221535: AAM - Pass Data to New Context Variables - Prof
 
 * Pass the following data points to their defined context variables for AAM.  Note:  They do not need to be included for the analytics call. 

NPI - pass to wb.npi
Target List - pass to wb.tar
Tactic - pass to wb.tc
All Concepts - pass to wb.allcncpt
All Specialties - pass to wb.allspclty
 *
 */

@Listeners(com.webmd.general.common.Listener.class)
public class PassAdobeAAMSegmentToDFP extends AdsCommon{
	private JavascriptExecutor jse;
	private static String[] keys = {"npi", "tar", "tc", "allconcpt", "allspclty"};
	private HashMap<String, String> expectedContextVariables = new HashMap(), actualContextVariables =new HashMap();

	//Method to get console value for Lottame
	public String getValuesOfLocalStorageFromConsole() {
		try{
			System.out.println("From LocalStorage class");
			this.jse = (JavascriptExecutor) getDriver();
			String ldccAUD = jse.executeScript("return window.localStorage.getItem('ldcc_aud')").toString();
			generateInfoReport("LDCC_AUD from Local storage is "+ldccAUD);
			return ldccAUD;
		}catch(NullPointerException e){
			generateInfoReport("Key was not there in console");
			return null;
		}
	}

	//Method to check Values with in cust_params and cookie value
	private void validateDMP(String cust_params){
		try{
			cust_params.isEmpty();

			String dmp = StringUtils.substringBetween(cust_params, "dmp=", "&");
			String ldcc_AUD = getValuesOfLocalStorageFromConsole();
			String aam = getDriver().manage().getCookieNamed("AAM").getValue();
			try{
				Assert.assertFalse(dmp.isEmpty());
				try{
					ldcc_AUD.isEmpty();
					generateInfoReport("Validating for AUD value in DMP");
					ldcc_AUD = StringUtils.substringAfter(ldcc_AUD, "segvarl_a");
					String[] ldccValues = ldcc_AUD.split("xl_");
					boolean flag = true;
					for(String ldcc : ldccValues){
						ldcc = ldcc.replace("x", "");
						if(!ldcc.equals("all"))
							ldcc = ldcc.replace("a", "");
						try{
							Assert.assertTrue(dmp.contains(ldcc));
						}catch(AssertionError e){
							flag = false;
							generateFailReport(ldcc+" value is not there in DMP: "+dmp);
						}
					}
					if(flag)
						generatePassReportWithNoScreenShot("DMP contains Lotame values");
				}catch(NullPointerException e){
					generateBoldReport("Lotame value is empty");
				}

				try{
					aam.isEmpty();
					aam = aam.replace("AAM%3D", "");
					Assert.assertTrue(dmp.contains(aam));
					generatePassReportWithNoScreenShot("DMP contains aam cookie value");
					try{
						Assert.assertFalse(dmp.contains("AAM%3D"));
						generatePassReportWithNoScreenShot("Text AAM removed from dmp value");
					}catch(AssertionError e){
						generateFailReport("DMP loaded with text AAM"+dmp);
					}
				}catch(AssertionError e){
					generateFailReport("DMP not contains aam cookie value, DMP: "+dmp+", AAM: "+aam);
				}catch(NullPointerException e){
					generateFailReport("AAM is NULL");
				}
			}catch(NullPointerException e){
				generateFailReport("DMP is NULL");
			}catch(AssertionError e){
				generateFailReport("DMP is NULL");
			}

		}catch(NullPointerException e){
			generateBoldReport("Cust_params is null");
		}
	}

	//Test for Anon user
	@Test(enabled = true, dataProvider = "dataProvider",priority = 0, groups={"aamCookie"})
	public void validateForAnonUser(String type, String URL){
		getDriver();
		getServer().newHar();
		generateInfoReport("Validating page type "+type);
		getURL(URL+"?faf=1");
		if(is404(getDriver().getTitle())){
			validateDMP(getSpecificKeyFromSecurePubadCall("cust_params"));
			
			generateInfoReport("Validating SSL call for AAM values for page of type "+type);
			validateSSLCallForAAMValues();
		}else
			generateSkipReport("404 displayed");
	}



	@DataProvider
	public  String[][] dataProvider() {
		return XlRead.fetchDataExcludingFirstRow("TestData/iuPartsTest.xls", "QA01");
	}
	//Method to access all the domains to make sure cookie value set in all the domains
	private void accessAllDomains(){
		generateInfoReport("Accessing ALl domains to make sure cookie inserted");
		String URLs[] = {"https://www.env.medscape.com", 
				"https://reference.env.medscape.com/",
				"https://www.env.medscape.org/",
				"https://www.env.medscape.com/academy/business",
				"https://www.env.medscape.com/consult",
				"https://www.env.medscape.com/video",
		"https://www.webmd.com/"};
		for(String URL: URLs)
			geturl(URL.replace("env.", env));
	}

	//@BeforeClass
	public void before(){
		accessAllDomains();
	}

	//Test for logged in user
	@Test(enabled = true, dataProvider = "dataProvider", priority = 1, groups={"aamCookie"})
	public void validateLoggedInUser(String type, String URL){ //String type, String URL
		login();
		getServer().newHar();
		generateInfoReport("Validating page type : "+type);
		geturl(URL);
		waitForAdCallFound();
		if(is404(getDriver().getTitle())){
			generateSkipReport("Page Not found");
		}else{
			validateDMP(getSpecificKeyFromSecurePubadCall("cust_params"));
			
			generateInfoReport("Validating SSL call for AAM values for page of type "+type);
			validateSSLCallForAAMValues();

			if(scrollTillNextLazyLoadFound()){
				generateInfoReport("Validating for Lazyload ad call");
				validateDMP(getSpecificKeyFromSecurePubadCall("cust_params"));
				
				generateInfoReport("Validating SSL call for AAM values");
				validateSSLCallForAAMValues();
			}
			if(isMediaNetRefreshHappened()){
				generateInfoReport("Validating Media net refresh Ad call");
				validateDMP(getSpecificKeyFromSecurePubadCall("cust_params"));
				
				generateInfoReport("Validating SSL call for AAM values");
				validateSSLCallForAAMValues();
			}
			if(clickNextButton()){
				generateInfoReport("Validating Next Event");
				validateDMP(getSpecificKeyFromSecurePubadCall("cust_params"));
				
				generateInfoReport("Validating SSL call for AAM values");
				validateSSLCallForAAMValues();
			}
		}
	}

	private void geturl(String url){
		getDriver().get(url);
		waitForPageLoaded();
	}

	private void prepareExpectedMap(){
		for(String key : keys){
			try{
				String value = null;
				switch(key){
				case "npi": value = getConsoleValue("DFPTargetKeys.userSegVars.dt");
				generateInfoReport("NPI value is "+value);
				break;

				case "tar"	: value =  getConsoleValue("DFPTargetKeys.userSegVars.tar");
				generateInfoReport("Tar value is "+value);
				break;

				case "tc" :  value = getConsoleValue("DFPTargetKeys.userSegVars.tc");
				generateInfoReport("TC value is "+value);
				break;

				case "allconcpt" :  value = getConsoleValue("DFPTargetKeys.pageSegVars.ac");
				generateInfoReport("All concepts value is "+value);
				break;

				case "allspclty" :  value = getConsoleValue("DFPTargetKeys.pageSegVars.as");
				generateInfoReport("All speciality value is "+value);
				break;
				}
				//value.isEmpty();
				if(value.equals(""))
					expectedContextVariables.put(key, "0");
				else
					expectedContextVariables.put(key, value);
			}catch(Exception e){
				expectedContextVariables.put(key, "0");
			}
		}
	}

	private boolean prepareActualMap(){
		boolean flag = false;
		Har har = getServer().getHar();
		List<HarEntry> entries = har.getLog().getEntries();
		for (HarEntry entry : entries) {
			if(flag)
				break;
			if (entry.getRequest().getUrl().contains("ssl.o.webmd.com")) {
				generateInfoReport("Identified ssl call");
				List<HarNameValuePair> queryParams = entry.getRequest().getQueryString();
				for (HarNameValuePair harNameValuePair : queryParams) {
					//generateInfoReport(harNameValuePair.getName()+" : "+harNameValuePair.getValue());
					switch(harNameValuePair.getName()){
					case "npi" : actualContextVariables.put("npi", harNameValuePair.getValue());
					flag = true;
					break;

					case "tar" : actualContextVariables.put("tar", harNameValuePair.getValue());
					flag = true;
					break;

					case "tc" : actualContextVariables.put("tc", harNameValuePair.getValue());
					flag = true;
					break;

					case "allcncpt" : actualContextVariables.put("allconcpt", harNameValuePair.getValue());
					flag = true;
					break;

					case "allspclty" : actualContextVariables.put("allspclty", harNameValuePair.getValue());
					flag = true;
					break;
					}
				}
			}
		}
		if(!flag)
			return false;
		else
			return true;
	}

	private void validateSSLCallForAAMValues(){
		prepareExpectedMap();
		if(prepareActualMap()){
			for(String key: keys){
				try{
					Assert.assertTrue(actualContextVariables.get(key).equals(expectedContextVariables.get(key)));
					generatePassReportWithNoScreenShot(key+" has matched values");
				}catch(AssertionError e){
					generateFailReport("Expecting value for "+key+" is "+expectedContextVariables.get(key)+" But actual value is "+actualContextVariables.get(key));
				}catch(Exception e){
					generateFailReport("Exception while validating "+key+"  "+e.toString());
				}
			}
		}else
			generateSkipReport("SSL call is not reported");
	}
}
