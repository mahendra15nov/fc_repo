package com.webmd.ads;

import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.general.common.XlRead;

public class TestDFPTargetKeysHomePage extends RegressionCommon {

	List<String> TestList;
	String prodURL;
	Map<String, String> expectedMap, actualMap;

	@BeforeClass(groups = { "DFPTarketKeys" })
	public void prepareTest() {
		prodURL = "";
		expectedMap = prepareExpectedMAP(prodURL);
	}

	@Test(groups = { "DFPTarketKeys" })
	public void test(String URL) {

	}

	@DataProvider
	public String[][] dataProvider() {
		return XlRead.fetchDataExcludingFirstRow("???.xls", "HomePages");
	}

}
