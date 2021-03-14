package org.harze.yuyukobot.processor.message;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import org.harze.yuyukobot.Utils;
import org.harze.yuyukobot.analyzer.AnalysisResult;
import org.harze.yuyukobot.analyzer.LuceneAnalyzers;
import org.harze.yuyukobot.analyzer.MessageAnalyzer;
import org.harze.yuyukobot.configuration.BotInfo;
import org.harze.yuyukobot.database.entities.DiscordUser;
import org.harze.yuyukobot.database.entities.GeneralContent;
import org.harze.yuyukobot.database.service.GeneralContentService;
import org.harze.yuyukobot.processor.MessageCreatedProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SearchProcessor implements MessageCreatedProcessor {

    private static final Integer DEFAULT_LIMIT = 5;
    private static final String OR_OPERATOR = "or";
    private static final DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
    private static final Map<String, Set<String>> searchVerbFollowUps = Map.ofEntries(
            Map.entry("give", Set.of("me", "us")),
            Map.entry("tell", Set.of("me", "us")),
            Map.entry("show", Set.of("me", "us")),
            Map.entry("fetch", Set.of("me", "us")),
            Map.entry("search", Set.of("me", "us"))
    );
    private static final Set<String> searchVerbs = searchVerbFollowUps.keySet();
    private static final String EXAMPLE = "Yuyuko, show me the truth"; // TODO load from BBDD

    @Autowired
    private BotInfo botInfo;
    @Autowired
    private MessageAnalyzer messageAnalyzer;
    @Autowired
    private GeneralContentService generalContentService;

    private String generateMultiVerbErrorResponse(AnalysisResult analysisResult) {
        // TODO properly with a table of texts for String.format()
        return "Sorry, you used **too many words about searching** content :confounded: \n" +
                "Try something like this:\n" + EXAMPLE;
    }

    private String generateNoContentErrorResponse(AnalysisResult analysisResult) {
        // TODO properly
        return "Sorry, but I can't see **anything to search** :neutral_face: \n" +
                "Try something like this:\n" + EXAMPLE;
    }

    private String generateNoTagsErrorResponse(AnalysisResult analysisResult) {
        // TODO properly
        return "Sorry, but I can't search without **any information** :sweat_smile: \n" +
                "Try something like this:\n" + EXAMPLE;
    }

    private String generateContentResponse(GeneralContent generalContent) {
        return generalContent.getContent() + "\n\n"
                + "Uploaded by " + generalContent.getDiscordUser().getUsername()
                + " at " + formatter.format(generalContent.getUpdatedAt());
    }

    private List<String> process(AnalysisResult analysisResult, Message message, String verb, DiscordUser author) {

        // Confirm that the is content after the verb that indicates the intent to search
        List<String> postVerbWords = analysisResult.getWords().stream()
                .dropWhile(word -> !verb.equals(LuceneAnalyzers.englishAnalyzeWord(word)))
                .dropWhile(word -> verb.equals(LuceneAnalyzers.englishAnalyzeWord(word)))
                .collect(Collectors.toList());
        if (postVerbWords.isEmpty())
            return List.of(generateNoContentErrorResponse(analysisResult));

        // Remove the follow ups to the verb
        if (searchVerbFollowUps.get(verb).contains(postVerbWords.get(0)))
            postVerbWords.remove(0);

        // TODO if a number comes up now, stablish it as the limit
        // A word like "a" or "an" would mean a 1 too

        // Extract the info for searching, tags too short are discarded
        List<String> info = postVerbWords.stream()
                .filter(word -> OR_OPERATOR.equals(word) || word.length() >= 3) // Ignore tags too (except OR)
                .collect(Collectors.toList());

        // If there is no info to search
        if (info.isEmpty())
            return List.of(generateNoTagsErrorResponse(analysisResult));

        // Split the tags by any OR into clauses
        List<List<String>> clauses = new ArrayList<>();
        List<String> currentClauses = new ArrayList<>();
        for (String infoTag : info) {
            if (OR_OPERATOR.equalsIgnoreCase(infoTag)) {
                if (!currentClauses.isEmpty()) {
                    clauses.add(currentClauses);
                    currentClauses = new ArrayList<>();
                }
            } else
                currentClauses.add(infoTag);
        }
        clauses.add(currentClauses);

        // Tag mapping (internal tags are english processed)
        List<List<String>> tagClauses = clauses.stream().map(andClause -> {
            List<String> tags = andClause.stream()
                    .filter(infoTag -> !analysisResult.getQuotes().contains(infoTag))
                    .map(LuceneAnalyzers::englishAnalyzeWord)
                    .filter(infoTag -> !infoTag.isBlank())
                    .collect(Collectors.toList());
            List<String> quotedTags = andClause.stream()
                    .filter(infoTag -> analysisResult.getQuotes().contains(infoTag))
                    .map(infoTag -> infoTag.replace("'", "")) // Sacrifice the ' so tsvector type works properly
                    .filter(infoTag -> !infoTag.isBlank())
                    .collect(Collectors.toList());
            tags.addAll(quotedTags);
            return tags;
        }).collect(Collectors.toList());

        return generalContentService.queryRanked(Utils.tsQueryBuilder(tagClauses), DEFAULT_LIMIT).stream()
                .map(this::generateContentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> generateResponse(MessageCreateEvent event, DiscordUser author) {

        Message message = event.getMessage();
        AnalysisResult analysisResult = messageAnalyzer.analyze(message.getContent());

        // Not enough words, drop attempt
        if (analysisResult.getMeaning().isEmpty() || analysisResult.getMeaning().size() < 2)
            return List.of();

        // Not calling the bot, drop
        if (!botInfo.getBotName().equals(analysisResult.getMeaning().get(0)))
            return List.of();

        // Detect the store intent
        Set<String> searchVerbsUsed = new HashSet<>(analysisResult.getMeaning());
        searchVerbsUsed.retainAll(searchVerbs);

        // Allow only a single verb with store as a meaning
        if (searchVerbsUsed.isEmpty())
            return List.of();
        else if (searchVerbsUsed.size() > 1)
            return List.of(generateMultiVerbErrorResponse(analysisResult));
        else
            return process(analysisResult, message, searchVerbsUsed.stream().findFirst().get(), author);
    }
}
