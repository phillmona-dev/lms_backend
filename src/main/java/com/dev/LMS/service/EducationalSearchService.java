package com.dev.LMS.service;

import com.dev.LMS.dto.EducationSearchResponseDto;
import com.dev.LMS.dto.EducationSearchResultDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class EducationalSearchService {
    private static final Logger log = LoggerFactory.getLogger(EducationalSearchService.class);

    private final RestTemplate restTemplate;
    private final String googleApiKey;
    private final String googleSearchEngineId;
    private final String youtubeApiKey;

    public EducationalSearchService(RestTemplateBuilder restTemplateBuilder,
                                    @Value("${app.search.google.api-key:}") String googleApiKey,
                                    @Value("${app.search.google.cx:}") String googleSearchEngineId,
                                    @Value("${app.search.youtube.api-key:}") String youtubeApiKey) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(4))
                .setReadTimeout(Duration.ofSeconds(7))
                .build();
        this.googleApiKey = googleApiKey;
        this.googleSearchEngineId = googleSearchEngineId;
        this.youtubeApiKey = youtubeApiKey;
    }

    public EducationSearchResponseDto searchEducationalContent(String query, String provider, int limit) {
        String normalizedQuery = normalizeQuery(query);
        String normalizedProvider = normalizeProvider(provider);
        int normalizedLimit = normalizeLimit(limit);
        String educationalQuery = buildEducationalQuery(normalizedQuery);
        String googleSearchUrl = buildGoogleSearchUrl(educationalQuery);
        String youtubeSearchUrl = buildYoutubeSearchUrl(educationalQuery);

        List<EducationSearchResultDto> results = new ArrayList<>();
        boolean apiBacked = false;

        int googleLimit = normalizedProvider.equals("ALL") ? Math.max(1, normalizedLimit / 2) : normalizedLimit;
        int youtubeLimit = normalizedProvider.equals("ALL") ? Math.max(1, normalizedLimit - googleLimit) : normalizedLimit;

        if ((normalizedProvider.equals("ALL") || normalizedProvider.equals("GOOGLE")) && hasGoogleApiConfig()) {
            List<EducationSearchResultDto> googleResults = searchGoogleApi(educationalQuery, googleLimit);
            if (!googleResults.isEmpty()) {
                results.addAll(googleResults);
                apiBacked = true;
            }
        }

        if ((normalizedProvider.equals("ALL") || normalizedProvider.equals("YOUTUBE")) && hasYoutubeApiConfig()) {
            List<EducationSearchResultDto> youtubeResults = searchYoutubeApi(educationalQuery, youtubeLimit);
            if (!youtubeResults.isEmpty()) {
                results.addAll(youtubeResults);
                apiBacked = true;
            }
        }

        if (results.isEmpty()) {
            results = fallbackResults(normalizedProvider, educationalQuery, googleSearchUrl, youtubeSearchUrl);
        }

        return new EducationSearchResponseDto(
                normalizedQuery,
                educationalQuery,
                normalizedProvider,
                apiBacked,
                googleSearchUrl,
                youtubeSearchUrl,
                results.stream().limit(normalizedLimit).toList()
        );
    }

    private List<EducationSearchResultDto> searchGoogleApi(String educationalQuery, int limit) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/customsearch/v1")
                    .queryParam("key", googleApiKey)
                    .queryParam("cx", googleSearchEngineId)
                    .queryParam("q", educationalQuery)
                    .queryParam("num", limit)
                    .queryParam("safe", "active")
                    .queryParam("lr", "lang_en")
                    .toUriString();

            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            if (response == null || !response.has("items")) {
                return List.of();
            }

            List<EducationSearchResultDto> results = new ArrayList<>();
            for (JsonNode item : response.get("items")) {
                String title = item.path("title").asText("Educational Resource");
                String link = item.path("link").asText("");
                String snippet = item.path("snippet").asText("Google educational result");
                String thumbnail = item.path("pagemap").path("cse_thumbnail").isArray()
                        ? item.path("pagemap").path("cse_thumbnail").path(0).path("src").asText(null)
                        : null;

                if (!link.isBlank()) {
                    results.add(new EducationSearchResultDto("GOOGLE", title, link, snippet, thumbnail, null));
                }
            }
            return results;
        } catch (Exception ex) {
            log.warn("Google educational search failed: {}", ex.getMessage());
            return List.of();
        }
    }

    private List<EducationSearchResultDto> searchYoutubeApi(String educationalQuery, int limit) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                    .queryParam("key", youtubeApiKey)
                    .queryParam("part", "snippet")
                    .queryParam("type", "video")
                    .queryParam("q", educationalQuery)
                    .queryParam("maxResults", limit)
                    .queryParam("safeSearch", "strict")
                    .queryParam("videoEmbeddable", "true")
                    .toUriString();

            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            if (response == null || !response.has("items")) {
                return List.of();
            }

            List<EducationSearchResultDto> results = new ArrayList<>();
            for (JsonNode item : response.get("items")) {
                String videoId = item.path("id").path("videoId").asText("");
                if (videoId.isBlank()) {
                    continue;
                }
                JsonNode snippetNode = item.path("snippet");
                String title = snippetNode.path("title").asText("Educational Video");
                String snippet = snippetNode.path("description").asText("YouTube educational video");
                String publishedAt = snippetNode.path("publishedAt").asText(null);
                String thumbnail = snippetNode.path("thumbnails").path("high").path("url").asText(null);
                String link = "https://www.youtube.com/watch?v=" + videoId;

                results.add(new EducationSearchResultDto("YOUTUBE", title, link, snippet, thumbnail, publishedAt));
            }
            return results;
        } catch (Exception ex) {
            log.warn("YouTube educational search failed: {}", ex.getMessage());
            return List.of();
        }
    }

    private List<EducationSearchResultDto> fallbackResults(String provider,
                                                           String educationalQuery,
                                                           String googleUrl,
                                                           String youtubeUrl) {
        List<EducationSearchResultDto> items = new ArrayList<>();
        if (provider.equals("ALL") || provider.equals("GOOGLE")) {
            items.add(new EducationSearchResultDto(
                    "GOOGLE",
                    "Open Google Educational Search",
                    googleUrl,
                    "Filtered with education keywords, safe search, and non-educational exclusions.",
                    null,
                    null
            ));
        }
        if (provider.equals("ALL") || provider.equals("YOUTUBE")) {
            items.add(new EducationSearchResultDto(
                    "YOUTUBE",
                    "Open YouTube Educational Search",
                    youtubeUrl,
                    "Filtered for tutorials, lessons, lectures, and learning-focused videos.",
                    null,
                    null
            ));
        }
        if (items.isEmpty()) {
            items.add(new EducationSearchResultDto(
                    "GOOGLE",
                    "Open Educational Search",
                    googleUrl,
                    "Educational query: " + educationalQuery,
                    null,
                    null
            ));
        }
        return items;
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query is required.");
        }
        return query.trim();
    }

    private String normalizeProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            return "ALL";
        }
        String value = provider.trim().toUpperCase(Locale.ROOT);
        if (!value.equals("ALL") && !value.equals("GOOGLE") && !value.equals("YOUTUBE")) {
            throw new IllegalArgumentException("provider must be one of ALL, GOOGLE, YOUTUBE");
        }
        return value;
    }

    private int normalizeLimit(int limit) {
        if (limit < 1) {
            return 6;
        }
        return Math.min(limit, 20);
    }

    private String buildEducationalQuery(String query) {
        return query + " tutorial lesson lecture course explained -music -movie -entertainment -gaming";
    }

    private String buildGoogleSearchUrl(String educationalQuery) {
        return "https://www.google.com/search?q="
                + URLEncoder.encode(educationalQuery + " site:.edu OR site:org", StandardCharsets.UTF_8)
                + "&safe=active";
    }

    private String buildYoutubeSearchUrl(String educationalQuery) {
        return "https://www.youtube.com/results?search_query="
                + URLEncoder.encode(educationalQuery, StandardCharsets.UTF_8);
    }

    private boolean hasGoogleApiConfig() {
        return !googleApiKey.isBlank() && !googleSearchEngineId.isBlank();
    }

    private boolean hasYoutubeApiConfig() {
        return !youtubeApiKey.isBlank();
    }
}
