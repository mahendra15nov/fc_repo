package com.webmd.ads;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.json.JSONException;
import org.json.JSONObject;
import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;

import io.restassured.path.json.JsonPath;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Listeners(com.webmd.general.common.Listener.class)
public class SegversWriter extends AdsCommon {

	List<String> ExpectReqHeaders = new ArrayList<>();
	List<String> ExpectWebSegVars = new ArrayList<>();
	List<String> ExpectPageSegVars = new ArrayList<>();
	List<String> ExpectUserSegVars = new ArrayList<>();
	List<String> ExpectLazyLoad = new ArrayList<>();
	String pageName;
	String req = "", web = "", page = "", user = "", lazy = "";
	String values;
	String DFP;
	Properties prop;
	OutputStream output;
	List<String> pagesList;
	HashMap<String, HashMap<String, List<String>>> hm;
	String fileName = "AdsSanity.xls";
	// POS_SZS, Debug
	String sheetName = "POS_SZS";
	HashMap<String, HashMap<String, ArrayList<String>>> testData;

	@BeforeTest(groups = { "DFPTarketKeys" })
	public void login1() {

		login(getProperty("username"), getProperty("password"));

		prop = new Properties();

		FileReader readerCon = null;
		try {
			readerCon = new FileReader("src/it/resources/segver.properties");
		} catch (FileNotFoundException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		try {
			prop.load(readerCon);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		try {
			output = new FileOutputStream("src/it/resources/segver.properties");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		Properties p = new Properties();
		FileInputStream fs = null;
		try {
			fs = new FileInputStream("src/it/resources/Config.properties");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
			p.load(fs);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Read the test data
		testData = getTestData(fileName, sheetName);
	}

	@Test(dataProvider = "dataProvider", groups = { "DFPTarketKeys" })
	public void segversWriter(String url) throws IOException, InterruptedException {
		if (isAdCallExpectedInDesktop(testData.get(url).get("PageType").get(0))) {
			getURL(url);
			waitForAdCallFound();
			generateSegvers(storingProcess(url), testData.get(url).get("PageType").get(0));

			ExpectReqHeaders.clear();
			ExpectWebSegVars.clear();
			ExpectPageSegVars.clear();
			ExpectUserSegVars.clear();
			ExpectLazyLoad.clear();
			pageName = "";
			req = "";
			web = "";
			page = "";
			user = "";
			lazy = "";
			values = "";
			DFP = "";
		}
	}

	@AfterTest(groups = { "DFPTarketKeys" })
	public void storeValues() {
		try {
			prop.store(output, "");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Copying properties file from source to destination

		try {
			String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
			System.out.println(date);

			File dest = new File("src/it/resources/segvarsbackup/segver_" + date + ".properties");
			if (!dest.exists())
				dest.createNewFile();

			FileUtils.copyFile(new File("src/it/resources/segver.properties"), dest);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@DataProvider
	public String[] dataProvider() {
		return getURLs(fileName, sheetName);
	}

	public void storeSegvers(String pageName, String comment) {
		System.out.println("storing Segvers for" + pageName);
		prop.setProperty(pageName, "ExpectedReqHeaders=" + req + ";ExpectedWebSegVars=" + web + ";ExpectedPageSegVars="
				+ page + ";ExpectedUserSegVars=" + user + ";ExpectedLazyLoad=" + lazy);
		if (!comment.isEmpty())
			prop.setProperty("#" + pageName + "-Comment", comment);
	}

	public void generateSegvers(String DFP, String pagename) throws NullPointerException {
		System.out.println("generating Segvers for " + pagename);
		// Object obj = new JSONObject(DFP);
		JsonPath jo = new JsonPath(DFP);
		List<Object> reqHeaders = extractListFromJson(jo, "reqHeaders");
		List<Object> webSegVars = extractListFromJson(jo, "webSegVars");
		List<Object> pageSegVars = extractListFromJson(jo, "pageSegVars");
		List<Object> userSegVars = extractListFromJson(jo, "userSegVars");

		List<Object> lazyLoad = null;
		try {
			lazyLoad = extractListFromJson(jo, "lazyLoad");
		} catch (NullPointerException e) {
		}
		String[] list = null;
		try {
			list = reqHeaders.get(0).toString().replace("{", "").replace("}", "").split(", ");
		} catch (Exception e) {

		}
		if (list != null) {
			for (String s : list) {
				try {
					ExpectReqHeaders.add(s.substring(0, s.indexOf('=')));
				} catch (Exception e) {

				}
			}
		}
		try {
			list = webSegVars.get(0).toString().replace("{", "").replace("}", "").split(", ");
		} catch (Exception e) {

		}
		if (list != null) {
			for (String s : list) {
				try {
					ExpectWebSegVars.add(s.substring(0, s.indexOf('=')));
				} catch (Exception e) {

				}
			}
		}
		try {
			list = pageSegVars.get(0).toString().replace("{", "").replace("}", "").split(", ");
		} catch (Exception e) {

		}
		if (list != null) {
			for (String s : list) {
				try {
					ExpectPageSegVars.add(s.substring(0, s.indexOf('=')));
				} catch (Exception e) {

				}
			}
		}
		try {
			list = userSegVars.get(0).toString().replace("{", "").replace("}", "").split(", ");
		} catch (Exception e) {

		}
		if (list != null) {
			for (String s : list) {
				ExpectUserSegVars.add(s.substring(0, s.indexOf('=')));
			}
		}
		try {
			list = lazyLoad.get(0).toString().replace("{", "").replace("}", "").split(", ");
		} catch (Exception e) {

		}
		if (list != null) {
			for (String s : list) {
				try {
					ExpectLazyLoad.add(s.substring(0, s.indexOf('=')));
				} catch (Exception e) {

				}
			}
		}
		for (String s : ExpectReqHeaders) {
			req = req + s + ",";
		}
		if (!req.isEmpty())
			req = req.substring(0, req.length() - 1);

		for (String s : ExpectWebSegVars) {
			web = web + s + ",";
		}
		if (!web.isEmpty())
			web = web.substring(0, web.length() - 1);

		for (String s : ExpectUserSegVars) {
			user = user + s + ",";
		}
		if (!user.isEmpty())
			user = user.substring(0, user.length() - 1);

		for (String s : ExpectPageSegVars) {
			page = page + s + ",";
		}
		if (!page.isEmpty())
			page = page.substring(0, page.length() - 1);

		for (String s : ExpectLazyLoad) {
			lazy = lazy + s + ",";
		}
		if (!lazy.isEmpty())
			lazy = lazy.substring(0, lazy.length() - 1);

		System.out.println(DFP);
		String comment = "";
		String rs = "";

		if (!comment.isEmpty())
			System.out.println("There is some miss match in curent vs earlier segvars, " + comment);
		else
			System.out.println("There is no miss match in curent vs earlier segvars.");
		storeSegvers(pagename, comment);
	}

	public String validateDuplicateKeys(List<String> ActualHeaders, List<String> ExpectedHeaders) {
		String duplicate = "";
		System.out.println("Checking  actual headers having more than one variable with same name");
		Set<String> Duplicates = findDuplicates(ActualHeaders);
		if (!Duplicates.isEmpty()) {
			System.out.println("Duplicates in current segvars " + Duplicates);
			duplicate = "following keys are duplicate in current segvars : ";
			for (String string : Duplicates) {
				duplicate = duplicate + string + ", ";
			}
		}

		// Expected keys and Actual keys comparison
		String compareEA = "";
		System.out.println("Expected headers " + ExpectedHeaders + "\n");
		System.out.println("Comparing expected and actual");
		try {
			if (!ExpectedHeaders.equals(ActualHeaders)) {
				boolean b = false;
				String temp = "";
				System.out.println("Comparing expected and actual is unsuccessful");

				for (String Expected : ExpectedHeaders) {
					if (!ActualHeaders.contains(Expected)) {
						b = true;
						System.out.println(Expected + " Not available in Actual******");
						temp = temp + Expected + ", ";
					}

				}
				if (b)
					compareEA = "missing following keys in current segvars : " + temp;
			}
		} catch (NullPointerException e) {

		}
		// Actual keys and Expected keys comparison
		String compareAE = "";
		System.out.println("Actual headers" + ActualHeaders + "\n");
		System.out.println("Comparing actual and expected");
		try {
			if (!ActualHeaders.equals(ExpectedHeaders)) {
				boolean b = false;
				String temp = "";
				System.out.println("Comparing actual and expected is Unsuccessful");
				for (String Actual : ActualHeaders) {
					if (!ExpectedHeaders.contains(Actual)) {
						b = true;
						System.out.println(Actual + " Not available in Expected------");
						temp = temp + Actual + ", ";
					}
				}
				if (b)
					compareAE = "missing following keys in earlier segvars : " + temp;
			}
		} catch (NullPointerException e) {

		}
		String comment = "";
		if (!duplicate.isEmpty())
			comment = comment + duplicate + " ";
		if (!compareEA.isEmpty())
			comment = comment + compareEA + " ";
		if (!compareAE.isEmpty())
			comment = comment + compareAE;

		return comment;
	}

	public Set<String> findDuplicates(List<String> listContainingDuplicates) {
		final Set<String> setToReturn = new HashSet();
		final Set<String> set1 = new HashSet();

		for (String yourStr : listContainingDuplicates) {
			if (!set1.add(yourStr)) {
				setToReturn.add(yourStr);
			}
		}
		return setToReturn;
	}

	public String storingProcess(String URL) throws InterruptedException {
		System.out.println("Storing process" + URL);
		// getDriver().get(URL);
		// Thread.sleep(2000);
		values = getDriver().getPageSource();
		DFP = values.substring(values.indexOf("{\"reqHeaders\""), values.indexOf("; var userCampaign"));
		return DFP;
	}

}