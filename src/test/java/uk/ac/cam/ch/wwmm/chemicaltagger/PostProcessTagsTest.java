package uk.ac.cam.ch.wwmm.chemicaltagger;

import junit.framework.Assert;

import org.junit.Test;

/************************************
 * Tests the PostProcessingTags Class.
 * @author lh359
 ************************************/
public class PostProcessTagsTest {
	
	@Test
	public void testMistaggedChemistry() {
		
          String sentence = "ROMP of 1. ATRP was performed on 3 ml of a benzene compound under 25\u00B0c";
  		  String expected = "NN-SYNTHESIZE ROMP IN-OF of CD-ALPHANUM 1 STOP . NN-SYNTHESIZE ATRP VBD was VBN performed IN-ON on CD 3 NN-VOL ml IN-OF of DT a OSCAR-CM benzene NN-CHEMENTITY compound IN-UNDER under CD 25 NN-TEMP \u00B0c";
          POSContainer posContainer = ChemistryPOSTagger.getInstance().runTaggers(sentence);
          Assert.assertEquals(expected,posContainer.getTokenTagTupleAsString());
 	}

	@Test
	public void testMistaggedVerbs() {
		
          String sentence = "The benzene worked form was filled";
  		  String expected = "DT-THE The OSCAR-CM benzene JJ worked NN form VBD was VB-FILL filled";
          POSContainer posContainer = ChemistryPOSTagger.getInstance().runTaggers(sentence);
          Assert.assertEquals(expected,posContainer.getTokenTagTupleAsString());
 	}

	
	
}
