package com.jvmdevelop.strife.controller;

import com.jvmdevelop.strife.model.User;
import com.jvmdevelop.strife.model.UserDetailsImpl;
import com.jvmdevelop.strife.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getUserById(userDetails.getId());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/getUserByLogin/{username}")
    public ResponseEntity<User> getUserByLogin(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByLogin(username));
    }

    @GetMapping("/getUserById/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String q) {
        return ResponseEntity.ok(userService.searchUsers(q));
    }

    @PostMapping("/cname")
    public ResponseEntity<User> changeName(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @RequestParam String username) {
        return ResponseEntity.ok(userService.changeName(userDetails.getUsername(), username));
    }

    @PostMapping("/description")
    public ResponseEntity<User> updateDescription(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @RequestParam String description) {
        return ResponseEntity.ok(userService.updateDescription(userDetails.getUsername(), description));
    }

    @PostMapping("/avatar")
    public ResponseEntity<User> updateAvatar(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                             @RequestParam String avatarUrl) {
        return ResponseEntity.ok(userService.updateAvatar(userDetails.getUsername(), avatarUrl));
    }
}
