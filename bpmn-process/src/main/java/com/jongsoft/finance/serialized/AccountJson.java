package com.jongsoft.finance.serialized;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.schedule.Periodicity;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.jsonschema.JsonSchema;
import io.micronaut.serde.annotation.Serdeable;
import java.io.Serializable;
import java.util.function.Supplier;
import lombok.Builder;
import lombok.Data;
import org.bouncycastle.util.encoders.Hex;

@Data
@Builder
@Serdeable
@JsonSchema(
    uri = "/account",
    title = "Account",
    description = "A account is a financial account that can be used to record transactions.")
public class AccountJson implements Serializable {

  /** The name of the account. */
  @NonNull private String name;

  /** The description of the account. */
  private String description;

  /** The currency of the account, in a 3-letter ISO currency code. */
  @NonNull private String currency;

  /** The icon of the account, in a base64 encoded string. */
  private String icon;

  private double interest;
  private Periodicity periodicity;

  private String iban;
  private String bic;
  private String number;

  /** The type of the account. */
  @NonNull private String type;

  public static AccountJson fromDomain(Account account, Supplier<byte[]> iconSupplier) {
    var builder =
        AccountJson.builder()
            .bic(account.getBic())
            .currency(account.getCurrency())
            .description(account.getDescription())
            .iban(account.getIban())
            .number(account.getNumber())
            .type(account.getType())
            .name(account.getName())
            .periodicity(account.getInterestPeriodicity())
            .interest(account.getInterest());

    if (account.getImageFileToken() != null) {
      builder.icon(Hex.toHexString(iconSupplier.get()));
    }

    return builder.build();
  }
}
