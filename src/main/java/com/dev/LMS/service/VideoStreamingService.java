package com.dev.LMS.service;

import com.dev.LMS.model.Course;
import com.dev.LMS.model.Lesson;
import com.dev.LMS.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

@Service
public class VideoStreamingService {

    private final CourseRepository courseRepository;

    @Value("${file.upload.base-path.lesson-videos}")
    private Path videosPath;

    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4", "video/webm", "video/ogg", "video/quicktime",
            "video/x-msvideo", "video/x-matroska"
    );

    public VideoStreamingService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /**
     * Save a video file for a lesson and update the lesson's videoPath.
     */
    public String uploadVideo(Course course, Lesson lesson, MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_VIDEO_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid video format. Allowed: MP4, WebM, OGG, MOV, AVI, MKV");
        }

        // Create directory if it doesn't exist
        Files.createDirectories(videosPath);

        // Build unique filename: courseId_lessonId_originalName
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "video.mp4";
        }
        // Sanitize filename
        originalName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = course.getCourseId() + "_" + lesson.getLesson_id() + "_" + originalName;

        // Delete old video if exists
        if (lesson.getVideoPath() != null && !lesson.getVideoPath().isBlank()) {
            Path oldFile = videosPath.resolve(lesson.getVideoPath());
            Files.deleteIfExists(oldFile);
        }

        // Save new video
        Path targetPath = videosPath.resolve(fileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Update lesson
        lesson.setVideoPath(fileName);
        courseRepository.save(course);

        return fileName;
    }

    /**
     * Get the video file as a Resource for streaming.
     */
    public Resource getVideoAsResource(Lesson lesson) throws MalformedURLException {
        if (lesson.getVideoPath() == null || lesson.getVideoPath().isBlank()) {
            throw new IllegalStateException("No video uploaded for this lesson");
        }

        Path videoFile = videosPath.resolve(lesson.getVideoPath());
        Resource resource = new UrlResource(videoFile.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalStateException("Video file not found or not readable");
        }

        return resource;
    }

    /**
     * Get the file size for Range header support.
     */
    public long getVideoFileSize(Lesson lesson) throws IOException {
        if (lesson.getVideoPath() == null || lesson.getVideoPath().isBlank()) {
            throw new IllegalStateException("No video uploaded for this lesson");
        }
        return Files.size(videosPath.resolve(lesson.getVideoPath()));
    }

    /**
     * Determine content type from filename.
     */
    public String getVideoContentType(String fileName) {
        if (fileName == null) return "video/mp4";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".webm")) return "video/webm";
        if (lower.endsWith(".ogg") || lower.endsWith(".ogv")) return "video/ogg";
        if (lower.endsWith(".mov")) return "video/quicktime";
        if (lower.endsWith(".avi")) return "video/x-msvideo";
        if (lower.endsWith(".mkv")) return "video/x-matroska";
        return "video/mp4";
    }

    /**
     * Delete the video file for a lesson.
     */
    public void deleteVideo(Course course, Lesson lesson) throws IOException {
        if (lesson.getVideoPath() != null && !lesson.getVideoPath().isBlank()) {
            Path videoFile = videosPath.resolve(lesson.getVideoPath());
            Files.deleteIfExists(videoFile);
            lesson.setVideoPath(null);
            courseRepository.save(course);
        }
    }
}
