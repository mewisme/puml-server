package mew.pumlserver.service;

import mew.pumlserver.model.Conversation;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationService {

    private final Map<String, Conversation> conversations = new ConcurrentHashMap<>();
    private static final long CONVERSATION_TTL_MINUTES = 30;

    /**
     * Create a new conversation and return its ID
     */
    public String createConversation() {
        String id = UUID.randomUUID().toString();
        Conversation conversation = new Conversation(id);
        conversations.put(id, conversation);
        return id;
    }

    /**
     * Get conversation by ID
     */
    public Conversation getConversation(String id) {
        Conversation conversation = conversations.get(id);
        if (conversation == null) {
            return null;
        }
        
        // Check if expired
        if (isExpired(conversation)) {
            conversations.remove(id);
            return null;
        }
        
        // Update last accessed time
        conversation.setLastAccessedAt(LocalDateTime.now());
        return conversation;
    }

    /**
     * Delete conversation by ID
     */
    public boolean deleteConversation(String id) {
        return conversations.remove(id) != null;
    }

    /**
     * Check if conversation is expired (not accessed for 30 minutes)
     */
    private boolean isExpired(Conversation conversation) {
        return conversation.getLastAccessedAt()
                .plusMinutes(CONVERSATION_TTL_MINUTES)
                .isBefore(LocalDateTime.now());
    }

    /**
     * Clean up expired conversations every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanupExpiredConversations() {
        conversations.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }

    /**
     * Get conversation count (for monitoring)
     */
    public int getConversationCount() {
        return conversations.size();
    }
}

