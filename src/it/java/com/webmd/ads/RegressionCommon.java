package com.webmd.ads;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.jayway.restassured.response.Response;
import com.webmd.common.AdsCommon;

public class RegressionCommon extends AdsCommon{

	public void getSegvarsFromPageSource(){

	}

	void getKeysFromPROD(String URL){

	}
	/*
	 * This method will return the keys of the requested segvar	
	 */
	private List <String> getKeys(String segvar, String endPoint){
		Map <String, String> map = null;
		Response respoonse;

		String pageSource = getDriver().getPageSource();
		String DFPT = pageSource.substring(pageSource.indexOf("{\"reqHeaders\""), 
				pageSource.indexOf("; var userCampaign"));

		JSONObject DFPTKeys = new JSONObject(DFPT);
		JSONObject Segvar = DFPTKeys.getJSONObject("segvar");
		List <String> segVarKeys = new ArrayList <String>(Segvar.keySet());

		//JSONObject endPointJson = 

		return segVarKeys;
	}
/**
 * It will prepare the expected MAP by using the end point data
 * @param ProdURL
 * @return
 */
	public Map<String, String> prepareExpectedMAP(String ProdURL){
		Map <String, String> expectedMap = null;
		getDriver().get(ProdURL);
		List <String> reqHeaderKeys = null;// = getKeys("requestHeaders");

		for(String key : reqHeaderKeys){

		}

		return expectedMap;

	}

	public void getPageType(String URL){

	}

}
