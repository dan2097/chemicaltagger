package uk.ac.cam.ch.wwmm.chemicaltagger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**************************************************
 * Corrects Mistagged tokens.
 * 
 * @author lh359, dl387
 **************************************************/
public class PostProcessTags {

	private static HashSet<String> tagSet = Utils.loadsTagsFromFile(PostProcessTags.class);
	/***********************************************
	 * Corrects known tagging problems in the tags in combinedtagsList.
	 * 
	 * @param posContainer
	 *            (POSContainer)
	 * @return posContainer (POSContainer)
	 **********************************************/
	
	public POSContainer correctCombinedTagsList(POSContainer posContainer) {

		List<String> tokenList = posContainer.getWordTokenList();
		List<String> combinedTags = posContainer.getCombinedTagsList();
		List<String> newTokenList = new ArrayList<String>();
		List<String> newCombinedTagsList = new ArrayList<String>();
		for (int i = 0; i < combinedTags.size(); i++) {
			String currentTag = combinedTags.get(i);
			String currentToken = tokenList.get(i);
			String newTag = combinedTags.get(i);
			newTag = correctMisTaggedVerbs(tokenList, combinedTags, i,
					currentTag, currentToken, newTag);
			newTag = correctMisTaggedMisc(combinedTags, i, currentTag,
					currentToken, newTag);
			newTag = correctMisTaggedDigits(combinedTags, i, currentTag,
					currentToken, newTag);

			if (tagSet.contains(currentToken)) {
				currentToken = currentToken.toLowerCase();
			}
			
			newCombinedTagsList.add(newTag);
			newTokenList.add(currentToken);
		}
		
		posContainer.setWordTokenList(newTokenList);
		
		posContainer.setCombinedTagsList(newCombinedTagsList);
		return posContainer;
	}

	/*************************************
	 * Corrects the mistagged verbs.
	 * 
	 * @param tokenList  (List<String>)
	 * @param combinedTags  (List <String>)
	 * @param i (Integer)
	 * @param currentTag (String)
	 * @param currentToken (String)
	 * @param newTag (String)
	 * @return newTag (String)
	 *************************************/
	private String correctMisTaggedVerbs(List<String> tokenList,
			List<String> combinedTags, int i, String currentTag,
			String currentToken, String newTag) {
		String currentTagLC = currentTag.toLowerCase();
		if (currentTagLC.startsWith("vb-") || 
				(currentTagLC.startsWith("nn") && !currentTagLC.startsWith("nn-state")
					&& !currentTagLC.startsWith("nn-apparatus")
					&& !currentTagLC.startsWith("nn-cycle")
					&& !currentTagLC.startsWith("nn-temp")
					&& !currentTagLC.startsWith("nn-pressure")
					&& !currentTagLC.startsWith("nn-time")
					&& !currentTagLC.startsWith("nn-molar")
					&& !currentTagLC.startsWith("nn-vacuum")
				    && !currentTagLC.startsWith("nnp")))  {
			List<String> beforeList = Arrays.asList("dt", "jj", "jj-chem", "dt-the");
			List<String> afterListJJ = Arrays.asList("jj", "nn-chementity", "nn-mixture", "nn-apparatus", "nn", "jj-chem");

			List<String> afterListNN = Arrays.asList("stop", "comma");

			if (stringBefore(beforeList, i, combinedTags)
					&& (i == combinedTags.size() || stringAfter(
							afterListNN, i, combinedTags))
					&& !currentTagLC.startsWith("nn-")) {
				newTag = "NN";
			}
				

			else if (stringBefore(beforeList, i, combinedTags)
					&& (stringAfter(afterListJJ, i, combinedTags) && 
							!currentTagLC.startsWith("nn-chementity")) && adjObjectExists(combinedTags,i)) {
				newTag = "JJ-CHEM";
			}
		}

		if (currentTagLC.startsWith("vb-precipitate")) {
			List<String> beforeList = Arrays.asList("jj", "oscar-cj", "jj-chem");
			List<String> afterList = Arrays.asList("in-of");
			List<String> notafterList = Arrays.asList("nn-time");

			if (stringBefore(beforeList, i, combinedTags)
					&& (stringAfter(afterList, i, combinedTags) || i == combinedTags
							.size())) {
				if (!stringAfter(notafterList, i, combinedTags)){
					newTag = "NN-CHEMENTITY";
				}	
			}
			if (i != 0) {
				int beforeIndex = i - 1;
				if (tokenList.get(beforeIndex).endsWith("ing")) {
					newTag = "NN-CHEMENTITY";
				}

			}
		}

		if (currentTagLC.startsWith("vb-")
				|| currentTagLC.startsWith("nn-synthesize")) {
			List<String> beforeList = Arrays.asList("dt-the", "dt");
			List<String> afterList = Arrays.asList("vb");

			if (stringBefore(beforeList, i, combinedTags)
					&& (stringAfterStartsWith(afterList, i, combinedTags))) {
				newTag = "NN-CHEMENTITY";
			}
		}

		if (currentTagLC.startsWith("vb")
				&& Utils.containsNumber(currentToken)) {
			newTag = "NN";
		}

		if (currentTagLC.startsWith("vbn")
				|| currentTagLC.startsWith("vbg")
				|| currentTagLC.startsWith("vb-")
				|| currentToken.endsWith("ed")) {

			List<String> afterList = Arrays.asList("oscar-cm", "nns", "nn-chementity", "oscar-cj", "jj-chem", "nnp");
			List<String> beforeList = Arrays.asList("dt", "rb", "rb-conj", "dt-the", "stop", "in-with", "in-of", "in-under");
			if (stringAfter(afterList, i, combinedTags)
					&& stringBefore(beforeList, i, combinedTags)) {
				newTag = "JJ-CHEM";
			}

		}

		if (currentTagLC.startsWith("vb-yield") ) {

			List<String> beforeList = Arrays.asList("nn-percent");
			if (stringBefore(beforeList, i, combinedTags)) {
				newTag = "NN-YIELD";
			}
			List<String> afterList = Arrays.asList("nn-chementity");
			beforeList = Arrays.asList("dt", "dt-the");
			if (stringAfter(afterList, i, combinedTags) && stringBefore(beforeList, i, combinedTags)){
				newTag = "JJ-COMPOUND";
			}
		}

		if (currentTagLC.startsWith("vb-filter")) {

			List<String> beforeList = Arrays.asList("oscar-cj", "jj-chem");
			if (stringBefore(beforeList, i, combinedTags)) {
				newTag = "NN";
			}

		}

		if (currentTagLC.startsWith("vb")) {

			List<String> beforeList = Arrays.asList("to");
			List<String> beforebeforeList = Arrays.asList("vb-heat");
			List<String> afterList = Arrays.asList("stop");
			if (stringBefore(beforeList, i, combinedTags)
					&& stringBefore(beforebeforeList, i - 1, combinedTags)
					&& stringAfter(afterList, i, combinedTags)) {
				newTag = "NN";
			}

		}

		if (currentTagLC.startsWith("nn-mixture")) {

			List<String> afterList = Arrays.asList("in-of");

			if (!stringAfter(afterList, i, combinedTags)) {
				newTag = "NN-CHEMENTITY";
			}

		}
		
		if (currentTagLC.startsWith("vb-dilute")) {//correct cases where dilute and the like are actually adjectives e.g. dilute sulfuric acid

			List<String> afterList = Arrays.asList("oscar-cm");

			if (stringAfter(afterList, i, combinedTags)) {
				newTag = "JJ-CHEM";
			}

		}

		/********
		 * Gerunds
		 */
		if (currentTagLC.startsWith("vb")
				&& (currentToken.toLowerCase().endsWith("ing") || currentToken
						.toLowerCase().endsWith("ed"))) {

			List<String> afterList = Arrays.asList("nn", "oscar-cm", "nns", "nn-chementity", "oscar-cj", "jj-chem", "jj", "nnp", "nn-state", "nn-apparatus");
			List<String> beforeList = Arrays.asList("dt", "dt-the", "cd", "oscar-cm");

			List<String> notList = Arrays.asList("in-of");
			if (stringAfter(afterList, i, combinedTags) && stringBefore(beforeList, i, combinedTags)) {
				newTag = "JJ";
			}
			else if (currentToken.toLowerCase().endsWith("ing") && stringBefore(beforeList, i, combinedTags) && !stringAfter(notList, i, combinedTags)) {
				newTag = "JJ-CHEM";
			}

		}

		if (currentTagLC.startsWith("vb") && !currentToken.toLowerCase().endsWith("ing")) {

			List<String> beforeList = Arrays.asList("dt", "dt-the", "in-in", "in-of", "rb");
			List<String> afterList = Arrays.asList("nn", "oscar-cm", "nns", "nn-chementity", "oscar-cj", "jj-chem", "jj", "nnp", "nn-state", "nn-apparatus");
			List<String> chemafterList = Arrays.asList("oscar-cm", "nn-chementity", "oscar-cj", "jj-chem");

			if (i != 0) {
				if (!tokenList.get(i - 1).equals("that")) {
					if (stringAfter(chemafterList, i, combinedTags)
							&& stringBefore(beforeList, i, combinedTags)) {
						newTag = "JJ-CHEM";
					}

					else if (stringBefore(beforeList, i, combinedTags)
							&& stringAfter(afterList, i, combinedTags)) {
						newTag = "JJ";
					} else if (stringBefore(beforeList, i, combinedTags)
							&& combinedTags.get(i + 1).toLowerCase()
									.startsWith("nn")) {
						newTag = "JJ";
					}

				}
			}
		}

		return newTag;
	}

	/******************************************
	 * Checks for if the adjective is describing an object.
	 * Checks if a noun tag exists before a preposition. If not then the current word is a noun and not an adjective
	 * @param combinedTags
	 * @param index
	 * @return boolean
	 */
	private boolean adjObjectExists(List<String> combinedTags, int index) {

		for (int i = index+1; i < combinedTags.size(); i++) {
			if (combinedTags.get(i).toLowerCase().startsWith("to") || combinedTags.get(i).toLowerCase().startsWith("in")){
				return false;
			}
			else if (combinedTags.get(i).toLowerCase().startsWith("nn")){
				return true;
			}
		}
		
		return false;
	}

	/*************************************
	 * Corrects the mistagged verbs.
	 * 
	 * @param combinedTags  (List <String>)
	 * @param i (Integer)
	 * @param currentTag (String)
	 * @param currentToken (String)
	 * @param newTag (String)
	 * @return newTag (String)
	 *************************************/
	private String correctMisTaggedDigits(List<String> combinedTags, int i,
			String currentTag, String currentToken, String newTag) {
		String currentTagLC = currentTag.toLowerCase();
		if (currentTagLC.equals("cd")
				|| (currentTagLC.startsWith("nn-") && Utils
						.containsNumber(currentToken))) {
			List<String> beforeList = Arrays.asList("in-of", "jj", "nn-chementity", "comma");
			List<String> afterList = Arrays.asList("-lrb-", "stop", "comma");
			if (stringBefore(beforeList, i, combinedTags)
					&& (stringAfter(afterList, i, combinedTags) || i == combinedTags
							.size())) {
				newTag = "CD-ALPHANUM";
			}
		}

		if (currentTagLC.equals("prp")
				|| currentToken.toLowerCase().startsWith("ii")) {
			List<String> beforeList = Arrays.asList("-lrb-");
			List<String> afterList = Arrays.asList("-rrb-");
			if (stringBefore(beforeList, i, combinedTags)
					&& (stringAfter(afterList, i, combinedTags))) {
				newTag = "CD-ALPHANUM";
			}
		}
		if (currentTagLC.equals("cd")) {

			List<String> afterList = Arrays.asList("stop", "comma", "-lrb-");

			if (stringAfter(afterList, i, combinedTags)
					&& !currentToken.contains(".") && currentToken.length() < 4) {
				newTag = "CD-ALPHANUM";
			}
		}

		if (currentTagLC.equals("cd-alphanum")) {

			List<String> afterList = Arrays.asList("nn-vol", "nn-mass");

			if (stringAfter(afterList, i, combinedTags)
					|| currentToken.contains(".") || currentToken.length() > 4) {
				newTag = "CD";
			}
		}
		return newTag;
	}

	/*************************************
	 * Corrects the mistagged verbs.
	 * 
	 * @param combinedTags  (List <String>)
	 * @param i (Integer)
	 * @param currentTag (String)
	 * @param currentToken (String)
	 * @param newTag (String)
	 * @return newTag (String)
	 *************************************/
	private String correctMisTaggedMisc(List<String> combinedTags, int i,
			String currentTag, String currentToken, String newTag) {
		String currentTagLC = currentTag.toLowerCase();
		if (currentTagLC.equals("nnp")
				&& StringUtils.equalsIgnoreCase(currentToken, "M")) {
			newTag = "NN-MOLAR";
		}
		if (i != 0 && currentTagLC.equals("nns")) {

			List<String> beforeList = Arrays.asList("stop");
			if (currentToken.endsWith("s")
					&& Character.isUpperCase(currentToken.charAt(0))) {
				if (!stringBefore(beforeList, i, combinedTags)){
					newTag = "NNPS";
				}
			}
		}

		if (currentTagLC.equals("rb")	&& currentToken.length() < 2) {

			if (Character.isUpperCase(currentToken.charAt(0))){
				newTag = "NNP";
			}
			else {
				newTag = "NN";
			}

		}
		if (i != 0 && currentTagLC.equals("nn")) {

			List<String> beforeList = Arrays.asList("stop");
			if (Character.isUpperCase(currentToken.charAt(0)) && !stringBefore(beforeList, i, combinedTags)){
				newTag = "NNP";
			}

		}

		if (i != 0	&& (currentTagLC.startsWith("vb") || currentTagLC.startsWith("nn-"))) {

			List<String> beforeList = Arrays.asList("stop", "rrb", "comma");
			List<String> afterList = Arrays.asList("nnp", "nns", "nn", "nnp-acronym");
			if (Character.isUpperCase(currentToken.charAt(0))
					&& !stringBefore(beforeList, i, combinedTags)
					&& stringAfter(afterList, i, combinedTags)){
				newTag = "JJ-CHEM";
			}

		}
		if (currentToken.equals("M")) {

			List<String> beforeList = Arrays.asList("cd");

			if (stringBefore(beforeList, i, combinedTags)) {
				newTag = "NN-MOLAR";
			}
		}

		if (currentToken.equals("K")) {

			List<String> beforeList = Arrays.asList("cd");

			if (stringBefore(beforeList, i, combinedTags)) {
				newTag = "NN-TEMP";
			}
		}
		if (currentTagLC.equals("''")) {
			newTag = "FW";
		}
		if (currentTagLC.equals("nn-mixture")) {

			List<String> afterList = Arrays.asList("vbd");

			if (stringAfter(afterList, i, combinedTags)) {
				newTag = "NN-CHEMENTITY";
			}
		}

		if (currentTagLC.startsWith("nn-concentrate")) {
			List<String> beforeList = Arrays.asList("jj", "oscar-cj", "jj-chem");
			List<String> afterList = Arrays.asList("in-of");

			if (!stringBefore(beforeList, i, combinedTags)
					&& (stringAfter(afterList, i, combinedTags))) {
				newTag = "NN";
			}
		}

		if (i != 0 && currentTagLC.startsWith("nn-add")) {
			List<String> beforeList = Arrays.asList("stop", "comma", "colon");
			if (!stringBefore(beforeList, i, combinedTags)&& Character.isUpperCase(currentToken.charAt(0))) {
				newTag = "NNP";
			}

		}

		if (currentTagLC.startsWith("jj")
				|| currentTagLC.startsWith("nnp")) {
			List<String> afterList = Arrays.asList("nn-campaign");
			if ((stringAfter(afterList, i, combinedTags) || string2after(
					afterList, i, combinedTags))
					&& Character.isUpperCase(currentToken.charAt(0))) {
				newTag = "NNP";
			}
		}
		if (currentToken.toLowerCase().equals("addition")) {
			List<String> beforeList = Arrays.asList("in-in");
			List<String> afterList = Arrays.asList("comma", "stop");

			if (stringBefore(beforeList, i, combinedTags)
					&& (stringAfter(afterList, i, combinedTags))) {
				newTag = "NN";
			}
		}

		if (currentToken.toLowerCase().startsWith("obtain")) {

			List<String> afterList = Arrays.asList("in-from");

			List<String> after2List = Arrays.asList("nnp");
			if (stringAfter(afterList, i, combinedTags)
					&& (string2after(after2List, i, combinedTags))) {
				newTag = "VB";
			}
		}
		if (currentTagLC.startsWith("nn-synthesize")) {

			List<String> afterList = Arrays.asList("nn-apparatus");
			List<String> beforeList = Arrays.asList("dt", "nn-apparatus", "rb-conj", "dt-the");
			if (stringAfter(afterList, i, combinedTags)
					&& stringBefore(beforeList, i, combinedTags)) {
				newTag = "JJ-CHEM";
			}

		}

		if (currentToken.equals("D")
				&& currentTagLC.equals("nn-time")) {

			List<String> beforeList = Arrays.asList("in-in");
			if (stringBefore(beforeList, i, combinedTags)) {
				newTag = "NN";
			}

		}
		return newTag;

	}

	/***********************************
	 * A boolean function that checks for the tokens before the current token.
	 * 
	 * @param beforeList (List<String>)
	 * @param index (Integer)
	 * @param combinedTags (List<String>)
	 * @return boolean
	 ***********************************/
	private boolean stringBefore(List<String> beforeList, int index,
			List<String> combinedTags) {
		
		if (index != 0) {
			int beforeIndex = index - 1;
			if (beforeList.contains(combinedTags.get(beforeIndex).toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	/**********************************
	 * A boolean function that checks for the tokens after the current token.
	 * 
	 * @param afterList (List<String>)
	 * @param index (Integer)
	 * @param combinedTags (List<String>)
	 * @return boolean
	 **********************************/
	private boolean stringAfter(List<String> afterList, int index,
			List<String> combinedTags) {

		int afterIndex = index + 1;
		if (afterIndex < combinedTags.size()) {
			if (afterList.contains(combinedTags.get(afterIndex).toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	/**********************************
	 * A boolean function that checks for what the tokens after the current
	 * token starts with.
	 * 
	 * @param afterList (List<String>)
	 * @param index (Integer)
	 * @param combinedTags (List<String>)
	 * @return boolean
	 **********************************/
	private boolean stringAfterStartsWith(List<String> afterList, int index,
			List<String> combinedTags) {
		
		int afterIndex = index + 1;
		if (afterIndex < combinedTags.size()) {
			for (String nextWord : afterList) {
				if (combinedTags.get(afterIndex).toLowerCase().startsWith(nextWord)) {
					return true;
				}
			}
		}
		return false;
	}

	/**********************************
	 * A boolean function that checks for the tokens after the current token.
	 * 
	 * @param afterList (List<String>)
	 * @param index (Integer)
	 * @param combinedTags (List<String>)
	 * @return boolean
	 **********************************/
	private boolean string2after(List<String> afterList, int index,
			List<String> combinedTags) {

		int after2Index = index + 2;
		if (after2Index < combinedTags.size()) {
			if (afterList.contains(combinedTags.get(after2Index).toLowerCase())) {
				return true;
			}
		}
		return false;
	}

}