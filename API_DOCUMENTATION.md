# API Documentation - Story Maker

Данный документ содержит описание всех конечных точек API, структуры запросов и ответов, а также бизнес-логику для реализации UI.

## Общие сведения
- **Базовый URL**: `api/`
- **Аутентификация**: JWT-токен. Передается в заголовке `Authorization: Bearer <token>`.
- **Формат данных**: JSON.
- **Пагинация**: Для списков используется `page` (по умолчанию 1) и `pageSize` (по умолчанию 20). 
- **Формат пагинированного ответа (`PagedResponse<T>`)**:
  ```json
  {
    "items": [...],
    "totalCount": 100,
    "page": 1,
    "pageSize": 20
  }
  ```

---

## 1. Аутентификация (`api/auth`)

### Регистрация
- **Метод**: `POST /api/auth/register`
- **Запрос (`RegisterRequest`)**:
  ```json
  {
    "username": "user123",
    "email": "user@example.com",
    "password": "securePassword"
  }
  ```
- **Ответ (`AuthResponse`)**:
  ```json
  {
    "token": "eyJhbG...",
    "expiresAt": "2026-04-16T12:00:00Z"
  }
  ```

### Вход
- **Метод**: `POST /api/auth/login`
- **Запрос (`LoginRequest`)**:
  ```json
  {
    "email": "user@example.com",
    "password": "securePassword"
  }
  ```
- **Ответ**: Аналогичен регистрации.

---

## 2. Управление историями (`api/story`)

### Получить все истории (Публично)
- **Метод**: `GET /api/story?page=1&pageSize=20`
- **Ответ**: `PagedResponse<StoryResponse>`
- **Логика**: Сортировка по дате создания (новые сверху).

### Мои истории (Требуется токен)
- **Метод**: `GET /api/story/me`
- **Ответ**: Список историй текущего пользователя.

### Создать историю
- **Метод**: `POST /api/story`
- **Запрос (`CreateStoryRequest`)**:
  ```json
  {
    "title": "Название",
    "description": "Описание"
  }
  ```
- **Логика**: Автором становится текущий пользователь.

### Редактирование/Удаление
- **Методы**: `PUT /api/story/{id}`, `DELETE /api/story/{id}`
- **Логика**: Доступно только автору истории.

---

## 3. Главы истории (`api/stories/{storyId}/chapters`)

### Список глав
- **Метод**: `GET /api/stories/{storyId}/chapters`
- **Ответ**: `PagedResponse<ChapterResponse>`
- **Поля `ChapterResponse`**: `id`, `storyId`, `content`, `sequenceNumber`, `createdAt`, `hasChoice`.

### Создать главу
- **Метод**: `POST /api/stories/{storyId}/chapters`
- **Запрос (`CreateChapterRequest`)**:
  ```json
  {
    "content": "Текст главы...",
    "sequenceNumber": 1 // Опционально
  }
  ```
- **Логика**: Только автор истории может добавлять главы.

---

## 4. Интерактивные выборы (`api/chapters/{chapterId}/choices`)

Выбор всегда бинарный (вариант 1 или 2) и имеет таймер.

### Создать выбор
- **Метод**: `POST /api/chapters/{chapterId}/choices`
- **Запрос**:
  ```json
  {
    "option1Text": "Пойти налево",
    "option2Text": "Пойти направо",
    "durationInMinutes": 60
  }
  ```
- **Логика**: Только автор истории. Одна глава = один активный выбор.

### Просмотр выбора (Читатель)
- **Метод**: `GET /api/chapters/{chapterId}/choices/{id}`
- **Логика**: Если выбор активен (`isClosed: false`), количество голосов скрыто (`null`). После истечения времени (`isClosed: true`) отображаются голоса и `winningOption`.

### Голосование
- **Метод**: `POST /api/chapters/{chapterId}/choices/{id}/vote`
- **Запрос**: `{ "option": 1 }` (или 2).
- **Логика**: Один пользователь — один голос. Голосовать можно только пока выбор активен.

---

## 5. Комментарии (`api/stories/{storyId}/comments`)

- **GET**: Список комментариев (пагинация, новые сверху).
- **POST**: Добавить комментарий (нужен токен).
- **DELETE**: Удалить (только свой комментарий).

---

## 6. Рейтинг (`api/stories/{storyId}/rating`)

### Получить рейтинг
- **Метод**: `GET /api/stories/{storyId}/rating`
- **Ответ (`RatingResponse`)**:
  ```json
  {
    "storyId": 1,
    "averageRating": 4.5,
    "voteCount": 10,
    "userScore": 5 // Оценка текущего пользователя (если есть)
  }
  ```

### Поставить оценку
- **Метод**: `POST /api/stories/{storyId}/rating`
- **Запрос**: `{ "score": 5 }` (от 1 до 5).
- **Логика**: Если оценка уже была — она перезаписывается.

---

## 7. Подписки (`api/stories/{storyId}/...`)

- **POST `/subscribe`**: Подписаться на историю.
- **DELETE `/unsubscribe`**: Отписаться.
- **GET `/subscribers`**: Список подписчиков (Только для автора истории).
- **GET `/api/stories/subscribed`**: Список историй, на которые подписан текущий пользователь.

---

## Основные модели данных (Справочно)
- **Story**: `Id, Title, Description, AuthorId, Rating, CreatedAt`.
- **Chapter**: `Id, StoryId, Content, SequenceNumber`.
- **Choice**: `Id, ChapterId, Option1, Option2, Option1Votes, Option2Votes, ExpiresAt, IsClosed`.
- **User**: `Id, Username, Email, PasswordHash`.
