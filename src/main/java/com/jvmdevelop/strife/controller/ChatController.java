package com.jvmdevelop.strife.controller;

import com.jvmdevelop.strife.model.*;
import com.jvmdevelop.strife.reqandresp.*;
import com.jvmdevelop.strife.service.ChatService;
import com.jvmdevelop.strife.service.MessageService;
import com.jvmdevelop.strife.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@AllArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/my")
    public ResponseEntity<List<Chat>> getMyChats(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<Chat> chats = chatService.findAllByUserId(currentUser.getId());
        return ResponseEntity.ok(chats);
    }

    @PostMapping("/createChat")
    public ResponseEntity<?> createChat(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                        @RequestBody CreateChatRequest request) {
        if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
            return ResponseEntity.badRequest().body("At least one user must be specified");
        }

        request.getUserIds().add(currentUser.getId());
        List<User> users = userService.findUsersByIds(request.getUserIds());
        if (users.size() < 2) {
            return ResponseEntity.badRequest().body("Some users not found");
        }

        Chat chat = new Chat();
        chat.setTitle(request.getTitle());
        chat.setUsers(users);
        chat.setIsTetATet(request.getIsTetATet() != null ? request.getIsTetATet() : false);
        chat.setRecipientId(request.getRecipientId());

        Chat created = chatService.createChat(chat);

        for (User user : users) {
            messagingTemplate.convertAndSendToUser(
                    user.getUsername(), "/queue/chats",
                    Map.of("type", "CHAT_CREATED", "chat", created));
        }

        return ResponseEntity.ok(created);
    }

    @PostMapping("/getCurrentChat")
    public ResponseEntity<?> getCurrentChat(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                            @RequestBody GetCurrentChatRequest request) {
        Chat targetChat = chatService.findTetATetChat(currentUser.getId(), request.getUserId());
        if (targetChat == null) {
            List<User> users = userService.findUsersByIds(List.of(currentUser.getId(), request.getUserId()));
            if (users.size() < 2) {
                return ResponseEntity.badRequest().body("User not found");
            }
            Chat newChat = new Chat();
            newChat.setTitle("Direct message");
            newChat.setUsers(users);
            newChat.setIsTetATet(true);
            newChat.setRecipientId(request.getUserId());
            targetChat = chatService.createChat(newChat);
        }
        return ResponseEntity.ok(targetChat);
    }

    @PostMapping("/addUserToChat")
    public ResponseEntity<?> addUserToChat(@RequestBody AddUserToChatRequest request) {
        Chat chat = chatService.findById(request.getChatId());
        if (chat == null) {
            return ResponseEntity.notFound().build();
        }
        User user = userService.findById(request.getUserId());
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        chatService.addUserToChat(chat, user);
        return ResponseEntity.ok(chat);
    }

    @PostMapping("/sendMessage")
    public ResponseEntity<?> sendMessage(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                         @RequestBody SendMessageRequest request) {
        Chat chat = chatService.findById(request.getChatId());
        if (chat == null) {
            return ResponseEntity.notFound().build();
        }

        User sender = userService.findById(currentUser.getId());

        Message message = new Message();
        message.setContent(request.getContent());
        message.setChat(chat);
        message.setSender(sender);

        Message created = messageService.createMessage(message);

        messagingTemplate.convertAndSend("/topic/chat/" + chat.getId(),
                Map.of("type", "MESSAGE_NEW", "message", created));

        return ResponseEntity.ok(created);
    }

    @PostMapping("/getChatMessages")
    public ResponseEntity<?> getChatMessages(@RequestBody GetChatMessagesRequest request) {
        Chat chat = chatService.findById(request.getChatId());
        if (chat == null) {
            return ResponseEntity.notFound().build();
        }
        List<Message> messages = messageService.findMessagesByChatId(request.getChatId(), request.getOffset());
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/editMessage")
    public ResponseEntity<?> editMessage(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                         @RequestBody EditMessageRequest request) {
        Message message = messageService.findById(request.getMessageId());
        if (message == null) {
            return ResponseEntity.notFound().build();
        }
        if (!message.getSender().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body("You can only edit your own messages");
        }

        message.setContent(request.getContent());
        message.setEdited(true);
        Message updated = messageService.updateMessage(message);

        messagingTemplate.convertAndSend("/topic/chat/" + message.getChatId(),
                Map.of("type", "MESSAGE_EDITED", "message", updated));

        return ResponseEntity.ok(updated);
    }

    @PostMapping("/deleteMessage")
    public ResponseEntity<?> deleteMessage(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                           @RequestBody DeleteMessageRequest request) {
        Message message = messageService.findById(request.getMessageId());
        if (message == null) {
            return ResponseEntity.notFound().build();
        }
        if (!message.getSender().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body("You can only delete your own messages");
        }

        Long chatId = message.getChatId();
        messageService.deleteMessage(message);

        messagingTemplate.convertAndSend("/topic/chat/" + chatId,
                Map.of("type", "MESSAGE_DELETED", "messageId", request.getMessageId()));

        return ResponseEntity.ok("Message deleted");
    }
}
