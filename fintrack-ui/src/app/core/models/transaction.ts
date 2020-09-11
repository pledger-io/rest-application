import {AccountRef} from "./account-ref";

interface TransactionDates {
  transaction: string
  booked: string
  interest: string
}

interface TransactionMeta {
  category: string
  budget: string
  contract: any
  failureCode: string
  tags?: string[]
}

interface TransactionSplit {
  description: string
  amount: number
}

class TransactionType {
  constructor(public code: string,
              public _class: string) {
  }

  get class() {
    return this._class;
  }
}

export class Transaction {
  constructor(public id: number,
              public amount: number,
              public description: string,
              public currency: string,
              public type: TransactionType,
              public dates: TransactionDates,
              public destination: AccountRef,
              public source: AccountRef,
              public metadata: TransactionMeta,
              public split: TransactionSplit[]) {
  }

  isSplit(): boolean {
    return this.split != null;
  }

  oppositeAccount(ref: AccountRef): AccountRef {
    if (this.destination.id == ref.id) {
      return this.source;
    }

    return this.destination;
  }

  amountFor(ref: AccountRef): number {
    if (this.destination.id == ref.id) {
      return this.amount;
    }
    return -this.amount;
  }

  isSystemCreated(): boolean {
    return !this.source.isSystem() && !this.destination.isSystem();
  }

  hasMetadata(): boolean {
    return this.metadata.tags.length > 0
      || this.metadata.budget != null
      || this.metadata.category != null
      || this.metadata.contract != null
  }

  static fromRemote(t: Transaction) {
    return new Transaction(
      t.id, t.amount, t.description, t.currency, t.type, t.dates,
      new AccountRef(t.destination.id, t.destination.type, t.destination.name),
      new AccountRef(t.source.id, t.source.type, t.source.name),
      t.metadata, t.split
    )
  }
}
