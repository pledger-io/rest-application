package com.jongsoft.finance.banking.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.banking.adapter.api.AccountProvider;
import com.jongsoft.finance.banking.adapter.api.Reconcile;
import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.domain.model.Account;
import com.jongsoft.finance.banking.domain.model.AccountReconciliation;
import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.banking.types.SystemAccountTypes;
import com.jongsoft.finance.core.adapter.api.StorageService;
import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Dates;

import jakarta.inject.Singleton;

import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Singleton
class ReconciliationSystem implements Reconcile {

    private final Logger log = org.slf4j.LoggerFactory.getLogger(ReconciliationSystem.class);

    static class ReconcileStore extends HashMap<Long, List<AccountReconciliation>> {}

    private final AccountProvider accountProvider;
    private final TransactionProvider transactionProvider;
    private final FilterProvider<TransactionProvider.FilterCommand> filterFactory;
    private final StorageService storageService;
    private final AuthenticationFacade authenticationFacade;

    private final ObjectMapper objectMapper = new JsonMapper();

    ReconciliationSystem(
            AccountProvider accountProvider,
            TransactionProvider transactionProvider,
            FilterProvider<TransactionProvider.FilterCommand> filterFactory,
            StorageService storageService,
            AuthenticationFacade authenticationFacade) {
        this.accountProvider = accountProvider;
        this.transactionProvider = transactionProvider;
        this.filterFactory = filterFactory;
        this.storageService = storageService;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    public Optional<AccountReconciliation> reconcile(
            Long accountId, int year, double startBalance, double endBalance) {
        var existing = fetchAccountsReconcile(accountId);
        if (existing.stream().anyMatch(r -> r.year() == year)) {
            throw StatusException.badRequest("Reconciliation already exists for year " + year);
        }

        Optional<AccountReconciliation> result =
                computeReconcile(existing, accountId, year, startBalance, endBalance);
        if (result.isEmpty()) {
            var nextYear = existing.stream().filter(r -> r.year() == year + 1).findFirst();
            while (nextYear.isPresent()) {
                var reconciliation = nextYear.get();
                log.debug("Automatic reconciliation retry for year {}", reconciliation.year());
                if (computeReconcile(
                                existing,
                                accountId,
                                reconciliation.year(),
                                reconciliation.startBalance(),
                                reconciliation.endBalance())
                        .isPresent()) {
                    break;
                }
                existing.remove(reconciliation);
                nextYear = existing.stream()
                        .filter(r -> r.year() == reconciliation.year() + 1)
                        .findFirst();
            }
            storeReconciliation(accountId, existing);
        }
        return result;
    }

    @Override
    public List<AccountReconciliation> fetchAccountsReconcile(Long accountId) {
        if (Files.exists(determineStoragePath())) {
            try {
                byte[] content = storageService
                        .read(Hex.toHexString(
                                authenticationFacade.authenticated().getBytes()))
                        .get();
                return objectMapper
                        .readValue(content, ReconcileStore.class)
                        .computeIfAbsent(accountId, k -> new ArrayList<>());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return new ArrayList<>();
    }

    private Optional<AccountReconciliation> computeReconcile(
            List<AccountReconciliation> existing,
            long accountId,
            int year,
            double startBalance,
            double endBalance) {
        var computedStart = computeStartBalance(accountId, year);
        if (computedStart != startBalance) {
            AccountReconciliation reconciliation =
                    new AccountReconciliation(year, startBalance, endBalance, computedStart);
            existing.add(reconciliation);
            storeReconciliation(accountId, existing);
            return Optional.of(reconciliation);
        }

        var computedEnd = computeEndBalance(accountId, year);
        if (computedEnd != endBalance) {
            reconcileYear(accountId, year, endBalance - computedEnd);
        }
        return Optional.empty();
    }

    private void reconcileYear(long accountId, int year, double amount) {
        Account reconcileAccount = accountProvider
                .lookup(SystemAccountTypes.RECONCILE)
                .getOrThrow(() -> StatusException.internalError("Reconcile account not found"));
        Account account = accountProvider
                .lookup(accountId)
                .getOrThrow(() -> StatusException.internalError("Account not found " + accountId));

        if (amount > 0) {
            Transaction.create(
                            reconcileAccount,
                            account,
                            LocalDate.of(year, 12, 31),
                            Math.abs(amount),
                            "Reconcile transaction")
                    .register();
        } else {
            Transaction.create(
                            account,
                            reconcileAccount,
                            LocalDate.of(year, 12, 31),
                            Math.abs(amount),
                            "Reconcile transaction")
                    .register();
        }
    }

    private double computeStartBalance(Long accountId, int year) {
        var filter = filterFactory
                .create()
                .accounts(Collections.List(new EntityRef(accountId)))
                .range(Dates.range(LocalDate.of(1900, 1, 1), LocalDate.of(year, 1, 1)));
        var computed = transactionProvider.balance(filter).getOrSupply(() -> BigDecimal.ZERO);
        return computed.doubleValue();
    }

    private double computeEndBalance(Long accountId, int year) {
        var filter = filterFactory
                .create()
                .accounts(Collections.List(new EntityRef(accountId)))
                .range(Dates.range(LocalDate.of(1900, 1, 1), LocalDate.of(year, 12, 31)));
        var computed = transactionProvider.balance(filter).getOrSupply(() -> BigDecimal.ZERO);
        return computed.doubleValue();
    }

    private void storeReconciliation(long accountId, List<AccountReconciliation> reconciliation) {
        try {
            ReconcileStore store;
            if (Files.exists(determineStoragePath())) {
                byte[] content = storageService
                        .read(Hex.toHexString(
                                authenticationFacade.authenticated().getBytes()))
                        .get();
                store = objectMapper.readValue(content, ReconcileStore.class);
            } else {
                store = new ReconcileStore();
            }

            store.put(accountId, reconciliation);
            String token = storageService.store(objectMapper.writeValueAsBytes(store));
            Files.move(
                    storageService.getUploadPath().resolve(token),
                    determineStoragePath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Path determineStoragePath() {
        String fileName = Hex.toHexString(authenticationFacade.authenticated().getBytes());
        return storageService.getUploadPath().resolve(fileName);
    }
}
