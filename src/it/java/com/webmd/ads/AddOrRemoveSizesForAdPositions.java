package com.webmd.ads;

import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;

/*PPE-180978 - Add / Remove sizes on pos 1520, pos 1420, pos 1145, pos 1004 on DEV01 / QA01
 * This user story is applicable for only mobile ad positions 
 * Applicable only for View article pages and Drug monograph pages 
 * */
 
public class AddOrRemoveSizesForAdPositions extends AdsCommon{
	
	@BeforeClass()
	public void beforeClass(){
		login("raruva@webmd.net","medscape");
	}
	
	@Test(dataProvider = "dataProviderViewArticle")
	public void validateViewArticle(String URL){//String type
		getDriver();
		getServer().newHar();
		getDriver().get(URL);
		/*String first = getSizesForSpecificPositionFromAdCall("101");
		String second = getSizesForSpecificPositionFromAdCall("520");
		System.out.println(first+" and "+second);*/
		
		scrollTillEnd();
		
		validate1004();
		validate1420();
		validate1520();
	}
	
	@Test(dataProvider = "dataProviderDrugMonograph")
	public void validateDrugMonograph(String URL){//String type
		getServer().newHar();
		getDriver().get(URL);
		scrollTillEnd();
		validate1145();
	}
	
	@DataProvider
	public  String[][] dataProviderViewArticle() {
		//return XlRead.fetchDataExcludingFirstRow("AdsSanity.xls", "ProdSanity");
		return new String[][]  {
			{"https://www.dev01.medscape.com/viewarticle/895129"}, 
			{"https://www.dev01.medscape.com/viewarticle/895115"},
			{"https://www.dev01.medscape.com/viewarticle/895129"}
			};
	}
	
	@DataProvider
	public  String[][] dataProviderDrugMonograph() {
		//return XlRead.fetchDataExcludingFirstRow("AdsSanity.xls", "ProdSanity");
		return new String[][]  {
			{"https://reference.dev01.medscape.com/drug/alka-seltzer-plus-cold-cough-effervescent-aspirin-chlorpheniramine-dextromethorphan-phenylephrine-iv-999390"}, 
			//{"https://www.medscape.com/pediatrics"}
			};
	}
	
	private String getSizes(String pos){
		
		String prev_iu_szs = "1x1,1x4,320x50|1x12,2x3|320x80|375x80,1x2|300x50|300x51|320x50|320x51|300x251|300x252,300x250|300x400|300x50|300x51|320x50|320x51";
		String prev_scp = "pos=1004|pos=2017|pos=1421&strnativekey=WUyRNBN8mNoxJvpseCvvaEQE|pos=1145|pos=1005|pos=1122";
		
		if (prev_scp.contains(pos)) {
			System.out.println(prev_scp);

			String[] size = prev_iu_szs.split(",");
			String[] positions = prev_scp.split("\\|");

			for (int i = 0; i < positions.length; i++) {
				if (positions[i].contains(pos))
					return size[i];
			}
		}
		
		return null;
		
	}
	
	private void validate1004(){
		generateInfoReport("Validating 1004");
		String sizes = getSizesForSpecificPositionFromAdCall("1004");
		try{
			Assert.assertTrue(sizes.contains("1x1"));
			Assert.assertFalse(sizes.contains("300x50|320x50"));
			generatePassReport("Only 1x1 loaded for 1004 ad position");
		}catch(AssertionError e){
			generateFailReport("Sizes loaded for 1004 are "+sizes);
		}
	}
	
	private void validate1520(){
		generateInfoReport("Validating 1520");
		String sizes = getSizesForSpecificPositionFromAdCall("1520");
		try{
			Assert.assertTrue(sizes.contains("300x255"));
			Assert.assertFalse(sizes.contains("2x3"));
			generatePassReport("Only 300x255 loaded for 1520 ad position");
		}catch(AssertionError e){
			generateFailReport("Sizes loaded for 1520 are "+sizes);
		}
	}
	
	private void validate1420(){
		generateInfoReport("Validating 1420");
		String sizes = getSizesForSpecificPositionFromAdCall("1420");
		try{
			Assert.assertTrue(sizes.contains("300x255"));
			Assert.assertTrue(sizes.contains("2x3"));
			generatePassReport("Expected sizes loaded for 1420 ad position");
		}catch(AssertionError e){
			generateFailReport("Sizes loaded for 1420 are "+sizes);
		}
	}
	
	private void validate1145(){
		generateInfoReport("Validating 1145");
		String sizes = getSizesForSpecificPositionFromAdCall("1145");
		try{
			Assert.assertTrue(sizes.contains("320x80"));
			Assert.assertTrue(sizes.contains("375x80"));
			Assert.assertTrue(sizes.contains("2x3"));
			generatePassReport("2x3 size added to 1145 ad position");
		}catch(AssertionError e){
			generateFailReport("Sizes loaded for 1004 are "+sizes);
		}
	}

}