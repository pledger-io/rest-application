package com.jongsoft.finance.banking.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.banking.adapter.api.TagProvider;
import com.jongsoft.finance.core.domain.FilterProvider;

import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("database")
@DisplayName("Database - Tags")
class TagProviderJpaIT extends JpaTestSetup {

    @Inject
    private TagProvider tagProvider;

    @Inject
    private FilterProvider<TagProvider.FilterCommand> filterFactory;

    @BeforeEach
    void setup() {
        loadDataset("sql/clean-up.sql", "sql/base-setup.sql", "sql/transaction/tag-provider.sql");
    }

    @Test
    @DisplayName("Lookup all tags")
    void lookup() {
        var check = tagProvider.lookup();

        Assertions.assertThat(check).hasSize(2);
    }

    @Test
    @DisplayName("Lookup tag by name")
    void lookup_name() {
        Assertions.assertThat(tagProvider.lookup("Nono")).hasSize(0);
        Assertions.assertThat(tagProvider.lookup("Bike")).hasSize(0);

        Assertions.assertThat(tagProvider.lookup("Sample").get().name()).isEqualTo("Sample");
    }

    @Test
    @DisplayName("Lookup tag by search")
    void lookup_search() {
        var check = tagProvider.lookup(filterFactory.create().name("mpl", false));
        Assertions.assertThat(check.content()).hasSize(1);

        var fullMatch = tagProvider.lookup(filterFactory.create().name("Car", true));
        Assertions.assertThat(fullMatch.content()).hasSize(1);
    }
}
