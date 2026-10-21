package com.example.quizapp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.example.quizapp.model.Quiz;

@Service
public class QuestionsService {

    // Key: quiz ID, Value: quiz
    private final Map<Integer, Quiz> quizzes = new ConcurrentHashMap<>();

    public List<Quiz> loadQuizzes() {
        return new ArrayList<>(quizzes.values());
    }

    public void addQuiz(Quiz quiz) {
        Objects.requireNonNull(quiz, "Quiz must not be null");
        Integer id = requireId(quiz);

        if (quizzes.containsKey(id)) {
            throw new IllegalArgumentException("Quiz already exists with ID: " + id);
        }
        quizzes.put(id, quiz);
    }

    public void editQuiz(Quiz quiz) {
        Objects.requireNonNull(quiz, "Quiz must not be null");
        Integer id = requireId(quiz);

        if (!quizzes.containsKey(id)) {
            throw new IllegalArgumentException("Quiz not found with ID: " + id);
        }
        quizzes.put(id, quiz);
    }

    public void deleteQuiz(Integer id) {
        Objects.requireNonNull(id, "ID must not be null");

        if (quizzes.remove(id) == null) {
            throw new IllegalArgumentException("Quiz not found with ID: " + id);
        }
    }

    // Source of unique quiz IDs. Starts at 1 and never reuses a value, even after deletes.
    private final AtomicInteger idCounter = new AtomicInteger(0);

    public Integer nextId() {
        return idCounter.incrementAndGet();
    }

    private Integer requireId(Quiz quiz) {
        Integer id = quiz.getId();
        if (id == null) {
            throw new IllegalArgumentException("Quiz ID must not be null");
        }
        return id;
    }
}
