package com.mybillbook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybillbook.dto.OpenAIMatchResponse;
import com.mybillbook.exception.OpenAIServiceException;
import com.mybillbook.model.Invoice;
import com.mybillbook.model.Payment;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAIService {

    private final com.theokanning.openai.service.OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.max-tokens:1000}")
    private Integer maxTokens;

    @Value("${openai.temperature:0.3}")
    private Double temperature;

    public OpenAIMatchResponse findMatchingInvoices(Payment payment, List<Invoice> availableInvoices) {
        String prompt = buildPrompt(payment, availableInvoices);

        try {
            ChatCompletionResult result = callOpenAI(prompt);
            String responseContent = extractResponseContent(result);
            return parseResponse(responseContent);
        } catch (Exception e) {
            log.error("Error calling OpenAI API for payment ID: {}", payment.getId(), e);
            throw new OpenAIServiceException("Failed to get AI recommendations: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(Payment payment, List<Invoice> invoices) {
        StringBuilder invoiceList = new StringBuilder();
        for (Invoice invoice : invoices) {
            invoiceList.append(String.format(
                "- Invoice Number: %s, Customer: %s, Pending Amount: ₹%s\n",
                invoice.getInvoiceNumber(),
                invoice.getCustomerName(),
                invoice.getPendingAmount()
            ));
        }

        return String.format("""
            You are a financial reconciliation assistant. Given payment and invoice data, identify the best matching invoice(s) for the payment.

            Payment Details:
            - Amount: ₹%s
            - Date: %s
            - Remark: "%s"
            - Mode: %s

            Available Invoices (pending/partially paid):
            %s

            Task: Analyze and return JSON response with:
            1. Best matching invoice(s)
            2. Confidence score (0.0 to 1.0)
            3. Clear reasoning

            Response Format (STRICT JSON):
            {
              "matches": [
                {
                  "invoice_number": "INV101",
                  "confidence": 0.92,
                  "reason": "Remark mentions INV101 explicitly and amount matches half the pending amount"
                }
              ]
            }

            Rules:
            - If amount > invoice pending, mention as potential overpayment in reason
            - Match customer names using fuzzy logic (e.g., "Ramesh" matches "Ramesh Traders")
            - Consider invoice number mentions in remarks
            - If multiple strong matches exist, return all with confidence scores
            - Minimum confidence threshold: 0.60
            - Return ONLY valid JSON, no additional text
            """,
            payment.getAmount(),
            payment.getPaymentDate(),
            payment.getRemark() != null ? payment.getRemark() : "No remark",
            payment.getPaymentMode(),
            invoiceList.toString()
        );
    }

    private ChatCompletionResult callOpenAI(String prompt) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt));

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .build();

        return openAiService.createChatCompletion(request);
    }

    private String extractResponseContent(ChatCompletionResult result) {
        if (result == null || result.getChoices() == null || result.getChoices().isEmpty()) {
            throw new OpenAIServiceException("Empty response from OpenAI");
        }

        String content = result.getChoices().get(0).getMessage().getContent();
        log.info("OpenAI Response: {}", content);

        // Clean up response - remove markdown code blocks if present
        content = content.trim();
        if (content.startsWith("```json")) {
            content = content.substring(7);
        }
        if (content.startsWith("```")) {
            content = content.substring(3);
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }

        return content.trim();
    }

    private OpenAIMatchResponse parseResponse(String jsonResponse) {
        try {
            OpenAIMatchResponse response = objectMapper.readValue(jsonResponse, OpenAIMatchResponse.class);

            // Filter out low confidence matches
            if (response.getMatches() != null) {
                response.setMatches(
                    response.getMatches().stream()
                        .filter(m -> m.getConfidence().compareTo(new BigDecimal("0.60")) >= 0)
                        .toList()
                );
            }

            return response;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse OpenAI response: {}", jsonResponse, e);
            throw new OpenAIServiceException("Invalid JSON response from AI: " + e.getMessage(), e);
        }
    }
}
