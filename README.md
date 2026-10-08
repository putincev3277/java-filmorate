# java-filmorate

Template repository for Filmorate project.

![Схема базы данных Filmorate](filmorate-db.webp)

## Схема базы данных

ER‑диаграмма отражает структуру БД для Filmorate с учётом нормализации (1НФ–3НФ).
Жанры, MPA‑рейтинги и связи (лайки, дружба) вынесены в отдельные таблицы, чтобы избежать дублирования и поддерживать бизнес‑логику.

### Таблицы

- **mpa** — справочник возрастных рейтингов (id, name, description).
- **genres** — справочник жанров (id, name).
- **users** — пользователи (email, login, name, birthday).
- **films** — фильмы (название, описание, дата релиза, длительность, ссылка на рейтинг MPA через `mpa_rating_id`).
- **film_genres** — связь «многие ко многим» между фильмами и жанрами.
- **friendships** — дружба между пользователями со статусами: `PENDING` (заявка), `CONFIRMED` (подтверждено).
- **likes** — лайки: связь фильма и пользователя (кто и какой фильм оценил).

Для таблицы `likes` созданы индексы `idx_likes_film_id` и `idx_likes_user_id` для ускорения выборок по фильму и пользователю.

### Примеры запросов для основных операций

**Топ‑5 самых популярных фильмов (по количеству лайков):**
```sql
SELECT f.name, COUNT(l.film_id) AS likes_count
FROM films f
LEFT JOIN likes l ON f.id = l.film_id
GROUP BY f.id, f.name
ORDER BY likes_count DESC
LIMIT 5;
