# API Documentation - Story Maker

Данный документ содержит описание всех конечных точек API, структуры запросов и ответов, а также бизнес-логику для реализации UI.

## Основная логика проекта

**Story Maker** — платформа для создания интерактивных историй с ветвящимся сюжетом.

**Как это работает:**
1. Пользователь создаёт **историю** и добавляет к ней **главы** в определённой последовательности.
2. В любой главе автор может установить **выбор** — бинарное решение (вариант 1 или 2), которое читатели проголосуют.
3. Выборы имеют **таймер** — пока он активен, голоса скрыты. После истечения времени определяется победивший вариант, и сюжет продолжается с учётом результата.
4. Читатели могут **оценивать** истории (1-5 звёзд), **комментировать** их и **подписываться** на обновления.
5. Автор управляет только своей историей: добавляет/редактирует главы, создаёт выборы, видит статистику голосов и подписчиков.

---

## Общие сведения
- **Базовый URL**: `api/`
- **Аутентификация**: JWT-токен. Передается в заголовке `Authorization: Bearer <token>`.
- **Формат данных**: JSON.
- **Загрузка файлов**: Для создания/обновления историй и пользователей используется `multipart/form-data`.
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

## 8. Загрузка изображений

Изображения хранятся в папке `uploads/` проекта и раздают через статический endpoint `/uploads/{тип}/{filename}`.

- **Папки**:
  - `/uploads/covers/` — обложки историй
  - `/uploads/avatars/` — аватары пользователей
- **Разрешённые форматы**: JPG, JPEG, PNG, WEBP
- **Максимальный размер**: 10MB
- **URL изображения**: `/uploads/covers/{GUID}.{extension}` или `/uploads/avatars/{GUID}.{extension}`

---

## 1. Аутентификация (`api/auth`)

### Регистрация
- **Метод**: `POST /api/auth/register`
- **Тип запроса**: `multipart/form-data`
- **Параметры (form fields)**:
  - `username` (string, required): Имя пользователя
  - `email` (string, required): Email адрес
  - `password` (string, required): Пароль (мин. 6 символов)
  - `avatar` (file, optional): Аватар пользователя (JPG, PNG, WEBP, макс. 10MB)
- **Ответ (`AuthResponse`)**:
  ```json
  {
    "token": "eyJhbG...",
    "expiresAt": "2026-04-16T12:00:00Z"
  }
  ```
- **Логика**: Возвращает JWT-токен. Если загружен аватар, он сохраняется в `uploads/avatars/`.

### Вход
- **Метод**: `POST /api/auth/login`
- **Тип запроса**: `application/json`
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
- **Тип запроса**: `multipart/form-data`
- **Параметры (form fields)**:
  - `title` (string, required): Заголовок истории
  - `description` (string, required): Описание истории
  - `coverImage` (file, optional): Изображение обложки (JPG, PNG, WEBP, макс. 10MB)
- **Ответ**: `StoryResponse`
- **Логика**: Автором становится текущий пользователь. Если загружено изображение, оно сохраняется в `uploads/covers/`.

### Обновить историю
- **Метод**: `PUT /api/story/{id}`
- **Тип запроса**: `multipart/form-data`
- **Параметры (form fields)**:
  - `title` (string, required): Заголовок истории
  - `description` (string, required): Описание истории
  - `coverImage` (file, optional): Новая обложка (заменяет старую)
- **Ответ**: `StoryResponse`
- **Логика**: Доступно только автору. Старая обложка удаляется при загрузке новой.

### Удалить историю
- **Метод**: `DELETE /api/story/{id}`
- **Логика**: Доступно только автору. Удаляет историю и **все связанные данные**:
  - Обложка истории (файл удаляется с диска)
  - Все главы (и связанные с ними выборы)
  - Все комментарии
  - Все голоса
  - Все подписки
  - Все рейтинги

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
    "sequenceNumber": 1 // Опционально, если не указано — автоматически
  }
  ```
- **Логика**: Только автор истории может добавлять главы.

### Обновить главу
- **Метод**: `PUT /api/stories/{storyId}/chapters/{id}`
- **Запрос (`UpdateChapterRequest`)**:
  ```json
  {
    "content": "Обновлённый текст главы",
    "sequenceNumber": 2 // Опционально
  }
  ```
- **Логика**: Только автор истории может обновлять.

### Удалить главу
- **Метод**: `DELETE /api/stories/{storyId}/chapters/{id}`
- **Логика**: Только автор истории может удалять. При удалении главы также удаляются связанные выбор и голоса (каскадное удаление).

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

### Просмотр выбора (Автор)
- **Метод**: `GET /api/chapters/{chapterId}/choices/{id}/author`
- **Логика**: Возвращает выбор с текущими счётчиками голосов, видимыми только автору истории (даже если выбор ещё активен).

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
    "userScore": 5
  }
  ```

### Поставить оценку
- **Метод**: `POST /api/stories/{storyId}/rating`
- **Запрос (`RateStoryRequest`)**: `{ "score": 5 }` (от 1 до 5).
- **Логика**: Если оценка уже была — она перезаписывается.

### Удалить свою оценку
- **Метод**: `DELETE /api/stories/{storyId}/rating`
- **Логика**: Удаляет оценку текущего пользователя.

---

## 7. Подписки (`api/stories/{storyId}/...`)

- **POST `/subscribe`**: Подписаться на историю.
- **DELETE `/unsubscribe`**: Отписаться.
- **GET `/subscribers`**: Список подписчиков (Только для автора истории).
- **GET `/api/stories/subscribed`**: Список историй, на которые подписан текущий пользователь.

---

## Основные модели данных (Справочно)
- **Story**: `Id, Title, Description, CoverImageUrl (nullable), AuthorId, Rating, CreatedAt`.
- **Chapter**: `Id, StoryId, Content, SequenceNumber`.
- **Choice**: `Id, ChapterId, Option1, Option2, Option1Votes, Option2Votes, ExpiresAt, IsClosed`.
- **User**: `Id, Username, Email, AvatarImageUrl (nullable), PasswordHash, CreatedAt`.

### Пример `StoryResponse`
```json
{
  "id": 1,
  "title": "Приключения в лесу",
  "description": "История о приключениях",
  "coverImageUrl": "/uploads/covers/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",
  "authorUsername": "author123",
  "rating": 4.5,
  "createdAt": "2026-04-28T12:00:00Z",
  "chapterCount": 3
}
```

### Пример `UserDto` (в ответах с аватаром)
```json
{
  "id": 1,
  "username": "author123",
  "email": "author@example.com",
  "avatarImageUrl": "/uploads/avatars/b2c3d4e5-f6a7-8901-bcde-f12345678901.png",
  "createdAt": "2026-04-28T10:00:00Z"
}
```

### Пример создания истории с обложкой (curl)
```bash
curl -X POST http://localhost:5157/api/story \
  -H "Authorization: Bearer <token>" \
  -F "title=Моя история" \
  -F "description=Описание истории" \
  -F "coverImage=@/path/to/image.jpg"
```

### Пример регистрации с аватаром (curl)
```bash
curl -X POST http://localhost:5157/api/auth/register \
  -F "username=myuser" \
  -F "email=myuser@example.com" \
  -F "password=securepass123" \
  -F "avatar=@/path/to/avatar.png"
```
