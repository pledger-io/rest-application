package com.jongsoft.finance.serialized;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.function.Supplier;

import org.bouncycastle.util.encoders.Hex;
import com.jongsoft.finance.domain.account.Contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractJson implements Serializable {

    private String name;
    private String description;
    private String company;
    private String contract;
    private boolean terminated;
    private LocalDate start;
    private LocalDate end;

    public static ContractJson fromDomain(Contract contract, Supplier<byte[]> attachementSupplier) {
        ContractJsonBuilder builder = ContractJson.builder()
                .name(contract.getName())
                .description(contract.getDescription())
                .company(contract.getCompany().getName())
                .start(contract.getStartDate())
                .end(contract.getEndDate())
                .terminated(contract.isTerminated());

        if (contract.isUploaded()) {
            builder.contract(Hex.toHexString(attachementSupplier.get()));
        }

        return builder.build();
    }

}
