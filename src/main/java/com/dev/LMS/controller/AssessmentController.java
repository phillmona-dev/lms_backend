package com.dev.LMS.controller;

import com.dev.LMS.dto.*;
import com.dev.LMS.exception.CourseNotFoundException;
import com.dev.LMS.model.*;
import com.dev.LMS.service.AssessmentService;
import com.dev.LMS.service.CourseService;
import com.dev.LMS.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContextException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@Controller
@RequestMapping("/course/{course-name}")
@Tag(name = "Assessments", description = "Endpoints for question banks, quizzes, assignments, submissions, and grading.")
public class AssessmentController {

    AssessmentService assessmentService;
    UserService userService;
    CourseService courseService;
    @PostMapping("/create-question") //tested
    @Operation(summary = "Create a question", description = "Adds a new question to the question bank of the specified course.")
    public ResponseEntity<?> addQuestion(@PathVariable("course-name") String courseName,
                                         @RequestBody Question question)
    {

        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found, Please register or login first");
            }
            if (!(user  instanceof Instructor)) {
                return ResponseEntity.status(405).body("You are not authorized to create an assignment");
            }
            Instructor instructor = (Instructor) user;
            System.out.println(question);
            assessmentService.createQuestion(courseName,question);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        catch(Exception e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/create-quiz") //tested
    @Operation(summary = "Create a quiz", description = "Creates a quiz for the specified course and notifies enrolled students.")
    public ResponseEntity<?> createQuiz(@PathVariable("course-name") String courseName,
                                        @RequestBody Quiz quiz)
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("DEBUG: Creating quiz for email: " + email);

        User user = userService.getUserByEmail(email);
        if (user == null) {
            System.out.println("DEBUG: User not found for email: " + email);
            return ResponseEntity.badRequest().body("User not found, Please register or login first");
        }
        System.out.println("DEBUG: User found: " + user.getClass().getSimpleName());
        System.out.println("DEBUG: User email: " + (user.getEmail() != null ? user.getEmail() : "null"));
        
        if (!(user  instanceof Instructor)) {
            System.out.println("DEBUG: User is not an instructor, actual type: " + user.getClass().getSimpleName());
            return ResponseEntity.status(403).body("You are not authorized to create an assignment");
        }
        try {
            System.out.println("DEBUG: Creating quiz...");
            assessmentService.createQuiz(courseName,quiz);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        catch(Exception e){
            System.out.println("DEBUG: Exception: " + e.getMessage());
            e.printStackTrace();
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
            return new ResponseEntity<>(errorMsg, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/get-questions") //tested
    @Operation(summary = "List question bank items", description = "Returns all questions currently available in the question bank for the specified course.")
    public ResponseEntity<?> getQuestions(@PathVariable("course-name") String courseName){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found, Please register or login first");
        }
        if (!(user  instanceof Instructor)) {
            return ResponseEntity.status(403).body("You are not authorized to create an assignment");
        }
        try {
            List<QuestionDto> questions = assessmentService.getQuestions(courseName);
            return  ResponseEntity.ok(questions);
        }
        catch(Exception e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    } @GetMapping("/quizzes")
    @Operation(summary = "List course quizzes", description = "Returns all quizzes for the specified course for authorized instructors and enrolled students.")
    public ResponseEntity<?> getQuizzes(@PathVariable("course-name") String courseName) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found, Please register or login first");
        }
        if (user instanceof Instructor || user instanceof Student) {
            try {
                Course course = courseService.getCourse(courseName);
                List<Quiz> quizzes = course.getQuizzes();
                List<QuizDto> quizDtos = quizzes.stream()
                        .map(QuizDto::toDto)
                        .toList();
                return ResponseEntity.ok(quizDtos);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to view quizzes: " + e.getMessage());
            }
        }
        return ResponseEntity.status(403).body("You are not authorized to view quizzes.");
    }

    @DeleteMapping("/quiz/{quizId}")
    @Operation(summary = "Delete quiz", description = "Deletes a quiz from the course. Only the course instructor can perform this action.")
    public ResponseEntity<?> deleteQuiz(@PathVariable("course-name") String courseName, 
                                       @PathVariable Long quizId) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found, Please register or login first");
            }
            if (!(user instanceof Instructor)) {
                return ResponseEntity.status(403).body("You are not authorized to delete quizzes.");
            }
            
            assessmentService.deleteQuiz(courseName, quizId);
            return ResponseEntity.ok("Quiz deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete quiz: " + e.getMessage());
        }
    }

    @PutMapping("/quiz/{quizId}")
    @Operation(summary = "Update quiz", description = "Updates quiz title and duration. Only the course instructor can perform this action.")
    public ResponseEntity<?> updateQuiz(@PathVariable("course-name") String courseName, 
                                      @PathVariable Long quizId,
                                      @RequestBody Quiz updatedQuiz) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found, Please register or login first");
            }
            if (!(user instanceof Instructor)) {
                return ResponseEntity.status(403).body("You are not authorized to update quizzes.");
            }
            
            QuizDto updatedQuizDto = assessmentService.updateQuiz(courseName, quizId, updatedQuiz);
            return ResponseEntity.ok(updatedQuizDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update quiz: " + e.getMessage());
        }
    }

    @PostMapping("/quiz/{quizId}/questions")
    @Operation(summary = "Add question to quiz", description = "Adds a question to an existing quiz. Only the course instructor can perform this action.")
    public ResponseEntity<?> addQuestionToQuiz(@PathVariable("course-name") String courseName, 
                                              @PathVariable Long quizId,
                                              @RequestBody Question question) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found, Please register or login first");
            }
            if (!(user instanceof Instructor)) {
                return ResponseEntity.status(403).body("You are not authorized to modify quizzes.");
            }
            
            assessmentService.addQuestionToQuiz(courseName, quizId, question);
            return ResponseEntity.ok("Question added to quiz successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add question to quiz: " + e.getMessage());
        }
    }

    @DeleteMapping("/quiz/{quizId}/questions/{questionId}")
    @Operation(summary = "Remove question from quiz", description = "Removes a question from an existing quiz. Only the course instructor can perform this action.")
    public ResponseEntity<?> removeQuestionFromQuiz(@PathVariable("course-name") String courseName, 
                                                  @PathVariable Long quizId,
                                                  @PathVariable Long questionId) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found, Please register or login first");
            }
            if (!(user instanceof Instructor)) {
                return ResponseEntity.status(403).body("You are not authorized to modify quizzes.");
            }
            
            assessmentService.removeQuestionFromQuiz(courseName, quizId, questionId);
            return ResponseEntity.ok("Question removed from quiz successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to remove question from quiz: " + e.getMessage());
        }
    }

    @PutMapping("/question/{questionId}")
    @Operation(summary = "Update question", description = "Updates question content, type, and choices. Only the course instructor can perform this action.")
    public ResponseEntity<?> updateQuestion(@PathVariable("course-name") String courseName, 
                                          @PathVariable Long questionId,
                                          @RequestBody Question updatedQuestion) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found, Please register or login first");
            }
            if (!(user instanceof Instructor)) {
                return ResponseEntity.status(403).body("You are not authorized to modify questions.");
            }
            
            QuestionDto questionDto = assessmentService.updateQuestion(courseName, questionId, updatedQuestion);
            return ResponseEntity.ok(questionDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update question: " + e.getMessage());
        }
    }

    @DeleteMapping("/question/{questionId}")
    @Operation(summary = "Delete question", description = "Deletes a question from the question bank. Only the course instructor can perform this action.")
    public ResponseEntity<?> deleteQuestion(@PathVariable("course-name") String courseName, 
                                          @PathVariable Long questionId) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found, Please register or login first");
            }
            if (!(user instanceof Instructor)) {
                return ResponseEntity.status(403).body("You are not authorized to delete questions.");
            }
            
            assessmentService.deleteQuestion(courseName, questionId);
            return ResponseEntity.ok("Question deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete question: " + e.getMessage());
        }
    }

    @GetMapping("/get-question-by-id") //tested
    @Operation(summary = "Get question by id", description = "Returns a single question from the course question bank using the supplied question id.")
    public ResponseEntity<?> getQuestions(@PathVariable("course-name") String courseName, @RequestBody int questionId){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found, Please register or login first");
        }
        if (!(user  instanceof Instructor)) {
            return ResponseEntity.status(403).body("You are not authorized to create an assignment");
        }
        try {
           QuestionDto question =  assessmentService.getQuestionById(courseName,questionId);
            return ResponseEntity.ok(question);
        }
        catch(Exception e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/{quizName}/take-quiz") //tested and fixed
    @Operation(summary = "Start a quiz attempt", description = "Generates a quiz submission for the authenticated student with quiz questions selected for that attempt.")
    public ResponseEntity<?> takeQuiz(
            @PathVariable("course-name") String courseName,
            @PathVariable("quizName") String quizName) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found. Please register or login first.");
        }
        if (!(user instanceof Student)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only students can take quizzes.");
        }

        try {
            QuizSubmissionDto quizSubmission = assessmentService.generateQuiz(courseName, quizName ,(Student)user);
            System.out.println(quizSubmission);
            return ResponseEntity.status(HttpStatus.CREATED).body(quizSubmission);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (CourseNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @PostMapping("/{quizName}/submit-quiz")
    @Operation(summary = "Submit quiz answers", description = "Submits the authenticated student's answers for the specified quiz attempt.")
    public ResponseEntity<?> submitQuiz(
            @PathVariable("course-name") String courseName,
            @PathVariable("quizName") String quizName,@RequestBody List<SubmittedQuestion> submittedQuestions) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found. Please register or login first.");
        }
        if (!(user instanceof Student)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only students can submit quizzes.");
        }
        try {
            assessmentService.submitQuiz(courseName, quizName, submittedQuestions,(Student) user);
            return ResponseEntity.status(HttpStatus.CREATED).body("submitted successfully");
        } catch (CourseNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @GetMapping("/{quizName}/grade")
    @Operation(summary = "Get quiz grade", description = "Calculates and returns the authenticated student's grade for the specified quiz.")
   // getQuizGrade(String quizTitle,String courseName , Student user)
    public ResponseEntity<?> getGrade(@PathVariable("course-name") String courseName , @PathVariable("quizName") String quizTitle){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found, Please register or login first");
        }
        if (!(user  instanceof Student)) {
            return ResponseEntity.status(403).body("You are not authorized to get the grade");
        }
        try {
            int grade = assessmentService.getQuizGrade(quizTitle,courseName,(Student) user );
            return  ResponseEntity.ok(grade);
        }
        catch(Exception e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping(value = "/create-assignment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create an assignment with optional attachment", description = "Creates a new assignment in the specified course with an optional attachment file. Only the course instructor can perform this action.")
    public ResponseEntity<?> createAssignmentWithAttachment(@PathVariable("course-name") String courseName,
                                                            @RequestParam("title") String title,
                                                            @RequestParam(value = "description", required = false) String description,
                                                            @RequestParam(value = "dueDate", required = false) String dueDate,
                                                            @RequestParam(value = "attachment", required = false) MultipartFile attachment)
    {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found, Please register or login first");
            }
            if (!(user instanceof Instructor)) {
                return ResponseEntity.status(403).body("You are not authorized to create an assignment");
            }

            Assignment assignment = new Assignment();
            assignment.setTitle(title);
            assignment.setDescription(description);
            if (dueDate != null && !dueDate.isBlank()) {
                assignment.setDueDate(java.time.LocalDateTime.parse(dueDate));
            }

            Instructor instructor = (Instructor) user;
            Course course = courseService.getCourse(courseName);
            AssignmentDto response = assessmentService.addAssignmentWithAttachment(course, assignment, instructor, attachment);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping(value = "/create-assignment", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create an assignment", description = "Creates a new assignment in the specified course. Only the course instructor can perform this action.")
    public ResponseEntity<?> createAssignment(@PathVariable("course-name") String courseName,
                                              @RequestBody Assignment assignment)
    {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found, Please register or login first");
            }
            if (!(user  instanceof Instructor)) {
                return ResponseEntity.status(403).body("You are not authorized to create an assignment");
            }
            Instructor instructor = (Instructor) user;
            // retrieving course
            Course course = courseService.getCourse(courseName);

            // returns true if the user is the instructor of this course
            AssignmentDto response = assessmentService.addAssignment(course,assignment,instructor);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }

    }

    @GetMapping("/assignment/{assignment_id}/attachment")
    @Operation(summary = "Download assignment attachment", description = "Downloads the instructor-provided attachment file for the specified assignment.")
    public ResponseEntity<?> getAssignmentAttachment(@PathVariable("course-name") String courseName,
                                                     @PathVariable("assignment_id") int assignmentId)
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found, Please register or login first.");
        }

        try {
            Course course = courseService.getCourse(courseName);
            Assignment assignment = assessmentService.getAssignment(course, user, assignmentId);
            byte[] attachmentData = assessmentService.downloadAssignmentAttachment(assignment);

            String fileName = assignment.getAttachmentFileName() != null ? assignment.getAttachmentFileName() : "assignment_attachment";
            MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
            if (assignment.getAttachmentFileType() != null && !assignment.getAttachmentFileType().isBlank()) {
                try {
                    contentType = MediaType.parseMediaType(assignment.getAttachmentFileType());
                } catch (Exception ignored) {
                    contentType = MediaType.APPLICATION_OCTET_STREAM;
                }
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(contentType)
                    .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                    .body(attachmentData);
        } catch (ApplicationContextException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to view assignment attachment: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/assignments") // "/view-assignments"
    @Operation(summary = "List course assignments", description = "Returns all assignments for the specified course for authorized instructors and enrolled students.")
    public ResponseEntity<?> viewAssignments(@PathVariable("course-name") String courseName)
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found, Please register or login first");
        }
        // only instructor and student are authorized
        if (user instanceof Instructor || user instanceof Student) {
            try {
                // retrieving course
                Course course = courseService.getCourse(courseName);
                // retrieve the assignments from the course
                List<Assignment> assignments = course.getAssignments();
                List<AssignmentDto> assignmentDtos = assessmentService.getAssignments(course, user);
                // return assignments in response
                return ResponseEntity.ok(Map.of("assignments", assignmentDtos));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to view this course assignment list: " + e.getMessage());
            }
        }
        return ResponseEntity.status(403).body("You are not authorized to view assignments.");
    }

    @GetMapping("/assignment/{assignment_id}/view")     // "/view-assignment/{id}"
    @Operation(summary = "Get assignment details", description = "Returns one assignment by assignment id for an authorized instructor or enrolled student.")
    public ResponseEntity<?> viewAssignment(@PathVariable("course-name") String courseName,
                                            @PathVariable("assignment_id") int assignment_id)
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found, Please register or login first");
        }
        // only instructor and student are authorized
        if (user instanceof Instructor || user instanceof Student) {
            try {
                // retrieving course
                Course course = courseService.getCourse(courseName);
                // retrieve the assignments from the course
                Assignment assignment = assessmentService.getAssignment(course, user, assignment_id);
                if (assignment == null) {
                    return ResponseEntity.status(404).body("Assignment not found");
                }
                // return the assignment in response
                return ResponseEntity.ok(new AssignmentDto(assignment));
            }
            catch (ApplicationContextException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to view assignment: " + e.getMessage());
            }
            catch (IllegalStateException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
        }
        return ResponseEntity.status(403).body("You are not authorized to view assignments.");
    }

    @PostMapping("/assignment/{assignment_id}/submit")   // "submit-assignment/{assignment_id}"
    @Operation(summary = "Submit an assignment", description = "Uploads a PDF submission for the specified assignment as the authenticated student.")
    public ResponseEntity<?> submitAssignment(@PathVariable("course-name") String courseName,
                                              @PathVariable("assignment_id") int assignmentId,
                                              @RequestParam("file") MultipartFile file)
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        if(user == null){
            return ResponseEntity.badRequest().body("User not found, Please register or login first.");
        }
        if(!(user instanceof Student)) {
            return ResponseEntity.status(403).body("You are not authorized to submit assignments.");
        }
        if(!file.getOriginalFilename().endsWith(".pdf")){
            return ResponseEntity.badRequest().body("Only PDF files are allowed.");
        }
        Student student = (Student) user;
        Course course = courseService.getCourse(courseName);
        Assignment assignment = assessmentService.getAssignment(course, user, assignmentId);
        try {
            String response = assessmentService.uploadSubmissionFile(file, assignment, student);
            return ResponseEntity.ok(response);
        }
        catch (ApplicationContextException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to submit assignment: " + e.getMessage());
        }
        catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/assignment/{assignment_id}/submissions")
    @Operation(summary = "List assignment submissions", description = "Returns all submissions for the specified assignment. Intended for the course instructor.")
    public ResponseEntity<?> getSubmissionsList(@PathVariable("course-name") String courseName,
                                                @PathVariable("assignment_id") int assignmentId)
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        if(user == null){
            return ResponseEntity.badRequest().body("User not found, Please register or login first.");
        }
        if(!(user instanceof Instructor)) {
            return ResponseEntity.status(403).body("You are not authorized to view students' submissions list.");
        }
        try {
            Course course = courseService.getCourse(courseName);
            Assignment assignment = assessmentService.getAssignment(course, user, assignmentId);
            List<AssignmentSubmissionDto> submissionsDto = assessmentService.getSubmissions(assignment);
            return ResponseEntity.ok(submissionsDto);
        }
        catch (ApplicationContextException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to view assignment submissions list: " + e.getMessage());
        }
        catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/assignment/{assignment_id}/submission/{submission_id}")
    @Operation(summary = "Download one assignment submission", description = "Downloads a single assignment submission file for review by the instructor.")
    public ResponseEntity<?> getAssignmentSubmission(@PathVariable("course-name") String courseName,
                                                     @PathVariable("assignment_id") int assignmentId,
                                                     @PathVariable("submission_id") int submissionId)
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        if(user == null){
            return ResponseEntity.badRequest().body("User not found, Please register or login first.");
        }
        if(!(user instanceof Instructor)) {
            return ResponseEntity.status(403).body("You are not authorized to view a students' submissions.");
        }
        try {
            Course course = courseService.getCourse(courseName);
            Assignment assignment = assessmentService.getAssignment(course, user, assignmentId);
            byte[] submissionFile = assessmentService.downloadSubmissionFile(assignment, submissionId);

            return ResponseEntity.status(200).contentType(MediaType.APPLICATION_PDF).body(submissionFile);
        }
        catch (ApplicationContextException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to view assignment submissions: " + e.getMessage());
        }
        catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/assignment/{assignment_id}/submission/{submission_id}/grade")
    @Operation(summary = "Grade an assignment submission", description = "Sets the grade for a specific assignment submission and notifies the student.")
    public ResponseEntity<?> gradeAssignment(@PathVariable("course-name") String courseName,
                                             @PathVariable("assignment_id") int assignmentId,
                                             @PathVariable("submission_id") int submissionId,
                                             @RequestBody Map<String, Integer> gradeMap)
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        if(user == null){
            return ResponseEntity.badRequest().body("User not found, Please register or login first.");
        }
        if(!(user instanceof Instructor)) {
            return ResponseEntity.status(403).body("You are not authorized to grade a students' submissions.");
        }
        try {
            Course course = courseService.getCourse(courseName);
            Assignment assignment = assessmentService.getAssignment(course, user, assignmentId);
            AssignmentSubmission submission = assessmentService.getSubmission(assignment, submissionId);
            AssignmentSubmissionDto gradedSubmission = assessmentService.setAssignmentGrade(submission, course, gradeMap);
            return ResponseEntity.status(HttpStatus.CREATED).body(gradedSubmission);
        }
        catch (ApplicationContextException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to grade assignment submissions: " + e.getMessage());
        }
        catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/assignment/{assignment_id}/get-grade")
    @Operation(summary = "Get assignment grade", description = "Returns the authenticated student's grade for the specified assignment.")
    public ResponseEntity<?> getAssignmentGrade(@PathVariable("course-name") String courseName,
                                             @PathVariable("assignment_id") int assignmentId,
                                             @RequestBody Map<String, Integer> gradeMap)
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        if(user == null){
            return ResponseEntity.badRequest().body("User not found, Please register or login first.");
        }
        if(!(user instanceof Student)) {
            return ResponseEntity.status(403).body("You are not authorized to view a submission's grade.");
        }
        try {
            Student student = (Student) user;
            Course course = courseService.getCourse(courseName);
            Assignment assignment = assessmentService.getAssignment(course, user, assignmentId);
            int grade = assessmentService.getAssignmentGrade(assignment, student);
            Map<String, Integer> response = new HashMap<>();
            response.put("grade", grade);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        catch (ApplicationContextException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
