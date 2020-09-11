import {EntityRef} from "./entity";

export class AccountRef extends EntityRef {
  constructor(id: number,
              public type: string,
              name: string) {
    super(id, name);
  }

  isOwn(): boolean {
    return this.type != 'debtor'
      && this.type != 'creditor'
      && this.type != 'reconcile'
      && this.type != 'debt'
      && this.type != 'loan'
      && this.type != 'mortgage'
  }

  isSystem(): boolean {
    return this.type == 'reconcile';
  }

  get frontEndType(): string {
    return this.type == 'debtor'
      ? 'revenue'
      : (this.type == 'creditor' ? 'expense' : 'own');
  }

  get path(): string {
    const LIABILITY_TYPES = ['loan', 'debt', 'mortgage'];

    let type = 'own';
    if (LIABILITY_TYPES.indexOf(this.type) > -1) {
      type = 'liability';
    } else if ('debtor' == this.type) {
      type = 'revenue';
    } else if ('creditor' == this.type) {
      type = 'expense';
    }

    return '/accounts/' + type + '/' + this.id;
  }

}
