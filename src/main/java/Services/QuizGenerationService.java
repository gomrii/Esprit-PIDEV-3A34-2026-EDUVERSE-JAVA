package Services;

import Entities.Quiz;

@Deprecated
public class QuizGenerationService {
    private final GeminiQuizGenerationService geminiQuizGenerationService = new GeminiQuizGenerationService();

    public Quiz generateQuiz(String titre, int duree, String niveau, int nombreQuestions)
            throws GeminiQuizGenerationService.GeminiQuizGenerationException {
        return geminiQuizGenerationService.generateQuiz(titre, duree, niveau, nombreQuestions);
    }
}
