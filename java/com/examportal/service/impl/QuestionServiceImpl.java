package com.examportal.service.impl;

import com.examportal.dto.BulkUploadResultDto;
import com.examportal.dto.QuestionDto;
import com.examportal.model.Course;
import com.examportal.model.Difficulty;
import com.examportal.model.Question;
import com.examportal.model.QuestionOption;
import com.examportal.model.QuestionType;
import com.examportal.repository.CourseRepository;
import com.examportal.repository.QuestionRepository;
import com.examportal.service.QuestionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for Question Bank management and CSV imports.
 */
@Service
@Transactional
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final CourseRepository courseRepository;

    public QuestionServiceImpl(QuestionRepository questionRepository, CourseRepository courseRepository) {
        this.questionRepository = questionRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Question> getQuestionsByTeacher(Long teacherId, Long courseId, Difficulty difficulty, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return questionRepository.findByTeacherAndFilters(teacherId, courseId, difficulty, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Question getQuestionByIdAndTeacher(Long questionId, Long teacherId) {
        return questionRepository.findByIdAndCourseTeacherId(questionId, teacherId)
                .orElseThrow(() -> new SecurityException("Question not found or you do not have permission to access it."));
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionDto getQuestionDtoByIdAndTeacher(Long questionId, Long teacherId) {
        Question question = getQuestionByIdAndTeacher(questionId, teacherId);

        QuestionDto dto = new QuestionDto();
        dto.setId(question.getId());
        dto.setCourseId(question.getCourse() != null ? question.getCourse().getId() : null);
        dto.setText(question.getText());
        dto.setType(question.getType());
        dto.setMarks(question.getMarks());
        dto.setDifficulty(question.getDifficulty());

        List<String> optionTexts = new ArrayList<>();
        Integer correctIndex = 0;

        List<QuestionOption> options = question.getOptions();
        for (int i = 0; i < options.size(); i++) {
            QuestionOption opt = options.get(i);
            optionTexts.add(opt.getOptionText());
            if (opt.isCorrect()) {
                correctIndex = i;
            }
        }

        dto.setOptions(optionTexts);
        dto.setCorrectOptionIndex(correctIndex);

        return dto;
    }

    @Override
    public void createQuestion(QuestionDto dto, Long teacherId) {
        Course course = verifyCourseOwnership(dto.getCourseId(), teacherId);
        validateQuestionDto(dto);

        Question question = new Question();
        question.setCourse(course);
        question.setText(dto.getText().trim());
        question.setType(dto.getType());
        question.setMarks(dto.getMarks());
        question.setDifficulty(dto.getDifficulty());

        attachOptionsToQuestion(question, dto);

        questionRepository.save(question);
    }

    @Override
    public void updateQuestion(Long questionId, QuestionDto dto, Long teacherId) {
        Question question = getQuestionByIdAndTeacher(questionId, teacherId);
        Course course = verifyCourseOwnership(dto.getCourseId(), teacherId);
        validateQuestionDto(dto);

        question.setCourse(course);
        question.setText(dto.getText().trim());
        question.setType(dto.getType());
        question.setMarks(dto.getMarks());
        question.setDifficulty(dto.getDifficulty());

        // Clear previous options and re-attach
        question.getOptions().clear();
        attachOptionsToQuestion(question, dto);

        questionRepository.save(question);
    }

    @Override
    public void deleteQuestion(Long questionId, Long teacherId) {
        Question question = getQuestionByIdAndTeacher(questionId, teacherId);
        questionRepository.delete(question);
    }

    @Override
    @Transactional(readOnly = true)
    public long getQuestionCountByTeacher(Long teacherId) {
        return questionRepository.countByCourseTeacherId(teacherId);
    }

    @Override
    public BulkUploadResultDto bulkUploadQuestions(MultipartFile file, Long courseId, Long teacherId) {
        Course course = verifyCourseOwnership(courseId, teacherId);
        BulkUploadResultDto result = new BulkUploadResultDto();

        if (file == null || file.isEmpty()) {
            result.addError(0, "Uploaded CSV file is empty.");
            return result;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }

                List<String> columns = parseCsvLine(line);

                // Skip header line if present
                if (lineNumber == 1 && isHeaderRow(columns)) {
                    continue;
                }

                if (columns.size() < 8) {
                    result.addError(lineNumber, "Expected 8 columns (question, optionA, optionB, optionC, optionD, correctOption, marks, difficulty), but found " + columns.size());
                    continue;
                }

                String questionText = columns.get(0).trim();
                String optA = columns.get(1).trim();
                String optB = columns.get(2).trim();
                String optC = columns.get(3).trim();
                String optD = columns.get(4).trim();
                String correctStr = columns.get(5).trim().toUpperCase();
                String marksStr = columns.get(6).trim();
                String difficultyStr = columns.get(7).trim().toUpperCase();

                if (questionText.isEmpty()) {
                    result.addError(lineNumber, "Question text cannot be blank.");
                    continue;
                }

                if (optA.isEmpty() || optB.isEmpty() || optC.isEmpty() || optD.isEmpty()) {
                    result.addError(lineNumber, "All four options (optionA, optionB, optionC, optionD) must be provided.");
                    continue;
                }

                int correctIndex = parseCorrectOptionIndex(correctStr);
                if (correctIndex == -1) {
                    result.addError(lineNumber, "Invalid correctOption '" + correctStr + "'. Must be A, B, C, or D.");
                    continue;
                }

                int marks = 1;
                try {
                    marks = Integer.parseInt(marksStr);
                    if (marks < 1) marks = 1;
                } catch (NumberFormatException e) {
                    marks = 1;
                }

                Difficulty difficulty = Difficulty.MEDIUM;
                try {
                    difficulty = Difficulty.valueOf(difficultyStr);
                } catch (IllegalArgumentException e) {
                    difficulty = Difficulty.MEDIUM;
                }

                // Construct and save valid question
                Question question = new Question();
                question.setCourse(course);
                question.setText(questionText);
                question.setType(QuestionType.MCQ);
                question.setMarks(marks);
                question.setDifficulty(difficulty);

                String[] rawOptions = new String[]{optA, optB, optC, optD};
                for (int i = 0; i < rawOptions.length; i++) {
                    QuestionOption option = new QuestionOption();
                    option.setQuestion(question);
                    option.setOptionText(rawOptions[i]);
                    option.setCorrect(i == correctIndex);
                    question.getOptions().add(option);
                }

                questionRepository.save(question);
                result.addSuccess();
            }

        } catch (Exception ex) {
            result.addError(0, "Error reading CSV file: " + ex.getMessage());
        }

        return result;
    }

    private Course verifyCourseOwnership(Long courseId, Long teacherId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));

        if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacherId)) {
            throw new SecurityException("You do not have permission to manage questions for course '" + course.getName() + "'.");
        }

        return course;
    }

    private void validateQuestionDto(QuestionDto dto) {
        if (dto.getCorrectOptionIndex() == null) {
            throw new IllegalArgumentException("Exactly one option must be marked as correct.");
        }

        if (dto.getType() == QuestionType.MCQ) {
            if (dto.getOptions() == null || dto.getOptions().size() < 4) {
                throw new IllegalArgumentException("MCQ questions require 4 options.");
            }
            for (int i = 0; i < 4; i++) {
                if (dto.getOptions().get(i) == null || dto.getOptions().get(i).trim().isEmpty()) {
                    throw new IllegalArgumentException("Option " + (char)('A' + i) + " cannot be blank.");
                }
            }
            if (dto.getCorrectOptionIndex() < 0 || dto.getCorrectOptionIndex() > 3) {
                throw new IllegalArgumentException("Correct option must be one of the 4 provided options.");
            }
        } else if (dto.getType() == QuestionType.TRUE_FALSE) {
            if (dto.getCorrectOptionIndex() < 0 || dto.getCorrectOptionIndex() > 1) {
                throw new IllegalArgumentException("True/False question must have either True (0) or False (1) marked correct.");
            }
        }
    }

    private void attachOptionsToQuestion(Question question, QuestionDto dto) {
        if (dto.getType() == QuestionType.MCQ) {
            for (int i = 0; i < 4; i++) {
                QuestionOption opt = new QuestionOption();
                opt.setQuestion(question);
                opt.setOptionText(dto.getOptions().get(i).trim());
                opt.setCorrect(i == dto.getCorrectOptionIndex());
                question.getOptions().add(opt);
            }
        } else if (dto.getType() == QuestionType.TRUE_FALSE) {
            String[] tfLabels = new String[]{"True", "False"};
            for (int i = 0; i < 2; i++) {
                QuestionOption opt = new QuestionOption();
                opt.setQuestion(question);
                opt.setOptionText(tfLabels[i]);
                opt.setCorrect(i == dto.getCorrectOptionIndex());
                question.getOptions().add(opt);
            }
        }
    }

    private boolean isHeaderRow(List<String> cols) {
        if (cols.isEmpty()) return false;
        String first = cols.get(0).toLowerCase();
        return first.contains("question") || first.contains("title");
    }

    private int parseCorrectOptionIndex(String correct) {
        switch (correct) {
            case "A": case "1": return 0;
            case "B": case "2": return 1;
            case "C": case "3": return 2;
            case "D": case "4": return 3;
            default: return -1;
        }
    }

    /**
     * Parse standard CSV line respecting quoted strings with commas.
     */
    private List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());

        return tokens;
    }
}
