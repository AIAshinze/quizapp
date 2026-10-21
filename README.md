# Quiz App

A web-based quiz application built with Spring Boot. Users register, log in, and take quizzes. Admins create, edit, and delete the questions.

## Features

- User registration and login with Spring Security
- Role-based access: `ADMIN` manages quizzes, `USER` takes them
- Admin dashboard listing every question with its correct answer
- Add, edit, and delete quiz questions
- Multiple-choice quizzes with a scored results page showing correct and incorrect answers
- Passwords stored as BCrypt hashes
- Server-rendered pages with Thymeleaf

## Tech Stack

- Java 17+
- Spring Boot (Web MVC, Security, Thymeleaf)
- Spring Security 6
- Thymeleaf templates
- JUnit 5 (Spring Boot test)

## Project Structure

```text
└── 📁quizapp
    └── 📁src
        └── 📁main
            └── 📁java
                └── 📁com
                    └── 📁example
                        └── 📁quizapp
                            └── 📁config
                                ├── WebSecurityConfig.java
                            └── 📁controller
                                ├── QuizController.java
                            └── 📁model
                                ├── Quiz.java
                                ├── User.java
                            └── 📁service
                                ├── QuestionsService.java
                                ├── QuizUserDetailsService.java
                            ├── QuizappApplication.java
            └── 📁resources
                └── 📁static
                └── 📁templates
                    ├── addQuiz.html
                    ├── editQuiz.html
                    ├── login.html
                    ├── Quiz.html
                    ├── QuizList.html
                    ├── register.html
                    ├── result.html
                ├── application.properties
        └── 📁test
            └── 📁java
                └── 📁com
                    └── 📁example
                        └── 📁quizapp
                            ├── QuizappApplicationTests.java
```

## Getting Started

### Prerequisites

- JDK 17 or newer
- Maven

### Run the application

```bash
mvn spring-boot:run
```

Then open <http://localhost:8080> in a browser. You will be redirected to the login page.

### Run the tests

```bash
mvn test
```

## Usage

1. **Register** at `/register`. Choose `User` or `Admin` from the role dropdown.
2. **Log in** at `/login`. You are redirected to `/home`.
3. **Admins** see the question list, where they can add, edit, or delete questions.
4. **Users** see the quiz. Answer every question and click **Submit Answers** to see your score.

## Pages and Endpoints

| Method | Path                  | Access        | Purpose                              |
|--------|-----------------------|---------------|--------------------------------------|
| GET    | `/login`              | Public        | Login page                           |
| GET    | `/register`           | Public        | Registration page                    |
| POST   | `/register`           | Public        | Create an account                    |
| GET    | `/home`               | Logged in     | Quiz (user) or question list (admin) |
| GET    | `/addQuiz`            | Admin         | Add question form                    |
| POST   | `/addQuiz`            | Admin         | Save new question                    |
| GET    | `/editQuiz/{id}`      | Admin         | Edit question form                   |
| POST   | `/editQuestion`       | Admin         | Save edited question                 |
| POST   | `/deleteQuiz/{id}`    | Admin         | Delete question                      |
| POST   | `/submitQuiz`         | Logged in     | Score submitted answers              |
| POST   | `/logout`             | Logged in     | Log out                              |

## Configuration

Settings live in `src/main/resources/application.properties`:

```properties
spring.application.name=quizapp
```

Add any other Spring Boot properties here, such as `server.port`.

## Notes and Limitations

- **In-memory storage.** Users and questions are kept in memory and are lost when the app restarts. Add a database before using this beyond development.
- **Open admin registration.** Anyone can register as an admin by choosing the role. This is intended for testing only. Restrict it before any real deployment.
- **Comma-separated options.** Answer options are entered as a comma-separated list, so an option cannot contain a comma.
- **Exact-match answers.** A user's answer is correct only if it matches the correct answer text exactly.
- **Forms use POST for edit and delete.** Plain HTML forms only send GET and POST, so these actions use POST.

## Future Improvements

- Persistent storage with a database (for example, Spring Data JPA with H2 or PostgreSQL)
- Password-protected admin registration or an admin seed on startup
- Validation of quiz input
- Quiz results history per user
