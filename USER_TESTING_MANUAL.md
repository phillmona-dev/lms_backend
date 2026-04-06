# 🎓 LMS — Complete User Testing Manual

> **Real-World Scenario**: *Addis Science School* is onboarding its Learning Management System.
> We will walk through **every feature** end-to-end — creating users, courses, lessons, videos, quizzes, assignments, monitoring progress, behavior reports, and analytics.

---

## Table of Contents

1. [Prerequisites & Setup](#1-prerequisites--setup)
2. [Scenario Overview — Cast of Characters](#2-scenario-overview--cast-of-characters)
3. [Phase 1 — Registration & Login](#3-phase-1--registration--login)
4. [Phase 2 — System Admin Setup](#4-phase-2--system-admin-setup)
5. [Phase 3 — Instructor: Course & Content](#5-phase-3--instructor-course--content)
6. [Phase 4 — Student: Enrollment & Learning](#6-phase-4--student-enrollment--learning)
7. [Phase 5 — Video Streaming](#7-phase-5--video-streaming)
8. [Phase 6 — Quizzes](#8-phase-6--quizzes)
9. [Phase 7 — Assignments](#9-phase-7--assignments)
10. [Phase 8 — Attendance (OTP)](#10-phase-8--attendance-otp)
11. [Phase 9 — Progress Monitoring](#11-phase-9--progress-monitoring)
12. [Phase 10 — Behavior Reports](#12-phase-10--behavior-reports)
13. [Phase 11 — School Administration](#13-phase-11--school-administration)
14. [Phase 12 — Bureau Analytics](#14-phase-12--bureau-analytics)
15. [Phase 13 — AI Insights](#15-phase-13--ai-insights)
16. [Phase 14 — RBAC Management](#16-phase-14--rbac-management)
17. [Phase 15 — User Management](#17-phase-15--user-management)
18. [Phase 16 — Notifications & Profile](#18-phase-16--notifications--profile)
19. [Troubleshooting](#19-troubleshooting)

---

## 1. Prerequisites & Setup

### 1.1 Required Software
| Tool | Version |
|------|---------|
| Java | 17+ |
| Maven | 3.9+ |
| Node.js | 18+ |
| PostgreSQL | 14+ |
| Web Browser | Chrome / Firefox (latest) |

### 1.2 Database Setup
```bash
# Create the PostgreSQL database
psql -U postgres
CREATE DATABASE LMS;
\q
```

### 1.3 Start the Backend
```bash
cd Learning-Management-System-main
mvn spring-boot:run
```
The backend starts on **http://localhost:8888**.
Swagger docs available at: **http://localhost:8888/swagger-ui.html**

### 1.4 Start the Frontend
```bash
cd lms-frontend
npm install    # first time only
npm run dev
```
The frontend starts on **http://localhost:5173** (or next available port).

### 1.5 Verify Everything is Running
- Open **http://localhost:5173** — you should see the Login page
- Open **http://localhost:8888/swagger-ui.html** — you should see the API documentation

---

## 2. Scenario Overview — Cast of Characters

We will create and use these accounts throughout testing:

| # | Name | Email | Password | Role |
|---|------|-------|----------|------|
| 1 | Admin Abebe | admin@school.com | admin12345 | SYSTEM_ADMINISTRATOR |
| 2 | Prof. Kebede | kebede@school.com | kebede1234 | TEACHER (Instructor) |
| 3 | Student Meron | meron@school.com | meron12345 | STUDENT |
| 4 | Student Dawit | dawit@school.com | dawit12345 | STUDENT |
| 5 | Parent Tigist | tigist@school.com | tigist1234 | PARENT |
| 6 | School Admin Sara | sara@school.com | sara123456 | SCHOOL_ADMINISTRATOR |
| 7 | Bureau Officer | bureau@gov.com | bureau1234 | BUREAU_OF_EDUCATION |

**Course we'll create**: *"Advanced Mathematics"* — a 20-hour course with lessons, videos, quizzes, and assignments.

---

## 3. Phase 1 — Registration & Login

### 3.1 Register the Instructor
1. Open **http://localhost:5173/register**
2. Fill in:
   - **Name**: `Prof. Kebede`
   - **Email**: `kebede@school.com`
   - **Password**: `kebede1234`
   - **Role**: Select **Instructor**
3. Click **Create Account**
4. ✅ **Expected**: Success message, redirected to Login page

### 3.2 Register a Student
1. Open **http://localhost:5173/register**
2. Fill in:
   - **Name**: `Student Meron`
   - **Email**: `meron@school.com`
   - **Password**: `meron12345`
   - **Role**: Select **Student**
3. Click **Create Account**
4. ✅ **Expected**: Success, redirected to Login

### 3.3 Login as Instructor
1. Open **http://localhost:5173/login**
2. Enter:
   - **Email**: `kebede@school.com`
   - **Password**: `kebede1234`
3. Click **Sign In**
4. ✅ **Expected**: Redirected to Dashboard. Navbar shows "Welcome, Prof. Kebede"

### ❌ Negative Test — Wrong Password
1. Try logging in with `kebede@school.com` and password `wrongpass`
2. ✅ **Expected**: Error message "Invalid email or password"

### ❌ Negative Test — Duplicate Registration
1. Try registering again with `kebede@school.com`
2. ✅ **Expected**: Error message "User with this email already exists"

---

## 4. Phase 2 — System Admin Setup

> Register the System Admin via the registration page (or use Swagger if the Register page only shows Student/Instructor).
> Since the frontend Register page only shows Student and Instructor roles, **use Swagger or curl** for admin roles.

### 4.1 Register Admin via Swagger/curl
```bash
curl -X POST http://localhost:8888/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin Abebe",
    "email": "admin@school.com",
    "password": "admin12345",
    "role": "SYSTEM_ADMINISTRATOR"
  }'
```
✅ **Expected**: `{"message": "User registered successfully"}`

### 4.2 Register remaining users via curl
```bash
# Student Dawit
curl -X POST http://localhost:8888/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Student Dawit","email":"dawit@school.com","password":"dawit12345","role":"STUDENT"}'

# Parent Tigist
curl -X POST http://localhost:8888/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Parent Tigist","email":"tigist@school.com","password":"tigist1234","role":"PARENT"}'

# School Admin Sara
curl -X POST http://localhost:8888/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"School Admin Sara","email":"sara@school.com","password":"sara123456","role":"SCHOOL_ADMINISTRATOR"}'

# Bureau Officer
curl -X POST http://localhost:8888/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Bureau Officer","email":"bureau@gov.com","password":"bureau1234","role":"BUREAU_OF_EDUCATION"}'
```

### 4.3 Create a School
1. Login as **Admin Abebe** (`admin@school.com`) on the frontend
2. Navigate to **Schools** in the top navbar
3. Click **Create School**
4. Fill in:
   - **Name**: `Addis Science School`
   - **Region**: `Addis Ababa`
   - **Code**: `ASS001`
5. Click **Create**
6. ✅ **Expected**: School appears in the list

### 4.4 Assign Users to School
1. Still on the **School Directory** page as Admin
2. In the **Assign User to School** section:
   - Select **Prof. Kebede** from the user dropdown
   - Select **Addis Science School** from the school dropdown
   - Click **Assign**
3. Repeat for: **Student Meron**, **Student Dawit**, **School Admin Sara**
4. ✅ **Expected**: Each user is assigned to the school successfully

---

## 5. Phase 3 — Instructor: Course & Content

### 5.1 Create a Course
1. **Login as Prof. Kebede** (`kebede@school.com`)
2. In the Dashboard sidebar, click **Manage Courses** (or navigate to `/management`)
3. Click **+ Create New Course**
4. Fill in:
   - **Course Name**: `Advanced Mathematics`
   - **Description**: `Comprehensive mathematics course covering calculus, algebra, and statistics for grade 10 students.`
   - **Duration**: `20`
5. Click **Create Course**
6. ✅ **Expected**: Course card appears with "A" avatar, title "Advanced Mathematics", and "20 hrs"

### ❌ Negative Test — Empty Course Name
1. Try creating a course with an empty name
2. ✅ **Expected**: Error "Course name is required and cannot be empty"

### ❌ Negative Test — Zero Duration
1. Try creating a course with duration `0`
2. ✅ **Expected**: Error "Course duration must be a positive number"

### 5.2 Add Lessons to the Course
1. Click on the **Advanced Mathematics** course card → goes to Course Details
2. Click **Add Lesson** (visible only to the instructor)
3. Create the following lessons:

| Lesson # | Title | Description |
|----------|-------|-------------|
| 1 | Introduction to Calculus | Limits, derivatives, and fundamental concepts |
| 2 | Linear Algebra Basics | Vectors, matrices, and linear transformations |
| 3 | Statistics & Probability | Data analysis, distributions, and probability theory |

4. ✅ **Expected**: Each lesson appears in the Lessons tab

### 5.3 Upload Lesson Resources
1. Click on **Lesson 1: Introduction to Calculus** → goes to Lesson Details
2. Go to the **Resources** tab
3. Click **Choose file** and select a PDF (e.g., `calculus_notes.pdf`)
4. Click **Upload Resource**
5. ✅ **Expected**: Resource appears in the list with download link

### 5.4 Add Questions to the Question Bank
1. Go back to the Course Details page
2. In the **Questions** tab (if visible) or use Swagger:
```bash
# Get instructor token first
TOKEN=$(curl -s -X POST http://localhost:8888/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"kebede@school.com","password":"kebede1234"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

# Create a multiple choice question
curl -X POST "http://localhost:8888/course/Advanced%20Mathematics/create-question" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "MULTIPLE_CHOICE",
    "content": "What is the derivative of x²?",
    "choices": [
      {"text": "2x", "correct": true},
      {"text": "x²", "correct": false},
      {"text": "2", "correct": false},
      {"text": "x", "correct": false}
    ]
  }'

# Create another question
curl -X POST "http://localhost:8888/course/Advanced%20Mathematics/create-question" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "MULTIPLE_CHOICE",
    "content": "What is the integral of 2x?",
    "choices": [
      {"text": "x² + C", "correct": true},
      {"text": "2x²", "correct": false},
      {"text": "x", "correct": false},
      {"text": "2", "correct": false}
    ]
  }'

# Create a TRUE/FALSE question
curl -X POST "http://localhost:8888/course/Advanced%20Mathematics/create-question" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "TRUE_FALSE",
    "content": "The derivative of a constant is always zero.",
    "choices": [
      {"text": "True", "correct": true},
      {"text": "False", "correct": false}
    ]
  }'
```
✅ **Expected**: Questions are added to the course question bank

---

## 6. Phase 4 — Student: Enrollment & Learning

### 6.1 Browse & Enroll in a Course
1. **Login as Student Meron** (`meron@school.com`)
2. On the Dashboard, click **Browse Courses** in the sidebar
3. Find **Advanced Mathematics** in the list
4. Click on it → goes to Course Details
5. Click **Enroll**
6. ✅ **Expected**: Enrollment success message, course now shows in "My Courses"

### 6.2 View Course Details
1. Go to Dashboard → **My Courses**
2. Click **Advanced Mathematics**
3. ✅ **Expected**: You can see:
   - Course name, description, instructor name
   - Lessons tab with 3 lessons listed
   - Assignments tab
   - Enrolled students count shows 1

### 6.3 Access a Lesson
1. Click on **Lesson 1: Introduction to Calculus**
2. ✅ **Expected**: Lesson Details page opens with tabs:
   - **▶ Video** (default tab) — shows "No Video Uploaded"
   - **Resources** — shows uploaded PDF
   - **Mark Attendance** — shows OTP input

### 6.4 Download a Resource
1. Go to the **Resources** tab
2. Click the download button on `calculus_notes.pdf`
3. ✅ **Expected**: File downloads to your computer

### ❌ Negative Test — Unenrolled Student
1. **Login as Student Dawit** (NOT enrolled yet)
2. Try to access `/courses/Advanced%20Mathematics`
3. ✅ **Expected**: Can view course info but cannot access certain features until enrolled

---

## 7. Phase 5 — Video Streaming

### 7.1 Upload a Video (Instructor)
1. **Login as Prof. Kebede** (`kebede@school.com`)
2. Navigate to **Advanced Mathematics** → **Lesson 1: Introduction to Calculus**
3. The **▶ Video** tab is selected by default
4. You should see:
   - "No Video Uploaded" empty state
   - **Upload Video** form below
5. Click **Choose a video file...** and select an MP4 video file
6. Click **Upload Video**
7. ✅ **Expected**:
   - Progress/loading indicator during upload
   - Success message: "Video uploaded successfully"
   - Video player now appears with the uploaded video

### 7.2 Play the Video (Instructor)
1. The video player should now be visible with playback controls
2. Click **Play** ▶
3. ✅ **Expected**: Video plays smoothly with full controls (play/pause, seek, volume, fullscreen)
4. Try **seeking** — drag the progress bar forward/backward
5. ✅ **Expected**: Video seeks correctly (HTTP Range streaming)

### 7.3 Watch Video as Student
1. **Login as Student Meron** (`meron@school.com`)
2. Navigate to **Advanced Mathematics** → **Lesson 1**
3. Go to the **▶ Video** tab
4. ✅ **Expected**: Video player visible and plays the instructor's video
5. ✅ **Expected**: No upload form or delete button (student view)

### 7.4 Replace a Video (Instructor)
1. **Login as Prof. Kebede**
2. Go to Lesson 1 → **▶ Video** tab
3. Scroll down to "Replace Video" form
4. Upload a different video file
5. ✅ **Expected**: New video replaces the old one

### 7.5 Delete a Video (Instructor)
1. Click **🗑 Delete Video** button below the video player
2. Confirm the deletion
3. ✅ **Expected**: Video removed, "No Video Uploaded" state returns

### ❌ Negative Test — Non-enrolled Student
1. **Login as Student Dawit** (not enrolled)
2. Try accessing the video stream URL directly
3. ✅ **Expected**: 403 Forbidden — "You must be enrolled to watch this video"

### ❌ Negative Test — Invalid Video Format
1. As instructor, try uploading a `.txt` file as video
2. ✅ **Expected**: Error "Invalid video format. Allowed: MP4, WebM, OGG, MOV, AVI, MKV"

---

## 8. Phase 6 — Quizzes

### 8.1 Create a Quiz (Instructor)
1. **Login as Prof. Kebede**
2. Navigate to **Advanced Mathematics** Course Details
3. Use Swagger or curl to create a quiz:
```bash
curl -X POST "http://localhost:8888/course/Advanced%20Mathematics/create-quiz" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "quizTitle": "Calculus Midterm Quiz",
    "quizDuration": "00:30:00"
  }'
```
4. ✅ **Expected**: Quiz created successfully, students receive notification

### 8.2 Take the Quiz (Student)
1. **Login as Student Meron**
2. Navigate to **Advanced Mathematics** → click on **Calculus Midterm Quiz**
3. Click **Start Quiz**
4. ✅ **Expected**: Quiz questions displayed with answer options
5. Select answers for each question
6. Click **Submit Quiz**
7. ✅ **Expected**: Quiz submitted successfully

### 8.3 View Quiz Grade
1. After submitting, the grade should be shown
2. Or navigate to the quiz page again
3. ✅ **Expected**: Grade/score is displayed (e.g., "2/3 correct")

---

## 9. Phase 7 — Assignments

### 9.1 Create an Assignment (Instructor)
1. **Login as Prof. Kebede**
2. Navigate to **Advanced Mathematics** Course Details
3. In the **Assignments** tab, create a new assignment:
   - **Title**: `Calculus Problem Set 1`
   - **Description**: `Solve the attached derivative and integral problems.`
   - **Due Date**: Set a future date (e.g., `2026-04-15T23:59:00`)
4. ✅ **Expected**: Assignment appears in the assignments list

### 9.2 Submit an Assignment (Student)
1. **Login as Student Meron**
2. Navigate to **Advanced Mathematics** → **Assignments** tab
3. Click on **Calculus Problem Set 1**
4. Upload a file (PDF/DOCX) as your submission
5. Click **Submit**
6. ✅ **Expected**: Submission uploaded successfully

### 9.3 View Submissions (Instructor)
1. **Login as Prof. Kebede**
2. Navigate to the assignment → click **View Submissions**
3. ✅ **Expected**: List of student submissions with names and files

### 9.4 Grade a Submission (Instructor)
1. Click on a student's submission
2. Assign a grade and provide feedback
3. ✅ **Expected**: Student receives a notification about the grade

---

## 10. Phase 8 — Attendance (OTP)

### 10.1 Generate OTP (Instructor)
1. **Login as Prof. Kebede**
2. Navigate to **Lesson 1: Introduction to Calculus**
3. Go to the **Generate OTP** tab
4. Click **Generate OTP**
5. ✅ **Expected**: A 6-digit OTP code is displayed (e.g., `483921`)
6. 📝 **Note down the OTP** — share it with students in class

### 10.2 Mark Attendance (Student)
1. **Login as Student Meron**
2. Navigate to **Lesson 1**
3. Go to the **Mark Attendance** tab
4. Enter the OTP code provided by the instructor
5. Click **Submit**
6. ✅ **Expected**: "Attendance marked successfully"

### ❌ Negative Test — Wrong OTP
1. Enter an incorrect OTP like `000000`
2. ✅ **Expected**: Error message — invalid OTP

### 10.3 View Attendance List (Instructor)
1. **Login as Prof. Kebede**
2. Navigate to **Lesson 1** → **Attendance List** tab
3. ✅ **Expected**: Shows "Student Meron" as attended

---

## 11. Phase 9 — Progress Monitoring

### 11.1 Student Views Own Progress
1. **Login as Student Meron**
2. On the Dashboard, the overview section should show progress stats
3. Or navigate to the Progress indicators on the dashboard
4. ✅ **Expected**: Shows enrolled courses, attended lessons, quiz scores, assignment submissions

### 11.2 Instructor Monitors Students
1. **Login as Prof. Kebede**
2. In the Dashboard sidebar, click **Student Progress** (or navigate to `/student-monitoring`)
3. ✅ **Expected**: Shows a list of enrolled students with:
   - Attendance rate
   - Quiz performance
   - Assignment completion

### 11.3 Parent Views Child Progress
1. First, link parent to student (as Admin):
```bash
# Login as admin
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8888/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@school.com","password":"admin12345"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

# Get Meron's public ID (UUID) from user list
curl -s http://localhost:8888/rbac/users \
  -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -c "
import sys, json
users = json.load(sys.stdin)
for u in users:
    print(f\"{u['name']}: {u['id']}\")
"

# Get Tigist's (parent) public ID too, then link:
curl -X POST http://localhost:8888/progress/parent-links \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"parentId":"<TIGIST_UUID>","studentId":"<MERON_UUID>"}'
```

2. **Login as Parent Tigist** (`tigist@school.com`)
3. Navigate to the **Progress Center** page
4. ✅ **Expected**: Shows Student Meron's progress — courses enrolled, grades, attendance

---

## 12. Phase 10 — Behavior Reports

### 12.1 Create a Behavior Report (Instructor)
1. **Login as Prof. Kebede**
2. Navigate to **Behavior Reports** page (`/behavior-reports`)
3. Select a course and a student
4. Create a report:
   - **Type**: e.g., `POSITIVE` or `CONCERN`
   - **Description**: `Meron showed excellent participation in today's calculus lesson.`
5. Click **Submit Report**
6. ✅ **Expected**: Report created successfully

### 12.2 View Behavior Reports
1. **As Instructor**: Can view reports they've created ("My Reported Issues")
2. **As Student**: Navigate to Behavior Reports to see own reports
3. **As Parent**: After linking, can view child's behavior reports
4. **As School Admin**: Can view all behavior reports for the school
5. ✅ **Expected**: Reports are visible based on role-appropriate access

---

## 13. Phase 11 — School Administration

### 13.1 School Admin Dashboard
1. **Login as School Admin Sara** (`sara@school.com`)
2. Navigate to **School Admin Center** (`/school-admin-center`)
3. ✅ **Expected**: Dashboard showing:
   - Total students, teachers, courses count
   - Performance indicators
   - Recent activity

### 13.2 View Student Progress Reports
1. In the School Admin Center, look for **Student Reports** section
2. ✅ **Expected**: School-wide student progress summaries

### 13.3 Publish an Announcement
1. In the School Admin Center, find the **Announcements** section
2. Create a new announcement:
   - **Title**: `Welcome Back to School`
   - **Content**: `Classes resume next Monday. Please check your course schedules.`
   - **Audience**: `ALL` (or specific role)
3. Click **Publish**
4. ✅ **Expected**: Announcement appears in the list and users receive notifications

---

## 14. Phase 12 — Bureau Analytics

### 14.1 Bureau Dashboard
1. **Login as Bureau Officer** (`bureau@gov.com`)
2. Navigate to **Bureau Analytics** (`/bureau-analytics`)
3. ✅ **Expected**: Dashboard showing:
   - Total schools count
   - Aggregate student/teacher counts
   - Average attendance rates
   - Performance comparisons

### 14.2 School Performance Comparison
1. View the **School Performance** section
2. ✅ **Expected**: School-by-school metrics for comparison

### 14.3 High-Risk Schools Identification
1. View the **High-Risk Schools** section
2. ✅ **Expected**: Schools with low attendance or elevated risk indicators are flagged

---

## 15. Phase 13 — AI Insights

### 15.1 View AI Insights
1. **Login as Prof. Kebede** (or Admin)
2. Navigate to **AI Insights** (`/ai-insights`)
3. ✅ **Expected**: Two sections:
   - **School Insights**: AI-derived performance/attendance/behavior signals
   - **At-Risk Students**: Students identified as at-risk for dropout or low performance

### 15.2 Review At-Risk Students
1. Click on **Students at Risk** section
2. ✅ **Expected**: List of students with risk indicators and recommendations

---

## 16. Phase 14 — RBAC Management

> Only accessible by **System Administrator** (Admin Abebe)

### 16.1 View Roles & Privileges
1. **Login as Admin Abebe** (`admin@school.com`)
2. Navigate to **RBAC Management** (`/rbac-management`)
3. ✅ **Expected**: Shows:
   - List of all roles (STUDENT, TEACHER, PARENT, SCHOOL_ADMINISTRATOR, etc.)
   - List of all privileges
   - User-role assignment interface

### 16.2 Create a Custom Role
1. Click **Create Role**
2. Fill in:
   - **Name**: `LAB_ASSISTANT`
   - **Description**: `Lab assistant with limited course view access`
   - Select privileges: `PROFILE_VIEW`, `COURSE_VIEW`, `LESSON_VIEW`
3. Click **Create**
4. ✅ **Expected**: New role appears in the roles list

### 16.3 Assign a Role to a User
1. Select a user from the user list
2. Assign the `LAB_ASSISTANT` role
3. ✅ **Expected**: User now has the new role and associated privileges

### 16.4 Revoke a Role
1. Select a user with multiple roles
2. Remove/revoke a role
3. ✅ **Expected**: Role removed, privileges updated accordingly

---

## 17. Phase 15 — User Management

> Only accessible by **System Administrator** or **School Administrator**

### 17.1 View All Users
1. **Login as Admin Abebe**
2. Navigate to **Users** in the top navbar (`/users-management`)
3. ✅ **Expected**: Table of all users with name, email, role, school

### 17.2 Create a User (Admin)
1. Click **Create User**
2. Fill in:
   - **Name**: `Teaching Assistant`
   - **Email**: `ta@school.com`
   - **Password**: `ta12345678`
   - **Role**: `TEACHER`
3. Click **Create**
4. ✅ **Expected**: User created and appears in the list

### 17.3 Search Users
1. Use the search bar to search by name or email
2. ✅ **Expected**: Filtered results shown

### 17.4 Delete a User
1. Find the user to delete
2. Click the delete button
3. ✅ **Expected**: User removed from the list

---

## 18. Phase 16 — Notifications & Profile

### 18.1 Check Notifications
1. Login as any user
2. On the Dashboard sidebar, click **Notifications**
3. ✅ **Expected**: List of notifications:
   - Students: enrollment confirmations, graded assignments, course updates
   - Instructors: new enrollments, submissions
   - All: announcements

### 18.2 Update Profile
1. Navigate to profile (click your name or profile link)
2. Update your name or other details
3. ✅ **Expected**: Profile updated successfully

### 18.3 Logout
1. Click **Logout** in the navbar
2. ✅ **Expected**: Redirected to Login page, token cleared

### ❌ Negative Test — Access Protected Page After Logout
1. After logout, try navigating to `/dashboard` directly
2. ✅ **Expected**: Redirected to Login page

---

## 19. Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| Backend won't start | Check PostgreSQL is running, verify `application.yml` DB credentials |
| Frontend shows blank | Check terminal for Vite errors, try `npm install` again |
| "Unauthorized" on API calls | Token may be expired — login again |
| "Course not found" in URL | Make sure URL uses exact course name (case-sensitive) |
| Video won't upload | Check file size < 500MB, ensure valid video format |
| Video won't play | Ensure backend is running, check browser console for errors |
| CORS errors | Frontend must use proxy (`/api` → `localhost:8888`), don't access backend directly |
| OTP not working | OTPs expire — generate a fresh one |

### Useful Backend Endpoints (Swagger)
- **Swagger UI**: http://localhost:8888/swagger-ui.html
- **API Docs**: http://localhost:8888/v3/api-docs

### Reset Everything
```bash
# Drop and recreate the database
psql -U postgres -c "DROP DATABASE IF EXISTS LMS;"
psql -U postgres -c "CREATE DATABASE LMS;"

# Restart the backend (tables auto-created by Hibernate)
mvn spring-boot:run
```

---

## ✅ Testing Checklist Summary

| # | Feature | Tested? |
|---|---------|---------|
| 1 | User Registration (Student, Instructor) | ☐ |
| 2 | User Registration (Admin, Parent, SchoolAdmin, Bureau) via API | ☐ |
| 3 | Login / Logout | ☐ |
| 4 | School Creation & User Assignment | ☐ |
| 5 | Course Creation (with validation) | ☐ |
| 6 | Lesson Creation | ☐ |
| 7 | Resource Upload & Download | ☐ |
| 8 | Question Bank (MCQ, True/False) | ☐ |
| 9 | Student Enrollment | ☐ |
| 10 | Video Upload (Instructor) | ☐ |
| 11 | Video Streaming & Playback (Instructor + Student) | ☐ |
| 12 | Video Replace & Delete | ☐ |
| 13 | Quiz Creation | ☐ |
| 14 | Quiz Taking & Grading | ☐ |
| 15 | Assignment Creation | ☐ |
| 16 | Assignment Submission | ☐ |
| 17 | Assignment Grading | ☐ |
| 18 | OTP Generation (Instructor) | ☐ |
| 19 | Attendance Marking (Student) | ☐ |
| 20 | Attendance List (Instructor) | ☐ |
| 21 | Student Progress View | ☐ |
| 22 | Instructor Student Monitoring | ☐ |
| 23 | Parent-Student Linking | ☐ |
| 24 | Parent Progress View | ☐ |
| 25 | Behavior Reports (Create/View) | ☐ |
| 26 | School Admin Dashboard & Reports | ☐ |
| 27 | School Announcements | ☐ |
| 28 | Bureau Analytics Dashboard | ☐ |
| 29 | High-Risk School Identification | ☐ |
| 30 | AI Insights & At-Risk Students | ☐ |
| 31 | RBAC: View/Create/Assign/Revoke Roles | ☐ |
| 32 | User Management (CRUD) | ☐ |
| 33 | Notifications | ☐ |
| 34 | Profile Update | ☐ |
| 35 | Dark/Light Theme Toggle | ☐ |
| 36 | Negative Tests (invalid inputs, unauthorized access) | ☐ |

---

> **Tip**: Work through the phases in order — each phase builds on the previous one's data.
> **Tip**: Keep multiple browser tabs or incognito windows open to switch between roles quickly.

*Last updated: April 4, 2026*
