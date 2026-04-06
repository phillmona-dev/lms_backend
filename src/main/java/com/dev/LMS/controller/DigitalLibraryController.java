package com.dev.LMS.controller;

import com.dev.LMS.dto.*;
import com.dev.LMS.model.User;
import com.dev.LMS.service.DigitalLibraryService;
import com.dev.LMS.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/library")
@Tag(name = "Digital Library", description = "Digital library resource discovery and management endpoints.")
public class DigitalLibraryController {
    private final DigitalLibraryService digitalLibraryService;
    private final UserService userService;

    public DigitalLibraryController(DigitalLibraryService digitalLibraryService, UserService userService) {
        this.digitalLibraryService = digitalLibraryService;
        this.userService = userService;
    }

    @GetMapping("/resources")
    @Operation(summary = "List library resources", description = "Returns resources with search and filter support.")
    public ResponseEntity<?> getResources(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String gradeLevel,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String availability,
            @RequestParam(required = false) String sort
    ) {
        try {
            User user = getCurrentUser();
            List<LibraryResourceDto> resources = digitalLibraryService.getResources(user, query, category, subject, gradeLevel, type, availability, sort);
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/resources/{resourceId}")
    @Operation(summary = "Get library resource", description = "Returns one library resource by id.")
    public ResponseEntity<?> getResource(@PathVariable Long resourceId) {
        try {
            User user = getCurrentUser();
            return ResponseEntity.ok(digitalLibraryService.getResource(resourceId, user));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping(value = "/resources", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create library resource", description = "Creates a new library resource with optional content and cover uploads.")
    public ResponseEntity<?> createResource(
            @RequestPart("metadata") LibraryResourceRequestDto metadata,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "cover", required = false) MultipartFile cover
    ) {
        try {
            User user = getCurrentUser();
            if (!canManageLibrary(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to add library resources.");
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    digitalLibraryService.createResource(metadata, file, cover, user)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping(value = "/resources/{resourceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update library resource", description = "Updates a library resource and optionally replaces files.")
    public ResponseEntity<?> updateResource(
            @PathVariable Long resourceId,
            @RequestPart("metadata") LibraryResourceRequestDto metadata,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "cover", required = false) MultipartFile cover
    ) {
        try {
            User user = getCurrentUser();
            if (!canManageLibrary(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to update library resources.");
            }
            return ResponseEntity.ok(digitalLibraryService.updateResource(resourceId, metadata, file, cover, user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/resources/{resourceId}")
    @Operation(summary = "Delete library resource", description = "Deletes a library resource.")
    public ResponseEntity<?> deleteResource(@PathVariable Long resourceId) {
        try {
            User user = getCurrentUser();
            if (!canManageLibrary(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete library resources.");
            }
            digitalLibraryService.deleteResource(resourceId);
            return ResponseEntity.ok(Map.of("message", "Resource deleted successfully."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/resources/{resourceId}/file")
    @Operation(summary = "Download resource file", description = "Downloads or opens the resource file if available.")
    public ResponseEntity<?> downloadResourceFile(@PathVariable Long resourceId) {
        try {
            LibraryResourceDto resource = digitalLibraryService.getResource(resourceId, getCurrentUser());
            byte[] data = digitalLibraryService.downloadResourceFile(resourceId);
            MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
            try {
                if (resource.getFileType() != null) {
                    contentType = MediaType.parseMediaType(resource.getFileType());
                }
            } catch (Exception ignored) {
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            }
            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header("Content-Disposition", "inline; filename=\"" + (resource.getFileName() == null ? "resource_file" : resource.getFileName()) + "\"")
                    .body(data);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/resources/{resourceId}/cover")
    @Operation(summary = "Download resource cover", description = "Returns a resource cover image if available.")
    public ResponseEntity<?> downloadResourceCover(@PathVariable Long resourceId) {
        try {
            LibraryResourceDto resource = digitalLibraryService.getResource(resourceId, getCurrentUser());
            byte[] data = digitalLibraryService.downloadResourceCover(resourceId);
            MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
            try {
                if (resource.getCoverFileType() != null) {
                    contentType = MediaType.parseMediaType(resource.getCoverFileType());
                }
            } catch (Exception ignored) {
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            }
            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header("Content-Disposition", "inline; filename=\"" + (resource.getCoverFileName() == null ? "cover" : resource.getCoverFileName()) + "\"")
                    .body(data);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/resources/{resourceId}/borrow")
    @Operation(summary = "Borrow resource", description = "Borrow a resource if copies are available.")
    public ResponseEntity<?> borrowResource(@PathVariable Long resourceId,
                                            @RequestBody(required = false) Map<String, Integer> body) {
        try {
            Integer dueDays = body == null ? null : body.get("dueDays");
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(digitalLibraryService.borrowResource(resourceId, getCurrentUser(), dueDays));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/resources/{resourceId}/return")
    @Operation(summary = "Return resource", description = "Return a borrowed resource.")
    public ResponseEntity<?> returnResource(@PathVariable Long resourceId) {
        try {
            return ResponseEntity.ok(digitalLibraryService.returnResource(resourceId, getCurrentUser()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/my/borrows")
    @Operation(summary = "My borrows", description = "Returns currently authenticated user's borrow history.")
    public ResponseEntity<?> myBorrows() {
        try {
            return ResponseEntity.ok(digitalLibraryService.myBorrows(getCurrentUser()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/resources/{resourceId}/favorite")
    @Operation(summary = "Favorite resource", description = "Adds a resource to current user's favorites.")
    public ResponseEntity<?> favoriteResource(@PathVariable Long resourceId) {
        try {
            digitalLibraryService.addFavorite(resourceId, getCurrentUser());
            return ResponseEntity.ok(Map.of("message", "Resource added to favorites."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/resources/{resourceId}/favorite")
    @Operation(summary = "Unfavorite resource", description = "Removes a resource from current user's favorites.")
    public ResponseEntity<?> unfavoriteResource(@PathVariable Long resourceId) {
        try {
            digitalLibraryService.removeFavorite(resourceId, getCurrentUser());
            return ResponseEntity.ok(Map.of("message", "Resource removed from favorites."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/my/favorites")
    @Operation(summary = "My favorites", description = "Returns current user's favorite resources.")
    public ResponseEntity<?> myFavorites() {
        try {
            return ResponseEntity.ok(digitalLibraryService.myFavorites(getCurrentUser()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/resources/{resourceId}/review")
    @Operation(summary = "Add/update review", description = "Adds or updates current user's rating and review for a resource.")
    public ResponseEntity<?> upsertReview(@PathVariable Long resourceId,
                                          @RequestBody LibraryReviewRequestDto request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(digitalLibraryService.upsertReview(resourceId, getCurrentUser(), request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/resources/{resourceId}/reviews")
    @Operation(summary = "List reviews", description = "Returns ratings and reviews for a resource.")
    public ResponseEntity<?> getReviews(@PathVariable Long resourceId) {
        try {
            return ResponseEntity.ok(digitalLibraryService.getReviews(resourceId));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Library dashboard", description = "Returns digital library summary metrics for current user.")
    public ResponseEntity<?> dashboard() {
        try {
            return ResponseEntity.ok(digitalLibraryService.dashboard(getCurrentUser()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/categories")
    @Operation(summary = "Library categories", description = "Returns distinct resource categories.")
    public ResponseEntity<?> categories() {
        try {
            Set<String> categories = digitalLibraryService.categories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/subjects")
    @Operation(summary = "Library subjects", description = "Returns distinct resource subjects.")
    public ResponseEntity<?> subjects() {
        try {
            Set<String> subjects = digitalLibraryService.subjects();
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/grade-levels")
    @Operation(summary = "Library grade levels", description = "Returns distinct resource grade levels.")
    public ResponseEntity<?> gradeLevels() {
        try {
            Set<String> gradeLevels = digitalLibraryService.gradeLevels();
            return ResponseEntity.ok(gradeLevels);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByEmail(email);
    }

    private boolean canManageLibrary(User user) {
        String role = user.getRole() == null ? "" : user.getRole().canonical().name();
        return role.equals("SYSTEM_ADMINISTRATOR")
                || role.equals("SCHOOL_ADMINISTRATOR")
                || role.equals("TEACHER");
    }
}
