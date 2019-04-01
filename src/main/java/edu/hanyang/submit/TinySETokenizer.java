package edu.hanyang.submit;

import java.util.List;

import edu.hanyang.indexer.Tokenizer;

import java.io.IOException;
import java.util.*;

import org.tartarus.snowball.ext.PorterStemmer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class TinySETokenizer implements Tokenizer {
	private SimpleAnalyzer Analyzer;
	private PorterStemmer Stemmer;
	
	public void setup() {
		Analyzer = new SimpleAnalyzer();
		Stemmer = new PorterStemmer();
	}
	
	public List<String> split(String text){
		List<String> result = new ArrayList<>();
		TokenStream tokenStream = Analyzer.tokenStream("fieldName", text);
		CharTermAttribute termAtt = tokenStream.addAttribute(CharTermAttribute.class);
		try {
			tokenStream.reset();
			while(tokenStream.incrementToken()) {
				String a = (termAtt.toString());
				Stemmer.setCurrent(a);
				Stemmer.stem();
				result.add(Stemmer.getCurrent());
			}
			tokenStream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return result;
	}
	
	public void clean() {
		Analyzer.close();
	}
}
