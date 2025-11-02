package com.mybillbook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIMatchResponse {

    private List<Match> matches;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Match {
        @JsonProperty("invoice_number")
        private String invoiceNumber;

        private BigDecimal confidence;
        private String reason;
    }
}
