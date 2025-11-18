package mew.pumlserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    private String id;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessedAt;
    private List<Message> messages;

    public Conversation(String id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
        this.messages = new ArrayList<>();
    }

    public void addMessage(String role, String content) {
        this.messages.add(new Message(role, content));
        this.lastAccessedAt = LocalDateTime.now();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}

