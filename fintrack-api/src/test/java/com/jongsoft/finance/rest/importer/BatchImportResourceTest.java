package com.jongsoft.finance.rest.importer;

import com.jongsoft.finance.core.date.Dates;
import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.domain.core.SettingProvider;
import com.jongsoft.finance.domain.importer.BatchImport;
import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.domain.importer.CSVConfigProvider;
import com.jongsoft.finance.domain.importer.ImportProvider;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.API;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Date;

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
                .created(Dates.toDate(LocalDate.of(2019, 1, 1)))
                .slug("batch-import-slug")
                .fileCode("sample big content")
                .build());
        Mockito.when(importProvider.lookup(Mockito.any(ImportProvider.FilterCommand.class))).thenReturn(resultPage);

        var response = subject.list(new ImportSearchRequest(0));

        Assertions.assertThat(response.getInfo().getRecords()).isEqualTo(1L);
        Assertions.assertThat(response.getContent().get(0).getSlug()).isEqualTo("batch-import-slug");
    }

    @Test
    void create() {
        var mockConfig = Mockito.mock(BatchImportConfig.class);

        Mockito.when(csvConfigProvider.lookup("sample-configuration")).thenReturn(API.Option(mockConfig));
        Mockito.when(mockConfig.createImport("token-sample")).thenReturn(
                BatchImport.builder()
                        .created(Dates.toDate(LocalDate.of(2019, 2, 1)))
                        .fileCode("token-sample")
                        .slug("xd2rsd-2fasd-q2ff-asd")
                        .build());

        var request = ImporterCreateRequest.builder()
                .configuration("sample-configuration")
                .uploadToken("token-sample")
                .build();
        var response = subject.create(request).blockingGet();

        Assertions.assertThat(response.getSlug()).isEqualTo("xd2rsd-2fasd-q2ff-asd");
        Mockito.verify(mockConfig).createImport("token-sample");
    }

    @Test
    void get() {
        Mockito.when(importProvider.lookup("xd2rsd-2fasd-q2ff-asd")).thenReturn(
                Maybe.just(BatchImport.builder()
                        .created(Dates.toDate(LocalDate.of(2019, 2, 1)))
                        .fileCode("token-sample")
                        .slug("xd2rsd-2fasd-q2ff-asd")
                        .config(BatchImportConfig.builder()
                                .id(1L)
                                .fileCode("xd2rsd-2fasd-33dfd-ddfa")
                                .name("sample-config.json")
                                .build())
                        .finished(Dates.toDate(LocalDate.of(2019, 2, 2)))
                        .totalExpense(200.2D)
                        .totalIncome(303.40D)
                        .build()));

        var response = subject.get("xd2rsd-2fasd-q2ff-asd").blockingGet();

        Assertions.assertThat(response.getSlug()).isEqualTo("xd2rsd-2fasd-q2ff-asd");
        Assertions.assertThat(response.getConfig().getFile()).isEqualTo("xd2rsd-2fasd-33dfd-ddfa");
        Assertions.assertThat(response.getConfig().getName()).isEqualTo("sample-config.json");
        Assertions.assertThat(response.getBalance().getTotalExpense()).isEqualTo(200.2D);
        Assertions.assertThat(response.getBalance().getTotalIncome()).isEqualTo(303.4D);
    }

    @Test
    void delete_success() {
        Mockito.when(importProvider.lookup("xd2rsd-2fasd-q2ff-asd")).thenReturn(
                Maybe.just(BatchImport.builder()
                        .id(1L)
                        .created(Dates.toDate(LocalDate.of(2019, 2, 1)))
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

        subject.delete("xd2rsd-2fasd-q2ff-asd")
                .blockingGet();
    }

    @Test
    void delete_alreadyFinished() {
        Mockito.when(importProvider.lookup("xd2rsd-2fasd-q2ff-asd")).thenReturn(
                Maybe.just(BatchImport.builder()
                        .created(Dates.toDate(LocalDate.of(2019, 2, 1)))
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

        subject.delete("xd2rsd-2fasd-q2ff-asd")
                .test()
                .assertNoValues()
                .assertErrorMessage("Cannot archive an import job that has finished running.");
    }

    @Test
    void config() {
        Mockito.when(csvConfigProvider.lookup()).thenReturn(Flowable.just(
                BatchImportConfig.builder()
                        .id(1L)
                        .name("Import config test")
                        .build()));

        var response = subject.config().test();

        response.assertValueCount(1);
        response.assertComplete();
    }

    @Test
    void createConfig() {
        Mockito.when(csvConfigProvider.lookup("sample-configuration")).thenReturn(API.Option());

        var request = CSVImporterConfigCreateRequest.builder()
                .name("sample-configuration")
                .fileCode("token-sample")
                .build();

        var response = subject.createConfig(request).blockingGet();

        Assertions.assertThat(response.getName()).isEqualTo("sample-configuration");
        Assertions.assertThat(response.getFile()).isEqualTo("token-sample");
    }
}
