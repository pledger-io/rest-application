package com.jongsoft.finance.rest.importer;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.importer.BatchImport;
import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.providers.CSVConfigProvider;
import com.jongsoft.finance.providers.ImportProvider;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class BatchImportResourceTest extends TestSetup {

    private BatchImportResource subject;

    @Mock
    private ImportProvider importProvider;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private CSVConfigProvider csvConfigProvider;
    @Mock
    private SettingProvider settingProvider;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        subject = new BatchImportResource(currentUserProvider, csvConfigProvider, importProvider, settingProvider);

        Mockito.when(currentUserProvider.currentUser()).thenReturn(ACTIVE_USER);
        new EventBus(applicationEventPublisher);
    }

    @Test
    void list() {
        var resultPage = ResultPage.of(BatchImport.builder()
                .id(1L)
                .created(DateUtils.toDate(LocalDate.of(2019, 1, 1)))
                .slug("batch-import-slug")
                .fileCode("sample big content")
                .build());
        Mockito.when(importProvider.lookup(Mockito.any(ImportProvider.FilterCommand.class))).thenReturn(resultPage);

        var response = subject.list(new ImportSearchRequest(0));

        assertThat(response.getInfo().getRecords()).isEqualTo(1L);
        assertThat(response.getContent().get(0).getSlug()).isEqualTo("batch-import-slug");
    }

    @Test
    void create() {
        var mockConfig = Mockito.mock(BatchImportConfig.class);

        Mockito.when(csvConfigProvider.lookup("sample-configuration")).thenReturn(Control.Option(mockConfig));
        Mockito.when(mockConfig.createImport("token-sample")).thenReturn(
                BatchImport.builder()
                        .created(DateUtils.toDate(LocalDate.of(2019, 2, 1)))
                        .fileCode("token-sample")
                        .slug("xd2rsd-2fasd-q2ff-asd")
                        .build());

        var request = ImporterCreateRequest.builder()
                .configuration("sample-configuration")
                .uploadToken("token-sample")
                .build();

        Assertions.assertThat(subject.create(request))
                .isNotNull()
                .hasFieldOrPropertyWithValue("slug", "xd2rsd-2fasd-q2ff-asd");

        Mockito.verify(mockConfig).createImport("token-sample");
    }

    @Test
    void get() {
        Mockito.when(importProvider.lookup("xd2rsd-2fasd-q2ff-asd")).thenReturn(
                Control.Option(BatchImport.builder()
                        .created(DateUtils.toDate(LocalDate.of(2019, 2, 1)))
                        .fileCode("token-sample")
                        .slug("xd2rsd-2fasd-q2ff-asd")
                        .config(BatchImportConfig.builder()
                                .id(1L)
                                .fileCode("xd2rsd-2fasd-33dfd-ddfa")
                                .name("sample-config.json")
                                .build())
                        .finished(DateUtils.toDate(LocalDate.of(2019, 2, 2)))
                        .totalExpense(200.2D)
                        .totalIncome(303.40D)
                        .build()));

        Assertions.assertThat(subject.get("xd2rsd-2fasd-q2ff-asd"))
                .isNotNull()
                .hasFieldOrPropertyWithValue("slug", "xd2rsd-2fasd-q2ff-asd")
                .hasFieldOrPropertyWithValue("config.file", "xd2rsd-2fasd-33dfd-ddfa")
                .hasFieldOrPropertyWithValue("config.name", "sample-config.json")
                .hasFieldOrPropertyWithValue("balance.totalExpense", 200.2D)
                .hasFieldOrPropertyWithValue("balance.totalIncome", 303.4D);
    }

    @Test
    void delete_success() {
        Mockito.when(importProvider.lookup("xd2rsd-2fasd-q2ff-asd")).thenReturn(
                Control.Option(BatchImport.builder()
                        .id(1L)
                        .created(DateUtils.toDate(LocalDate.of(2019, 2, 1)))
                        .fileCode("token-sample")
                        .slug("xd2rsd-2fasd-q2ff-asd")
                        .config(BatchImportConfig.builder()
                                .id(1L)
                                .fileCode("xd2rsd-2fasd-33dfd-ddfa")
                                .name("sample-config.json")
                                .build())
                        .totalExpense(200.2D)
                        .totalIncome(303.40D)
                        .build()));

        Assertions.assertThat(subject.delete("xd2rsd-2fasd-q2ff-asd"))
                .isEqualTo("xd2rsd-2fasd-q2ff-asd");
    }

    @Test
    void delete_alreadyFinished() {
        Mockito.when(importProvider.lookup("xd2rsd-2fasd-q2ff-asd")).thenReturn(
                Control.Option(BatchImport.builder()
                        .created(DateUtils.toDate(LocalDate.of(2019, 2, 1)))
                        .fileCode("token-sample")
                        .slug("xd2rsd-2fasd-q2ff-asd")
                        .config(BatchImportConfig.builder()
                                .id(1L)
                                .fileCode("xd2rsd-2fasd-33dfd-ddfa")
                                .name("sample-config.json")
                                .build())
                        .finished(new Date())
                        .totalExpense(200.2D)
                        .totalIncome(303.40D)
                        .build()));

        Assertions.assertThatThrownBy(() -> subject.delete("xd2rsd-2fasd-q2ff-asd"))
                .isInstanceOf(StatusException.class)
                .hasMessage("Cannot archive an import job that has finished running.");
    }

    @Test
    void config() {
        Mockito.when(csvConfigProvider.lookup()).thenReturn(Collections.List(
                BatchImportConfig.builder()
                        .id(1L)
                        .name("Import config test")
                        .build()));

        Assertions.assertThat(subject.config())
                .isNotNull()
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("name", "Import config test");
    }

    @Test
    void createConfig() {
        Mockito.when(csvConfigProvider.lookup("sample-configuration")).thenReturn(Control.Option());

        var request = CSVImporterConfigCreateRequest.builder()
                .name("sample-configuration")
                .fileCode("token-sample")
                .build();

        Assertions.assertThat(subject.createConfig(request))
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "sample-configuration")
                .hasFieldOrPropertyWithValue("file", "token-sample");
    }
}
