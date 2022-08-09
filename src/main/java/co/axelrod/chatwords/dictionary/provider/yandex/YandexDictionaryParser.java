package co.axelrod.chatwords.dictionary.provider.yandex;

import co.axelrod.chatwords.dictionary.provider.yandex.model.dictionary.Definition;
import co.axelrod.chatwords.dictionary.provider.yandex.model.dictionary.Mean;
import co.axelrod.chatwords.dictionary.provider.yandex.model.dictionary.Synonym;
import co.axelrod.chatwords.dictionary.provider.yandex.model.dictionary.YandexDictionaryResponse;
import lombok.Builder;
import lombok.experimental.UtilityClass;

import java.util.stream.Collectors;

@UtilityClass
public class YandexDictionaryParser {
    @Builder
    static class TranslationDetail {
        int minFrequency;
        int maxFrequency;
        int maxDefinitions;
        int maxTranslations;
        int maxSynonyms;
        int maxMeans;
        int maxExamples;
    }

    private TranslationDetail shortTranslation() {
        return TranslationDetail.builder()
                .minFrequency(3)
                .maxFrequency(10)
                .maxDefinitions(2)
                .maxTranslations(2)
                .maxSynonyms(0)
                .maxMeans(0)
                .maxExamples(1)
                .build();
    }

    private TranslationDetail detailedTranslation() {
        return TranslationDetail.builder()
                .minFrequency(0)
                .maxFrequency(10)
                .maxDefinitions(Integer.MAX_VALUE)
                .maxTranslations(Integer.MAX_VALUE)
                .maxSynonyms(Integer.MAX_VALUE)
                .maxMeans(Integer.MAX_VALUE)
                .maxExamples(Integer.MAX_VALUE)
                .build();
    }

    public static String requestWord(Definition definition) {
        return definition.getText();
    }

    public static String shortTranslation(Definition definition) {
        return definition.getTr().get(0).getText();
    }

    public static String fullTranslation(String sourceWord, String targetWord, YandexDictionaryResponse response) {
        return formatTranslation(sourceWord, targetWord, response, shortTranslation());
    }

    public static String fullDetailedTranslation(String sourceWord, String targetWord, YandexDictionaryResponse response) {
        return formatTranslation(sourceWord, targetWord, response, detailedTranslation());
    }

    private static String formatTranslation(String sourceWord, String targetWord, YandexDictionaryResponse response, TranslationDetail details) {
        try {
            StringBuilder text = new StringBuilder();

            // Повышаем приоритет перевода искомого слова
            response.getDef().forEach(definition -> {
                if (definition.getText().equalsIgnoreCase(sourceWord) || definition.getText().equalsIgnoreCase(targetWord)) {
                    definition.getTr().forEach(translation -> {
                        Integer currentFrequency = (Integer) translation.getAdditionalProperties().get("fr");
                        translation.getAdditionalProperties().put("fr", currentFrequency + details.maxFrequency);
                    });
                }
            });

            // Удаляем переводы с низкой частотой использования
            response.getDef().forEach(definition -> definition.getTr().removeIf(translation -> {
                Integer frequency = (Integer) translation.getAdditionalProperties().get("fr");
                return frequency != null && frequency < details.minFrequency;
            }));

            // Удаляем определения без переводов (в том числе с ранее удаленными переводами)
            response.getDef().removeIf(definition -> definition.getTr().isEmpty());

            // Сортируем перевод по частоте использования
            response.getDef().sort((firstDefinition, secondDefinition) -> {
                Integer firstMaxFrequency = (Integer) firstDefinition.getTr().get(0).getAdditionalProperties().get("fr");
                Integer secondMaxFrequency = (Integer) secondDefinition.getTr().get(0).getAdditionalProperties().get("fr");

                if (firstMaxFrequency != null && secondMaxFrequency != null) {
                    return secondMaxFrequency.compareTo(firstMaxFrequency);
                } else {
                    return 0;
                }
            });

            // Сокращаем количество переводов
            response.getDef()
                    .stream()
                    .limit(details.maxDefinitions)
                    .forEach(definition -> {
                                text
                                        .append("<b>")
                                        .append(definition.getText())
                                        .append("</b>")
                                        .append(" ")
                                        .append("<i>")
                                        .append(definition.getPos())
                                        .append("</i>");

                                if (definition.getAdditionalProperties().containsKey("ts")) {
                                    text
                                            .append("\n")
                                            .append(definition.getAdditionalProperties().get("ts"));
                                }

                                if (definition.getAdditionalProperties().containsKey("gen") && definition.getAdditionalProperties().containsKey("anm")) {
                                    text
                                            .append("\n")
                                            .append(definition.getAdditionalProperties().get("gen"))
                                            .append(", ")
                                            .append(definition.getAdditionalProperties().get("anm"));
                                }

                                text.append(definition.getTr()
                                        .stream()
                                        .limit(details.maxTranslations)
                                        .map(tr -> {
                                            StringBuilder translation = new StringBuilder();
                                            translation
                                                    .append("\n- ")
                                                    .append(tr.getText());

                                            // Сортируем синонимы по частоте использования
                                            // TODO
//                                            tr.getSyn().sort((firstSynonym, secondSynonym) -> {
//                                                Integer firstMaxFrequency = (Integer) firstSynonym.getAdditionalProperties().get("fr");
//                                                Integer secondMaxFrequency = (Integer) secondSynonym.getAdditionalProperties().get("fr");
//
//                                                if (firstMaxFrequency != null && secondMaxFrequency != null) {
//                                                    return secondMaxFrequency.compareTo(firstMaxFrequency);
//                                                } else {
//                                                    return 0;
//                                                }
//                                            });

                                            // Синонимы
                                            if (tr.getSyn() != null && !tr.getSyn().isEmpty() && details.maxSynonyms > 0) {
                                                translation.append(", ");
                                                translation.append(tr.getSyn().stream()
                                                        .limit(details.maxSynonyms)
                                                        .map(Synonym::getText)
                                                        .collect(Collectors.joining(", "))
                                                );
                                            }

                                            // Примеры
                                            if (tr.getMean() != null && !tr.getMean().isEmpty() && details.maxMeans > 0) {
                                                translation.append(" (");
                                                translation.append(tr.getMean().stream()
                                                        .limit(details.maxMeans)
                                                        .map(Mean::getText)
                                                        .collect(Collectors.joining(", ")));
                                                translation.append(")");
                                            }

                                            // Примеры использования
                                            if (tr.getEx() != null && !tr.getEx().isEmpty()) {
                                                translation.append("\n<i>");
                                                translation.append(tr.getEx().stream()
                                                        .limit(details.maxExamples)
                                                        .map(example -> example.getText() + " (" + example.getTr().get(0).getText() + ")")
                                                        .findFirst().get());
                                                translation.append("</i>");
                                            }

                                            return translation.toString();
                                        }).collect(Collectors.joining()));
                                text.append("\n\n");
                            }
                    );

            return text.toString();
        } catch (Exception ex) {
            return "Перевод не найден.";
        }
    }
}
