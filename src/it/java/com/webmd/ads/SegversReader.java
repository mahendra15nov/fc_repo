package com.webmd.ads;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SegversReader {
	static String Actualdata;
	static Properties pr = new Properties();


	public static void configReADER() throws FileNotFoundException, IOException {
		FileReader readerCon = new FileReader("src/it/resources/Config.properties");
		pr.load(readerCon);		
	}

	public static void propReader() throws FileNotFoundException, IOException {
		FileReader reader = new FileReader("src/it/resources/segver.properties");
		pr.load(reader);
	}

	public static HashMap<String, List<String>> getSegvers(String Actualpage) {

		HashMap<String, List<String>> DFPTkeys = new HashMap<>();
		try {
			List<String> hp = Arrays.asList(pr.getProperty(Actualpage).split(";"));

			for (String seg : hp) {
				String t = seg;
				switch (t.substring(0, t.indexOf("="))) {
				case "ExpectedReqHeaders":
					DFPTkeys.put("ExpectedReqHeaders",
							Arrays.asList(seg.substring(seg.indexOf("ExpectedReqHeaders=") + 19).split(",")));
					break;
				case "ExpectedWebSegVars":
					DFPTkeys.put("ExpectedWebSegVars",
							Arrays.asList(seg.substring(seg.indexOf("ExpectedWebSegVars=") + 19).split(",")));
					break;
				case "ExpectedPageSegVars":
					DFPTkeys.put("ExpectedPageSegVars",
							Arrays.asList(seg.substring(seg.indexOf("ExpectedPageSegVars=") + 20).split(",")));
					break;
				case "ExpectedUserSegVars":
					DFPTkeys.put("ExpectedUserSegVars",
							Arrays.asList(seg.substring(seg.indexOf("ExpectedUserSegVars=") + 20).split(",")));
					break;
				case "ExpectedLazyLoad":
					DFPTkeys.put("ExpectedLazyLoad",
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

	public static HashMap<String, List<String>> getDFPTKeys(String page) throws FileNotFoundException, IOException {
		propReader();
		switch (page) {
		case "ViewArticle":
			Actualdata = "ViewArticle";
			break;
		case "EmedicineArticle":
			Actualdata = "EmedicineArticle";
			break;
		case "ReferenceArticle":
			Actualdata = "ReferenceArticle";
			break;
		case "SlideshowArticle":
			Actualdata = "SlideshowArticle";
			break;
		default:
			System.out.println("Page was undefined");
		}

		return getSegvers(Actualdata);
	}

	public static HashMap<String, HashMap<String, List<String>>> segversReader() throws IOException {
		HashMap<String, HashMap<String, List<String>>> hm = new HashMap<>();

		configReADER();
		String[] str = new String[] { "ViewArticle", "SlideshowArticle", "EmedicineArticle", "ReferenceArticle" };
		for (String page : str) {
			hm.put(page, getDFPTKeys(page));
		}
		return hm;
	}

	public static void main(String[] a) throws IOException {
		
		System.out.println(segversReader());
	}
	/*
	  @DataProvider public String[][] dataProvider() {
	  
	  return XlRead.fetchDataExcludingFirstRow("AdsSanity.xls", "ProdSanity");
	  
	  return new String[][] { { "https://www.medscape.com/viewarticle/899796"
	  }, {
	  "https://reference.medscape.com/drug/acetadote-cetylev-antidote-acetylcysteine-antidote-343740"
	 }, { "https://emedicine.medscape.com/article/2500072-overview" }, {
	  "https://www.medscape.com/slideshow/2018-compensation-urologist-6009675"
	  } };
	  
	  }
	 */
}