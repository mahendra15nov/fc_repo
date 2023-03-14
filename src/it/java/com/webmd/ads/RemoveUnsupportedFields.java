package com.webmd.ads;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;

public class RemoveUnsupportedFields extends AdsCommon{
	
	@BeforeMethod()
	public void beforeMethod(){
		getDriver();
		login();
	}
	
	@AfterMethod()
	public void afterMethod(){
		getDriver().close();
	}
	
	@Test
	public void test(){
		
	}

}
