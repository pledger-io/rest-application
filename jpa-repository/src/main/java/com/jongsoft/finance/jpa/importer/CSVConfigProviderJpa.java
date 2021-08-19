package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.jpa.importer.entity.CSVImportConfig;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.providers.CSVConfigProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.control.Optional;
import io.reactivex.Flowable;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Singleton
public class CSVConfigProviderJpa implements CSVConfigProvider {

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    public CSVConfigProviderJpa(ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    public Optional<BatchImportConfig> lookup(String name) {
        log.trace("CSVConfiguration lookup by name {}", name);

        var hql = """
                select b from CSVImportConfig b 
                where b.name = :name
                    and b.user.username = :username
                """;

        return entityManager.<CSVImportConfig>blocking()
                .hql(hql)
                .set("name", name)
                .set("username", authenticationFacade.authenticated())
                .maybe()
                .map(this::convert);
    }

    @Override
    public Flux<BatchImportConfig> lookup() {
        log.trace("CSVConfiguration listing");

        var hql = """
                select b from CSVImportConfig b
                where b.user.username = :username""";

        return entityManager.<CSVImportConfig>reactive()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .flow()
                .map(this::convert);
    }

    private BatchImportConfig convert(CSVImportConfig source) {
        if (source == null) {
            return null;
        }

        return BatchImportConfig.builder()
                .id(source.getId())
                .name(source.getName())
                .fileCode(source.getFileCode())
                .user(UserAccount.builder()
                        .id(source.getUser().getId())
                        .username(source.getUser().getUsername())
                        .build())
                .build();
    }

}
