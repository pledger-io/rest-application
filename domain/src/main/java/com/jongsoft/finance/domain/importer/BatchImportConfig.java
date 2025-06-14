package com.jongsoft.finance.domain.importer;

import com.jongsoft.finance.annotation.Aggregate;
import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.messaging.commands.importer.CreateConfigurationCommand;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Aggregate
@AllArgsConstructor
public class BatchImportConfig implements AggregateBase, Serializable {

  private Long id;

  private String name;
  private String fileCode;
  private String type;

  private transient UserAccount user;

  @BusinessMethod
  public BatchImportConfig(UserAccount user, String type, String name, String fileCode) {
    this.user = user;
    this.name = name;
    this.fileCode = fileCode;
    this.type = type;

    CreateConfigurationCommand.configurationCreated(type, name, fileCode);
  }

  public BatchImport createImport(String fileCode) {
    return new BatchImport(this, this.user, fileCode);
  }
}
