package com.dev.LMS.controller;

import java.net.URLConnection;
import java.util.*;
import com.dev.LMS.dto.*;
import com.dev.LMS.model.*;
import com.dev.LMS.service.CourseService;
import com.dev.LMS.service.UserService;
import com.dev.LMS.service.VideoStreamingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@RestController
@RequestMapping("/course")
@Tag(name = "Courses", description = "Endpoints for course creation, enrollment, lessons, resources, OTP attendance, video streaming, and attendance tracking.")
public class CourseController
{
    private static final Logger log = LoggerFactory.getLogger(CourseController.class);

    private final CourseService courseService;
    private final UserService userService;
    private final VideoStreamingService videoStreamingService;



    @PostMapping("/create-course")
    @Operation(summary = "Create a course", description = "Creates a new course owned by the authenticated instructor.")
    public ResponseEntity<?> createCourse(@RequestBody Course course) {
        try{
            // Validate required fields
            if (course.getName() == null || course.getName().isBlank()) {
                return ResponseEntity.badRequest().body("Course name is required and cannot be empty");
            }
            if (course.getDescription() == null || course.getDescription().isBlank()) {
                return ResponseEntity.badRequest().body("Course description is required and cannot be empty");
            }
            if (course.getDuration() == null || course.getDuration() <= 0) {
                return ResponseEntity.badRequest().body("Course duration must be a positive number");
            }

            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found, Please register or login first");
            }
            if (!(user  instanceof Instructor)) {
                return ResponseEntity.status(403).body("You are not authorized to create a course");
            }
            Instructor instructor = (Instructor) user;
            Course createdcourse = courseService.createCourse(course, instructor);
            CourseDto courseDto = new CourseDto(createdcourse);
            return ResponseEntity.ok(courseDto);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body("An error occurred" + e.getMessage());
        }
    }

    @GetMapping("/search-course/{courseName}")
    @Operation(summary = "Search course by name", description = "Returns course details for the given course name.")
    public ResponseEntity<?> getCourse(@Valid  @PathVariable("courseName") String courseName ){
        Course course = courseService.getCourse(courseName);
        if(course == null){
            return ResponseEntity.badRequest().body("Course not found");
        }
        CourseDto courseDto = new CourseDto(course);
        return ResponseEntity.ok(courseDto);
    }
    @GetMapping("/course/{id}")
    @Operation(summary = "Get course by id", description = "Returns course details using the legacy numeric course id.")
    public ResponseEntity<?> getCourse(@PathVariable("id") int id){
        Course course = courseService.getCourseById(id);
        if(course == null){
            return ResponseEntity.badRequest().body("Course not found");
        }
        CourseDto courseDto = new CourseDto(course);
        return ResponseEntity.ok(courseDto);
    }

    @GetMapping("/get-all-courses")
    @Operation(summary = "List all courses", description = "Returns every course currently available in the LMS.")
    public ResponseEntity<?> getAllCourses(){
        List<Course> courseList = courseService.getAllCourses();
        if(courseList == null){
            return ResponseEntity.ok().body("No courses found");
        }
        List<CourseDto> courseDtoList = new ArrayList<>();
        for(Course course: courseList){courseDtoList.add(new CourseDto(course));}
        return ResponseEntity.ok(courseDtoList);
    }

    @GetMapping("/get-my-courses")
    @Operation(summary = "List my courses", description = "Returns the authenticated instructor's created courses or the authenticated student's enrolled courses.")
    public ResponseEntity<?> getMyCourses(){
      try{
          String email = SecurityContextHolder.getContext().getAuthentication().getName();
          User user = userService.getUserByEmail(email);
          if (user == null) {
              return ResponseEntity.badRequest().body("User not found, Please register or login first");
          }
          if ((user  instanceof Instructor)) {
              Instructor instructor = (Instructor) user;
              Set<Course> createdCourses = courseService.getCreatedCourses(instructor);
              if(createdCourses == null){
                  return ResponseEntity.ok().body("No courses found");
              }
              Set<CourseDto> courseDtoList = new HashSet<>();
              for(Course course: createdCourses){courseDtoList.add(new CourseDto(course));}
              return ResponseEntity.ok(courseDtoList);

          }
          if (user instanceof Student){
               Student student = (Student) user;
              Set<Course> enrolledCourses = courseService.getEnrolledCourses(student);
              if(enrolledCourses == null){
                  return ResponseEntity.ok().body("No courses found");
              }
              Set<CourseDto> courseDtoList = new HashSet<>();
              for(Course course: enrolledCourses){courseDtoList.add(new CourseDto(course));}
              return ResponseEntity.ok(courseDtoList);

          }
          else
              return getAllCourses();

      }
      catch (Exception e){
          return ResponseEntity.badRequest().body("An error occurred" + e.getMessage());
      }
    }

    @PostMapping("/course/{courseName}/add-lesson")
    @Operation(summary = "Add a lesson to a course", description = "Adds a new lesson to the specified course. Only the course instructor can perform this action.")
    public ResponseEntity<?> addLesson(@PathVariable("courseName") String courseName, @RequestBody Lesson lesson){
        try{
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found, Please register or login first");
            }
            if (!(user  instanceof Instructor)) {
                return ResponseEntity.status(403).body("You are not authorized to add a lesson to this course");
            }
            else{
                Course course = courseService.getCourse(courseName);
                if(course == null){
                    return ResponseEntity.badRequest().body("Course not found");
                }
                Instructor instructor = (Instructor) user;
                if(course.getInstructor().getId() != instructor.getId()){
                    return ResponseEntity.status(403).body("You are not authorized to add a lesson to this course");
                }
                else{
                    Lesson addedLesson = courseService.addLesson(course, lesson);
                    LessonDto  lessonDto = new LessonDto(addedLesson);
                    return ResponseEntity.ok(lessonDto);
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("An error occurred" + e.getMessage());
        }
    }

    @GetMapping("/course/{courseName}/lessons")
    @Operation(summary = "List lessons in a course", description = "Returns all lessons that belong to the specified course.")
    public ResponseEntity<?> getAllLessons(@PathVariable("courseName") String courseName){
        try{
            Course course = courseService.getCourse(courseName);
            if(course == null){
                return ResponseEntity.badRequest().body("Course not found");
            }
            List<Lesson> lessons = course.getLessons();
            List<LessonDto> lessonDtoList = new ArrayList<>();
            for(Lesson lesson: lessons){lessonDtoList.add(new LessonDto(lesson));}
            return ResponseEntity.ok(lessonDtoList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("An error occurred" + e.getMessage());
        }
    }

    @GetMapping("/course/{courseName}/lessons/{lessonId}")
    @Operation(summary = "Get lesson details", description = "Returns lesson information. The course instructor receives detailed lesson data, while others receive the public lesson view.")
    public ResponseEntity<?> getLesson(@PathVariable("courseName") String courseName,@PathVariable("lessonId") int lessonId){
        try{
            Course course = courseService.getCourse(courseName);
            if(course == null){
                return ResponseEntity.badRequest().body("Course not found");
            }
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found, Please register or login first");
            }
            Lesson lesson = courseService.getLessonbyId(course, lessonId);
            if(lesson == null){
                return ResponseEntity.badRequest().body("Lesson not found");
            }
            if (user  instanceof Instructor ) {
                Instructor instructor = (Instructor) user;
                if (instructor.getId() != course.getInstructor().getId()) { //instructor of that course
                    LessonDto  lessonDto = new LessonDto(lesson);
                    return ResponseEntity.ok(lessonDto); //just simple data
                }
                else { //Instructor of the course
                    DetailedLessonDto detailedLessonDto = new DetailedLessonDto(lesson);
                    return ResponseEntity.ok(detailedLessonDto);
                }
            }
            else { //Student or may be admin
                LessonDto  lessonDto = new LessonDto(lesson);
                return ResponseEntity.ok(lessonDto);
            }

        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("An error occurred" + e.getMessage());
        }

    }

    @PostMapping("/course/{courseName}/lessons/{lessonId}/add-resource")
    @Operation(summary = "Upload a lesson resource", description = "Uploads a file resource to the specified lesson. Only the instructor of that course is allowed.")
    public ResponseEntity<?> addResource(@PathVariable("courseName") String courseName,@PathVariable("lessonId") int lessonId,@RequestParam MultipartFile file){
        try{
            Course course = courseService.getCourse(courseName);
            if(course == null){
                return ResponseEntity.badRequest().body("Course not found");
            }
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found, Please register or login first");
            }
            if (!(user instanceof Instructor)) return ResponseEntity.status(403).body("You are not authorized to add a resource to this lesson");
            Instructor instructor = (Instructor) user;
            if (instructor.getId() != course.getInstructor().getId()) return ResponseEntity.badRequest().body("You are not authorized to add a resource to this lesson");
            String message = courseService.addLessonResource(course, lessonId, file);
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("An error occurred" + e.getMessage());
        }

    }

    @GetMapping("/course/{courseName}/lessons/{lessonId}/resources")
    @Operation(summary = "List lesson resources", description = "Returns the resources attached to a lesson if the current user has access to them.")
    public ResponseEntity<?> getAllResources(@PathVariable("courseName") String courseName,@PathVariable("lessonId") int lessonId){
        try{
            Course course = courseService.getCourse(courseName);
            if(course == null){
                return ResponseEntity.badRequest().body("Course not found");
            }
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found, Please register or login first");
            }
            List<LessonResourceDto> resources = courseService.getLessonResources(course, user, lessonId);

            return  ResponseEntity.ok(resources);

        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("An error occurred" + e.getMessage());
        }
    }

    @GetMapping("/course/{courseName}/lessons/{lessonId}/resources/{resourceId}")
    @Operation(summary = "Download a lesson resource", description = "Downloads a single lesson resource file by resource id if the current user has permission to access it.")
    public ResponseEntity<?> getResource(@PathVariable("courseName") String courseName,
                                         @PathVariable("lessonId") int lessonId,
                                         @PathVariable("resourceId") int resourceId
    ){
        try{
            Course course = courseService.getCourse(courseName);
            if(course == null){
                return ResponseEntity.badRequest().body("Course not found");
            }
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found, Please register or login first");
            }
            byte[] resourceFile = courseService.getFileResources(course, user, lessonId, resourceId);
            List<LessonResourceDto> lessonResources = courseService.getLessonResources(course,user,lessonId);
            LessonResourceDto resource = null;
            for (LessonResourceDto lessonResourceDto : lessonResources) {
                if (lessonResourceDto.getResource_id() == resourceId){
                    resource = lessonResourceDto;
                    break;
                }
            }
            String mimeType = URLConnection.guessContentTypeFromName(resource.getFile_name());
            return ResponseEntity.status(200).contentType(MediaType.parseMediaType(mimeType)).body(resourceFile);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("An error occurred" + e.getMessage());
        }

    }

    @PostMapping("/course/{courseName}/enroll")
    @Operation(summary = "Enroll in a course", description = "Enrolls the authenticated student in the specified course.")
    public ResponseEntity<?> enrollCourse(@PathVariable("courseName") String courseName){
       try {
           String email = SecurityContextHolder.getContext().getAuthentication().getName();
           User user = userService.getUserByEmail(email);
           if (!(user instanceof Student))
               return ResponseEntity.status(403).body("You are not authorized to enroll in this course");
           Set<CourseDto> enrolledCourses = courseService.enrollCourse(courseName, user);
           return ResponseEntity.ok(enrolledCourses);
       }
       catch (Exception e){
           return ResponseEntity.badRequest().body("An error occurred" + e.getMessage());
       }

    }

    @GetMapping("/course/{courseName}/enrolled")
    @Operation(summary = "List enrolled students", description = "Returns the students currently enrolled in the specified course.")
    public ResponseEntity<?> getEnrolledStudents(@PathVariable("courseName") String courseName){
        try{
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                throw new Exception("User not found, Please register or login first");
            }
            Set<StudentDto> studentDtos = courseService.getEnrolledStd(courseName);
            return ResponseEntity.ok(studentDtos);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body("An error occurred" + e.getMessage());
        }
    }

    @DeleteMapping("/course/{courseName}/remove-student/{studentId}")
    @Operation(summary = "Remove a student from a course", description = "Removes a student from the specified course. Only the instructor who owns the course can do this.")
    public ResponseEntity<?> removeEnrolledStd(@PathVariable("courseName") String courseName, @PathVariable("studentId") int studentId)
    {
        try{

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found, Please register or login first");
        }
        if (!(user instanceof Instructor)) {
            return ResponseEntity.status(403).body("You are not authorized to remove a student from this course");
        }
        Instructor instructor = (Instructor) user;
        Course course = courseService.getCourse(courseName);
        if (course == null) return ResponseEntity.badRequest().body("Course not found");
        if (instructor.getId() != course.getInstructor().getId()) {
            return ResponseEntity.status(403).body("You are not authorized to remove a student from this course");
        }

        courseService.removeEnrolledstd(course, instructor, studentId);
        return ResponseEntity.ok("Student removed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("An error occurred" + e.getMessage());
        }
    }

    @PostMapping("/course/{courseName}/lessons/{lessonId}/generate-OTP")
    @Operation(summary = "Generate lesson attendance OTP", description = "Generates a one-time attendance code for the lesson and sends it to enrolled students.")
    public ResponseEntity<?> generateOTP(@PathVariable("courseName") String courseName,@PathVariable int lessonId,@RequestParam("duration") int duration){
        try{
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);

            if (user == null) {return ResponseEntity.badRequest().body("User not found, Please register or login first");}
            if (!(user instanceof Instructor)) {return ResponseEntity.badRequest().body("You are not authorized");}

            Instructor instructor = (Instructor) user;
            Course course = courseService.getCourse(courseName);

            if (course == null) return ResponseEntity.badRequest().body("Course not found");
            Lesson lesson = courseService.getLessonbyId(course, lessonId);

            if (lesson == null) {
                return ResponseEntity.badRequest().body("Lesson id not found.");
            }

            if (duration <= 0) {
                return ResponseEntity.badRequest().body("Duration must be greater than 0 minutes.");
            }

            // Allow re-generating OTP at any time; generating a new OTP invalidates the previous one.

            if (course.getInstructor().getId() != instructor.getId()) {return ResponseEntity.badRequest().body("You are not authorized to generate OTP for this course.");}

            //get enrolled students
            Set<Student> students = courseService.getEnrolledStd(course);
            int otp = courseService.generateOTP(
                    course, students, instructor, lesson, duration
            );
            return ResponseEntity.ok(otp);

        }catch (Exception e){
            log.error("Failed to generate OTP for course='{}', lessonId={}, duration={}", courseName, lessonId, duration, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "Failed to generate OTP",
                    "error", e.getMessage() == null ? "Unknown error" : e.getMessage()
            ));
        }


    }


    @PostMapping("/course/{courseName}/lessons/{lessonId}/attendLesson")
    @Operation(summary = "Attend a lesson with OTP", description = "Marks the authenticated student as present for the lesson after validating the provided OTP code.")
    public ResponseEntity<?> attendLesson(@PathVariable("courseName") String courseName,@PathVariable int lessonId,@RequestParam("otp") int otp)
    {
        try{
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {return ResponseEntity.badRequest().body("User not found, Please register or login first");}
            if (! (user instanceof Student)) {return ResponseEntity.badRequest().body("You are not allowed to attend lessons");}

            Student student = (Student) user;
            Course course = courseService.getCourse(courseName);
            if (course == null) return ResponseEntity.badRequest().body("Course not found");
            if (!(student.getEnrolled_courses().contains(course))) {
                return ResponseEntity.status(403).body("Student is not enrolled course");
            }

            Lesson lesson = courseService.getLessonbyId(course, lessonId);
            if (lesson == null) return ResponseEntity.badRequest().body("Lesson id not found");
            if (lesson.getAttendees().contains(student)) {
                return ResponseEntity.status(403).body("Lesson already attended");
            }

            LessonDto lessonDto = courseService.attendLesson(course, student, lesson, otp);
            return ResponseEntity.ok(lessonDto);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body("An error occurred" + e.getMessage());
        }



    }

    @GetMapping("/course/{courseName}/attended-lessons")
    @Operation(summary = "List attended lessons", description = "Returns the lessons in the specified course that the authenticated student has already attended.")
    public ResponseEntity<?> getAttendedLessons(@PathVariable("courseName") String courseName ){
        try{
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);

            if (user == null) return ResponseEntity.badRequest().body("User not found, Please register or login first");
            if (! (user instanceof Student)) return ResponseEntity.badRequest().build();

            Student student = (Student) user;
            Course course = courseService.getCourse(courseName);
            if (course == null) return ResponseEntity.badRequest().body("Course not found");
            Set <LessonDto>lessondto = courseService.getLessonAttended(course, student);
            return ResponseEntity.ok(lessondto);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body("An error occurred" + e.getMessage());
        }
    }


    @GetMapping("/course/{courseName}/lessons/{lessonId}/attendanceList")
    @Operation(summary = "Get lesson attendance list", description = "Returns the students who attended the specified lesson.")
    public ResponseEntity<?> getAttendance(@PathVariable("courseName") String courseName,@PathVariable int lessonId){
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);

            if (user == null) {
                return ResponseEntity.badRequest().body("User not found, Please register or login first");
            }
            if (!(user instanceof Instructor)) {
                return ResponseEntity.badRequest().body("You are not authorized");
            }

            Instructor instructor = (Instructor) user;
            Course course = courseService.getCourse(courseName);

            if (course == null) return ResponseEntity.badRequest().body("Course not found");
            Lesson lesson = courseService.getLessonbyId(course, lessonId);

            if (lesson == null) return ResponseEntity.badRequest().body("Lesson id not found");

            List<StudentDto> attendanceList = courseService.getAttendance(lesson);
            return ResponseEntity.ok(attendanceList);

        }
        catch (Exception e){
            return ResponseEntity.badRequest().body("An error occurred" + e.getMessage());
        }
    }


    // ==================== VIDEO STREAMING ENDPOINTS ====================

    @PostMapping("/course/{courseName}/lessons/{lessonId}/upload-video")
    @Operation(summary = "Upload lesson video", description = "Uploads a video file for the specified lesson. Only the course instructor can upload. Replaces any existing video.")
    public ResponseEntity<?> uploadVideo(
            @PathVariable("courseName") String courseName,
            @PathVariable("lessonId") int lessonId,
            @RequestParam("video") MultipartFile video) {
        try {
            Course course = courseService.getCourse(courseName);
            if (course == null) return ResponseEntity.badRequest().body("Course not found");

            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) return ResponseEntity.badRequest().body("User not found");
            if (!(user instanceof Instructor))
                return ResponseEntity.status(403).body("Only instructors can upload videos");

            Instructor instructor = (Instructor) user;
            if (instructor.getId() != course.getInstructor().getId())
                return ResponseEntity.status(403).body("You are not the instructor of this course");

            Lesson lesson = courseService.getLessonbyId(course, lessonId);
            if (lesson == null) return ResponseEntity.badRequest().body("Lesson not found");

            String fileName = videoStreamingService.uploadVideo(course, lesson, video);
            return ResponseEntity.ok(Map.of(
                    "message", "Video uploaded successfully",
                    "fileName", fileName,
                    "hasVideo", true
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/course/{courseName}/lessons/{lessonId}/stream-video")
    @Operation(summary = "Stream lesson video", description = "Streams the lesson video with HTTP Range support for seeking. Accessible by the course instructor and enrolled students who attended.")
    public ResponseEntity<?> streamVideo(
            @PathVariable("courseName") String courseName,
            @PathVariable("lessonId") int lessonId,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            Course course = courseService.getCourse(courseName);
            if (course == null) return ResponseEntity.badRequest().body("Course not found");

            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) return ResponseEntity.badRequest().body("User not found");

            // Authorization: instructor of course OR enrolled student
            if (user instanceof Instructor) {
                Instructor instructor = (Instructor) user;
                if (instructor.getId() != course.getInstructor().getId())
                    return ResponseEntity.status(403).body("Not authorized to view this video");
            } else if (user instanceof Student) {
                Student student = (Student) user;
                if (!student.getEnrolled_courses().contains(course))
                    return ResponseEntity.status(403).body("You must be enrolled to watch this video");
            } else {
                return ResponseEntity.status(403).body("Not authorized");
            }

            Lesson lesson = courseService.getLessonbyId(course, lessonId);
            if (lesson == null) return ResponseEntity.badRequest().body("Lesson not found");

            Resource videoResource = videoStreamingService.getVideoAsResource(lesson);
            long fileSize = videoStreamingService.getVideoFileSize(lesson);
            String contentType = videoStreamingService.getVideoContentType(lesson.getVideoPath());

            // Handle Range requests for video seeking/scrubbing
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] ranges = rangeHeader.substring(6).split("-");
                long rangeStart = Long.parseLong(ranges[0]);
                long rangeEnd = ranges.length > 1 && !ranges[1].isEmpty()
                        ? Long.parseLong(ranges[1])
                        : Math.min(rangeStart + 1_048_576 - 1, fileSize - 1); // 1MB chunks

                if (rangeEnd >= fileSize) rangeEnd = fileSize - 1;
                long contentLength = rangeEnd - rangeStart + 1;

                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", contentType);
                headers.set("Accept-Ranges", "bytes");
                headers.set("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileSize);
                headers.setContentLength(contentLength);

                // Read the specific byte range
                byte[] data = new byte[(int) contentLength];
                try (var is = videoResource.getInputStream()) {
                    is.skip(rangeStart);
                    is.read(data, 0, (int) contentLength);
                }

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .headers(headers)
                        .body(data);
            }

            // Full file response (no Range header)
            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Accept-Ranges", "bytes")
                    .header("Content-Length", String.valueOf(fileSize))
                    .body(videoResource);

        } catch (IllegalStateException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("An error occurred: " + e.getMessage());
        }
    }

    @DeleteMapping("/course/{courseName}/lessons/{lessonId}/delete-video")
    @Operation(summary = "Delete lesson video", description = "Deletes the video for the specified lesson. Only the course instructor can delete.")
    public ResponseEntity<?> deleteVideo(
            @PathVariable("courseName") String courseName,
            @PathVariable("lessonId") int lessonId) {
        try {
            Course course = courseService.getCourse(courseName);
            if (course == null) return ResponseEntity.badRequest().body("Course not found");

            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) return ResponseEntity.badRequest().body("User not found");
            if (!(user instanceof Instructor))
                return ResponseEntity.status(403).body("Only instructors can delete videos");

            Instructor instructor = (Instructor) user;
            if (instructor.getId() != course.getInstructor().getId())
                return ResponseEntity.status(403).body("You are not the instructor of this course");

            Lesson lesson = courseService.getLessonbyId(course, lessonId);
            if (lesson == null) return ResponseEntity.badRequest().body("Lesson not found");

            videoStreamingService.deleteVideo(course, lesson);
            return ResponseEntity.ok("Video deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("An error occurred: " + e.getMessage());
        }
    }

}
