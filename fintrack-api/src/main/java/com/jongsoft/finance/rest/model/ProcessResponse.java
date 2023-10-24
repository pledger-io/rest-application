package com.jongsoft.finance.rest.model;

import io.micronaut.serde.annotation.Serdeable;
import org.camunda.bpm.engine.history.HistoricProcessInstance;

import java.time.Duration;
import java.util.Date;

@Serdeable.Serializable
public class ProcessResponse {

    private final HistoricProcessInstance wrapped;

    public ProcessResponse(HistoricProcessInstance wrapped) {
        this.wrapped = wrapped;
    }

    public String getId() {
        return wrapped.getId();
    }

    public String getProcess() {
        return wrapped.getProcessDefinitionKey();
    }

    public String getBusinessKey() {
        return wrapped.getBusinessKey();
    }

    public Integer getVersion() {
        return wrapped.getProcessDefinitionVersion();
    }

    public String getState() {
        return wrapped.getState();
    }

    public Dates getDates() {
        return new Dates();
    }

    public class Dates {

        public Date getStart() {
            return wrapped.getStartTime();
        }

        public Date getEnd() {
            return wrapped.getEndTime();
        }

        public Duration getDuration() {
            if (wrapped.getDurationInMillis() != null) {
                return Duration.ofMillis(wrapped.getDurationInMillis());
            }

            return null;
        }
    }
}
