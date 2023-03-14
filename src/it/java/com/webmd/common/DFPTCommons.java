package com.webmd.common;

import static com.jayway.restassured.RestAssured.given;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.Cookie;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.webmd.general.objectrepo.LoginPageObjects;

/**
 * Common functions fo DFPTC keys validation
 * 
 * @author amahendra
 * 
 *         Version V1.0 Udpated Date : Aug 2018 2019
 *
 */
public class DFPTCommons extends AdsCommon {

	protected static Properties segVarsprop = new Properties();
	protected static Properties configProp = new Properties();
	protected StringBuilder ut = new StringBuilder();
	protected static String summaryAd;
	protected static String tarValue = "";
	protected static String cidValue = "";
	protected static Set<String> localeValue = new HashSet<String>();
	protected static String vitValue = "";
	protected static String token = "";
	protected static Set<String> jspiter;
	protected static int DFPCount;
	protected static Iterator<String> iter;
	protected static Map<String, String> map = new LinkedHashMap<>();
	protected static LinkedHashMap<String, String> jspMap = new LinkedHashMap<>();
	protected static JSONObject obj, obj1, obj2, obj3, obj4;
	public static String url;
	protected static String guid = null;
	protected static HashMap<String, String> expectedReqHeadersMap = new HashMap<>();
	protected static HashMap<String, String> expectedWebSegVarsMap = new HashMap<>();
	protected static HashMap<String, String> expectedPageSegVarsMap = new HashMap<>();
	protected static HashMap<String, String> expectedUserSegVarsMap = new HashMap<>();
	static String art;
	protected boolean proclivityUser = false;
	protected static LinkedHashMap<String, String> proclivityData = new LinkedHashMap<>();

	public DFPTCommons() throws IOException {
		FileReader reader = null;
		FileReader readerConfig = null;
		try {
			reader = new FileReader("src/it/resources/segver.properties");
			readerConfig = new FileReader("src/it/resources/Config.properties");
		} catch (FileNotFoundException e1) {
		}
		try {
			segVarsprop.load(reader);
			configProp.load(readerConfig);
		} catch (IOException e) {
		} finally {
			if (reader != null)
				reader.close();
			if (readerConfig != null)
				readerConfig.close();
		}
	}

	/**
	 * This will return the page type based on URL
	 * 
	 * @param URL
	 *            : URL for the webpage
	 * @return : return the type of page
	 */
	public static String getPageType(String URL) {
		if (URL.contains("viewarticle") || URL.contains("verartigo") || URL.contains("verarticulo")
				|| URL.contains("voirarticle") || URL.contains("artikelansicht"))
			return "ViewArticle";
		else if (URL.contains("emedicine"))
			return "EmedicineArticle";
		else if (URL.contains("slideshow"))
			return "SlideshowArticle";
		else if (URL.contains("reference"))
			return "ReferenceArticle";
		else if (URL.contains("viewcollection") || URL.contains("vercolecao") || URL.contains("voircollection")
				|| URL.contains("uebersicht") || URL.contains("vercoleccion"))
			return "Collection";
		else if (URL.contains("viewpublication"))
			return "Publication";
		else if (URL.contains("index"))
			return "Index";
		else if (URL.contains("video"))
			return "VideoArticle";
		else if (URL.contains("article"))
			return "Article";
		else if (URL.split("/").length >= 2)
			return "Homepage";
		else
			return null;
	}

	/**
	 * IT will read the segvars from properties file for respective page.
	 * 
	 * @param Actualpage
	 *            :
	 * @return
	 */
	public static HashMap<String, List<String>> getSegvers(String Actualpage) {

		HashMap<String, List<String>> DFPTkeys = new HashMap<>();
		try {
			List<String> hp = Arrays.asList(segVarsprop.getProperty(Actualpage).split(";"));

			for (String seg : hp) {
				String t = seg;
				switch (t.substring(0, t.indexOf("="))) {
				case "ExpectedReqHeaders":
					DFPTkeys.put("reqHeaders",
							Arrays.asList(seg.substring(seg.indexOf("ExpectedReqHeaders=") + 19).split(",")));
					break;
				case "ExpectedWebSegVars":
					DFPTkeys.put("webSegVars",
							Arrays.asList(seg.substring(seg.indexOf("ExpectedWebSegVars=") + 19).split(",")));
					break;
				case "ExpectedPageSegVars":
					DFPTkeys.put("pageSegVars",
							Arrays.asList(seg.substring(seg.indexOf("ExpectedPageSegVars=") + 20).split(",")));
					break;
				case "ExpectedUserSegVars":
					DFPTkeys.put("userSegVars",
							Arrays.asList(seg.substring(seg.indexOf("ExpectedUserSegVars=") + 20).split(",")));
					break;
				case "ExpectedLazyLoad":
					DFPTkeys.put("lazyLoad",
							Arrays.asList(seg.substring(seg.indexOf("ExpectedLazyLoad=") + 17).split(",")));
					break;
				default:
					break;
				}
			}
		} catch (NullPointerException e) {

		}
		return DFPTkeys;
	}

	/**
	 * This will compare the DFPT keys with expected keys and actual keys
	 * Expected keys will be coming from PROD Actual keys will be coming from
	 * respective environment
	 * 
	 * @param source
	 * @param comp
	 * @return
	 */
	public static List<String> compareDFPTKeys(List<String> source, List<String> comp) {
		// Verifying the dt key available in UserSegvars or not.
		ArrayList<String> list = new ArrayList<>();
		for (String key : source) {
			if (!comp.contains(key))
				list.add(key);
		}
		return list;

	}

	/**
	 * Function will compare the actual value with expected value.
	 * 
	 * @param expected_map
	 * @param actual_map
	 * @return
	 */
	public String comparingActualAndExpectedValues(Map<String, String> expected_map, Map<String, Object> actual_map) {

		String report = "";// Following values has miss match for respective
		// keys:
		for (String key : actual_map.keySet()) {

			Object expected = expected_map.get(key);
			Object actual = actual_map.get(key);
			String a = "";
			String e = "";
			switch (key) {
			case "domainCategory":
			case "domain":
				a = actual.toString();
				e = StringUtils.substringBetween(getDriver().getCurrentUrl(), "://", ".");
				generateReport(a.equals(e), key + " has been verified successfully.",
						key + " --> expected value : " + e + ", actual value : " + a);
				break;
			case "requestEnv":
				a = actual.toString();
				e = env.replace(".", "");
				generateReport(a.equals(e), "requestEnv has been verified successfully.",
						"requestEnv --> expected value : " + e + ", actual value : " + a);
				break;
			case "device":
				a = actual.toString();
				if (breakPoint.equals("1"))
					e = "MOBILE";
				if (breakPoint.equals("4"))
					e = "PC";
				generateReport(a.equals(e), "device has been verified successfully.",
						"device --> expected value : " + e + ", actual value : " + a);
				break;
			case "enableDomain":
				generateWarningReport(key + " value has been skipped for now:: [TO-DO]");
				break;
			case "tar":
				generateWarningReport(key + " value has been skipped for now:: [TO-DO]");
			case "ssp":
				generateWarningReport(key + " value has been skipped for now:: [TO-DO]");
				break;
			default:
				report = valueVerification(expected, actual, key, report);
			}
		}
		if (!report.isEmpty())
			return report.substring(0, report.length() - 1);
		else
			return report;
	}

	private String valueVerification(Object expected, Object actual, String key, String report) {
		// compare the expected value from actual value
		if (expected != null && actual != null) {

			if (expected instanceof String) {
				String exp = URLDecoder.decode(expected.toString());
				if (key.equalsIgnoreCase("tc") || key.equalsIgnoreCase("tar") || key.equalsIgnoreCase("enableDomain")) {
					StringBuilder tcv = new StringBuilder();
					String[] act = actual.toString().split(",");
					String[] ext = URLDecoder.decode(expected.toString()).split(",");
					List<String> aact = Arrays.asList(ext);
					for (String tc : Arrays.asList(act)) {
						if (!aact.contains(tc)) {
							tcv.append(tc + ", ");
						}
					}
					String rs = "";
					if (tcv.length() != 0) {
						rs = tcv.substring(0, tcv.length() - 2);
						report = report + "Following " + key + " values are not found in expected map :" + rs + "#";
					}

				} else {
					if (!exp.equalsIgnoreCase(actual.toString())) {
						generateInfoReport("Expected value : " + exp + ", Actual value : " + actual.toString()
								+ " for key : " + key + " are not matching");
						report = report + "Expected value : " + exp + ", Actual value : " + actual.toString()
								+ " for key : " + key + " are not matching" + "#";
					}
				}
			} else {
				if (expected instanceof JSONArray) {
					if (proclivityUser) {
						// Code need to be implemented
					}
				} else {
					if (expected instanceof Boolean) {
						boolean expectedValueb = Boolean.parseBoolean(expected.toString());
						boolean actualValueb = Boolean.parseBoolean(actual.toString());
						if (expectedValueb != actualValueb) {
							generateInfoReport("Expected value : " + expectedValueb + ", Actual value : " + actualValueb
									+ " for key : " + key + " are not matching");
							report = report + "Expected value : " + expectedValueb + ", Actual value : " + actualValueb
									+ " for key : " + key + " are not matching" + "#";
						}
					}
				}
			}
		} else {
			String msg1 = null;
			String msg2 = null;
			if (expected != null) {
				msg1 = URLDecoder.decode(expected.toString());
			}
			if (actual != null) {
				msg2 = URLDecoder.decode(actual.toString());
			}
			generateInfoReport("Expected value : " + msg1 + ", Actual value : " + msg2 + " for key : " + key
					+ " are not matching");
			report = report + "Expected value : " + msg1 + ", Actual value : " + msg2 + " for key : " + key
					+ " are not matching" + "#";

		}
		return report;
	}
}
