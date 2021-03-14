package org.harze.yuyukobot.analyzer;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Set;

@Builder
@Getter
public class AnalysisResult {
    private final List<String> meaning;
    private final List<String> words;
    private final List<String> quotes;
    private final Set<String> urls;
}
