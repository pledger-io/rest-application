package com.jongsoft.finance.domain.user;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Role implements Serializable {

  private String name;
}
