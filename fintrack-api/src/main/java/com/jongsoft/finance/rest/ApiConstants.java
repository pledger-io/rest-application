package com.jongsoft.finance.rest;

public class ApiConstants {
  public static final String TAG_REACT_APP = "React::App";
  public static final String TAG_ACCOUNTS = "Accounts";
  public static final String TAG_ACCOUNTS_TRANSACTIONS = "Accounts::Transactions";
  public static final String TAG_BUDGETS = "Budgets";
  public static final String TAG_CATEGORIES = "Categories";
  public static final String TAG_CONTRACTS = "Contracts";
  public static final String TAG_ATTACHMENTS = "Attachments";

  public static final String TAG_TRANSACTION = "Transactions";
  public static final String TAG_TRANSACTION_IMPORT = TAG_TRANSACTION + "::Import";
  public static final String TAG_TRANSACTION_ANALYTICS = TAG_TRANSACTION + "::Analytics";
  public static final String TAG_TRANSACTION_TAGGING = TAG_TRANSACTION + "::Tags";

  public static final String TAG_REPORTS = "Reports";

  public static final String TAG_AUTOMATION = "Automation";
  public static final String TAG_AUTOMATION_RULES = TAG_AUTOMATION + "::Rules";
  public static final String TAG_AUTOMATION_PROCESSES = TAG_AUTOMATION + "::Processes";

  public static final String TAG_SETTINGS = "Settings";
  public static final String TAG_SETTINGS_CURRENCIES = TAG_SETTINGS + "::Currencies";
  public static final String TAG_SETTINGS_LOCALIZATION = TAG_SETTINGS_CURRENCIES + "::Locale";

  public static final String TAG_SECURITY = "Security";
  public static final String TAG_SECURITY_USERS = TAG_SECURITY + "::Users";

  private ApiConstants() {}
}
