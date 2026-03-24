package com.jongsoft.finance.invoice.adapter.rest;

import com.jongsoft.finance.RestTestSetup;
import com.jongsoft.finance.extension.PledgerContext;
import com.jongsoft.finance.extension.PledgerRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@DisplayName("Regression - Invoices")
public class InvoiceTest extends RestTestSetup {

    @Test
    @DisplayName("Create invoice with line, finalize, and find by number")
    void invoiceLifecycle(PledgerContext context, PledgerRequests requests) {
        context.withUser("invoice-flow@account.local");
        requests.authenticate("invoice-flow@account.local");

        var clientId =
                requests.createClient(Map.of("name", "Client A", "email", "a@client.test"))
                        .statusCode(201)
                        .extract()
                        .jsonPath()
                        .getLong("id");

        var templateId =
                requests.createInvoiceTemplate(
                                Map.of(
                                        "name", "Standard",
                                        "headerContent", "Hello",
                                        "footerContent", "Thanks"))
                        .statusCode(201)
                        .extract()
                        .jsonPath()
                        .getLong("id");

        var taxBracketId =
                requests.createTaxBracket(Map.of("name", "VAT 21", "rate", 0.21D))
                        .statusCode(201)
                        .extract()
                        .jsonPath()
                        .getLong("id");

        var invoiceId =
                requests.createInvoice(
                                Map.of(
                                        "invoiceNumber", "INV-1001",
                                        "clientId", clientId,
                                        "invoiceDate", LocalDate.now().toString(),
                                        "dueDate", LocalDate.now().plusDays(14).toString(),
                                        "templateId", templateId))
                        .statusCode(201)
                        .body("invoiceNumber", equalTo("INV-1001"))
                        .body("finalized", equalTo(false))
                        .extract()
                        .jsonPath()
                        .getLong("id");

        requests.addInvoiceLine(
                        invoiceId,
                        Map.of(
                                "description", "Consulting",
                                "quantity", 2D,
                                "unit", "hours",
                                "unitPrice", 150D,
                                "taxBracketId", taxBracketId))
                .statusCode(201)
                .body("lines", hasSize(1))
                .body("subtotal", equalTo(300F))
                .body("finalized", equalTo(false));

        requests.finalizeInvoice(invoiceId, "pdf-token-1")
                .statusCode(200)
                .body("finalized", equalTo(true))
                .body("pdfToken", equalTo("pdf-token-1"));

        requests.findInvoices("INV-1001", null, null)
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].invoiceNumber", equalTo("INV-1001"))
                .body("[0].finalized", equalTo(true));

        requests.fetchInvoice(invoiceId)
                .statusCode(200)
                .body("invoiceNumber", equalTo("INV-1001"))
                .body("total", equalTo(363F));
    }

    @Test
    @DisplayName("Reject invoice line with time entry from non-billable project")
    void rejectNonBillableTimeEntryOnInvoice(PledgerContext context, PledgerRequests requests) {
        context.withUser("invoice-billable@account.local");
        requests.authenticate("invoice-billable@account.local");

        var clientId =
                requests.createClient(Map.of("name", "Client B", "email", "b@client.test"))
                        .statusCode(201)
                        .extract()
                        .jsonPath()
                        .getLong("id");

        var billableProjectId =
                requests.createProject(
                                Map.of("name", "Paid work", "clientId", clientId, "billable", true))
                        .statusCode(201)
                        .extract()
                        .jsonPath()
                        .getLong("id");

        var internalProjectId =
                requests.createProject(
                                Map.of("name", "Internal", "clientId", clientId, "billable", false))
                        .statusCode(201)
                        .extract()
                        .jsonPath()
                        .getLong("id");

        var day = LocalDate.now();
        requests.createTimeEntry(
                        Map.of(
                                "projectId", internalProjectId,
                                "date", day.toString(),
                                "hours", 4D,
                                "description", "Admin"))
                .statusCode(201);

        var timeEntryId =
                requests.findTimeEntries(day, day, internalProjectId, false)
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getLong("[0].id");

        var templateId =
                requests.createInvoiceTemplate(Map.of("name", "T2")).statusCode(201).extract().jsonPath().getLong("id");

        var taxBracketId =
                requests.createTaxBracket(Map.of("name", "Zero", "rate", 0D)).statusCode(201).extract().jsonPath().getLong("id");

        var invoiceId =
                requests.createInvoice(
                                Map.of(
                                        "invoiceNumber", "INV-2002",
                                        "clientId", clientId,
                                        "invoiceDate", day.toString(),
                                        "dueDate", day.plusDays(7).toString(),
                                        "templateId", templateId))
                        .statusCode(201)
                        .extract()
                        .jsonPath()
                        .getLong("id");

        requests.addInvoiceLine(
                        invoiceId,
                        Map.of(
                                "description", "From internal",
                                "quantity", 4D,
                                "unit", "hours",
                                "unitPrice", 50D,
                                "taxBracketId", taxBracketId,
                                "timeEntryIds", List.of(timeEntryId)))
                .statusCode(400)
                .body("message", equalTo("Time entries from non-billable projects cannot be invoiced."));

        requests.createTimeEntry(
                        Map.of(
                                "projectId", billableProjectId,
                                "date", day.toString(),
                                "hours", 2D,
                                "description", "Dev"))
                .statusCode(201);

        var billableEntryId =
                requests.findTimeEntries(day, day, billableProjectId, false)
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getLong("[0].id");

        requests.addInvoiceLine(
                        invoiceId,
                        Map.of(
                                "description", "Dev work",
                                "quantity", 2D,
                                "unit", "hours",
                                "unitPrice", 100D,
                                "taxBracketId", taxBracketId,
                                "timeEntryIds", List.of(billableEntryId)))
                .statusCode(201)
                .body("lines", hasSize(1));
    }

    @Test
    @DisplayName("Fetch non-existing invoice")
    void fetchMissingInvoice(PledgerContext context, PledgerRequests requests) {
        context.withUser("invoice-missing@account.local");
        requests.authenticate("invoice-missing@account.local");

        requests.fetchInvoice(9_999_999L)
                .statusCode(404)
                .body("message", equalTo("Invoice is not found"));
    }
}
