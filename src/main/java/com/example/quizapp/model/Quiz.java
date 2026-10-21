package com.example.quizapp.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Quiz {

    private Integer id;
    private String questionText;
    private ArrayList<String> options; // Keep options as ArrayList<String>
    private String correctAnswer;

    // No-argument constructor (required for Thymeleaf binding)
    public Quiz() {
        this.options = new ArrayList<>();
    }

    // Constructor with parameters
    public Quiz(Integer id, String questionText, ArrayList<String> options, String correctAnswer) {
        this.id = id;
        this.questionText = questionText;
        this.options = (options == null) ? new ArrayList<>() : options;
        this.correctAnswer = correctAnswer;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<String> options) {
        this.options = (options == null) ? new ArrayList<>() : options;
    }

    // Helper method to get options as a comma-separated string
    public String getOptionsAsString() {
        if (options == null || options.isEmpty()) {
            return "";
        }
        return String.join(",", options);
    }

    // Helper method to set options from a comma-separated string
    public void setOptionsFromString(String optionsString) {
        if (optionsString == null || optionsString.isBlank()) {
            this.options = new ArrayList<>();
            return;
        }
        this.options = Arrays.stream(optionsString.split(","))
                .map(String::trim)
                .filter(option -> !option.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    @Override
    public String toString() {
        return ("ID: " + id
                + "\nQuestion: " + questionText
                + "\nOptions: " + options
                + "\nCorrect Answer: " + correctAnswer);
    }
}
