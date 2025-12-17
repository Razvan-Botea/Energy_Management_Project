package com.example.support.config;

import com.example.support.config.dtos.ChatMessageDTO;
import com.example.support.config.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendMessage(@RequestBody ChatMessageDTO message) {
        System.out.println("CONTROLLER: Received message: " + message.content());
        chatService.processMessage(message);
        return ResponseEntity.ok().build();
    }

    public void processMessage(ChatMessageDTO msg) {
        System.out.println("SERVICE: Processing: " + msg.content());
    }
}