package com.jongsoft.finance.serialized;

import com.jongsoft.finance.domain.account.Contract;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.jsonschema.JsonSchema;
import io.micronaut.serde.annotation.Serdeable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.function.Supplier;
import lombok.Builder;
import lombok.Data;
import org.bouncycastle.util.encoders.Hex;

@Data
@Builder
@Serdeable
@JsonSchema(title = "Contract", description = "Contract details", uri = "/contract")
public class ContractJson implements Serializable {

  @NonNull private String name;
  private String description;
  @NonNull private String company;

  /** The contract attachment as a hex string. */
  private String contract;

  private boolean terminated;
  @NonNull private LocalDate start;
  @NonNull private LocalDate end;

  public static ContractJson fromDomain(Contract contract, Supplier<byte[]> attachmentSupplier) {
    ContractJsonBuilder builder =
        ContractJson.builder()
            .name(contract.getName())
            .description(contract.getDescription())
            .company(contract.getCompany().getName())
            .start(contract.getStartDate())
            .end(contract.getEndDate())
            .terminated(contract.isTerminated());

    if (contract.isUploaded()) {
      builder.contract(Hex.toHexString(attachmentSupplier.get()));
    }

    return builder.build();
  }
}
