package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.bpmn.camunda.ApplicationContextElResolver;
import com.jongsoft.finance.bpmn.camunda.CamundaDatasourceConfiguration;
import com.jongsoft.finance.bpmn.camunda.MicronautExpressionManager;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.List;

@Factory
public class ProcessEngineConfiguration {

    private final Logger log;
    private final ApplicationContext applicationContext;
    private final CamundaDatasourceConfiguration camundaDatasourceConfiguration;

    public ProcessEngineConfiguration(final ApplicationContext applicationContext,
            final CamundaDatasourceConfiguration camundaDatasourceConfiguration) {
        this.applicationContext = applicationContext;
        this.camundaDatasourceConfiguration = camundaDatasourceConfiguration;
        this.log = LoggerFactory.getLogger(getClass());
    }

    @Context
    public ProcessEngine processEngine() throws IOException {
        var configuration = new StandaloneProcessEngineConfiguration();

        configuration.setHistory(org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_FULL)
                .setJobExecutorActivate(true)
                .setMetricsEnabled(false)
                .setJdbcDriver(camundaDatasourceConfiguration.getDriverClassName())
                .setJdbcUrl(camundaDatasourceConfiguration.getUrl())
                .setJdbcUsername(camundaDatasourceConfiguration.getUsername())
                .setJdbcPassword(camundaDatasourceConfiguration.getPassword())
                .setDatabaseSchemaUpdate(camundaDatasourceConfiguration.getAutoUpdate())
                .setProcessEngineName("fintrack")
                .setHistoryCleanupEnabled(true)
                .setExpressionManager(new MicronautExpressionManager(new ApplicationContextElResolver(applicationContext)));

        configuration.setHistoryCleanupBatchSize(250);
        configuration.setHistoryCleanupBatchWindowStartTime("01:00");
        configuration.setHistoryCleanupBatchWindowEndTime("03:00");

        var processEngine = configuration.buildProcessEngine();
        log.info("Created camunda process engine");

        deployResources(processEngine);
        return processEngine;
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
