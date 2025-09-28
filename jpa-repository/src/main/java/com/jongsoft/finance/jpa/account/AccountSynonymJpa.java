package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "account_synonym")
public class AccountSynonymJpa extends EntityJpa {

  private String synonym;

  @ManyToOne private AccountJpa account;

  @Builder
  public AccountSynonymJpa(String synonym, AccountJpa account) {
    this.synonym = synonym;
    this.account = account;
  }

  public AccountSynonymJpa() {}
}
