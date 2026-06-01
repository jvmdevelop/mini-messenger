package com.jvmdevelop.strife.service;

import com.jvmdevelop.strife.exception.ExistException;
import com.jvmdevelop.strife.model.User;
import com.jvmdevelop.strife.repo.UserRepo;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String USER_CACHE_PREFIX = "user:";
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("(?i)^[a-z0-9._%+\\-]+@[a-z0-9.\\-]+\\.[a-z]{2,}$");

    public User add(User user) throws ExistException {
        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            throw new ExistException("Username already exists");
        }
        User saved = userRepo.save(user);
        cacheUser(saved);
        return saved;
    }

    public User getUserByLogin(String username) {
        User cached = (User) redisTemplate.opsForValue().get(USER_CACHE_PREFIX + username);
        if (cached != null) return cached;

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        cacheUser(user);
        return user;
    }

    public User getUserById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User findById(Long userId) {
        return userRepo.findById(userId).orElse(null);
    }

    public List<User> findUsersByIds(List<Long> userIds) {
        return userRepo.findAllById(userIds);
    }

    public List<User> searchUsers(String query) {
        return userRepo.findByUsernameContainingIgnoreCase(query);
    }

    @Transactional
    public User changeName(String currentUsername, String newUsername) {
        User user = userRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        redisTemplate.delete(USER_CACHE_PREFIX + currentUsername);
        user.setUsername(newUsername);
        userRepo.save(user);
        cacheUser(user);
        return user;
    }

    @Transactional
    public User updateAvatar(String currentUsername, String avatarUrl) {
        User user = userRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setAvatarUrl(avatarUrl);
        userRepo.save(user);
        cacheUser(user);
        return user;
    }

    @Transactional
    public User updateDescription(String currentUsername, String description) {
        User user = userRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setDescription(description);
        userRepo.save(user);
        cacheUser(user);
        return user;
    }

    @Transactional
    public void updateLastSeen(String username) {
        userRepo.findByUsername(username).ifPresent(user -> {
            user.setLastSeen(LocalDateTime.now());
            userRepo.save(user);
        });
    }

    private void cacheUser(User user) {
        redisTemplate.opsForValue().set(USER_CACHE_PREFIX + user.getUsername(), user, 1, TimeUnit.HOURS);
    }
}
