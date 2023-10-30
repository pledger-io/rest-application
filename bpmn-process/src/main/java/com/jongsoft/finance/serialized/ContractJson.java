package com.jongsoft.finance.serialized;

import com.jongsoft.finance.domain.account.Contract;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Data;
import org.bouncycastle.util.encoders.Hex;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.function.Supplier;

@Data
@Builder
@Serdeable
public class ContractJson implements Serializable {

    private String name;
    private String description;
    private String company;
    private String contract;
    private boolean terminated;
    private LocalDate start;
    private LocalDate end;

    public static ContractJson fromDomain(Contract contract, Supplier<byte[]> attachmentSupplier) {
        ContractJsonBuilder builder = ContractJson.builder()
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
