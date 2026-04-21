package com.example.storytmakerui.api.repository;

/**
 * Единая точка доступа ко всем репозиториям API.
 * Используйте этот класс для вызова всех методов сервера.
 *
 * Пример использования:
 * <pre>
 * // Инициализация в Application
 * ApiClient.init(context);
 *
 * // Вход
 * Result<AuthResponse> result = Repositories.auth.login("user@email.com", "password");
 * if (result.isSuccess()) {
 *     AuthResponse auth = result.getData();
 *     // Успешный вход
 * } else {
 *     // Ошибка: result.getError().getMessage()
 * }
 * </pre>
 */
public class Repositories {
    public static final AuthRepository auth = new AuthRepository();
    public static final StoryRepository story = new StoryRepository();
    public static final ChapterRepository chapter = new ChapterRepository();
    public static final ChoiceRepository choice = new ChoiceRepository();
    public static final RatingRepository rating = new RatingRepository();
    public static final CommentRepository comment = new CommentRepository();

    private Repositories() {
        // Утилитный класс, нельзя создавать экземпляры
    }
}
