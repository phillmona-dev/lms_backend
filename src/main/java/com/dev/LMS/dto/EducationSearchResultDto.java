package com.dev.LMS.dto;

public class EducationSearchResultDto {
    private String source;
    private String title;
    private String url;
    private String snippet;
    private String thumbnailUrl;
    private String publishedAt;

    public EducationSearchResultDto() {
    }

    public EducationSearchResultDto(String source, String title, String url, String snippet, String thumbnailUrl, String publishedAt) {
        this.source = source;
        this.title = title;
        this.url = url;
        this.snippet = snippet;
        this.thumbnailUrl = thumbnailUrl;
        this.publishedAt = publishedAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }
}
