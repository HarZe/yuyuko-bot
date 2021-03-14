package org.harze.yuyukobot.processor.message;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import org.harze.yuyukobot.Utils;
import org.harze.yuyukobot.analyzer.AnalysisResult;
import org.harze.yuyukobot.analyzer.LuceneAnalyzers;
import org.harze.yuyukobot.analyzer.MessageAnalyzer;
import org.harze.yuyukobot.configuration.BotInfo;
import org.harze.yuyukobot.database.entities.DiscordUser;
import org.harze.yuyukobot.database.entities.StoreRequest;
import org.harze.yuyukobot.database.service.StoreRequestService;
import org.harze.yuyukobot.processor.MessageCreatedProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class StoreProcessor implements MessageCreatedProcessor {

    private static final Set<String> storeVerbs = Set.of("store", "save", "rememb", "import");
    private static final String TAG_SEPARATOR = "as";
    private static final String EXAMPLE = "Yuyuko, please **store** this _link_ **as** a _pretty artwork_ https://www.pixiv.net/en/artworks/75494207"; // TODO load from BBDD

    @Autowired
    private BotInfo botInfo;
    @Autowired
    private MessageAnalyzer messageAnalyzer;
    @Autowired
    private StoreRequestService storeRequestService;

    private String simpleQuotedList(List<String> list, String joiner) {
        return list.stream().map(t -> "`" + t + "`").collect(Collectors.joining(joiner));
    }

    private String generateMultiVerbErrorResponse(AnalysisResult analysisResult) {
        // TODO properly with a table of texts for String.format()
        return "Sorry, you used **too many words about storing** content :confounded: \n" +
                "Try something like this:\n" + EXAMPLE;
    }

    private String generateNoContentErrorResponse(AnalysisResult analysisResult) {
        // TODO properly
        return "Sorry, but I can't see **anything to store** :neutral_face: \n" +
                "Try something like this:\n" + EXAMPLE;
    }

    private String generateNoTagsErrorResponse(AnalysisResult analysisResult) {
        // TODO properly
        return "Sorry, but I won't store something without some **information to search** for it later :sweat_smile: \n" +
                "Try something like this:\n" + EXAMPLE;
    }

    private String generateConfirmationResponse(List<String> content, List<String> tags, boolean contentUrl) {
        return "**Do you want to store " +
                (contentUrl ? (content.size() == 1 ? "this link" : "these links") : "this text") + "?**" +
                (contentUrl ? "\n" : ("\n" + simpleQuotedList(content, "\n") + "\n")) +
                "Classified as: " + simpleQuotedList(tags, ", ") +
                "\nReact with :thumbsup: to confirm";
    }

    private String tagify(Collection<String> tags) {
        return tags.stream().sorted().map(t -> "'" + t + "'").collect(Collectors.joining(" "));
    }

    private synchronized void createStoreRequest(DiscordUser author, String messageId, String content, Map<String, String> tags) {
        StoreRequest storeRequest = new StoreRequest();
        storeRequest.setId(UUID.randomUUID());
        storeRequest.setDiscordUser(author);
        storeRequest.setRequestMessageId(messageId);
        storeRequest.setContent(content);
        storeRequest.setTags(tagify(tags.keySet()));
        storeRequest.setReadableTags(tagify(tags.values()));
        storeRequest.setCreatedAt(Utils.now());
        storeRequestService.create(storeRequest);
    }

    private String process(AnalysisResult analysisResult, Message message, String verb, DiscordUser author) {

        // Confirm that the is content after the verb that indicates the intent to store
        List<String> postVerbWords = analysisResult.getWords().stream()
                .dropWhile(word -> !verb.equals(LuceneAnalyzers.englishAnalyzeWord(word)))
                .dropWhile(word -> verb.equals(LuceneAnalyzers.englishAnalyzeWord(word)))
                .collect(Collectors.toList());
        if (postVerbWords.isEmpty())
            return generateNoContentErrorResponse(analysisResult);

        // Looking for the content, either URLs or text
        if (!postVerbWords.contains(TAG_SEPARATOR))
            return generateNoTagsErrorResponse(analysisResult);

        // Extract the content and the info to search it
        List<String> content = postVerbWords.stream()
                .takeWhile(word -> !TAG_SEPARATOR.equals(word))
                .collect(Collectors.toList());
        List<String> info = postVerbWords.stream()
                .dropWhile(word -> !TAG_SEPARATOR.equals(word))
                .dropWhile(TAG_SEPARATOR::equals)
                .filter(word -> word.length() >= 3) // Ignore tags too short
                .collect(Collectors.toList());

        // If there is no info to classify the content
        if (info.isEmpty())
            return generateNoTagsErrorResponse(analysisResult);

        // If there are URLs, the content is those URLs
        boolean urlContent = !analysisResult.getUrls().isEmpty();
        if (urlContent) {
            content = new ArrayList<>(analysisResult.getUrls());
        }

        // If there is content to store
        if (content.isEmpty())
            return generateNoContentErrorResponse(analysisResult);

        // Tag mapping (internal tags are english processed)
        Map<String, String> tags = info.stream()
                .filter(infoTag -> !analysisResult.getQuotes().contains(infoTag))
                .filter(infoTag -> !LuceneAnalyzers.englishAnalyzeWord(infoTag).isBlank())
                .collect(Collectors.toMap(LuceneAnalyzers::englishAnalyzeWord, String::toString));
        Map<String, String> quotedTags = info.stream()
                .filter(infoTag -> analysisResult.getQuotes().contains(infoTag))
                .map(infoTag -> infoTag.replace("'", "")) // Sacrifice the ' so tsvector type works properly
                .filter(infoTag -> !infoTag.isBlank())
                .collect(Collectors.toMap(String::toString, String::toString));
        tags.putAll(quotedTags);

        // Store the request
        createStoreRequest(author, message.getId().asString(), String.join(" ", content), tags);

        // Generate a confirmation response message
        List<String> sortedEnglishTags = tags.values().stream().sorted().collect(Collectors.toList());
        return generateConfirmationResponse(content, sortedEnglishTags, urlContent);
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
        Set<String> storeVerbsUsed = new HashSet<>(analysisResult.getMeaning());
        storeVerbsUsed.retainAll(storeVerbs);

        // Allow only a single verb with store as a meaning
        if (storeVerbsUsed.isEmpty())
            return List.of();
        else if (storeVerbsUsed.size() > 1)
            return List.of(generateMultiVerbErrorResponse(analysisResult));
        else
            return List.of(process(analysisResult, message, storeVerbsUsed.stream().findFirst().get(), author));
    }
}
