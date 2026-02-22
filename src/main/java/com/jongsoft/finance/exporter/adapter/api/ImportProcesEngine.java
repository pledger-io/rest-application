package com.jongsoft.finance.exporter.adapter.api;

import com.jongsoft.finance.core.domain.model.ProcessVariable;
import com.jongsoft.finance.exporter.domain.model.UserTask;

import java.util.List;

public interface ImportProcesEngine {

    List<UserTask> getTasksForBatch(String batchSlug);

    void completeTask(String batchSlug, String taskId, ProcessVariable userData);
}
