package com.jvmdevelop.strife.config;

import com.jvmdevelop.strife.model.UserDetailsImpl;
import com.jvmdevelop.strife.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    private static final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth) {
            UserDetailsImpl user = (UserDetailsImpl) auth.getPrincipal();
            onlineUsers.add(user.getUsername());
            log.info("User connected: {}", user.getUsername());

            messagingTemplate.convertAndSend("/topic/presence",
                    Map.of("type", "USER_ONLINE",
                            "userId", user.getId(),
                            "username", user.getUsername()));
        }
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth) {
            UserDetailsImpl user = (UserDetailsImpl) auth.getPrincipal();
            onlineUsers.remove(user.getUsername());
            userService.updateLastSeen(user.getUsername());
            log.info("User disconnected: {}", user.getUsername());

            messagingTemplate.convertAndSend("/topic/presence",
                    Map.of("type", "USER_OFFLINE",
                            "userId", user.getId(),
                            "username", user.getUsername()));
        }
    }

    public static Set<String> getOnlineUsers() {
        return Set.copyOf(onlineUsers);
    }
}
