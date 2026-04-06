package com.dev.LMS.controller;

import com.dev.LMS.dto.EducationSearchResponseDto;
import com.dev.LMS.service.EducationalSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@Tag(name = "Educational Search", description = "Search educational content from Google and YouTube.")
public class EducationalSearchController {
    private final EducationalSearchService educationalSearchService;

    public EducationalSearchController(EducationalSearchService educationalSearchService) {
        this.educationalSearchService = educationalSearchService;
    }

    @GetMapping("/education")
    @Operation(summary = "Search educational content",
            description = "Searches YouTube and/or Google using educational filters only.")
    public ResponseEntity<EducationSearchResponseDto> searchEducation(
            @RequestParam("q") String query,
            @RequestParam(value = "provider", defaultValue = "ALL") String provider,
            @RequestParam(value = "limit", defaultValue = "8") int limit) {
        return ResponseEntity.ok(educationalSearchService.searchEducationalContent(query, provider, limit));
    }
}
