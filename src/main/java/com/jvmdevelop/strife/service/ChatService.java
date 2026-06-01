package com.jvmdevelop.strife.service;

import com.jvmdevelop.strife.model.Chat;
import com.jvmdevelop.strife.model.User;
import com.jvmdevelop.strife.repo.ChatRepo;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class ChatService {

    private final ChatRepo chatRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CHAT_CACHE_PREFIX = "chat:";

    public Chat createChat(Chat chat) {
        Chat savedChat = chatRepository.save(chat);
        redisTemplate.opsForValue().set(CHAT_CACHE_PREFIX + savedChat.getId(), savedChat, 1, TimeUnit.HOURS);
        return savedChat;
    }

    public Chat findById(Long chatId) {
        Chat chatFromCache = (Chat) redisTemplate.opsForValue().get(CHAT_CACHE_PREFIX + chatId);
        if (chatFromCache == null) {
            Chat chat = chatRepository.findById(chatId).orElse(null);
            if (chat != null) {
                redisTemplate.opsForValue().set(CHAT_CACHE_PREFIX + chatId, chat, 1, TimeUnit.HOURS);
            }
            return chat;
        }
        return chatFromCache;
    }

    public Chat findTetATetChat(Long currentUserId, Long otherUserId) {
        List<Chat> chats = chatRepository.findByIsTetATetTrueAndUsers_Id(currentUserId);
        for (Chat chat : chats) {
            boolean containsOther = chat.getUsers().stream().anyMatch(u -> u.getId().equals(otherUserId));
            if (containsOther) {
                redisTemplate.opsForValue().set(CHAT_CACHE_PREFIX + chat.getId(), chat, 1, TimeUnit.HOURS);
                return chat;
            }
        }
        return null;
    }

    public List<Chat> findChatsByUserId(Long userId) {
        return chatRepository.findByIsTetATetTrueAndUsers_Id(userId);
    }

    public List<Chat> findAllByUserId(Long userId) {
        return chatRepository.findAllByUserId(userId);
    }

    public void addUserToChat(Chat chat, User user) {
        chat.getUsers().add(user);
        chatRepository.save(chat);
        redisTemplate.opsForValue().set(CHAT_CACHE_PREFIX + chat.getId(), chat, 1, TimeUnit.HOURS);
    }
}