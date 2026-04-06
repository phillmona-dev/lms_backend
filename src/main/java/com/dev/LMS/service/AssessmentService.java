package com.dev.LMS.service;

import com.dev.LMS.dto.*;
import com.dev.LMS.model.*;
import com.dev.LMS.repository.ChoiceRepository;
import com.dev.LMS.repository.CourseRepository;
import com.dev.LMS.repository.QuizSubmissionRepositry;
import com.dev.LMS.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class AssessmentService {
    private CourseRepository courseRepository;
    private UserRepository userRepository;
    private QuizSubmissionRepositry quizSubmissionRepositry;
    private ChoiceRepository choiceRepository;
    private NotificationService notificationService;
    private EmailService emailService;
    @Value("${file.upload.base-path.assignment-submissions}")
    private String UPLOAD_DIR;
    @Value("${file.upload.base-path.assignment-attachments}")
    private String ASSIGNMENT_ATTACHMENT_UPLOAD_DIR;

    AssessmentService(CourseRepository courseRepository, UserRepository userRepository, QuizSubmissionRepositry quizSubmissionRepositry, ChoiceRepository choiceRepository, NotificationService NotificationService, EmailService EmailService){
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.quizSubmissionRepositry = quizSubmissionRepositry;
        this.choiceRepository = choiceRepository;
        this.notificationService =  NotificationService;
        this.emailService = EmailService;
    }

    public void createQuestion(String courseName , Question question ){
        Course course = courseRepository.findByName(courseName)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseName));
        question.setChoices(normalizeChoices(question.getChoices()));
        course.addQuestion(question);
        courseRepository.save(course);
    }
    // no need for course so I remove it
    public QuestionDto getQuestionById(String courseName, int questionId){
        Course course= courseRepository.findByName(courseName).
                orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseName));;
        List<Question> questions = course.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            Question temp = questions.get(i);
            if(temp.getId() == questionId ){
                return QuestionDto.toDto(temp);
            }
        }
        throw  new IllegalArgumentException("No question by this Id: "+questionId);
    }
    // change parameter Course-> CourseId
    public List<QuestionDto> getQuestions(String courseName){
        Course course = courseRepository.findByName(courseName).
                orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseName));
        List<Question> questionList = course.getQuestions();
        List<QuestionDto> questionDtoList = new ArrayList<>();
        for (int i = 0; i < questionList.size(); i++){
            questionDtoList.add(QuestionDto.toDto(questionList.get(i)));
        }
        if(questionList.isEmpty())
            throw new IllegalArgumentException("No questions available for this course.");
        return questionDtoList;
    }
    // change parameter Course-> CourseId
    public void createQuiz(String courseName , Quiz quizRequest){
        Course course = courseRepository.findByName(courseName)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseName));

        // Build a managed quiz entity and only attach managed question-bank entities by id.
        Quiz newQuiz = new Quiz();
        newQuiz.setQuizTitle(quizRequest.getQuizTitle());
        newQuiz.setQuizDuration(quizRequest.getQuizDuration());
        newQuiz.setSubmissions(new ArrayList<>());
        newQuiz.setQuestions(new ArrayList<>());
        course.addQuiz(newQuiz);

        if (quizRequest.getQuestions() != null && !quizRequest.getQuestions().isEmpty()) {
            Set<Long> addedQuestionIds = new HashSet<>();
            for (Question requestedQuestion : quizRequest.getQuestions()) {
                if (requestedQuestion == null || requestedQuestion.getId() == null) {
                    continue;
                }
                if (!addedQuestionIds.add(requestedQuestion.getId())) {
                    continue;
                }

                Question managedQuestion = course.getQuestions().stream()
                        .filter(q -> q.getId().equals(requestedQuestion.getId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Question not found in course bank: " + requestedQuestion.getId()));

                newQuiz.addQuestion(managedQuestion);
            }
        }

        Notification notificationMessage = notificationService.createNotification("A new quiz " + newQuiz.getQuizTitle() + " was added to your course work" );
        Set<Student> enrolled_students = course.getEnrolled_students();
        String subject = "New Quiz was added";
        String content = "A new quiz added " + newQuiz.getQuizTitle() + "in Course: " + course.getName();
        for (Student student : enrolled_students) {
            notificationService.addNotifcationStudent(notificationMessage, student);
            emailService.sendEmail(
                    student.getEmail(),
                    student.getName(),
                    subject,
                    content,
                    course.getInstructor().getName()
            );
        }

        courseRepository.save(course);
    }
    
    @Transactional
    public void deleteQuiz(String courseName, Long quizId) {
        Course course = courseRepository.findByName(courseName)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseName));
        
        Quiz quizToDelete = course.getQuizzes().stream()
                .filter(quiz -> quiz.getQuizID().equals(quizId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found with ID: " + quizId));

        // Detach all quiz submissions from related entities before removing quiz.
        List<QuizSubmission> submissions = new ArrayList<>(quizToDelete.getSubmissions());
        for (QuizSubmission submission : submissions) {
            if (submission.getQuestions() != null) {
                for (Question question : new ArrayList<>(submission.getQuestions())) {
                    if (question.getSubmissions() != null) {
                        question.getSubmissions().remove(submission);
                    }
                }
                submission.getQuestions().clear();
            }

            if (submission.getStudent() != null && submission.getStudent().getQuizSubmissions() != null) {
                submission.getStudent().getQuizSubmissions().remove(submission);
            }

            submission.setStudent(null);
            submission.setQuiz(null);
        }

        quizToDelete.getSubmissions().clear();
        quizToDelete.getQuestions().clear();
        course.getQuizzes().remove(quizToDelete);
        courseRepository.save(course);
    }
    
    public QuizDto updateQuiz(String courseName, Long quizId, Quiz updatedQuiz) {
        Course course = courseRepository.findByName(courseName)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseName));
        
        Quiz existingQuiz = course.getQuizzes().stream()
                .filter(quiz -> quiz.getQuizID().equals(quizId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found with ID: " + quizId));
        
        // Update quiz properties
        if (updatedQuiz.getQuizTitle() != null && !updatedQuiz.getQuizTitle().trim().isEmpty()) {
            existingQuiz.setQuizTitle(updatedQuiz.getQuizTitle());
        }
        if (updatedQuiz.getQuizDuration() != null) {
            existingQuiz.setQuizDuration(updatedQuiz.getQuizDuration());
        }
        
        courseRepository.save(course);
        return QuizDto.toDto(existingQuiz);
    }
    
    public void addQuestionToQuiz(String courseName, Long quizId, Question question) {
        Course course = courseRepository.findByName(courseName)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseName));
        
        Quiz quiz = course.getQuizzes().stream()
                .filter(q -> q.getQuizID().equals(quizId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found with ID: " + quizId));
        
        // Find the actual question from the course's question bank
        Question courseQuestion = course.getQuestions().stream()
            .filter(q -> q.getId().equals(question.getId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Question not found in course bank: " + question.getId()));
        
        quiz.addQuestion(courseQuestion);
        courseRepository.save(course);
    }
    
    public void removeQuestionFromQuiz(String courseName, Long quizId, Long questionId) {
        Course course = courseRepository.findByName(courseName)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseName));
        
        Quiz quiz = course.getQuizzes().stream()
                .filter(q -> q.getQuizID().equals(quizId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found with ID: " + quizId));
        
        Question questionToRemove = quiz.getQuestions().stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Question not found in quiz: " + questionId));
        
        quiz.getQuestions().remove(questionToRemove);
        courseRepository.save(course);
    }
    
    public QuestionDto updateQuestion(String courseName, Long questionId, Question updatedQuestion) {
        Course course = courseRepository.findByName(courseName)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseName));
        
        Question existingQuestion = course.getQuestions().stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + questionId));
        
        // Update question properties
        if (updatedQuestion.getContent() != null && !updatedQuestion.getContent().trim().isEmpty()) {
            existingQuestion.setContent(updatedQuestion.getContent());
        }
        if (updatedQuestion.getType() != null) {
            existingQuestion.setType(updatedQuestion.getType());
        }
        if (updatedQuestion.getCorrectAnswer() != null && !updatedQuestion.getCorrectAnswer().trim().isEmpty()) {
            existingQuestion.setCorrectAnswer(updatedQuestion.getCorrectAnswer());
        }
        if (updatedQuestion.getChoices() != null && !updatedQuestion.getChoices().isEmpty()) {
            existingQuestion.setChoices(normalizeChoices(updatedQuestion.getChoices()));
        }
        
        courseRepository.save(course);
        return QuestionDto.toDto(existingQuestion);
    }

    private List<Choice> normalizeChoices(List<Choice> choices) {
        if (choices == null || choices.isEmpty()) {
            return new ArrayList<>();
        }

        List<Choice> normalized = new ArrayList<>();
        for (Choice choice : choices) {
            if (choice == null || choice.getValue() == null) {
                continue;
            }
            String value = choice.getValue().trim();
            if (value.isEmpty()) {
                continue;
            }
            Choice normalizedChoice = new Choice();
            normalizedChoice.setValue(value);
            normalized.add(normalizedChoice);
        }
        return normalized;
    }
    
    @Transactional
    public void deleteQuestion(String courseName, Long questionId) {
        Course course = courseRepository.findByName(courseName)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseName));
        
        Question questionToDelete = course.getQuestions().stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + questionId));
        
        // Delete from linking tables first to avoid FK constraints
        choiceRepository.deleteChoicesByQuestionId(questionId);
        choiceRepository.deleteQuizQuestionsByQuestionId(questionId);
        course.getQuestions().remove(questionToDelete);
        courseRepository.save(course);
    }
    
    // change parameter Course-> CourseId
    public QuizSubmissionDto generateQuiz(String courseName, String quizTitle , Student student) {
        Course course = courseRepository.findByName(courseName)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseName));

        List<Quiz> quizzes = course.getQuizzes();
        if(quizzes.isEmpty())
            throw new IllegalStateException("No quizzes available for "+courseName+" course");
        Quiz currentQuiz = null;
        boolean isFound = false;
        for (int i = 0; i < quizzes.size(); i++) {
            Quiz temp = quizzes.get(i);
            if(temp.getQuizTitle().equals(quizTitle)){
                currentQuiz = temp;
                isFound = true;
                break;
            }
        }
        if(!isFound)
            throw new IllegalStateException("This quiz dose not exit.");

        List<QuizSubmission> existingSubmissions = currentQuiz.getSubmissions();
        if (existingSubmissions != null) {
            boolean alreadyTaken = existingSubmissions.stream()
                    .anyMatch(submission -> submission.getStudent() != null
                            && Objects.equals(submission.getStudent().getId(), student.getId()));
            if (alreadyTaken) {
                throw new IllegalStateException("You have already taken this quiz. Multiple attempts are not allowed.");
            }
        }

        List<Question> selectedQuestions = new ArrayList<>(currentQuiz.getQuestions());
        if (selectedQuestions.isEmpty()) {
            throw new IllegalStateException("This quiz has no questions assigned.");
        }
        QuizSubmission quizSubmission = new QuizSubmission();
        quizSubmission.setQuestions(selectedQuestions);
        for(Question q: quizSubmission.getQuestions()){
            q.addSubmission(quizSubmission);
        }
        quizSubmission.setSubmittedQuestions(new ArrayList<>());
        quizSubmission.setGrade(0);
        quizSubmission.setStudent(student);
        student.addQuizSubmission(quizSubmission);
        quizSubmission.setQuiz(currentQuiz);
        currentQuiz.addQuizSubmission(quizSubmission);
        course.setQuiz(currentQuiz);
        quizSubmissionRepositry.save(quizSubmission);
        courseRepository.save(course);
        return QuizSubmissionDto.toDto(quizSubmission);
    }
    public void submitQuiz(String courseName, String quizTitle,List<SubmittedQuestion> studentSubmittedQuestions,Student student){
        Course course = courseRepository.findByName(courseName)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseName));
        List<Quiz> quizzes = course.getQuizzes();
        if(quizzes.isEmpty())
            throw new IllegalStateException("No quizzes available for "+courseName+" course");
        Quiz currentQuiz = null;
        int index = 0;
        boolean isFound = false;
        for (int i = 0; i < quizzes.size(); i++) {
            currentQuiz = quizzes.get(i);
            if(currentQuiz.getQuizTitle().equals(quizTitle)){
                index = i;
                isFound = true;
                break;
            }
        }
        if(!isFound) {
            throw new IllegalStateException("This quiz dose not exit.");
        }
        List<QuizSubmission> quizSubmissions = course.getQuizzes().get(index).getSubmissions();
        QuizSubmission quizSubmission = null;
        for (QuizSubmission q : quizSubmissions){
            if(q.getStudent().getId() == student.getId())
                quizSubmission = q;
        }
        if(quizSubmission == null)
            throw new IllegalStateException("there is no submission.");
        if(studentSubmittedQuestions == null || studentSubmittedQuestions.isEmpty())
            throw new IllegalStateException("Your submission is empty.");
        if(quizSubmission.getQuestions().isEmpty())
            throw new IllegalStateException("Question is empty.");
        SubmittedQuestion submittedQuestion = null;
        List<SubmittedQuestion> submittedQuestions= new ArrayList<>();
        for (int i = 0; i < studentSubmittedQuestions.size(); i++) {
            submittedQuestion = studentSubmittedQuestions.get(i);
            if (submittedQuestion.getStudentAnswer() == null) {
                System.out.println(submittedQuestion.getStudentAnswer());
                throw new IllegalStateException("Student answer cannot be null");
            }
            submittedQuestion.setSubmission(quizSubmission);
            submittedQuestion.setQuestion(quizSubmission.getQuestions().get(i));
            submittedQuestions.add(submittedQuestion);
        }
        quizSubmission.setSubmittedQuestions(submittedQuestions);
        currentQuiz.addQuizSubmission(quizSubmission);
        quizzes.set(index,currentQuiz);
        course.setQuizzes(quizzes);
        courseRepository.save(course);
    }

    public void gradeQuiz(String quizTitle,String courseName){
        Course course = courseRepository.findByName(courseName)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseName));
        List<Quiz> quizzes = course.getQuizzes();
        if(quizzes.isEmpty()) throw new IllegalArgumentException("No quizzes available for "+courseName+" course");
        Quiz currentQuiz = null;
        boolean isFound = false;
        int index = 0;
        for (int i = 0; i < quizzes.size(); i++) {
            Quiz temp = quizzes.get(i);
            if(temp.getQuizTitle().equals(quizTitle)){
                index = i;
                currentQuiz = temp;
                isFound = true;
                break;
            }
        }
        if(!isFound)
            throw new IllegalStateException("This quiz dose not exit.");
        List<QuizSubmission> quizSubmissions = currentQuiz.getSubmissions();
        if(quizSubmissions.isEmpty())
            throw new IllegalStateException("There is no submission ");
        for (int i = 0; i < quizSubmissions.size(); i++) {
            QuizSubmission currentQuizSubmission = quizSubmissions.get(i);
            List<SubmittedQuestion> submittedQuestions = currentQuizSubmission.getSubmittedQuestions();
            int grade=0;
            for (int j = 0; j < submittedQuestions.size(); j++){
                SubmittedQuestion CurrentSubmittedQuestion =submittedQuestions.get(j);
                Question currentQuestion = CurrentSubmittedQuestion.getQuestion();
                if(CurrentSubmittedQuestion.getStudentAnswer().
                        equals(currentQuestion.getCorrectAnswer()))
                    grade++;
            }
            quizSubmissions.get(i).setGrade(grade);
        }
        currentQuiz.setSubmissions(quizSubmissions);
        quizzes.set(index,currentQuiz);
        courseRepository.save(course);
    }
    public int getQuizGrade(String quizTitle,String courseName , Student user) {
        gradeQuiz(quizTitle,courseName);
        Course course = courseRepository.findByName(courseName)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseName));
        List<Quiz> quizzes = course.getQuizzes();
        if(quizzes.isEmpty()) throw new IllegalArgumentException("No quizzes available for "+courseName+" course");
        Quiz currentQuiz = null;
        boolean isFound = false;
        for (int i = 0; i < quizzes.size(); i++) {
            Quiz temp = quizzes.get(i);
            if(temp.getQuizTitle().equals(quizTitle)){
                currentQuiz = temp;
                isFound = true;
                break;
            }
        }
        if(!isFound)
            throw new IllegalStateException("This quiz dose not exit.");
        List<QuizSubmission> quizSubmissions = currentQuiz.getSubmissions();
        for (int i = 0; i < quizSubmissions.size(); i++) {
            if(quizSubmissions.get(i).getStudent().equals(user)){
                return quizSubmissions.get(i).getGrade();
            }
        }
        throw new IllegalStateException("There is no submission for this student: "+ user.getName());
    }
    public  List<Assignment> getAssignmentSub(Assignment assignment){
        List<Assignment> assignmentList = null;
        return assignmentList;
    }


    public AssignmentDto addAssignment(Course course, Assignment assignment, Instructor instructor){
        Set<Course> instructorCourses = instructor.getCreatedCourses();
        if(instructorCourses.contains(course)){
            course.addAssignment(assignment);

            Notification notificationMessage = notificationService.createNotification("A new assignment " + assignment.getTitle() + " was added to your course work" );
            Set<Student> enrolled_students = course.getEnrolled_students();
            String subject = "New Assignment was added";
            String content = "A new assignment added " + assignment.getTitle() + "in Course: " + course.getName();
            for (Student student : enrolled_students) {
                notificationService.addNotifcationStudent(notificationMessage, student);
                emailService.sendEmail(
                        student.getEmail(),
                        student.getName(),
                        subject,
                        content,
                        course.getInstructor().getName()
                );
            }

            courseRepository.save(course);
            return new AssignmentDto(assignment);
        }
        throw new IllegalStateException("You are not authorized to add assignments to this course");
    }

    public AssignmentDto addAssignmentWithAttachment(
            Course course,
            Assignment assignment,
            Instructor instructor,
            MultipartFile attachmentFile
    ) {
        Set<Course> instructorCourses = instructor.getCreatedCourses();
        if (!instructorCourses.contains(course)) {
            throw new IllegalStateException("You are not authorized to add assignments to this course");
        }

        if (attachmentFile != null && !attachmentFile.isEmpty()) {
            String storedPath = storeAssignmentAttachment(attachmentFile);
            assignment.setAttachmentFileName(attachmentFile.getOriginalFilename());
            assignment.setAttachmentFileType(attachmentFile.getContentType());
            assignment.setAttachmentFilePath(storedPath);
        }

        return addAssignment(course, assignment, instructor);
    }

    public List<AssignmentDto> getAssignments(Course course, User user){
        List<Assignment> assignments = List.of();
        if(user instanceof Instructor){
            Instructor instructor = (Instructor) user;
            Set<Course> instructorCourses = instructor.getCreatedCourses();
            if(instructorCourses.contains(course))
                assignments = course.getAssignments();
            else
                throw new IllegalStateException("You are not the instructor of this course");
        } else {
            Student student = (Student) user;
            Set<Course> studentCourses = student.getEnrolled_courses();
            if (studentCourses.contains(course))
                assignments = course.getAssignments();
            else
                throw new IllegalStateException("You are not enrolled in this course");
        }
        List<AssignmentDto> assignmentDtos = new ArrayList<>();
        for (Assignment assignment : assignments) {
            assignmentDtos.add(new AssignmentDto(assignment));
        }
        return assignmentDtos;
    }

    public Assignment getAssignment(Course course, User user, int assignmentId){
        List<Assignment> assignments = List.of();
        if(user instanceof Instructor){
            Instructor instructor = (Instructor) user;
            Set<Course> instructorCourses = instructor.getCreatedCourses();
            if(instructorCourses.contains(course))
                assignments = course.getAssignments();
            else
                throw new ApplicationContextException("You are not the instructor of this course");
        } else {
            Student student = (Student) user;
            Set<Course> studentCourses = student.getEnrolled_courses();
            if (studentCourses.contains(course))
                assignments = course.getAssignments();
            else
                throw new ApplicationContextException("You are not enrolled in this course");
        }
        for (Assignment assignment : assignments) {
            if(assignment.getAssignmentId() == assignmentId)
                return assignment;
        }
        throw new IllegalStateException("Assignment not found");
    }

    public String uploadSubmissionFile(MultipartFile file, Assignment assignment, Student student){
        String filePath = UPLOAD_DIR + file.getOriginalFilename();

        // database part
        AssignmentSubmission a = new AssignmentSubmission();
        a.setFileName(file.getOriginalFilename());
        a.setFileType(file.getContentType());
        a.setFilePath(filePath);
        a.setAssignment(assignment);
      
        // sets the submission's student and adds the submission to the student's submissions list
        student.addAssignmentSubmission(a);
        // saving through user repo
        userRepository.save(student);

        // storing in the actual file system
        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Unable to store the file at " + filePath);
        }
        return "file successfully uploaded to " + filePath;
    }

    public List<AssignmentSubmissionDto> getSubmissions(Assignment assignment){
        List<AssignmentSubmissionDto> dtos = new ArrayList<>();
        List<AssignmentSubmission> submissions = assignment.getSubmissions();
        for (AssignmentSubmission submisson : submissions) {
            dtos.add(new AssignmentSubmissionDto(submisson));
        }
        return dtos;
    }

    public AssignmentSubmission getSubmission(Assignment assignment, int submissionId){
        List<AssignmentSubmission> submissions = assignment.getSubmissions();
        for (AssignmentSubmission submission : submissions) {
            if(submission.getSubmissionId() == submissionId){
                return submission;
            }
        }
        throw new IllegalStateException("Submission not found");
    }

    public byte[] downloadSubmissionFile(Assignment assignment, int submissionId){ // String fileName
        // retrieving the assignment submission object by ID
        List<AssignmentSubmission> submissions = assignment.getSubmissions();
        AssignmentSubmission sub = new AssignmentSubmission();
        for (AssignmentSubmission submisson : submissions) {
            if(submisson.getSubmissionId() == submissionId){
                sub = submisson;
                break;
            }
        }
        // storing the file into a byte array
        String filePath = sub.getFilePath();
        try {
            byte[] submissionData = Files.readAllBytes(new File(filePath).toPath());
            return submissionData;
        } catch (IOException e) {
            throw new RuntimeException("Unable to load the file from " + filePath);
        }
    }

    public byte[] downloadAssignmentAttachment(Assignment assignment) {
        if (assignment.getAttachmentFilePath() == null || assignment.getAttachmentFilePath().isBlank()) {
            throw new IllegalStateException("No attachment available for this assignment.");
        }
        try {
            return Files.readAllBytes(new File(assignment.getAttachmentFilePath()).toPath());
        } catch (IOException e) {
            throw new RuntimeException("Unable to load the assignment attachment from " + assignment.getAttachmentFilePath());
        }
    }

    private String storeAssignmentAttachment(MultipartFile file) {
        try {
            Path uploadDir = Paths.get(ASSIGNMENT_ATTACHMENT_UPLOAD_DIR);
            Files.createDirectories(uploadDir);

            String originalName = file.getOriginalFilename() == null ? "assignment_attachment" : file.getOriginalFilename();
            String sanitizedName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            String uniqueFileName = UUID.randomUUID() + "_" + sanitizedName;
            Path destination = uploadDir.resolve(uniqueFileName);

            file.transferTo(destination.toFile());
            return destination.toString();
        } catch (IOException e) {
            throw new RuntimeException("Unable to store assignment attachment.");
        }
    }

    public AssignmentSubmissionDto setAssignmentGrade(AssignmentSubmission a, Course course, Map<String, Integer> gradeMap) {
        a.setGrade(gradeMap.get("grade"));
        a.setGraded(true);

        Notification notificationMessage = notificationService.createNotification("Your submission to " + a.getAssignment().getTitle() + " got graded " + " in Course: " + course.getName());
        Student student = a.getStudent();
        notificationService.addNotifcationStudent(notificationMessage, student);

        String subject = "Assignment got Graded";
        String content = "Your submission to " + a.getAssignment().getTitle() + " got graded " + " in Course: " + course.getName();

        emailService.sendEmail(
                student.getEmail(),
                student.getName(),
                subject,
                content,
                course.getInstructor().getName()
        );


        courseRepository.save(course);
        return new AssignmentSubmissionDto(a);
    }
    public int getAssignmentGrade(Assignment assignment, Student student) {
        List<AssignmentSubmission> studentSubmissions = student.getAssignmentSubmissions();
        for (AssignmentSubmission submission : studentSubmissions) {
            if(submission.getAssignment().equals(assignment)){
                if(submission.isGraded())
                    return submission.getGrade();
                else
                    throw new ApplicationContextException("Your submission wasn't graded yet.");
            }
        }
        throw new IllegalStateException("You have no submissions for this assignment .");
    }

}
