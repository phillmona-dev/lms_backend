package com.dev.LMS.dto;

import java.util.List;

public class EducationSearchResponseDto {
    private String query;
    private String educationalQuery;
    private String provider;
    private boolean apiBacked;
    private String googleSearchUrl;
    private String youtubeSearchUrl;
    private List<EducationSearchResultDto> results;

    public EducationSearchResponseDto() {
    }

    public EducationSearchResponseDto(String query,
                                      String educationalQuery,
                                      String provider,
                                      boolean apiBacked,
                                      String googleSearchUrl,
                                      String youtubeSearchUrl,
                                      List<EducationSearchResultDto> results) {
        this.query = query;
        this.educationalQuery = educationalQuery;
        this.provider = provider;
        this.apiBacked = apiBacked;
        this.googleSearchUrl = googleSearchUrl;
        this.youtubeSearchUrl = youtubeSearchUrl;
        this.results = results;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getEducationalQuery() {
        return educationalQuery;
    }

    public void setEducationalQuery(String educationalQuery) {
        this.educationalQuery = educationalQuery;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean isApiBacked() {
        return apiBacked;
    }

    public void setApiBacked(boolean apiBacked) {
        this.apiBacked = apiBacked;
    }

    public String getGoogleSearchUrl() {
        return googleSearchUrl;
    }

    public void setGoogleSearchUrl(String googleSearchUrl) {
        this.googleSearchUrl = googleSearchUrl;
    }

    public String getYoutubeSearchUrl() {
        return youtubeSearchUrl;
    }

    public void setYoutubeSearchUrl(String youtubeSearchUrl) {
        this.youtubeSearchUrl = youtubeSearchUrl;
    }

    public List<EducationSearchResultDto> getResults() {
        return results;
    }

    public void setResults(List<EducationSearchResultDto> results) {
        this.results = results;
    }
}
