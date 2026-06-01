package com.jvmdevelop.strife.controller;

import com.jvmdevelop.strife.model.UserDetailsImpl;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@AllArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{chatId}/typing")
    public void typing(@DestinationVariable Long chatId, Principal principal) {
        UserDetailsImpl user = extractUser(principal);
        if (user == null) return;

        messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/typing",
                Map.of("userId", user.getId(), "username", user.getUsername()));
    }

    @MessageMapping("/chat/{chatId}/read")
    public void markRead(@DestinationVariable Long chatId, Principal principal) {
        UserDetailsImpl user = extractUser(principal);
        if (user == null) return;

        messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/read",
                Map.of("userId", user.getId(), "username", user.getUsername()));
    }

    private UserDetailsImpl extractUser(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            return (UserDetailsImpl) auth.getPrincipal();
        }
        return null;
    }
}
