# Music Bro Bot 🎵

![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?logo=spring-boot&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?logo=Hibernate&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?logo=postgresql&logoColor=white)
![Telegram Bot API](https://img.shields.io/badge/Telegram_Bot_API-26A5E4?logo=telegram&logoColor=white)
![Docker](https://img.shields.io/badge/-Docker-2496ED?logo=Docker&logoColor=white)

Telegram-бот, который узнаёт любимые жанры пользователя и раз в день присылает ему свежий трек по подборке [Last.fm](https://www.last.fm/). Умеет отдавать трек по запросу, хранить историю рекомендаций и подстраивается под часовой пояс и язык пользователя.

## Возможности

- 🎵 Подбор трека под жанр через Last.fm API, без повторов из истории
- ⏰ Ежедневная рассылка в удобное для пользователя время (с учётом часового пояса)
- 🎸 Настройка любимых жанров (Rock / Ambient / Country / Jazz) через инлайн-клавиатуру
- 📜 История последних рекомендаций с возможностью очистки
- 🌍 Локализация интерфейса: `ru`, `en`, `de`, `es`, `fr` — определяется по языку клиента Telegram и сохраняется для фоновых рассылок

## Стек

- Java 17, Spring Boot
- Spring Data JPA (Hibernate) + PostgreSQL
- [TelegramBots](https://github.com/rubenlagus/TelegramBots)
- Docker / Docker Compose

## Как это работает

```
Update (Telegram) → Bot → Service (User / TrackRecommendation / Notification) → Repository → PostgreSQL
                                  ↓
                          LastFmService → Last.fm API
```

- `Bot` — разбирает апдейты от Telegram и определяет язык пользователя.
- `UserService` — создание/обновление пользователя, часовой пояс, язык, жанры.
- `TrackRecommendationService` — подбор нового трека под жанры пользователя и работа с историей; используется как по запросу, так и планировщиком.
- `NotificationService` — раз в минуту проверяет, кому пора отправить трек по расписанию (`@Scheduled`).
- `LocalizationService` — обёртка над `MessageSource`, отдаёт тексты на нужном языке.

## Быстрый старт

### 1. Переменные окружения

Создайте `.env` в корне проекта:

```env
BOT_NAME=your_bot_username
BOT_TOKEN=123456:AA...           # токен из @BotFather
LASTFM_API_KEY=your_lastfm_key   # https://www.last.fm/api/account/create
SHARED_SECRET=your_lastfm_secret
```

### 2. Запуск через Docker Compose

```bash
docker compose up --build
```

Поднимет Postgres и приложение. База и таблицы создаются автоматически.

### 3. Локальный запуск (без Docker)

```bash
# поднимите Postgres из docker-compose.yml (или свою локальную БД)
./mvnw spring-boot:run
```

## Основные команды бота

| Команда | Описание |
|---|---|
| `/start` | Начать / открыть главное меню |
| `/tz <offset>` | Указать часовой пояс относительно UTC, например `/tz 3` |
| `/update_time HH:mm` | Время ежедневной рассылки, например `/update_time 14:00` |

Дальше всё управляется инлайн-кнопками: выбор жанров, настройки, история.

## Локализация

Тексты лежат в `src/main/resources/messages*.properties`. Базовый файл `messages.properties` — английский, он же fallback, если язык клиента не подходит ни под один из переводов. Во всех файлах один и тот же набор ключей — при добавлении новой фразы обязательно добавляйте её сразу во все пять файлов, иначе для части языков вылетит `NoSuchMessageException`.

## Структура проекта

```
src/main/java/com/ivank/music_telegram_bot/
├── bot/            # обработка апдейтов Telegram
├── config/         # регистрация бота в Telegram API
├── model/          # JPA-сущности
├── repository/     # Spring Data репозитории
├── service/        # бизнес-логика
├── ui/             # фабрика инлайн-клавиатур
└── api/            # DTO для ответа Last.fm
```

## Разработка

Проект собирается через Maven (`./mvnw`). Тесты — `./mvnw test`. Схема БД управляется Hibernate автоматически, отдельных миграций (Flyway/Liquibase) нет — при изменении сущностей столбцы добавляются на лету.
