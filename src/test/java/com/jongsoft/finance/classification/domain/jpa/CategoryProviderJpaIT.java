package com.jongsoft.finance.classification.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.classification.adapter.api.CategoryProvider;
import com.jongsoft.finance.core.domain.FilterProvider;

import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Database - Categories")
class CategoryProviderJpaIT extends JpaTestSetup {

    @Inject
    private CategoryProviderJpa categoryProviderJpa;

    @Inject
    private FilterProvider<CategoryProvider.FilterCommand> filterFactory;

    @BeforeEach
    void setUp() {
        loadDataset("sql/clean-up.sql", "sql/base-setup.sql", "sql/user/category-provider.sql");
    }

    @Test
    @DisplayName("Lookup all categories")
    void lookup() {
        var response = categoryProviderJpa.lookup();

        Assertions.assertThat(response).hasSize(2);
    }

    @Test
    @DisplayName("Lookup category by id")
    void lookup_byId() {
        var response = categoryProviderJpa.lookup(1L);

        Assertions.assertThat(response.isPresent()).isTrue();
        Assertions.assertThat(response.get().getId()).isEqualTo(1L);
        Assertions.assertThat(response.get().getLabel()).isEqualTo("Grocery");
    }

    @Test
    @DisplayName("Lookup category by label")
    void lookup_byLabel() {
        var response = categoryProviderJpa.lookup("Grocery").get();

        Assertions.assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Lookup categories by partial name")
    void lookup_byFilter() {
        var command = filterFactory.create().label("gro", false);

        var response = categoryProviderJpa.lookup(command);

        Assertions.assertThat(response.total()).isEqualTo(1);
        Assertions.assertThat(response.content().get().getId()).isEqualTo(1L);
    }
}
