package com.dev.LMS.service;

import com.dev.LMS.dto.*;
import com.dev.LMS.model.*;
import com.dev.LMS.repository.LibraryBorrowRepository;
import com.dev.LMS.repository.LibraryFavoriteRepository;
import com.dev.LMS.repository.LibraryResourceRepository;
import com.dev.LMS.repository.LibraryReviewRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DigitalLibraryService {
    private final LibraryResourceRepository libraryResourceRepository;
    private final LibraryBorrowRepository libraryBorrowRepository;
    private final LibraryFavoriteRepository libraryFavoriteRepository;
    private final LibraryReviewRepository libraryReviewRepository;

    @Value("${file.upload.base-path.library-resources}")
    private String LIBRARY_RESOURCE_UPLOAD_DIR;

    @Value("${file.upload.base-path.library-covers}")
    private String LIBRARY_COVER_UPLOAD_DIR;

    public DigitalLibraryService(
            LibraryResourceRepository libraryResourceRepository,
            LibraryBorrowRepository libraryBorrowRepository,
            LibraryFavoriteRepository libraryFavoriteRepository,
            LibraryReviewRepository libraryReviewRepository
    ) {
        this.libraryResourceRepository = libraryResourceRepository;
        this.libraryBorrowRepository = libraryBorrowRepository;
        this.libraryFavoriteRepository = libraryFavoriteRepository;
        this.libraryReviewRepository = libraryReviewRepository;
    }

    @Transactional
    public LibraryResourceDto createResource(
            LibraryResourceRequestDto request,
            MultipartFile file,
            MultipartFile coverFile,
            User creator
    ) {
        validateResourceRequest(request);
        LibraryResource resource = new LibraryResource();
        applyRequestToResource(resource, request);
        resource.setCreatedBy(creator);

        if (file != null && !file.isEmpty()) {
            String storedPath = storeFile(file, LIBRARY_RESOURCE_UPLOAD_DIR);
            resource.setFileName(file.getOriginalFilename());
            resource.setFileType(file.getContentType());
            resource.setFilePath(storedPath);
        }

        if (coverFile != null && !coverFile.isEmpty()) {
            String storedCoverPath = storeFile(coverFile, LIBRARY_COVER_UPLOAD_DIR);
            resource.setCoverFileName(coverFile.getOriginalFilename());
            resource.setCoverFileType(coverFile.getContentType());
            resource.setCoverFilePath(storedCoverPath);
        }

        if ((resource.getAccessType() == LibraryAccessType.DOWNLOAD_ONLY
                || resource.getAccessType() == LibraryAccessType.DOWNLOAD_AND_LINK)
                && (resource.getFilePath() == null || resource.getFilePath().isBlank())) {
            throw new IllegalArgumentException("A downloadable file is required for the selected access type.");
        }

        LibraryResource saved = libraryResourceRepository.save(resource);
        return toDto(saved, creator);
    }

    @Transactional
    public LibraryResourceDto updateResource(
            Long resourceId,
            LibraryResourceRequestDto request,
            MultipartFile file,
            MultipartFile coverFile,
            User actor
    ) {
        LibraryResource resource = getResourceEntity(resourceId);
        applyRequestToResource(resource, request);

        if (file != null && !file.isEmpty()) {
            deleteLocalFile(resource.getFilePath());
            String storedPath = storeFile(file, LIBRARY_RESOURCE_UPLOAD_DIR);
            resource.setFileName(file.getOriginalFilename());
            resource.setFileType(file.getContentType());
            resource.setFilePath(storedPath);
        }

        if (coverFile != null && !coverFile.isEmpty()) {
            deleteLocalFile(resource.getCoverFilePath());
            String storedCoverPath = storeFile(coverFile, LIBRARY_COVER_UPLOAD_DIR);
            resource.setCoverFileName(coverFile.getOriginalFilename());
            resource.setCoverFileType(coverFile.getContentType());
            resource.setCoverFilePath(storedCoverPath);
        }

        if ((resource.getAccessType() == LibraryAccessType.DOWNLOAD_ONLY
                || resource.getAccessType() == LibraryAccessType.DOWNLOAD_AND_LINK)
                && (resource.getFilePath() == null || resource.getFilePath().isBlank())) {
            throw new IllegalArgumentException("A downloadable file is required for the selected access type.");
        }

        return toDto(libraryResourceRepository.save(resource), actor);
    }

    @Transactional
    public void deleteResource(Long resourceId) {
        LibraryResource resource = getResourceEntity(resourceId);
        deleteLocalFile(resource.getFilePath());
        deleteLocalFile(resource.getCoverFilePath());
        libraryResourceRepository.delete(resource);
    }

    @Transactional(readOnly = true)
    public List<LibraryResourceDto> getResources(
            User currentUser,
            String query,
            String category,
            String subject,
            String gradeLevel,
            String resourceType,
            String availability,
            String sortBy
    ) {
        List<LibraryResource> resources = libraryResourceRepository.findAll();
        Set<Long> favoriteIds = libraryFavoriteRepository.findByUserOrderByCreatedAtDesc(currentUser).stream()
                .map(f -> f.getResource().getId())
                .collect(Collectors.toSet());
        Set<Long> borrowedIds = libraryBorrowRepository.findByUserAndStatusOrderByBorrowedAtDesc(currentUser, LibraryBorrowStatus.BORROWED).stream()
                .map(b -> b.getResource().getId())
                .collect(Collectors.toSet());

        return resources.stream()
                .filter(r -> matchesQuery(r, query))
                .filter(r -> matchesCategory(r, category))
                .filter(r -> matchesSubject(r, subject))
                .filter(r -> matchesGradeLevel(r, gradeLevel))
                .filter(r -> matchesType(r, resourceType))
                .filter(r -> matchesAvailability(r, availability))
                .sorted(resourceComparator(sortBy))
                .map(r -> {
                    boolean favoritedByMe = favoriteIds.contains(r.getId());
                    boolean borrowedByMe = borrowedIds.contains(r.getId());
                    return toDto(r, currentUser, favoritedByMe, borrowedByMe);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public LibraryResourceDto getResource(Long resourceId, User user) {
        return toDto(getResourceEntity(resourceId), user);
    }

    @Transactional(readOnly = true)
    public byte[] downloadResourceFile(Long resourceId) {
        LibraryResource resource = getResourceEntity(resourceId);
        if (resource.getFilePath() == null || resource.getFilePath().isBlank()) {
            throw new IllegalStateException("No downloadable file available for this resource.");
        }
        return readFileBytes(resource.getFilePath());
    }

    @Transactional(readOnly = true)
    public byte[] downloadResourceCover(Long resourceId) {
        LibraryResource resource = getResourceEntity(resourceId);
        if (resource.getCoverFilePath() == null || resource.getCoverFilePath().isBlank()) {
            throw new IllegalStateException("No cover image available for this resource.");
        }
        return readFileBytes(resource.getCoverFilePath());
    }

    @Transactional
    public LibraryBorrowDto borrowResource(Long resourceId, User user, Integer dueDays) {
        LibraryResource resource = getResourceEntity(resourceId);
        if (resource.getAvailableCopies() == null || resource.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No available copies for this resource.");
        }
        Optional<LibraryBorrow> existingActiveBorrow = libraryBorrowRepository.findByUserAndResourceAndStatus(
                user, resource, LibraryBorrowStatus.BORROWED
        );
        if (existingActiveBorrow.isPresent()) {
            throw new IllegalStateException("You already borrowed this resource.");
        }

        LibraryBorrow borrow = new LibraryBorrow();
        borrow.setResource(resource);
        borrow.setUser(user);
        borrow.setStatus(LibraryBorrowStatus.BORROWED);
        int days = dueDays != null && dueDays > 0 ? dueDays : 14;
        borrow.setDueDate(LocalDateTime.now().plusDays(days));

        resource.setAvailableCopies(Math.max(0, resource.getAvailableCopies() - 1));
        libraryResourceRepository.save(resource);
        return LibraryBorrowDto.from(libraryBorrowRepository.save(borrow));
    }

    @Transactional
    public LibraryBorrowDto returnResource(Long resourceId, User user) {
        LibraryResource resource = getResourceEntity(resourceId);
        LibraryBorrow borrow = libraryBorrowRepository.findByUserAndResourceAndStatus(user, resource, LibraryBorrowStatus.BORROWED)
                .orElseThrow(() -> new IllegalStateException("No active borrow record for this resource."));
        borrow.setStatus(LibraryBorrowStatus.RETURNED);
        borrow.setReturnedAt(LocalDateTime.now());

        int total = resource.getTotalCopies() == null ? 0 : resource.getTotalCopies();
        int available = resource.getAvailableCopies() == null ? 0 : resource.getAvailableCopies();
        resource.setAvailableCopies(Math.min(total, available + 1));
        libraryResourceRepository.save(resource);
        return LibraryBorrowDto.from(libraryBorrowRepository.save(borrow));
    }

    @Transactional(readOnly = true)
    public List<LibraryBorrowDto> myBorrows(User user) {
        return libraryBorrowRepository.findByUserOrderByBorrowedAtDesc(user).stream()
                .map(LibraryBorrowDto::from)
                .toList();
    }

    @Transactional
    public void addFavorite(Long resourceId, User user) {
        LibraryResource resource = getResourceEntity(resourceId);
        boolean exists = libraryFavoriteRepository.findByUserAndResource(user, resource).isPresent();
        if (exists) {
            return;
        }
        LibraryFavorite favorite = new LibraryFavorite();
        favorite.setUser(user);
        favorite.setResource(resource);
        libraryFavoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(Long resourceId, User user) {
        LibraryResource resource = getResourceEntity(resourceId);
        libraryFavoriteRepository.findByUserAndResource(user, resource)
                .ifPresent(libraryFavoriteRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<LibraryResourceDto> myFavorites(User user) {
        return libraryFavoriteRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(LibraryFavorite::getResource)
                .map(resource -> toDto(resource, user, true, isBorrowedByUser(resource, user)))
                .toList();
    }

    @Transactional
    public LibraryReviewDto upsertReview(Long resourceId, User user, LibraryReviewRequestDto request) {
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        LibraryResource resource = getResourceEntity(resourceId);
        LibraryReview review = libraryReviewRepository.findByUserAndResource(user, resource)
                .orElseGet(() -> {
                    LibraryReview r = new LibraryReview();
                    r.setUser(user);
                    r.setResource(resource);
                    return r;
                });
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return LibraryReviewDto.from(libraryReviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<LibraryReviewDto> getReviews(Long resourceId) {
        LibraryResource resource = getResourceEntity(resourceId);
        return libraryReviewRepository.findByResourceOrderByUpdatedAtDesc(resource).stream()
                .map(LibraryReviewDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public LibraryDashboardDto dashboard(User user) {
        long totalResources = libraryResourceRepository.count();
        long availableResources = libraryResourceRepository.findAll().stream()
                .filter(r -> r.getAvailableCopies() != null && r.getAvailableCopies() > 0)
                .count();

        return LibraryDashboardDto.builder()
                .totalResources(totalResources)
                .availableResources(availableResources)
                .totalBorrows(libraryBorrowRepository.count())
                .activeBorrows(libraryBorrowRepository.countByStatus(LibraryBorrowStatus.BORROWED))
                .myBorrows(libraryBorrowRepository.countByUser(user))
                .myFavorites(libraryFavoriteRepository.countByUser(user))
                .build();
    }

    @Transactional(readOnly = true)
    public Set<String> categories() {
        return libraryResourceRepository.findAll().stream()
                .map(LibraryResource::getCategory)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Transactional(readOnly = true)
    public Set<String> subjects() {
        return libraryResourceRepository.findAll().stream()
                .map(LibraryResource::getSubject)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Transactional(readOnly = true)
    public Set<String> gradeLevels() {
        return libraryResourceRepository.findAll().stream()
                .map(LibraryResource::getGradeLevel)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private void applyRequestToResource(LibraryResource resource, LibraryResourceRequestDto request) {
        validateResourceRequest(request);
        resource.setTitle(request.getTitle().trim());
        resource.setAuthor(request.getAuthor().trim());
        resource.setPublisher(request.getPublisher());
        resource.setIsbn(request.getIsbn());
        resource.setDescription(request.getDescription());
        resource.setResourceType(request.getResourceType() == null ? LibraryResourceType.BOOK : request.getResourceType());
        resource.setCategory(request.getCategory());
        resource.setSubject(request.getSubject());
        resource.setGradeLevel(request.getGradeLevel());
        resource.setTags(request.getTags());
        resource.setLanguage(request.getLanguage());
        resource.setPublicationYear(request.getPublicationYear());
        resource.setPages(request.getPages());
        resource.setExternalUrl(request.getExternalUrl());
        resource.setAccessType(request.getAccessType() == null ? LibraryAccessType.DOWNLOAD_ONLY : request.getAccessType());
        int totalCopies = request.getTotalCopies() == null || request.getTotalCopies() <= 0 ? 1 : request.getTotalCopies();
        resource.setTotalCopies(totalCopies);
        if (resource.getAvailableCopies() == null) {
            resource.setAvailableCopies(totalCopies);
        } else {
            resource.setAvailableCopies(Math.min(totalCopies, Math.max(0, resource.getAvailableCopies())));
        }
    }

    private void validateResourceRequest(LibraryResourceRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Resource data is required.");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required.");
        }
        if (request.getAuthor() == null || request.getAuthor().isBlank()) {
            throw new IllegalArgumentException("Author is required.");
        }
    }

    private LibraryResource getResourceEntity(Long resourceId) {
        return libraryResourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalStateException("Resource not found with id: " + resourceId));
    }

    private boolean matchesQuery(LibraryResource resource, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String q = query.trim().toLowerCase();
        return asText(resource.getTitle()).contains(q)
                || asText(resource.getAuthor()).contains(q)
                || asText(resource.getCategory()).contains(q)
                || asText(resource.getTags()).contains(q)
                || asText(resource.getDescription()).contains(q);
    }

    private boolean matchesCategory(LibraryResource resource, String category) {
        if (category == null || category.isBlank() || "ALL".equalsIgnoreCase(category)) {
            return true;
        }
        return category.trim().equalsIgnoreCase(valueOrEmpty(resource.getCategory()));
    }

    private boolean matchesSubject(LibraryResource resource, String subject) {
        if (subject == null || subject.isBlank() || "ALL".equalsIgnoreCase(subject)) {
            return true;
        }
        return subject.trim().equalsIgnoreCase(valueOrEmpty(resource.getSubject()));
    }

    private boolean matchesGradeLevel(LibraryResource resource, String gradeLevel) {
        if (gradeLevel == null || gradeLevel.isBlank() || "ALL".equalsIgnoreCase(gradeLevel)) {
            return true;
        }
        return gradeLevel.trim().equalsIgnoreCase(valueOrEmpty(resource.getGradeLevel()));
    }

    private boolean matchesType(LibraryResource resource, String type) {
        if (type == null || type.isBlank() || "ALL".equalsIgnoreCase(type)) {
            return true;
        }
        return resource.getResourceType() != null && resource.getResourceType().name().equalsIgnoreCase(type);
    }

    private boolean matchesAvailability(LibraryResource resource, String availability) {
        if (availability == null || availability.isBlank() || "ALL".equalsIgnoreCase(availability)) {
            return true;
        }
        boolean available = resource.getAvailableCopies() != null && resource.getAvailableCopies() > 0;
        if ("AVAILABLE".equalsIgnoreCase(availability)) {
            return available;
        }
        if ("UNAVAILABLE".equalsIgnoreCase(availability)) {
            return !available;
        }
        return true;
    }

    private Comparator<LibraryResource> resourceComparator(String sortBy) {
        if (sortBy == null) {
            return Comparator.comparing(LibraryResource::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
        }
        return switch (sortBy.toLowerCase()) {
            case "title" -> Comparator.comparing(r -> asText(r.getTitle()));
            case "author" -> Comparator.comparing(r -> asText(r.getAuthor()));
            case "year" -> Comparator.comparing(r -> Optional.ofNullable(r.getPublicationYear()).orElse(0), Comparator.reverseOrder());
            default -> Comparator.comparing(LibraryResource::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
        };
    }

    private LibraryResourceDto toDto(LibraryResource resource, User user) {
        return toDto(resource, user, isFavoritedByUser(resource, user), isBorrowedByUser(resource, user));
    }

    private LibraryResourceDto toDto(LibraryResource resource, User user, boolean favoritedByMe, boolean borrowedByMe) {
        List<LibraryReview> reviews = libraryReviewRepository.findByResourceOrderByUpdatedAtDesc(resource);
        long ratingsCount = reviews.size();
        double averageRating = ratingsCount == 0
                ? 0.0
                : reviews.stream().mapToInt(LibraryReview::getRating).average().orElse(0.0);
        long favoritesCount = libraryFavoriteRepository.countByResource(resource);
        return LibraryResourceDto.from(resource, averageRating, ratingsCount, favoritesCount, favoritedByMe, borrowedByMe);
    }

    private boolean isFavoritedByUser(LibraryResource resource, User user) {
        return libraryFavoriteRepository.findByUserAndResource(user, resource).isPresent();
    }

    private boolean isBorrowedByUser(LibraryResource resource, User user) {
        return libraryBorrowRepository.findByUserAndResourceAndStatus(user, resource, LibraryBorrowStatus.BORROWED).isPresent();
    }

    private String storeFile(MultipartFile file, String directory) {
        try {
            Path uploadDir = Paths.get(directory);
            Files.createDirectories(uploadDir);

            String original = file.getOriginalFilename() == null ? "resource_file" : file.getOriginalFilename();
            String sanitized = original.replaceAll("[^a-zA-Z0-9._-]", "_");
            String unique = UUID.randomUUID() + "_" + sanitized;
            Path destination = uploadDir.resolve(unique);
            file.transferTo(destination.toFile());
            return destination.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.");
        }
    }

    private byte[] readFileBytes(String filePath) {
        try {
            return Files.readAllBytes(new File(filePath).toPath());
        } catch (IOException e) {
            throw new RuntimeException("Unable to load file from " + filePath);
        }
    }

    private void deleteLocalFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException ignored) {
        }
    }

    private String asText(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
