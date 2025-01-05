package com.example.collaboration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class CollaborationApplication {
    public static void main(String[] args) {
        SpringApplication.run(CollaborationApplication.class, args);
    }
}

// WebSocket Configuration
@Configuration
@EnableWebSocket
class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new CollaborationHandler(), "/collaborate").setAllowedOrigins("*");
    }
}

// WebSocket Handler
class CollaborationHandler extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        System.out.println("New connection established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen() && !s.getId().equals(session.getId())) {
                s.sendMessage(message);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        System.out.println("Connection closed: " + session.getId());
    }
}

// REST Controller for Document Management
@RestController
@RequestMapping("/api/documents")
class DocumentController {
    private final Map<Integer, Document> documents = new ConcurrentHashMap<>();
    private int idCounter = 1;

    @GetMapping
    public List<Document> getAllDocuments() {
        return new ArrayList<>(documents.values());
    }

    @PostMapping
    public Document createDocument(@RequestBody Document doc) {
        doc.setId(idCounter++);
        documents.put(doc.getId(), doc);
        return doc;
    }

    @GetMapping("/{id}")
    public Document getDocument(@PathVariable int id) {
        return documents.get(id);
    }

    @PutMapping("/{id}")
    public Document updateDocument(@PathVariable int id, @RequestBody Document updatedDoc) {
        Document doc = documents.get(id);
        if (doc != null) {
            doc.setContent(updatedDoc.getContent());
            doc.setTitle(updatedDoc.getTitle());
        }
        return doc;
    }

    @DeleteMapping("/{id}")
    public String deleteDocument(@PathVariable int id) {
        documents.remove(id);
        return "Document deleted successfully!";
    }
}

// Document Model
class Document {
    private int id;
    private String title;
    private String content;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
