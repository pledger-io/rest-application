package com.jongsoft.finance.domain.importer;

import com.jongsoft.finance.annotation.Aggregate;
import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.messaging.commands.importer.CompleteImportJobCommand;
import com.jongsoft.finance.messaging.commands.importer.CreateImportJobCommand;
import com.jongsoft.finance.messaging.commands.importer.DeleteImportJobCommand;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@Aggregate
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class BatchImport implements AggregateBase {

  private Long id;
  private Date created;
  private Date finished;

  private String slug;
  private String fileCode;

  private transient BatchImportConfig config;
  private transient UserAccount user;

  private double totalIncome;
  private double totalExpense;

  @BusinessMethod
  public BatchImport(BatchImportConfig config, UserAccount user, String fileCode) {
    this.user = user;
    this.slug = UUID.randomUUID().toString();
    this.config = config;
    this.fileCode = fileCode;

    CreateImportJobCommand.importJobCreated(config.getId(), slug, fileCode);
  }

  public void archive() {
    if (this.finished != null) {
      throw StatusException.badRequest("Cannot archive an import job that has finished running.");
    }

    DeleteImportJobCommand.importJobDeleted(id);
  }

  @BusinessMethod
  public void finish(Date date) {
    if (this.finished != null) {
      throw StatusException.badRequest("Cannot finish an import which has already completed.");
    }

    this.finished = date;
    CompleteImportJobCommand.importJobCompleted(id);
  }
}
