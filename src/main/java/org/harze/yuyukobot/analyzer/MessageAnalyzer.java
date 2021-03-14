package org.harze.yuyukobot.analyzer;

import org.harze.yuyukobot.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MessageAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(MessageAnalyzer.class);

    public Set<String> urls(List<String> whitespaceWords) {
        if (whitespaceWords.isEmpty())
            return Collections.emptySet();
        return whitespaceWords.stream()
                .filter(Utils::validUrl)
                .collect(Collectors.toSet());
    }

    public List<String> removeUrls(List<String> whitespaceWords) {
        if (whitespaceWords.isEmpty())
            return Collections.emptyList();
        return whitespaceWords.stream()
                .filter(s -> !s.contains("://"))
                .collect(Collectors.toList());
    }

    public AnalysisResult analyze(String message) {

        if (message == null || message.isBlank())
            return AnalysisResult.builder().build();

        // 1st phase - Remove the URLs and store them separately

        List<String> whitespaceAnalysis = LuceneAnalyzers.whitespaceAnalyze(message); // Remove whitespace between words
        Set<String> urls = urls(whitespaceAnalysis);                                  // Detect the URLs
        List<String> words = removeUrls(whitespaceAnalysis);                          // Unaltered words with not URLs

        // 2nd phase - Separating the quoted text maintaining the order of the words

        List<String> unquotedTokens = new ArrayList<>();
        List<String> quotedTokens = new ArrayList<>();

        // Getting the quoted tokens assuming it starts without quotes
        int index = 0;
        for (String token : Arrays.stream(String.join(" ", words).split("\"")).collect(Collectors.toList())) {
            if (index % 2 == 0)
                unquotedTokens.add(token);
            else
                quotedTokens.add(token);
            index++;
        }

        // 3rd phase - Combine separately: the simple words cleaned up keeping quoted text as words, and the meaningful non-quoted text as words

        // Combining the tokens, cleaning the not quoted tokens
        List<String> cleanWords = new ArrayList<>();
        List<String> meaningfulWords = new ArrayList<>();
        List<String> quotes = new ArrayList<>();
        int unQuotedIdx = 0;
        int quotedIdx = 0;
        while (unQuotedIdx < unquotedTokens.size() || quotedIdx < quotedTokens.size()) {
            if (unQuotedIdx < unquotedTokens.size()) {
                List<String> simpleWords = LuceneAnalyzers.simpleAnalyze(unquotedTokens.get(unQuotedIdx));
                cleanWords.addAll(simpleWords);
                meaningfulWords.addAll(LuceneAnalyzers.englishAnalyze(simpleWords));
                unQuotedIdx++;
            }
            if (quotedIdx < quotedTokens.size()) {
                cleanWords.add(quotedTokens.get(quotedIdx));
                quotes.add(quotedTokens.get(quotedIdx));
                quotedIdx++;
            }
        }

        // Finally combine all into a POJO
        return AnalysisResult.builder()
                .meaning(meaningfulWords)
                .quotes(quotes)
                .words(cleanWords)
                .urls(urls)
                .build();
    }
}
