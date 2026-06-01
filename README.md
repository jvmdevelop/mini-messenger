# mini-messenger

[![Java](https://img.shields.io/badge/Java-ED8B00?logo=openjdk&logoColor=white)](https://img.shields.io/badge/Java-ED8B00?logo=openjdk&logoColor=white) [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=spring-boot&logoColor=white)](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=spring-boot&logoColor=white) [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white)](https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white) [![Redis](https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white)](https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white) [![License](https://img.shields.io/badge/license-ISC-blue)](https://img.shields.io/badge/license-ISC-blue)

Real-time messenger. Personal and group chats, typing indicators, online status, JWT auth.

## Stack

Java 21, Spring Boot 3.4.2, PostgreSQL, Redis, WebSocket (STOMP)

## Run

```
docker-compose up
```

Or without docker (needs postgres + redis running):

```
mvn spring-boot:run
```

Config via `application.properties` or env vars:

```
SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
SPRING_DATA_REDIS_HOST, SPRING_DATA_REDIS_PORT
JWT_SECRET, JWT_EXPIRATION
```

## API

### Auth

```
POST /api/v1/auth/register - register (body: username, email, password)
POST /api/v1/auth/login    - login (body: username, password)
```

### Users

```
GET  /api/v1/me                       - current user
GET  /api/v1/getUserByLogin/{username} - by username
GET  /api/v1/getUserById/{id}         - by id
GET  /api/v1/users/search?q=john      - search
GET  /api/v1/online                   - online users list
POST /api/v1/cname?username=new       - change username
POST /api/v1/description?description= - update description
POST /api/v1/avatar?avatarUrl=        - update avatar
```

### Chats

```
GET  /api/v1/chat/my              - my chats
POST /api/v1/chat/createChat      - create chat (body: title, userIds, isTetATet, recipientId)
POST /api/v1/chat/getCurrentChat  - get or create DM (body: userId)
POST /api/v1/chat/addUserToChat   - add user (body: chatId, userId)
```

### Messages

```
POST /api/v1/chat/sendMessage     - send (body: chatId, content)
POST /api/v1/chat/getChatMessages - history (body: chatId, offset)
POST /api/v1/chat/editMessage     - edit (body: messageId, content)
POST /api/v1/chat/deleteMessage   - delete (body: messageId)
```

### WebSocket

STOMP endpoint: `/ws`

**Messages** - subscribe to `/topic/chat/{chatId}` for real-time events:

```json
{"type": "MESSAGE_NEW", "message": {...}}
{"type": "MESSAGE_EDITED", "message": {...}}
{"type": "MESSAGE_DELETED", "messageId": 42}
```

**Typing** - send to `/app/chat/{chatId}/typing`, subscribe to `/topic/chat/{chatId}/typing`:

```json
{"userId": 1, "username": "john"}
```

**Presence** - subscribe to `/topic/presence`:

```json
{"type": "USER_ONLINE", "userId": 1, "username": "john"}
{"type": "USER_OFFLINE", "userId": 1, "username": "john"}
```

**New chats** - subscribe to `/user/{username}/queue/chats` for chat creation notifications.

## License

ISC
