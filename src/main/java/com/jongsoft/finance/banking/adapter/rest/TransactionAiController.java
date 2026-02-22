package com.jongsoft.finance.banking.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.banking.adapter.api.TransactionAi;
import com.jongsoft.finance.banking.domain.model.TransactionResult;
import com.jongsoft.finance.rest.TransactionExtractorApi;
import com.jongsoft.finance.rest.model.ExtractTransactionRequest;
import com.jongsoft.finance.rest.model.ExtractedTransactionResponse;
import com.jongsoft.finance.rest.model.ExtractedTransactionResponseFrom;
import com.jongsoft.finance.rest.model.ExtractedTransactionResponseType;

import jakarta.inject.Singleton;

@Singleton
class TransactionAiController implements TransactionExtractorApi {

    private final TransactionAi transactionAi;

    TransactionAiController(TransactionAi transactionAi) {
        this.transactionAi = transactionAi;
    }

    @Override
    public ExtractedTransactionResponse extractTransaction(
            ExtractTransactionRequest extractTransactionRequest) {
        TransactionResult extracted = transactionAi
                .extractTransaction(extractTransactionRequest.getText())
                .getOrThrow(() ->
                        StatusException.badRequest("Unable to extract transaction from text."));

        ExtractedTransactionResponse response = new ExtractedTransactionResponse();
        response.setDate(extracted.date());
        response.setAmount(extracted.amount());
        response.setDescription(extracted.description());
        if (extracted.from() != null) {
            response.setFrom(new ExtractedTransactionResponseFrom(
                    extracted.from().id(), extracted.from().name()));
        }
        if (extracted.to() != null) {
            response.setTo(new ExtractedTransactionResponseFrom(
                    extracted.to().id(), extracted.to().name()));
        }
        response.setType(
                ExtractedTransactionResponseType.valueOf(extracted.type().name()));
        return response;
    }
}
