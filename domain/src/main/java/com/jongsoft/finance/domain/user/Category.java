package com.jongsoft.finance.domain.user;

import com.jongsoft.finance.annotation.Aggregate;
import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.messaging.commands.category.CreateCategoryCommand;
import com.jongsoft.finance.messaging.commands.category.DeleteCategoryCommand;
import com.jongsoft.finance.messaging.commands.category.RenameCategoryCommand;
import com.jongsoft.lang.Control;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@Aggregate
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Category implements AggregateBase {

  private Long id;
  private String label;
  private String description;

  private LocalDate lastActivity;
  private transient UserAccount user;

  private boolean delete;

  @BusinessMethod
  public Category(UserAccount user, String label) {
    this.user = user;
    this.label = label;
    CreateCategoryCommand.categoryCreated(label, description);
  }

  @BusinessMethod
  public void rename(String label, String description) {
    var hasChanged =
        Control.Equal(this.label, label).append(this.description, description).isNotEqual();

    if (hasChanged) {
      this.label = label;
      this.description = description;
      RenameCategoryCommand.categoryRenamed(id, label, description);
    }
  }

  @BusinessMethod
  public void remove() {
    this.delete = true;
    DeleteCategoryCommand.categoryDeleted(id);
  }

  @Override
  public String toString() {
    return getLabel();
  }
}
