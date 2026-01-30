package com.example.chatbotmc.prompt;

public class PromptBuilder {

    
    public static String minecraftPrompt(String message) {

        String question = "What is the best modpack for Minecraft?";
        
        return """
            You are a Minecraft modpack assistant.
            Explain mods clearly and accurately.
            Do not invent features.
    
            Question:
            %s
            """.formatted(question);
        }
    }
