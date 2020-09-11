export class BatchImport {
  constructor(public slug: string,
              public created: string,
              public finished: string,
              public balance: BatchImportBalance,
              public config: BatchConfig) {
  }
}

class BatchImportBalance {
  constructor(public income: number,
              public expense: number) {
  }
}

class CustomIndicator {
  constructor(public deposit: string,
              public credit: string) {
  }
}

export class ImportConfiguration {
  constructor(public hasHeader: boolean,
              public applyRules: boolean,
              public generateAccounts: boolean,
              public dateFormat: string,
              public delimiter: string,
              public accountId: number,
              public columnRoles: string[],
              public customIndicator: CustomIndicator) {
  }

  public static toServer(json: string) {
    let read = JSON.parse(json) as ImportConfiguration;

    return {
      'has-headers': read.hasHeader,
      'apply-rules': read.applyRules,
      'generate-accounts': read.generateAccounts,
      'date-format': read.dateFormat,
      'delimiter': read.delimiter,
      'accountId': read.accountId,
      'column-roles': read.columnRoles,
      'custom-indicator': read.customIndicator
    }
  }
}

export class BatchConfig {
  constructor(public name: string,
              public id: number,
              public file: string) {
  }
}

export class CreateBatchImportRequest {
  constructor(public configuration: string,
              public uploadToken: string) {
  }
}

export class CreateBatchConfigRequest {
  constructor(public name: string,
              public fileCode: string) {
  }
}
