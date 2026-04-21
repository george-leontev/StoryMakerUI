# Система API для Story Maker

## Быстрый старт

### 1. Инициализация
Приложение уже инициализирует API клиент в `StoryMakerApp.kt`. Убедитесь, что в `AndroidManifest.xml` указано:
```xml
<application android:name=".StoryMakerApp">
```

### 2. Примеры использования

#### Аутентификация
```kotlin
// Вход
val result = Repositories.auth.login("user@email.com", "password")
result.onSuccess { authResponse ->
    // Успешный вход, токен сохранён автоматически
    println("Токен: ${authResponse.token}")
}
result.onFailure { error ->
    println("Ошибка: ${error.message}")
}

// Регистрация
val result = Repositories.auth.register("username", "user@email.com", "password")

// Проверка авторизации
if (Repositories.auth.isAuthenticated()) {
    // Пользователь вошёл в систему
}

// Выход
Repositories.auth.logout()
```

#### Истории
```kotlin
// Получить все истории (с пагинацией)
val result = Repositories.story.getStories(page = 1, pageSize = 20)
result.onSuccess { pagedResponse ->
    pagedResponse.items.forEach { story ->
        println("История: ${story.title}")
    }
    println("Всего историй: ${pagedResponse.totalCount}")
}

// Получить мои истории
val myStories = Repositories.story.getMyStories()

// Создать новую историю
val newStory = Repositories.story.createStory("Название", "Описание")
newStory.onSuccess { story ->
    println("Создана история с ID: ${story.id}")
}

// Обновить историю
Repositories.story.updateStory(storyId, "Новое название", "Новое описание")

// Удалить историю
Repositories.story.deleteStory(storyId)
```

#### Главы
```kotlin
// Получить главы истории
val chapters = Repositories.chapter.getChapters(storyId = 1, page = 1)
chapters.onSuccess { response ->
    response.items.forEach { chapter ->
        println("Глава ${chapter.sequenceNumber}: ${chapter.content}")
    }
}

// Создать главу
Repositories.chapter.createChapter(storyId = 1, content = "Текст главы...")
```

#### Голосования (Выборы)
```kotlin
// Создать выбор для главы
Repositories.choice.createChoice(
    chapterId = 1,
    option1Text = "Пойти налево",
    option2Text = "Пойти направо",
    durationInMinutes = 60
)

// Получить текущий выбор
Repositories.choice.getChoice(chapterId = 1).onSuccess { choice ->
    if (choice.isClosed) {
        println("Победил вариант ${choice.winningOption}")
        println("Голоса: ${choice.option1Votes} vs ${choice.option2Votes}")
    } else {
        println("Голосование активно")
    }
}

// Проголосовать
Repositories.choice.vote(chapterId = 1, choiceId = 1, option = 1)
```

#### Рейтинг
```kotlin
// Получить рейтинг истории
Repositories.rating.getRating(storyId = 1).onSuccess { rating ->
    println("Средняя оценка: ${rating.averageRating}")
    println("Моя оценка: ${rating.userScore ?: "ещё не ставил"}")
}

// Поставить оценку
Repositories.rating.setRating(storyId = 1, score = 5)
```

#### Комментарии
```kotlin
// Получить комментарии
Repositories.comment.getComments(storyId = 1).onSuccess { response ->
    response.items.forEach { comment ->
        println("${comment.username}: ${comment.content}")
    }
}

// Добавить комментарий
Repositories.comment.createComment(storyId = 1, content = "Отличная история!")

// Удалить комментарий
Repositories.comment.deleteComment(storyId = 1, commentId = 1)
```

### 3. Корутинки

Все методы используют Kotlin Coroutines (`suspend` функции). Вызовите их внутри `launch` или `async`:

```kotlin
// В Activity/Fragment
lifecycleScope.launch {
    val result = Repositories.story.getStories()
    result.onSuccess { /* обновить UI */ }
    result.onFailure { /* показать ошибку */ }
}
```

### 4. Обработка ошибок

Все методы возвращают `Result<T>`:
- `onSuccess { }` — вызывается при успехе
- `onFailure { }` — вызывается при ошибке (сеть, сервер)

```kotlin
val result = Repositories.auth.login(email, password)
when {
    result.isSuccess -> {
        val token = result.getOrNull()!!.token
        // Перейти к главному экрану
    }
    result.isFailure -> {
        val error = result.exceptionOrNull()
        when {
            error is IOException -> showError("Нет сети")
            error is HttpException -> showError("Ошибка сервера: ${error.code()}")
            else -> showError("Неизвестная ошибка")
        }
    }
}
```

### 5. Базовый URL

По умолчанию: `http://10.0.2.2:8080/` (для эмулятора Android)

Для реального устройства измените в `ApiClient.kt`:
```kotlin
private const val BASE_URL = "http://ВАШ_IP:8080/"
```

## Структура проекта

```
app/src/main/java/com/example/storytmakerui/
├── api/
│   ├── models/           # DTO модели данных
│   │   ├── AuthResponse.kt
│   │   ├── StoryResponse.kt
│   │   ├── ChapterResponse.kt
│   │   ├── ChoiceResponse.kt
│   │   ├── RatingResponse.kt
│   │   └── CommentResponse.kt
│   ├── services/         # Retrofit интерфейсы
│   │   ├── AuthService.kt
│   │   ├── StoryService.kt
│   │   ├── ChapterService.kt
│   │   ├── ChoiceService.kt
│   │   ├── RatingService.kt
│   │   └── CommentService.kt
│   ├── interceptor/      # HTTP интерцепторы
│   │   └── AuthInterceptor.kt
│   ├── repository/       # Репозитории с бизнес-логикой
│   │   ├── AuthRepository.kt
│   │   ├── StoryRepository.kt
│   │   ├── ChapterRepository.kt
│   │   ├── ChoiceRepository.kt
│   │   ├── RatingRepository.kt
│   │   ├── CommentRepository.kt
│   │   └── Repositories.kt  # Единая точка доступа
│   └── ApiClient.kt      # Настройка Retrofit
├── utils/
│   └── PreferenceManager.kt  # Хранение токена
└── StoryMakerApp.kt      # Application класс
```

## Зависимости

- **Retrofit 2.11.0** — сетевые запросы
- **OkHttp 4.12.0** — HTTP клиент + логирование
- **Gson 2.11.0** — парсинг JSON
- **Kotlin Coroutines 1.9.0** — асинхронность
- **Lifecycle 2.8.7** — ViewModel и корутины
