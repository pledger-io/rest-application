package com.jongsoft.finance.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class Role implements Serializable {

    private String name;

}
