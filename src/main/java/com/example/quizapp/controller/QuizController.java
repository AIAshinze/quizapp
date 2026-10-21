package com.example.quizapp.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.quizapp.model.Quiz;
import com.example.quizapp.service.QuestionsService;
import com.example.quizapp.service.QuizUserDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class QuizController {

    private static final String ROLE_USER = "USER";              // passed to the service, which adds the ROLE_ prefix
    private static final String ADMIN_ROLE = "ADMIN";            // role name passed to the service
    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";  // compared against granted authorities

    private final QuizUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final QuestionsService questionsService;

    public QuizController(QuizUserDetailsService userDetailsService,
            AuthenticationManager authenticationManager,
            QuestionsService questionsService) {
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.questionsService = questionsService;
    }

    // ---------- Home (role-based) ----------
    @GetMapping("/home")
    public String homepage(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("quizzes", sortedQuizzes());
        return isAdmin(authentication) ? "QuizList" : "Quiz";
    }

    // ---------- Login and registration ----------
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(defaultValue = ROLE_USER) String role,
            HttpServletRequest request,
            HttpServletResponse response) {
        // Only "ADMIN" grants admin; any other value falls back to a regular user.
        String assignedRole = ADMIN_ROLE.equalsIgnoreCase(role) ? ADMIN_ROLE : ROLE_USER;

        try {
            userDetailsService.registerUser(username, email, password, assignedRole);
        } catch (IllegalArgumentException e) {
            return "redirect:/register?error";
        }

        // Authenticate the new user and store the context in the session so the next request is logged in.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context, request, response);

        return "redirect:/home";
    }

    // ---------- Quiz management (admin) ----------
    @GetMapping("/addQuiz")
    public String showAddQuizForm(Model model) {
        model.addAttribute("quiz", new Quiz());
        return "addQuiz";
    }

    @PostMapping("/addQuiz")
    public String addQuiz(@RequestParam String questionText,
            @RequestParam String options,
            @RequestParam String correctAnswer,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (!isAdmin(authentication)) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to add a quiz.");
            return "redirect:/home";
        }

        try {
            Quiz quiz = buildQuiz(questionsService.nextId(), questionText, options, correctAnswer);
            questionsService.addQuiz(quiz);
            redirectAttributes.addFlashAttribute("success", "Quiz added successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/home";
    }

    @GetMapping("/editQuiz/{id}")
    public String showEditQuizForm(@PathVariable int id,
            Model model,
            RedirectAttributes redirectAttributes) {
        Optional<Quiz> quiz = findQuiz(id);
        if (quiz.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Question not found.");
            return "redirect:/home";
        }
        model.addAttribute("quiz", quiz.get());
        return "editQuiz";
    }

    @PostMapping("/editQuestion")
    public String editQuestion(@RequestParam int id,
            @RequestParam String questionText,
            @RequestParam String options,
            @RequestParam String correctAnswer,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (!isAdmin(authentication)) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to edit a quiz.");
            return "redirect:/home";
        }

        try {
            questionsService.editQuiz(buildQuiz(id, questionText, options, correctAnswer));
            redirectAttributes.addFlashAttribute("success", "Question updated.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/home";
    }

    @PostMapping("/deleteQuiz/{id}")
    public String deleteQuiz(@PathVariable int id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (!isAdmin(authentication)) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to delete a quiz.");
            return "redirect:/home";
        }

        try {
            questionsService.deleteQuiz(id);
            redirectAttributes.addFlashAttribute("success", "Question deleted.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/home";
    }

    // ---------- Taking the quiz ----------
    @PostMapping("/submitQuiz")
    public String evaluateQuiz(@RequestParam Map<String, String> allParams, Model model) {
        List<Quiz> quizzes = sortedQuizzes();
        List<String> userAnswers = new ArrayList<>();
        int correctAnswers = 0;

        for (Quiz quiz : quizzes) {
            // Inputs are named answer<quizId>, so matching does not depend on list position.
            String userAnswer = allParams.get("answer" + quiz.getId());
            userAnswers.add(userAnswer);
            if (userAnswer != null && userAnswer.equals(quiz.getCorrectAnswer())) {
                correctAnswers++;
            }
        }

        model.addAttribute("quizzes", quizzes);
        model.addAttribute("userAnswers", userAnswers);
        model.addAttribute("correctAnswers", correctAnswers);
        model.addAttribute("totalQuestions", quizzes.size());
        return "result";
    }

    // ---------- Helpers ----------
    private List<Quiz> sortedQuizzes() {
        return questionsService.loadQuizzes().stream()
                .sorted(Comparator.comparing(Quiz::getId))
                .toList();
    }

    private Optional<Quiz> findQuiz(Integer id) {
        return questionsService.loadQuizzes().stream()
                .filter(q -> Objects.equals(q.getId(), id))
                .findFirst();
    }

    private Quiz buildQuiz(Integer id, String questionText, String options, String correctAnswer) {
        Quiz quiz = new Quiz();
        quiz.setId(id);
        quiz.setQuestionText(questionText);
        quiz.setOptionsFromString(options);
        quiz.setCorrectAnswer(correctAnswer);
        return quiz;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> ADMIN_AUTHORITY.equals(a.getAuthority()));
    }
}
