package com.example.support.config;

import com.example.support.config.RabbitMQConfig;
import com.example.support.config.dtos.ChatMessageDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
public class ChatService {

    private final RabbitTemplate rabbitTemplate;
    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    public ChatService(RabbitTemplate rabbitTemplate, WebClient.Builder webClientBuilder) {
        this.rabbitTemplate = rabbitTemplate;
        this.webClient = webClientBuilder.build();
    }

    public void processMessage(ChatMessageDTO msg) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "chat.message", msg);

        if (!msg.isFromAdmin()) {
            String response = checkRules(msg.content());

            if (response == null) {
                response = callGeminiAI(msg.content());
            }
            ChatMessageDTO reply = new ChatMessageDTO(
                    msg.senderId(),
                    response,
                    true,
                    System.currentTimeMillis()
            );
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "chat.message", reply);
        }
    }

    private String checkRules(String content) {
        String lower = content.toLowerCase();

        if (lower.contains("hello") || lower.contains("hi")) return "Hello! I am your energy assistant. How can I help?";
        if (lower.contains("price") || lower.contains("cost")) return " The current energy price is dynamic, averaging around 0.80 RON/kWh.";
        if (lower.contains("bill") || lower.contains("invoice")) return "You can view your detailed bills in the 'History' tab of your dashboard.";
        if (lower.contains("device") && lower.contains("add")) return "To add a new device, please contact an administrator.";
        if (lower.contains("limit") || lower.contains("max")) return "Overconsumption limits are set to protect your circuit. Check 'Device Details'.";
        if (lower.contains("hours")) return "Our support team is available from 9:00 AM to 5:00 PM, Monday to Friday.";
        if (lower.contains("outage") || lower.contains("power cut")) return "If you are experiencing a power outage, please call the emergency grid number: 112.";
        if (lower.contains("save") || lower.contains("efficiency")) return "Tip: Unplug devices when not in use to save up to 10% on your bill.";
        if (lower.contains("login") || lower.contains("password")) return "If you forgot your password, use the 'Reset Password' link on the login page.";
        if (lower.contains("bye") || lower.contains("goodbye")) return "Goodbye! Have a great day and stay energy efficient!";

        return null;
    }

    private String callGeminiAI(String question) {
        try {

            String requestBody = """
                {
                  "model": "llama-3.3-70b-versatile",
                  "messages": [
                    {
                        "role": "system", 
                        "content": "You are a helpful energy assistant. Keep answers short."
                    },
                    {
                        "role": "user", 
                        "content": "%s"
                    }
                  ]
                }
                """.formatted(question.replace("\"", "'").replace("\n", " "));

            System.out.println("Sending to Groq: " + requestBody);

            String responseJson = webClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions") // Hardcoded to match curl
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + geminiApiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseJson);

            return root.path("choices").get(0)
                    .path("message")
                    .path("content").asText();

        } catch (Exception e) {
            if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                System.err.println("API Response Body: " +
                        ((org.springframework.web.reactive.function.client.WebClientResponseException) e).getResponseBodyAsString());
            }
            e.printStackTrace();
            return "I am currently unable to reach the AI service. Please try again later.";
        }
    }
}