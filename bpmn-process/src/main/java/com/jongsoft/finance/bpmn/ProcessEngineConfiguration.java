package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.ProcessVariable;
import com.jongsoft.finance.bpmn.camunda.*;
import com.jongsoft.finance.core.DataSourceMigration;
import com.jongsoft.finance.importer.api.TransactionDTO;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.serde.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.List;

@Slf4j
@Factory
@Requires(beans = DataSourceMigration.class, notEnv = "no-camunda")
public class ProcessEngineConfiguration {

    private final ApplicationContext applicationContext;
    private final CamundaDatasourceConfiguration camundaDatasourceConfiguration;

    public ProcessEngineConfiguration(
            ApplicationContext applicationContext,
            CamundaDatasourceConfiguration camundaDatasourceConfiguration) {
        this.applicationContext = applicationContext;
        this.camundaDatasourceConfiguration = camundaDatasourceConfiguration;
    }

    @Context
    public ProcessEngine processEngine() throws IOException {
        var configuration = new StandaloneProcessEngineConfiguration();

        configuration.setHistory(camundaDatasourceConfiguration.getHistoryLevel())
                .setJobExecutorActivate(true)
                .setMetricsEnabled(true)
                .setJdbcDriver(camundaDatasourceConfiguration.getDriverClassName())
                .setJdbcUrl(camundaDatasourceConfiguration.getUrl())
                .setJdbcUsername(camundaDatasourceConfiguration.getUsername())
                .setJdbcPassword(camundaDatasourceConfiguration.getPassword())
                .setDatabaseSchemaUpdate(camundaDatasourceConfiguration.getAutoUpdate())
                .setProcessEngineName("fintrack")
                .setHistoryCleanupEnabled(true)
                .setExpressionManager(new MicronautExpressionManager(new MicronautElResolver(applicationContext)));

        configuration.setHistoryCleanupBatchSize(250);
        configuration.setHistoryCleanupBatchWindowStartTime("01:00");
        configuration.setHistoryCleanupBatchWindowEndTime("03:00");
        configuration.setHistoryTimeToLive("P1D");
        configuration.setResolverFactories(List.of(new MicronautBeanResolver(applicationContext)));
        configuration.setCustomPreVariableSerializers(List.of(
                new JsonRecordSerializer<>(applicationContext.getBean(ObjectMapper.class), ProcessVariable.class),
                new JsonRecordSerializer<>(applicationContext.getBean(ObjectMapper.class), TransactionDTO.class)));

        var processEngine = configuration.buildProcessEngine();
        log.info("Created camunda process engine");

        deployResources(processEngine);
        return processEngine;
    }

    @Context
    public HistoryService historyService(ProcessEngine processEngine) {
        return processEngine.getHistoryService();
    }

    @Context
    public TaskService taskService(ProcessEngine processEngine) {
        return processEngine.getTaskService();
    }

    @Context
    public RuntimeService runtimeService(ProcessEngine processEngine) {
        return processEngine.getRuntimeService();
    }

    private void deployResources(ProcessEngine processEngine) throws IOException {
        log.info("Searching for deployable camunda processes");

        PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
        for (String extension : List.of("dmn", "cmmn", "bpmn")) {
            for (Resource resource :
                    resourceLoader.getResources(PathMatchingResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + extension + "/*/*." + extension)) {
                log.info("Deploying model: {}", resource.getFilename());
                processEngine.getRepositoryService().createDeployment()
                        .name("MicronautAutoDeployment")
                        .addInputStream(resource.getFilename(), resource.getInputStream())
                        .enableDuplicateFiltering(true)
                        .deploy();
            }
        }
    }

}
