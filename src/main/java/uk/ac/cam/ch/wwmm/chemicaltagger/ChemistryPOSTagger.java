package uk.ac.cam.ch.wwmm.chemicaltagger;

import uk.ac.cam.ch.wwmm.oscar.Oscar;


/**************************************************************
 * Converts string input into tokenised and tagged text.
 * 
 * Runs a tokenisers 4 taggers against the input:
 * - Spectra: recognises and pulls out spectra information. 
 * - OSCAR  : recognises chemical entities. 
 * - Regex  : recognises chemistry related words that are not
 * recognised by OSCAR . 
 * - OpenNLP: recognises common english parts of speech .
 * It then combines the output of OSCAR, Regex and OpenNLP
 * taggers and then performs postprocessing on tags
 * 
 * @author lh359, dmj30, dl387
 ***************************************************************/
public final class ChemistryPOSTagger {

	static final boolean DEFAULT_PRIORITISE_OSCAR = false;
	static final boolean DEFAULT_USE_SPECTRA_TAGGER = false;
		
	private OscarTagger oscarTagger;
	private RegexTagger regexTagger;
	private OpenNLPTagger openNLPTagger;
	private ChemicalTaggerTokeniser ctTokeniser;
	
	/**************************************
	 * Private Singleton holder.
	 ***************************************/
	private static class TaggerHolder {
		private static final ChemistryPOSTagger INSTANCE = new ChemistryPOSTagger();
	}
	
	/**************************************
	 * Gets the default ChemistryPOSTagger instance - recommended for
	 * standard ChemicalTagger processing.
	 * 
	 * @return ChemistryPOSTagger instance
	 ***************************************/
	public static ChemistryPOSTagger getDefaultInstance() {
		return TaggerHolder.INSTANCE;
	}

	
	/**
	 * Custom constructor for setting up non-standard ChemicalTagger operations
	 */
	public ChemistryPOSTagger (ChemicalTaggerTokeniser ctTokeniser, 
			OscarTagger oscarTagger, RegexTagger regexTagger, OpenNLPTagger openNLPTagger) {
		
		this.ctTokeniser = ctTokeniser;
		this.oscarTagger = oscarTagger;
		this.regexTagger = regexTagger;
		this.openNLPTagger = openNLPTagger;
	}
	
	private ChemistryPOSTagger() {
		Oscar oscar = new Oscar();
		ctTokeniser = new OscarTokeniser(oscar);
		oscarTagger = new OscarTagger(oscar);
		regexTagger = new RegexTagger();
		openNLPTagger = OpenNLPTagger.getInstance();
	}


	/**************************************
	 * Getter method for ChemicalTaggerTokeniser.
	 * @return ctTokeniser (ChemicalTaggerTokeniser)
	 ***************************************/
	public ChemicalTaggerTokeniser getCTTokeniser() {
		return ctTokeniser;
	}
	
	/**************************************
	 * Getter method for RegexTagger.
	 * @return regexTagger (RegexTagger)
	 ***************************************/
	public RegexTagger getRegexTagger() {
		return regexTagger;
	}
	
	/**************************************
	 * Getter method for OscarTagger.
	 * @return oscarTagger (OscarTagger)
	 ***************************************/
	public OscarTagger getOscarTagger() {
		return oscarTagger;
	}
	
	/**************************************
	 * Getter method for OpenNLPTagger.
	 * @return openNLPTagger (OpenNLPTagger)
	 ***************************************/
	public OpenNLPTagger getOpenNLPTagger() {
		return openNLPTagger;
	}
	
	
	/*****************************************************
	 * Overloading method for runTaggers passing the default flags
	 * for prioritiseOscar and useSpectraTagger to
	 * {@link ChemistryPOSTagger#runTaggers(String, boolean, boolean)}
	 * 
	 * @param inputSentence(String)
	 * @return POSContainer
	 *****************************************************/
	public POSContainer runTaggers(final String inputSentence) {
		return runTaggers(inputSentence, DEFAULT_PRIORITISE_OSCAR, DEFAULT_USE_SPECTRA_TAGGER);
	}
	
	
	/*****************************************************
	 * Normalises the inputSentence then runs Tokeniser and Taggers on them.
	 * and returns a POSContainer object.
	 * Prioritises OSCAR tags if prioritiseOscar is True
	 * else it prioritises regexTagger.
	 *****************************************************/
	public POSContainer runTaggers(String inputSentence, final boolean prioritiseOscar, boolean useSpectraTagger) {
		
		POSContainer posContainer = new POSContainer();
		posContainer = normaliseAndTokeniseInput(inputSentence, posContainer, useSpectraTagger);		
		posContainer = oscarTagger.runTagger(posContainer);
		posContainer = regexTagger.runTagger(posContainer);
		posContainer = openNLPTagger.runTagger(posContainer);
		posContainer.setPrioritiseOscar(prioritiseOscar);
		posContainer.combineTaggers();	
		posContainer = RecombineTokens.recombineHyphenedTokens(posContainer);
		posContainer =  new PostProcessTags().correctCombinedTagsList(posContainer);
		return posContainer;
	}
	
	
	/*******************************************
	 * Normalises the inputText, extracts the spectra if required and then passes it to the relevant tokeniser.
	 * @param inputSentence (String)
	 * @param posContainer  (POSContainer)
	 * @param useSpectraTagger (boolean)
	 * @return posContainer (POSContainer)
	 */
	private POSContainer normaliseAndTokeniseInput(String inputSentence,POSContainer posContainer, boolean useSpectraTagger) {
		inputSentence = Formatter.normaliseText(inputSentence);
		posContainer.setInputText(inputSentence);
		if (useSpectraTagger){
		    posContainer = SpectraTagger.runTagger(posContainer);
		}
		posContainer = ctTokeniser.tokenise(posContainer);
		return posContainer;
	}
		
		
}
