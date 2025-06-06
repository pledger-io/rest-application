package com.jongsoft.finance.core;

public interface Process<T, Y> {

  T run(Y unitOfWork);
}
