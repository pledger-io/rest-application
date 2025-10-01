package com.jongsoft.finance.rest.contract;

import com.jongsoft.finance.rest.model.ContractResponse;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable.Serializable
record ContractOverviewResponse(List<ContractResponse> active, List<ContractResponse> terminated) {}
