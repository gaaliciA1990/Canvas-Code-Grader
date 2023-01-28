# Canvas-Code Graduate Capstone

## Project Status

[![Maven Build](https://github.com/Canvas-Code-Capstone/Canvas-Code/actions/workflows/build.yml/badge.svg)](https://github.com/Canvas-Code-Capstone/Canvas-Code/actions/workflows/build.yml)

[![Code Coverage](https://github.com/Canvas-Code-Capstone/Canvas-Code/blob/master/.github/badges/jacoco.svg)](https://github.com/Canvas-Code-Capstone/Canvas-Code/actions/workflows/CodeCoverage.yml)

# Project Overview

Canvas is widely used in the Seattle University CS department for course material sharing and assignment submissions. However, code assignment submission for core 
CS courses like Programming and Problem Solving I and II, and Data Structures, is currently handled separately. Students in these classes usually submit their code
by copying their files to the department’s CS1 Linux Server and running an instructor provided script to perform compilation checks, in addition to copying the files
to a grading location. Instructors must prepare the submission script for each assignment and produce their own solution to enforce deadlines. Instructors and graders
also connect to CS1 to review, test and grade the assignments. Finally, instructors and graders enter the grades in Canvas. In some sections, students upload the code
files to Canvas, but then the instructor/grader must manually download, compile, and run the code on their own. If any of the submitted code fails to compile, the 
student will not get credit for the assignment. To alleviate this manual submission process for students, graders, and instructors, this project intends to simplify 
the code assignment submission process by 1) allowing students to submit their code files on Canvas, 2) evaluate student code on submissions and provide live feedback,
and 3) enable instructors and graders to view and execute the code in Canvas while grading. 


## Solution
Our solution will be a browser extension, starting with a chrome extension application that has oAuth2/SSO login with Canvas. Both the students and the instructors will
download the extension onto their browser and sign in their Canvas accounts to grant file read permissions for students and file read and write permissions for 
instructors and graders. The V1 extension will implement an overlay onto Canvas, showing an evaluate submission button that students will click evaluate the code files
before submitting the assignment on Canvas. The results of evaluation will be displayed on the Canvas page, indicating a successful pass, or a failure with the accompanying
error message, so they can address the issues and resubmit. Instructors will upload their evaluation scripts to Canvas for their given assignments so our extension can 
access the files during execution of code. Instructors and graders will have an overlay view in Canvas of the submitted assignment alongside the rubric. The overlay will 
execute the student’s submitted code and return the results. The code will be in read only view, to avoid editing the student’s submission.


