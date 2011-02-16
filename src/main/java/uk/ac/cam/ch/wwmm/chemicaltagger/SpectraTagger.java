package uk.ac.cam.ch.wwmm.chemicaltagger;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscardata.DataAnnotation;
import uk.ac.cam.ch.wwmm.oscardata.DataParser;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;


public class SpectraTagger {
	
	
	
	
	public POSContainer runTagger(POSContainer posContainer){
		Tokeniser tokeniser = Tokeniser.getInstance();
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance()
				.makeTokenisedDocument(tokeniser, posContainer.getInputText());

		
		List<DataAnnotation> annotations = DataParser.findData(procDoc);

		StringBuilder newInputText = new StringBuilder();
		String sentence = posContainer.getInputText();
		int offset = 0;
	
		List<Element> spectraList = new ArrayList<Element>();
		for (DataAnnotation dataAnnotation : annotations) {
			if (dataAnnotation.getAnnotatedElement().getLocalName().equals("spectrum")){
			   spectraList.add(dataAnnotation.getAnnotatedElement());
			   newInputText.append(sentence.substring(offset,dataAnnotation.getStart()));
			   offset = dataAnnotation.getEnd();
			   
			}
			 
		}
		newInputText.append(sentence.substring(offset,sentence.length()));
		posContainer.setInputText(newInputText.toString());
		posContainer.setSpectrumList(spectraList);
		return posContainer;
	}

}