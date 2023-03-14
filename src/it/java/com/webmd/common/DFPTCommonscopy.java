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
import org.json.JSONArray;
import org.json.JSONObject;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class DFPTCommonscopy extends AdsCommon {

	protected static Properties segVarsprop = new Properties();
	protected static Properties configProp = new Properties();
	protected static StringBuffer ut = new StringBuffer();
	protected static String summaryAd;
	protected static String tarValue = "";
	protected static String cidValue = "";
	protected static Set<String> localeValue = new HashSet<String>();
	protected static String vitValue = "";
	protected static String token = "";
	protected static Set<String> jspiter;
	protected static int DFPCount;
	protected static Iterator iter;
	protected static Map<String, String> requesthEPoint = new LinkedHashMap<String, String>();
	protected static Map<String, String> pageSegvarsEPoint = new LinkedHashMap<String, String>();
	protected static Map<String, String> webSegvarsEPoint = new LinkedHashMap<String, String>();
	protected static Map<String, String> userSegvarsEPoint = new LinkedHashMap<String, String>();
	protected static Map<String, String> map = new LinkedHashMap<String, String>();
	protected static LinkedHashMap<String, String> jspMap = new LinkedHashMap<String, String>();
	protected static JSONObject obj, obj1, obj2, obj3, obj4;
	protected static String URL;
	protected static String guid = null;
	protected static HashMap<String, String> expected_reqHeadersMap = new HashMap<>();
	protected static HashMap<String, String> expected_webSegVarsMap = new HashMap<>();
	protected static HashMap<String, String> expected_pageSegVarsMap = new HashMap<>();
	protected static HashMap<String, String> expected_userSegVarsMap = new HashMap<>();
	// static CopyFilesFromLinux cp=new CopyFilesFromLinux();
	static String art;
	protected static boolean proclivityUser = false;
	protected static LinkedHashMap<String, String> proclivityData = new LinkedHashMap<>();

	public DFPTCommonscopy() {
		FileReader reader = null;
		FileReader readerConfig = null;
		try {
			reader = new FileReader("src/it/resources/segver.properties");
			readerConfig = new FileReader("src/it/resources/Config.properties");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			segVarsprop.load(reader);
			configProp.load(readerConfig);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * loadWebSegVars Method inserts the Key value pairs into expectMap Keys:
	 * env, envp, spon, auth, pc
	 */
	public static void loadwebSegVars(List<String> webSegVars) {
		System.out.println("*** web segvars keys abd values from end point **");
		iter = webSegVars.iterator();
		while (iter.hasNext()) {

			String key = (String) iter.next();
			String value = null;

			switch (key) {
			case "pc":
				if (jspMap.containsKey(key)) {
					value = jspMap.get(key);
				} else if (URL.contains("vercolecao") || URL.contains("verartigo") || URL.contains("viewarticle")
						|| URL.contains("viewcollection") || URL.contains("vercoleccion")
						|| URL.contains("voircollection") || URL.contains("vercolecao") || URL.contains("article")
						|| URL.contains("verarticulo") || URL.contains("vercoleccion") || URL.contains("artikelansicht")
						|| URL.contains("uebersicht") || URL.contains("voirarticle") || URL.contains("voircollection")
						|| (URL.contains("emedicine") && URL.contains("article"))
						|| (URL.contains("reference") && URL.contains("drug"))
						|| (URL.contains("reference") && URL.contains("slideshow"))) {
					value = "content";
				} else if (URL.split("/").length == 3 || URL.split("/").length == 2) {
					value = "hp";
				} else if (URL.contains("index")) {
					value = "indexpage";
				} else {
					value = "0";
				}
				break;
			case "auth":
				value = "1"; // Logged in users
				break;
			case "spon":
				value = map.get("spon");
				if (URL.contains("/index")) {
					Response response = null;
					try {
						String indexId = URL.substring(URL.indexOf('_') + 1, URL.lastIndexOf('_'));
						// System.out.println(indexId);
						response = given().get("http://bucketgenservice01-app-" + configProp.getProperty("env")
								+ "prf.iad1.medscape.com:8080/bucketgenservice/bucketgenservice/v1/getMetadata/id/"
								+ indexId + "").then().extract().response();
					} catch (Exception ee) {

					}
					try {
						String responseJSON = response.asString();
						JSONObject obj = new JSONObject(responseJSON);
						value = (String) obj.getJSONObject("metaDataProperties").get("rendering.overridetag");
					} catch (Exception e) {
						value = "0";
					}
				} else {
					try {
						for (String Key : jspMap.keySet()) {
							if (Key.equals("spon") || Key.equals("override") || Key.contains("overrride")) {
								value = jspMap.get(Key);
								break;
							}
						}

						if ((value.equals("0"))) {
							if (jspMap.containsKey("s_refpath")) {
								value = jspMap.get("s_refpath");
							} else if (jspMap.containsKey("refpath"))
								value = jspMap.get("refpath");
						}
					} catch (Exception e) {

					}
				}
				if (value == null) {
					value = "0";
				}
				break;
			case "env":
				String testenv = configProp.getProperty("env").replace(".", "");
				if (testenv.isEmpty())
					value = "0";
				else
					value = "1";
				break;
			case "envp":
				value = configProp.getProperty("env").replace(".", "");
				break;
			}
			// Insert key and value to the expectMap
			System.out.println("Key : " + key + " / value : " + value + "");
			webSegvarsEPoint.put(key, value);

		}

	}

	/*
	 * loadpageSegVars Method inserts the Key value pairs into expectMap Keys:
	 * ssp, art, ac, as, cg, scg, ck, pub
	 */
	public static void loadpageSegVars(List<String> pageSegVars) {
		System.out.println("***** Page Segvars keys and values from end point ***");

		iter = pageSegVars.iterator();

		if (URL.contains("viewarticle") || URL.contains("viewcollection") || URL.contains("vercoleccion")
				|| URL.contains("voircollection") || URL.contains("vercolecao") || URL.contains("viewpublication")) {
			Response response = null;

			int index = URL.lastIndexOf('/');
			String id = URL.substring(index + 1);
			if (id.contains("#")) {
				id = id.split("#", 2)[0];
			}
			if (id.contains("?")) {
				id = id.split("\\?", 2)[0];
			}
			if (id.contains("_")) {
				id = id.substring(0, id.indexOf('_'));
			}

			if (URL.contains("viewarticle")) {

				// calling contentmedataservice for viewarticle pages
				response = given()
						.get("http://contentmetadataservice-app-" + configProp.getProperty("env")
								+ "prf.iad1.medscape.com:8080/contentmetadataservice/getArticle?legacyId=" + id)
						.then().extract().response();
			} else if (URL.contains("viewcollection") || URL.contains("vercoleccion") || URL.contains("voircollection")
					|| URL.contains("vercolecao")) {
				// calling contentmedataservice for viewcollection pages
				response = given()
						.get("http://contentmetadataservice-app-" + configProp.getProperty("env")
								+ "prf.iad1.medscape.com:8080/contentmetadataservice/getCollection?legacyId=" + id)
						.then().extract().response();
			} else if (URL.contains("viewpublication")) {
				// calling contentmedataservice for viewpublication pages
				response = given()
						.get("http://contentmetadataservice-app-" + configProp.getProperty("env")
								+ "prf.iad1.medscape.com:8080/contentmetadataservice/getPublication?legacyId=" + id)
						.then().extract().response();
			}
			int code = response.statusCode();

			String responseJSON = response.asString();

			JsonPath res = new JsonPath(responseJSON);

			while (iter.hasNext()) {

				String key = (String) iter.next();
				String value = null;

				switch (key) {
				case "ssp":
					if (jspMap != null) {
						if (jspMap.containsKey(key)) {
							value = jspMap.get(key);
						} else {
							String ssp = response.path("leadTopicCenterID").toString();
							value = ssp;
						}
					}
					break;
				case "art":
					String art = response.path("legacyId").toString();
					value = art;
					break;
				case "ac":
					String allConcepts = response.path("allConcepts");
					if (allConcepts == null)
						value = "0";
					else
						value = allConcepts;
					break;
				case "as":
					String allSpecialties = response.path("allSpecialties");
					if (allSpecialties == null)
						value = "0";
					else
						value = allSpecialties;
					break;
				case "cg":
					String cg = response.path("contentGroupID").toString();
					value = cg;
					break;
				case "scg":
					String scg = response.path("leadConceptID").toString();
					value = scg;
					break;
				case "ck":
					value = "0";
					if (jspMap.containsKey(key)) {
						value = jspMap.get(key);
					} else {
						if (!URL.contains("viewarticle"))
							value = "cc-" + id;
					}
					break;
				case "pub":
					int pubID = response.path("publicationID");
					String pub = String.valueOf(pubID);
					value = pub;
					break;
				case "asb":
					value = "No end point";
					break;
				case "acb":
					value = "No end point";
					break;
				default:
					value = "No key and value";
				}
				System.out.println("key : " + key + " / Value : " + value);
				pageSegvarsEPoint.put(key, value);
			}
		} else if (URL.contains("emedicine") || (URL.contains("reference") && (!URL.contains("slideshow")))) {
			// Connect to File system and retrieve values
			Response response = null;
			String articleID = "";
			boolean exists;
			if (URL.contains("emedicine") && URL.contains("article")) {
				articleID = URL.substring(URL.lastIndexOf('/') + 1, URL.indexOf('-'));
				if (art != null) {
					articleID = art;
				}
				response = given().get("http://contentmetadataservice-app-" + configProp.getProperty("env")
						+ "prf.iad1.medscape.com:8080/contentmetadataservice/getReferenceArticle?legacyId=" + articleID)
						.then().extract().response();

			} else if (URL.contains("drug")) {

				if (!URL.contains("#")) {
					articleID = URL.substring(URL.lastIndexOf('-') + 1);
					// exists=cp.checkFileAndDownload(articleID,prop.getProperty("ENV"),"drug");
				} else {
					articleID = URL.substring(URL.lastIndexOf('-') + 1, URL.indexOf('#'));
					// exists=cp.checkFileAndDownload(articleID,prop.getProperty("ENV"),"drug");
				}
				if (art != null) {
					articleID = art;
				}

				response = given().get("http://contentmetadataservice-app-" + configProp.getProperty("env")
						+ "prf.iad1.medscape.com:8080/contentmetadataservice/getReferenceDrug?legacyId=" + articleID)
						.then().extract().response();
			}
			while (iter.hasNext()) {

				String key = (String) iter.next();
				String value = null;

				switch (key) {
				case "ssp":
					value = "0";
					if (jspMap.containsKey(key)) {
						value = jspMap.get(key);
					} else {
						if (response.path("ssp") != null)
							value = response.path("ssp").toString();
					}
					if (value == null)
						value = "0";
					/*
					 * if(!value.equals("0")){
					 * if(URL.contains("emedicine")&&URL.contains( "article")) {
					 * value="38"; } else if(URL.contains("drug")) { value="7";
					 * }}
					 */
					break;
				case "art":
					value = "";
					if (URL.contains("emedicine") && URL.contains("article")) {
						value = "ckb" + articleID;// .substring(0,
													// articleID.indexOf('-'));
					} else if (URL.contains("drug")) {
						value = "drg" + articleID;
					}
					break;
				case "ac":
					value = "0";
					if (articleID != null) {
						if (response.path("ac") != null)
							value = response.path("ac").toString();
					}
					if (value == null) {
						value = "0";
					}
					break;
				case "as":
					value = "0";
					if (articleID != null) {
						if (response.path("as") != null)
							value = response.path("as").toString();
					}
					if (value == null) {
						value = "0";
					}
					break;
				case "cg":
					value = "0";
					if (articleID.equals("0")) {// Mahendra:: Changes : if
												// (art.equals("0"))
						value = "0";
					} else {
						if (response.path("cg") != null)
							value = response.path("cg").toString();
					}
					if (value == null) {
						value = "0";
					}
					/*
					 * if(URL.contains("emedicine")&&URL.contains( "article")) {
					 * value="502"; } else if(URL.contains("drug")) {
					 * value="501"; }
					 */
					break;
				case "scg":
					value = "0";
					if (articleID != null) {
						if (response.path("scg") != null)
							value = response.path("scg").toString();
					}
					if (value == null) {
						value = "0";
					} else {
						if (value.isEmpty())
							value = "0";
					}
					/*
					 * if(URL.contains("emedicine")&&URL.contains( "article")) {
					 * value="8"; } else if(URL.contains("drug")) { value="0"; }
					 */
					break;
				case "ck":
					value = "0";
					if (jspMap.containsKey(key)) {
						value = jspMap.get(key);
					}
					break;
				case "pub":
					value = "0";
					if (articleID != null) {
						if (response.path("pub-id") != null)
							value = response.path("pub-id").toString();
					}
					if (value == null) {
						value = "0";
					}
					break;
				case "asb":
					value = "No Implementation";
					break;
				case "acb":
					value = "No Implementation";
					break;
				}
				System.out.println("key : " + key + " / Value : " + value);
				pageSegvarsEPoint.put(key, value);
			}
		} else {
			while (iter.hasNext()) {
				String key = (String) iter.next();
				String value = null;

				switch (key) {
				case "ssp":
					value = "0";
					if (jspMap.containsKey(key)) {
						value = jspMap.get(key);
					}
					break;
				case "art":
					value = "0";
					break;
				case "ac":
					value = "0";
					break;
				case "as":
					value = "0";
					break;
				case "cg":
					value = "0";
					break;
				case "scg":
					value = "0";
					if (jspMap.containsKey(key)) {
						value = jspMap.get(key);
					}
					if (value == null)
						value = "0";
					break;
				case "ck":
					value = "0";
					if (jspMap.containsKey(key)) {
						value = jspMap.get(key);
					}
					break;
				case "pub":
					value = "0";
					break;
				}
				System.out.println("key : " + key + " / Value : " + value);
				pageSegvarsEPoint.put(key, value);
			}
		}
	}

	/*
	 * Load userSegVars into expectedMap
	 */
	public static void loaduserSegVars(List<String> userSegvars) {
		System.out.println("***** User Segvars keys and values from end point ***");

		iter = userSegvars.iterator();

		if (URL.contains("viewarticle") || URL.contains("viewcollection") || URL.contains("viewpublication")
				|| URL.contains("vercoleccion") || URL.contains("voircollection") || URL.contains("vercolecao")) {
			Response response = null;

			int index = URL.lastIndexOf('/');
			String id = URL.substring(index + 1);
			if (id.contains("#")) {
				id = id.split("#", 2)[0];
			}
			if (id.contains("?")) {
				id = id.split("\\?", 2)[0];
			}
			if (id.contains("_")) {
				id = id.substring(0, id.indexOf('_'));
			}

			if (URL.contains("viewarticle")) {
				// calling contentmedataservice for viewarticle pages
				response = given()
						.get("http://contentmetadataservice-app-" + configProp.getProperty("env")
								+ "prf.iad1.medscape.com:8080/contentmetadataservice/getArticle?legacyId=" + id)
						.then().extract().response();
			} else if (URL.contains("viewcollection") || URL.contains("vercoleccion") || URL.contains("voircollection")
					|| URL.contains("vercolecao")) {
				// calling contentmedataservice for viewcollection pages
				response = given()
						.get("http://contentmetadataservice-app-" + configProp.getProperty("env")
								+ "prf.iad1.medscape.com:8080/contentmetadataservice/getCollection?legacyId=" + id)
						.then().extract().response();
			} else if (URL.contains("viewpublication")) {
				// calling contentmedataservice for viewpublication pages
				response = given()
						.get("http://contentmetadataservice-app-" + configProp.getProperty("env")
								+ "prf.iad1.medscape.com:8080/contentmetadataservice/getPublication?legacyId=" + id)
						.then().extract().response();
			}
			String responseJSON1 = "";
			if (URL.contains("viewarticle") || (URL.contains("viewcollection") || URL.contains("vercoleccion")
					|| URL.contains("voircollection") || URL.contains("vercolecao"))
					|| URL.contains("viewpublication")) {
				Response response1 = given().get("http://profile-app-" + configProp.getProperty("env")
						+ "prf.iad1.medscape.com:8080/ws/services/ProfileService/profile/" + guid
						+ "?requestId=1001&format=json").then().extract().response();
				responseJSON1 = response1.asString();

				responseJSON1 = responseJSON1.replace(
						"<ns:getProfileResponse xmlns:ns=\"http://service.ps.prof.webmd.com\"><ns:return>", "");
			}
			int code = response.statusCode();

			String responseJSON = response.asString();

			JsonPath res = new JsonPath(responseJSON);
			JsonPath res1 = new JsonPath(responseJSON1);

			while (iter.hasNext()) {

				String key = (String) iter.next();
				String value = null;

				switch (key) {
				case "ssp":
					if (jspMap != null) {
						if (jspMap.containsKey(key)) {
							value = jspMap.get(key);
						} else {
							String ssp = response.path("leadTopicCenterID").toString();
							value = ssp;
						}
					}
					break;
				case "art":
					String art = response.path("legacyId").toString();
					value = art;
					break;
				case "ac":
					String allConcepts = response.path("allConcepts").toString();
					value = allConcepts;
					break;
				case "as":
					String allSpecialties = response.path("allSpecialties").toString();
					value = allSpecialties;
					break;
				case "cg":
					String cg = response.path("contentGroupID").toString();
					value = cg;
					break;
				case "scg":
					String scg = response.path("leadConceptID").toString();
					value = scg;
					break;
				case "ck":
					String ck = response.path("conferenceKey").toString();
					value = ck;
					break;
				case "pub":
					int pubID = response.path("publicationID");
					String pub = String.valueOf(pubID);
					value = pub;
					break;
				case "val":
					List<String> vals = res1.getList("profile.professionStatus.valstatus");
					if (vals.size() >= 1)
						value = vals.get(0);
					break;
				case "vit":
					value = vitValue;
					break;
				case "st":
					List<String> st = res1.getList("profile.contact.st");
					if (st.size() >= 1)
						value = st.get(0);
					break;
				case "ct":
					List<String> ct = res1.getList("profile.contact.co");
					if (ct.size() >= 1)
						value = ct.get(0);
					else {
						value = "";
					}
					break;
				case "usp":
					List<String> spid = res1.getList("profile.professions.spid");
					if (spid.size() >= 1)
						value = spid.get(0);
					else {
						value = "";
					}
					break;
				case "tar":
					if (tarValue != null)
						value = tarValue;
					else
						value = "0";
					break;
				case "pf":
					List<String> profid = res1.getList("profile.professions.profid");
					if (profid.size() >= 1)
						value = profid.get(0);
					else {
						value = "";
					}
					break;
				case "gd":
					List<String> guid = res1.getList("profile.professionStatus.guid");
					if (guid.size() >= 1)
						value = (Long.parseLong(guid.get(0)) * 27) + "";
					else {
						value = "";
					}
					break;
				case "occ":
					List<String> occid = res1.getList("profile.professions.occid");
					if (occid.size() >= 1)
						value = occid.get(0);
					if (value.isEmpty())
						value = "0";
					else {
						value = "0";
					}
					break;
				case "sa":
					value = summaryAd;
					break;
				case "tc":
					value = ut.toString();
					break;
				default:
					value = "No key and value";
				}
				System.out.println("Keys : " + key + " / Value : " + value);
				userSegvarsEPoint.put(key, value);
			}
		} else if (URL.contains("emedicine") || (URL.contains("reference") && (!URL.contains("slideshow")))) {
			// Connect to File system and retrieve values
			String articleID = "";
			Response response = null;
			Response response1 = null;
			JsonPath res = null;
			if (URL.contains("emedicine") && URL.contains("article")) {
				articleID = URL.substring(URL.lastIndexOf('/') + 1, URL.indexOf("-"));
				// exists=cp.checkFileAndDownload(articleID,prop.getProperty("ENV"),"emedicine");
			} else if (URL.contains("drug")) {

				if (!URL.contains("#")) {
					articleID = URL.substring(URL.lastIndexOf('-') + 1);
					// exists=cp.checkFileAndDownload(articleID,prop.getProperty("ENV"),"drug");
				} else {
					articleID = URL.substring(URL.lastIndexOf('-') + 1, URL.indexOf('#'));
					// exists=cp.checkFileAndDownload(articleID,prop.getProperty("ENV"),"drug");
				}
			}
			if (URL.contains("emedicine")) {

				// calling contentmedataservice for viewarticle pages

				response = given().get("http://contentmetadataservice-app-" + configProp.getProperty("env")
						+ "prf.iad1.medscape.com:8080/contentmetadataservice/getReferenceArticle?legacyId=" + articleID)
						.then().extract().response();
				response1 = given().get("http://profile-app-" + configProp.getProperty("env")
						+ "prf.iad1.medscape.com:8080/ws/services/ProfileService/profile/" + guid
						+ "?requestId=1001&format=json").then().extract().response();
				String responseJSON1 = response1.asString();

				responseJSON1 = responseJSON1.replace(
						"<ns:getProfileResponse xmlns:ns=\"http://service.ps.prof.webmd.com\"><ns:return>", "");

				res = new JsonPath(responseJSON1);
			} else if (URL.contains("reference") && (!URL.contains("slideshow"))) {
				// calling contentmedataservice for viewcollection pages
				response = given().get("http://contentmetadataservice-app-" + configProp.getProperty("env")
						+ "prf.iad1.medscape.com:8080/contentmetadataservice/getReferenceDrug?legacyId=" + articleID)
						.then().extract().response();
				response1 = given().get("http://profile-app-" + configProp.getProperty("env")
						+ "prf.iad1.medscape.com:8080/ws/services/ProfileService/profile/" + guid
						+ "?requestId=1001&format=json").then().extract().response();
				String responseJSON1 = response1.asString();

				responseJSON1 = responseJSON1.replace(
						"<ns:getProfileResponse xmlns:ns=\"http://service.ps.prof.webmd.com\"><ns:return>", "");

				res = new JsonPath(responseJSON1);
			}

			if (proclivityUser) {
				String call = "https://api.qa00.medscape.com/adpredictionservice/transform?callback=jQuery111302666929430362812_1536168816499&q=";

				String param1After = "";
				try {
					param1After = call + URLEncoder.encode("{\"dfpData\":{\"pageSegVars\":{\"art\":\""
							+ proclivityData.get("art") + "\",\"cg\":\"" + proclivityData.get("cg") + "\",\"pub\":\""
							+ proclivityData.get("pub") + "\",\"scg\":\"" + proclivityData.get("scg") + "\",\"ssp\":\""
							+ proclivityData.get("ssp") + "\"},\"reqHeaders\":{\"device\":\""
							+ proclivityData.get("device") + "\",\"domain\":\"" + proclivityData.get("domain")
							+ "\",\"domainCategory\":\"" + proclivityData.get("domainCategory") + "\",\"ep\":\""
							+ proclivityData.get("ep") + "\",\"kw\":\"" + proclivityData.get("kw")
							+ "\"},\"userSegVars\":{\"pf\":\"" + proclivityData.get("pf") + "\",\"usp\":\""
							+ proclivityData.get("usp") + "\",\"dt\":\"" + proclivityData.get("dt")
							+ "\"},\"webSegVars\":{\"pc\":\"" + proclivityData.get("pc") + "\",\"spon\":\""
							+ proclivityData.get("spon")
							+ "\"}},\"requestedSteps\":[\"pb\",\"blbll\"]}&_=1536168816501}", "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				System.out.println(param1After);
				Response proc_response = given().get(param1After).then().extract().response();

				String proc_responseJSON = proc_response.asString();
			}
			boolean exists;

			while (iter.hasNext()) {

				String key = (String) iter.next();
				String value = null;

				switch (key) {
				case "ssp":
					value = "0";
					if (jspMap.containsKey(key)) {
						value = jspMap.get(key);
					} else {
						value = response.path("ssp").toString();
					}
					if (value == null)
						value = "0";
					/*
					 * if(!value.equals("0")){
					 * if(URL.contains("emedicine")&&URL.contains( "article")) {
					 * value="38"; } else if(URL.contains("drug")) { value="7";
					 * }}
					 */
					break;
				case "art":
					value = "";
					if (URL.contains("emedicine") && URL.contains("article")) {
						value = "ckb" + articleID;// .substring(0,
													// articleID.indexOf('-'));
					} else if (URL.contains("drug")) {
						value = "drg" + articleID;
					}
					break;
				case "ac":
					value = "0";
					if (articleID != null) {
						value = response.path("ac").toString();
					}
					if (value == null) {
						value = "0";
					}

					break;
				case "as":
					value = "0";
					if (articleID != null) {
						// value = rda.getText("as");
						value = response.path("as").toString();
					}
					if (value == null) {
						value = "0";
					}
					break;
				case "cg":
					value = "0";

					if (URL.contains("emedicine") && URL.contains("article")) {
						value = response.path("cg").toString();
					} else if (URL.contains("reference")) {
						value = response.path("cg").toString();
					}
					break;
				case "scg":
					value = "0";
					if (URL.contains("emedicine") && URL.contains("article")) {
						value = response.path("scg").toString();
					} else if (URL.contains("reference")) {
						value = response.path("scg").toString();
					}
					if (value == null || value.length() == 0) {
						value = "0";
					}
					break;
				case "ck":
					value = "0";
					if (response.body().toString().contains("ck")) {
						value = response.path("conferenceKey").toString();
					} else {
						value = "0";
					}
					break;
				case "pub":
					value = "0";
					if (URL.contains("reference")) {
						value = "0";
					} else if (URL.contains("emedicine")) {
						value = "";
					}
					break;
				case "val":
					List<String> vals = res.getList("profile.professionStatus.valstatus");
					if (vals.size() >= 1)
						value = vals.get(0);
					break;
				case "vit":
					value = vitValue;
					break;
				case "pbs":
					// NOT IMPLEMENTED
					value = "";
					break;
				case "st":
					List<String> st = res.getList("profile.contact.st");
					if (st.size() >= 1)
						value = st.get(0);
					break;
				case "pbr":
					// NOT IMPLEMENTED
					value = "";
					break;
				case "tar":
					value = tarValue;
					break;
				case "occ":
					List<String> occid = res.getList("profile.professions.occid");
					if (occid.size() >= 1)
						value = occid.get(0);
					if (value.isEmpty())
						value = "0";
					else {
						value = "0";
					}
					break;
				case "sa":
					value = summaryAd;
					break;
				case "tc":
					value = ut.toString();
					break;
				case "dt":
					// NOT IMPLEMENTED
					value = "";
					break;
				case "ct":
					List<String> ct = res.getList("profile.contact.co");
					if (ct.size() >= 1)
						value = ct.get(0);
					else {
						value = "";
					}
					break;
				case "pb":
					// NOT IMPLEMENTED
					value = "";
					break;
				case "usp":
					List<String> spid = res.getList("profile.professions.spid");
					if (spid.size() >= 1)
						value = spid.get(0);
					else {
						value = "";
					}
					break;
				case "pdi":
					// NOT IMPLEMENTED
					value = "";
					break;
				case "pf":
					List<String> profid = res.getList("profile.professions.profid");
					if (profid.size() >= 1)
						value = profid.get(0);
					else {
						value = "";
					}
					break;
				case "masid":
					value = "";
					break;
				case "gd":
					List<String> guid = res.getList("profile.professionStatus.guid");
					if (guid.size() >= 1)
						value = (Long.parseLong(guid.get(0)) * 27) + "";
					else {
						value = "";
					}
					break;
				case "pdp":
					// NOT IMPLEMENTED
					value = "";
					break;
				case "pds":
					// NOT IMPLEMENTED
					value = "";
					break;
				case "pbp":
					// NOT IMPLEMENTED
					value = "";
					break;
				}
				userSegvarsEPoint.put(key, value);
				System.out.println("key : " + key + " / value : " + value);
			}
		} else {

			Response response1 = given().get("http://profile-app-" + configProp.getProperty("env")
					+ "prf.iad1.medscape.com:8080/ws/services/ProfileService/profile/" + guid
					+ "?requestId=1001&format=json").then().extract().response();
			String responseJSON1 = response1.asString();

			responseJSON1 = responseJSON1
					.replace("<ns:getProfileResponse xmlns:ns=\"http://service.ps.prof.webmd.com\"><ns:return>", "");

			JsonPath res = new JsonPath(responseJSON1);

			while (iter.hasNext()) {

				String key = (String) iter.next();
				String value = null;

				switch (key) {
				case "ssp":
					value = "0";
					if (jspMap.containsKey(key)) {
						value = jspMap.get(key);
					}
					break;
				case "art":
					value = "0";
					break;
				case "ac":
					value = "0";
					break;
				case "as":
					value = "0";
					break;
				case "cg":
					value = "0";
					break;
				case "scg":
					value = "0";
					if (jspMap.containsKey(key)) {
						value = jspMap.get(key);
					}
					if (value == null)
						value = "0";
					break;
				case "ck":
					value = "0";
					if (jspMap.containsKey(key)) {
						value = jspMap.get(key);
					}
					break;
				case "pub":
					value = "0";
					break;
				case "val":
					List<String> vals = res.getList("profile.professionStatus.valstatus");
					if (vals.size() >= 1)
						value = vals.get(0);
					break;
				case "vit":
					value = vitValue;
					break;
				case "st":
					List<String> st = res.getList("profile.contact.st");
					if (st.size() >= 1)
						value = st.get(0);
					break;
				case "ct":
					List<String> ct = res.getList("profile.contact.co");
					if (ct.size() >= 1)
						value = ct.get(0);
					else {
						value = "";
					}
					break;
				case "usp":
					List<String> spid = res.getList("profile.professions.spid");
					if (spid.size() >= 1)
						value = spid.get(0);
					else {
						value = "";
					}
					break;
				case "tar":
					value = tarValue;
					break;
				case "pf":
					List<String> profid = res.getList("profile.professions.profid");
					if (profid.size() >= 1)
						value = profid.get(0);
					else {
						value = "";
					}
					break;
				case "gd":
					List<String> guid = res.getList("profile.professionStatus.guid");
					if (guid.size() >= 1)
						value = (Long.parseLong(guid.get(0)) * 27) + "";
					else {
						value = "";
					}
					break;
				case "occ":
					List<String> occid = res.getList("profile.professions.occid");
					if (occid.size() >= 1)
						value = occid.get(0);
					if (value.isEmpty())
						value = "0";
					else {
						value = "0";
					}
					break;
				case "sa":
					value = summaryAd;
					break;
				case "tc":
					value = ut.toString();
					break;
				}

				System.out.println("key : " + key + " / value : " + value);
				userSegvarsEPoint.put(key, value);
			}
		}

	}

	/*
	 * load requestHeaders: ep, requestEnv, device
	 */
	public static void loadrequestHeaders(List<String> requestHeader) {
		System.out.println("*** Request header keys and values from end point ** ");
		iter = requestHeader.iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			String value = null;
			switch (key) {
			case "domainCategory":
				if ((URL.startsWith("https://www") || URL.startsWith("http://www")) && URL.contains("medscape.com")) {
					value = "www";
				} else if ((URL.startsWith("https://www") || URL.startsWith("http://www"))
						&& URL.contains("medscape.org")) {
					value = "cme";
				} else if (URL.startsWith("https://reference") || URL.startsWith("http://reference")) {
					value = "reference";
				} else if ((URL.startsWith("https://deutsch") | URL.startsWith("http://deutsch"))
						|| URL.contains("/search/de?q")) {
					value = "medscapemedizin";
				} else if ((URL.startsWith("https://espanol") || URL.startsWith("http://espanol"))
						|| URL.contains("/search/es?q")) {
					value = "espanol";
				} else // Srikanth - I have modified the value from "francais"
						// to "france"
				if ((URL.startsWith("https://francais") || URL.startsWith("http://francais"))
						|| URL.contains("/search/fr?q")) {
					value = "france";
				} else if ((URL.startsWith("https://portugues") || URL.startsWith("http://portugues"))
						|| URL.contains("/search/pt?q")) {
					value = "portugues";
				} else if (URL.startsWith("https://emedicine") || URL.startsWith("http://emedicine")) {
					value = "emedicine";
				} else if ((URL.startsWith("https://search") || URL.startsWith("http://search"))
						&& URL.contains("/search/?q")) {
					value = "www";
				}
				break;
			case "domain":
				if (URL.startsWith("https://www") || URL.startsWith("http://www")) {
					value = "www";
				} else if (URL.startsWith("https://reference") || URL.startsWith("http://reference")) {
					value = "reference";
				} else if (URL.startsWith("https://deutsch") || URL.startsWith("http://deutsch")) {
					value = "deutsch";
				} else if (URL.startsWith("https://espanol") || URL.startsWith("http://espanol")) {
					value = "espanol";
				} else if (URL.startsWith("https://francais") || URL.startsWith("http://francais")) {
					value = "francais";
				} else if (URL.startsWith("https://portugues") || URL.startsWith("http://portugues")) {
					value = "portugues";
				} else if (URL.startsWith("https://emedicine") || URL.startsWith("http://emedicine")) {
					value = "emedicine";
				} else if (URL.startsWith("https://search") || URL.startsWith("http://search")) {
					value = "search";
				}
				break;
			case "enableDomain":
				value = map.get("enableDomain");
				break;
			case "ep":
				value = "1";
				break;
			case "requestEnv":
				value = configProp.getProperty("env").replace(".", "").toString();
				break;
			case "device":
				if (configProp.getProperty("breakPoint").equalsIgnoreCase("1"))
					value = "MOBILE";
				else
					value = "PC";
				break;
			case "ipAddress":
				value = "NO Implementation";
				break;
			}
			System.out.println("Key : " + key + " / value : " + value);
			requesthEPoint.put(key, value);
		}

	}

	/*
	 * This method calls the Profile Service Reads tar, validatedLocales,
	 * userCampaigns
	 */
	public static void loadUserCampaigns() {
		System.out.println("** User Compaigns from end point **");
		try {
			Response response = given().get("http://profile-app-" + configProp.getProperty("env")
					+ "prf.iad1.medscape.com:8080/ws/services/ProfileService/profile/" + guid
					+ "?requestId=1001&format=json").then().extract().response();

			String responseJSON = response.asString();

			responseJSON = responseJSON
					.replace("<ns:getProfileResponse xmlns:ns=\"http://service.ps.prof.webmd.com\"><ns:return>", "");

			JsonPath res = new JsonPath(responseJSON);
			// Code to read the tar and userCampaign values
			List<String> cid = res.getList("profile.campaign.cid");

			for (int i = 0; i < cid.size(); i++) {
				String camid = res.get("profile.campaign[" + i + "].cid");
				String adId = res.get("profile.campaign[" + i + "].adid");

				String showad = res.get("profile.campaign[" + i + "].showad");

				if (adId != null) { // When adid is present in the profile
									// service

					if (showad.equals("1")) {
						tarValue = tarValue + camid + "_" + adId;
					}
					cidValue = cidValue + camid + ":" + adId;
				} else { // When adid is not present in the profile service
					cidValue = cidValue + camid + ":";
				}

				// String showad = res.get("profile.campaign["+i+"].showad");
				String showa = res.get("profile.campaign[" + i + "].showa");
				String showt = res.get("profile.campaign[" + i + "].showt");

				if (showad.equals("1")) {
					cidValue = cidValue + ":2";
				}
				if (showa.equals("1")) {
					cidValue = cidValue + ":4";
				}
				if (showt.equals("1")) {
					cidValue = cidValue + ":5";
				}

				if (i < cid.size() - 1) {
					tarValue = tarValue + ",";
					cidValue = cidValue + ",";
				}

			}
			if (tarValue.isEmpty() || tarValue == null) {
				tarValue = "0";
			} else {
				System.out.println("tar Value before altering: " + tarValue);
				tarValue = tarValue.replace(",,,", ",").replace(",,", ",");// .replace(String.valueOf(tarValue.charAt(tarValue.length()-1)),
																			// "");
				tarValue = tarValue.substring(0, tarValue.lastIndexOf(','));
				System.out.println("After altering: " + tarValue);
			}

			System.out.println("tar Values: " + tarValue);
			System.out.println("Campaign Values: " + cidValue);

			// Code to validate the val status of the user
			List<String> valStatus = res.getList("profile.professionStatus.valstatus");
			System.out.println("valStatus size: " + valStatus.size());
			for (int i = 0; i < valStatus.size(); i++) {
				String val = res.get("profile.professionStatus[" + i + "].valstatus");
				if (val.equals("1")) {
					String locale = res.get("profile.professionStatus[" + i + "].locale").toString();
					locale = locale.substring(locale.lastIndexOf('_'));
					// localeValue = localeValue+locale;
					localeValue.add(locale);
				}
			}
			if (localeValue.size() > 0) {
				System.out.println("localeValue Length: " + localeValue.size());
			} else {
				localeValue.add("0");
			}

			// if (configProp.getProperty("GUID").equals(guid)) {// 21988613
			// modified
			System.out.println("Vit Value:");
			if (res.get("profile.attributes[1].attrid").toString().equals("vit")) {
				vitValue = res.get("profile.attributes[1].val");
				System.out.println(vitValue);
			}
			/*
			 * } else { vitValue = ""; }
			 */
		} catch (Exception e) {
			localeValue.add("0");
			tarValue = "0";
			cidValue = "";

		}
	}

	/*
	 * This method calls the CP service and loads userTactics into expectedMap
	 * Assigns sa=1 if summaryAd is active and sa=0 if summaryAd is false
	 */
	public static void loaduserTactics() {
		System.out.println("*** User Tractics from end point ** ");
		try {
			Response response = given()
					.get("http://api." + configProp.getProperty("env") + "medscape.com/cp/user/" + guid + "").then()
					.extract().response();

			// String responseJSON = response.asString();
			// System.out.println("responseJSON - "+responseJSON);
			// JsonPath res = new JsonPath(responseJSON);
			JsonPath res = response.jsonPath();
			// Code to read the userTactics from CP Service response
			List<Integer> userTactics = res.getList("activeTactics.id");
			for (int i = 0; i < userTactics.size(); i++) {
				ut.append(userTactics.get(i).toString());
				if (i != userTactics.size() - 1) {
					ut.append(",");
				}
			}
			System.out.println("ut -- " + ut);
			/*
			 * if (ut.length() != 0) userSegvarsEPoint.put("tc ",
			 * ut.toString());
			 */
			// Code to find the summaryAd value from CP Service response
			List<Boolean> sa = res.getList("activeTactics.mi.summaryAd.active");
			summaryAd = null;

			for (int i = 0; i < sa.size(); i++) {
				if (sa.get(i)) {
					summaryAd = "1";
					break;
				} else
					continue;
			}
			if (summaryAd == null) {
				summaryAd = "0";
			}
		} catch (Exception e) {
			// ut.append("Exception occured");
			summaryAd = "0";
		}
	}

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
		else if (URL.split("/").length == 3 || URL.split("/").length == 2)
			return "Homepage";
		else
			return null;
	}

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
		// System.out.println(Actualpage + " DFPT Keys = " + DFPTkeys);
		return DFPTkeys;
	}

	public static List<String> compareDFPTKeys(List<String> source, List<String> comp) {
		// Verifying the dt key available in UserSegvars or not.
		ArrayList<String> list = new ArrayList<>();
		for (String key : source) {
			if (!comp.contains(key))
				list.add(key);
		}
		return list;

	}

	public static String comparingActualAndExpectedValues(Map<String, Object> expected_map,
			Map<String, Object> actual_map) {

		String report = "";// Following values has miss match for respective
							// keys:
		for (String key : actual_map.keySet()) {

			Object expected = expected_map.get(key);
			Object actual = actual_map.get(key);
			// compare the expected value from actual value
			if (expected != null && actual != null) {
				if (expected instanceof String) {
					String exp = URLDecoder.decode(expected.toString());
					if (key.equalsIgnoreCase("tc") || key.equalsIgnoreCase("tar")) {
						StringBuffer tcv = new StringBuffer();
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
							System.out.println("Expected value : " + exp + ", Actual value : " + actual.toString()
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
								System.out.println("Expected value : " + expectedValueb + ", Actual value : "
										+ actualValueb + " for key : " + key + " are not matching");
								report = report + "Expected value : " + expectedValueb + ", Actual value : "
										+ actualValueb + " for key : " + key + " are not matching" + "#";
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
				System.out.println("Expected value : " + msg1 + ", Actual value : " + msg2 + " for key : " + key
						+ " are not matching");
				report = report + "Expected value : " + msg1 + ", Actual value : " + msg2 + " for key : " + key
						+ " are not matching" + "#";

			}
		}
		if (!report.isEmpty())
			return report.substring(0, report.length() - 1);
		else
			return report;
	}

}
