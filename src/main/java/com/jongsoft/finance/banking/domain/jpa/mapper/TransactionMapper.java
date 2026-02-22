package com.jongsoft.finance.banking.domain.jpa.mapper;

import com.jongsoft.finance.banking.adapter.api.LinkableProvider;
import com.jongsoft.finance.banking.domain.jpa.entity.TagJpa;
import com.jongsoft.finance.banking.domain.jpa.entity.TransactionJournal;
import com.jongsoft.finance.banking.domain.jpa.entity.TransactionJpa;
import com.jongsoft.finance.banking.domain.jpa.entity.TransactionMetaJpa;
import com.jongsoft.finance.banking.domain.model.Classifier;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.lang.API;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
public abstract class TransactionMapper {

    private final List<LinkableProvider<? extends Classifier>> metadataProviders;

    protected TransactionMapper(List<LinkableProvider<? extends Classifier>> metadataProviders) {
        this.metadataProviders = metadataProviders;
    }

    @Mapper.Mapping(to = "currency", from = "#{transactionJournal.currency.code}")
    @Mapper.Mapping(to = "metadata", from = "#{this.convertMetaData(transactionJournal.metadata)}")
    @Mapper.Mapping(to = "transactions", from = "#{this.toDomain(transactionJournal.transactions)}")
    @Mapper.Mapping(to = "deleted", from = "#{transactionJournal.deleted != null}")
    @Mapper.Mapping(to = "tags", from = "#{this.convertTags(transactionJournal.tags)}")
    public abstract Transaction toDomain(TransactionJournal transactionJournal);

    public abstract Transaction.Part toDomain(TransactionJpa part);

    public List<Transaction.Part> toDomain(Set<TransactionJpa> parts) {
        return parts.stream().map(this::toDomain).collect(Collectors.toList());
    }

    public Set<String> convertTags(Collection<TagJpa> tags) {
        if (tags == null) return Collections.emptySet();
        return tags.stream().map(TagJpa::getName).collect(Collectors.toSet());
    }

    public Map<String, Classifier> convertMetaData(Set<TransactionMetaJpa> metaDataSet) {
        Map<String, Classifier> converted = new HashMap<>();

        metaDataSet.stream()
                .map(metadata -> metadataProviders.stream()
                        .filter(provider -> provider.typeOf().equals(metadata.getRelationType()))
                        .findFirst()
                        .map(provider -> API.Tuple(
                                metadata.getRelationType(),
                                provider.lookup(metadata.getEntityId())))
                        .orElse(null))
                .filter(Objects::nonNull)
                .forEach(pair -> converted.put(pair.getFirst(), pair.getSecond().get()));

        return converted;
    }
}
