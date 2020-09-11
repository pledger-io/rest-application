export enum EntityType {
  BUDGET, DEBIT_ACCOUNT, CREDIT_ACCOUNT, OWN_ACCOUNT, CATEGORY, CONTRACT, TAG
}

export class EntityRef {
  constructor(public id: number,
              public name: string) {
  }
}
