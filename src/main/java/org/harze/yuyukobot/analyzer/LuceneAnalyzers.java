package org.harze.yuyukobot.analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LuceneAnalyzers {

    private static final Logger log = LoggerFactory.getLogger(LuceneAnalyzers.class);

    public static List<String> analyze(String message, Analyzer analyzer) {
        try {
            List<String> result = new ArrayList<>();
            TokenStream tokenStream = analyzer.tokenStream(null, message);
            CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                if (!attr.toString().isBlank())
                    result.add(attr.toString());
            }
            return result;
        } catch (IOException e) {
            log.error("Error while analyzing with {} the content: {}", analyzer.getClass().getSimpleName(), message, e);
            return Collections.emptyList();
        }
    }

    public static List<String> analyze(List<String> words, Analyzer analyzer) {
        return analyze(String.join(" ", words), analyzer);
    }

    public static List<String> englishAnalyze(String message) {
        return analyze(message, new EnglishAnalyzer());
    }

    public static List<String> englishAnalyze(List<String> words) {
        return analyze(words, new EnglishAnalyzer());
    }

    public static List<String> simpleAnalyze(String message) {
        return analyze(message, new SimpleAnalyzer());
    }

    public static List<String> simpleAnalyze(List<String> words) {
        return analyze(words, new SimpleAnalyzer());
    }

    public static List<String> whitespaceAnalyze(String message) {
        return analyze(message, new WhitespaceAnalyzer());
    }

    public static List<String> whitespaceAnalyze(List<String> words) {
        return analyze(words, new WhitespaceAnalyzer());
    }

    public static String englishAnalyzeWord(String word) {
        List<String> englishWord = analyze(word, new EnglishAnalyzer());
        return englishWord.isEmpty() ? "" : englishWord.get(0);
    }
}
