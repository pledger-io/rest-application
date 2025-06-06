package com.jongsoft.finance.jpa.tag;

import com.jongsoft.finance.jpa.query.JpaFilterBuilder;
import com.jongsoft.finance.providers.TagProvider;

public class TagFilterCommand extends JpaFilterBuilder<TagJpa>
    implements TagProvider.FilterCommand {

  public TagFilterCommand() {
    query().fieldEq("archived", false);
    orderAscending = true;
    orderBy = "name";
  }

  @Override
  public TagFilterCommand name(String value, boolean exact) {
    if (exact) {
      query().fieldEq("name", value);
    } else {
      query().fieldLike("name", value);
    }

    return this;
  }

  @Override
  public TagFilterCommand page(int page, int pageSize) {
    limitRows = pageSize;
    skipRows = page * pageSize;
    return this;
  }

  @Override
  public void user(String username) {
    query().fieldEq("user.username", username);
  }

  @Override
  public Class<TagJpa> entityType() {
    return TagJpa.class;
  }
}
