package com.webmd.ads;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;

/*
 * PPE-163496: Add custom size to 122 to allow for flow thru of Drug Mono carousel units
 */

@Listeners(com.webmd.general.common.Listener.class)
public class AddCustomSizeTo122OfDrugMonograph extends AdsCommon{
	
	@BeforeMethod
	public void beforeMethod(){
		login();
		getServer().newHar();
	}
	
	/*
	 * PPE-164372: Desktop> Verify whether custom size 300x401 added to Ad call for 122 creative
	 */
	@Test(dataProvider = "dataProvider")
	public void verifyCustomSizeAdded(){
		
		getDriver().get("https://reference.medscape.com/drug/anascorp-antivenin-centruroides-scorpion-999676");
		
		String availableSizes = getSizesForSpecificPositionFromAdCall("122");
		
		if(availableSizes!=null){
			if(availableSizes.contains("300x401"))
				generatePassReport("carousel unit size added to reference Page");
			else
				generateFailReport("carousel unit size not added to reference Page");
			
		}else{
			generateInfoReport("122 not loaded in the page");
		}
	}
	
	@DataProvider
	public  String[][] dataProvider() {
		return XlRead.fetchDataExcludingFirstRow("TestData/AddCustomSizeTo122Ad.xls", "Positive");
			}

	/*
	 * 
	 */
	@Test(enabled = true, dataProvider = "dataProvider")
	public void verifyNoImpactOnMobile(){
		
		getDriver().get("https://reference.medscape.com/drug/anascorp-antivenin-centruroides-scorpion-999676");
		
		String availableSizes = getSpecificKeyFromSecurePubadCall("prev_iu_szs");
		
		if(availableSizes!=null){
			if(!availableSizes.contains("300x401"))
				generatePassReport("carousel unit size not added to reference Page on mobile device");
			else
				generateFailReport("carousel unit size added to reference Page on mobile device");
			
		}else{
			generateInfoReport("No Sizes loaded in the Ad call");
		}
	}
	
	@Test(dataProvider = "negativeTest")
	public void verifyNoImpactCustomSizeAdded(){
		
		//getDriver().get("https://www.medscape.com/viewarticle/881178");
		getDriver().get("https://reference.medscape.com/viewarticle/882078");
		
		String availableSizes = getSizesForSpecificPositionFromAdCall("122");
		
		if(availableSizes!=null){
			if(!availableSizes.contains("300x401"))
				generatePassReport("carousel unit size not added to non reference Page");
			else
				generateFailReport("carousel unit size added to non reference Page");
			
		}else{
			generateInfoReport("122 not loaded in the page");
		}
	}
	@DataProvider
	public  String[][] negativeTest() {
		return XlRead.fetchDataExcludingFirstRow("TestData/AddCustomSizeTo122Ad.xls", "NegativeTest");
			}


}
