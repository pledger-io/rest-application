export class TransactionRuleGroup {
  constructor(public name: string,
              public sort: number) {
  }
}

export class RuleChange {
  constructor(public id: number,
              public field: string,
              public change: string) {
  }
}

export enum RuleOperation {
  EQUALS,
  CONTAINS,
  STARTS_WITH,
  LESS_THAN,
  MORE_THAN
}

export class RuleCondition {
  constructor(public id: number,
              public field: string,
              public operation: string,
              public condition: string) {
  }
}

export class TransactionRule {
  constructor(public id: number,
              public name: string,
              public description: string,
              public active: boolean,
              public restrictive: boolean,
              public changes: RuleChange[],
              public conditions: RuleCondition[]) {
  }

}
