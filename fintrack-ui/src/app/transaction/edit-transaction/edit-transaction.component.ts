import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Account, AccountForm, AccountService} from '../../accounts/account.service';
import {NgbDateParserFormatter} from '@ng-bootstrap/ng-bootstrap';
import {CustomDateParserFormatter} from '../../core/custom-date-parser-formatter';
import {AccountRef, EntityRef, EntityType, Transaction} from '../../core/core-models';
import {RouterHistory} from '../../core/router-history';
import {Subscription} from 'rxjs';
import {ToastService} from '../../core/core-services';

class EditTransactionForm {
  constructor(public description: string = null,
              public amount: number = null,
              public currency: string = null,
              public date: string = null,
              public category: EntityRef = null,
              public budget: EntityRef = null,
              public contract: EntityRef = null,
              public source: AccountRef = null,
              public destination: AccountRef = null,
              public tags: string[] = []) {
  }

  static fromTransaction(transaction: Transaction): EditTransactionForm {
    return new EditTransactionForm(
      transaction.description,
      transaction.amount,
      transaction.currency,
      transaction.dates.transaction,
      transaction.metadata.category ? new EntityRef(-1, transaction.metadata.category) : null,
      transaction.metadata.budget ? new EntityRef(-1, transaction.metadata.budget) : null,
      transaction.metadata.contract ? new EntityRef(-1, transaction.metadata.contract) : null,
      transaction.source,
      transaction.destination,
      transaction.metadata.tags
    );
  }

  validate() {
    this.source = typeof this.source === 'string' ? null : this.source;
    this.destination = typeof this.destination === 'string' ? null : this.destination;
    this.budget = typeof this.budget === 'string' ? null : this.budget;
    this.contract = typeof this.contract === 'string' ? null : this.contract;
    this.category = typeof this.category === 'string' ? null : this.category;
  }

  noDestination(): boolean {
    return typeof this.destination === 'string';
  }

  noSource(): boolean {
    return typeof this.source === 'string';
  }
}

class SplitRecord {
  constructor(public description: string, public amount: number) {
  }
}

class TransactionType {
  constructor(private _type: string) {
  }

  get isDebit(): boolean {
    return this._type == 'debit';
  }

  get isCredit(): boolean {
    return this._type == 'credit';
  }

  get isTransfer(): boolean {
    return this._type == 'transfer';
  }
}

@Component({
  selector: 'app-edit-transaction',
  templateUrl: './edit-transaction.component.html',
  styleUrls: ['./edit-transaction.component.scss'],
  providers: [{ provide: NgbDateParserFormatter, useClass: CustomDateParserFormatter }]
})
export class EditTransactionComponent implements OnInit, OnDestroy {

  EntityType = EntityType;

  private _transactionId: number;
  private _account: Account;
  private _ownAccounts: AccountRef[];
  private _model: EditTransactionForm;
  private _processing: boolean;

  private _splitTransaction: boolean;
  private _splits: SplitRecord[];

  private _typeOfTransaction: TransactionType;
  private _subscription: Subscription;

  constructor(private route: ActivatedRoute,
              private _accountService: AccountService,
              private _toasty: ToastService,
              private _history: RouterHistory) { }

  get model(): EditTransactionForm {
    return this._model;
  }

  get typeOfTransaction(): TransactionType {
    return this._typeOfTransaction;
  }

  get account(): Account {
    return this._account;
  }

  get ownAccounts(): AccountRef[] {
    return this._ownAccounts;
  }

  get processing(): boolean {
    return this._processing;
  }

  get isEditOnDestination(): boolean {
    return this.compareAccount(this.account, this.model.destination as AccountRef);
  }

  get isSourceManaged(): boolean {
    return this.isEditOnDestination && (this.typeOfTransaction.isTransfer
      || (this.model.destination && !(this.model.destination as AccountRef).isOwn()));
  }

  get isDestinationManaged(): boolean {
    return !this.isEditOnDestination && (this.typeOfTransaction.isTransfer || (this.model.source && !this.model.source.isOwn()));
  }

  get splitTransaction(): boolean {
    return this._splitTransaction;
  }

  get splitRecords(): SplitRecord[] {
    return this._splits;
  }

  compareAccount(a1: AccountRef, a2: AccountRef) {
    return a1 != null && a2 != null && a1.id == a2.id;
  }

  createAccount(name: any, type: string) {
    this._processing = true;
    this._accountService.create({
      name,
      currency: 'EUR',
      type
    } as AccountForm)
      .then(account => {
        const accountRef = new AccountRef(account.id, account.type, account.name);
        if (type == 'creditor') {
          this._model.destination = accountRef;
        } else {
          this._model.source = accountRef;
        }
      })
      .then(() => this._toasty.success('page.transaction.edit.account.created'))
      .finally(() => this._processing = false);
  }

  ngOnInit() {
    this._model = new EditTransactionForm();
    this._subscription = new Subscription();
    this._splits = [];

    this._subscription.add(this.route.paramMap.subscribe(params => {
      if (params.get('transactionId')) {
        this._transactionId = parseInt(params.get('transactionId'));
      }

      if (params.get('type')) {
        this._typeOfTransaction = new TransactionType(params.get('type'));

        this._accountService.getOwnAccounts()
          .then(a => this._ownAccounts = a.map(a => new AccountRef(a.id, a.type, a.name)));
      }
    }));

    this._subscription.add(this.route.data.subscribe(data => {
      this._account = data.account as Account;

      if (data.transaction) {
        const transaction = data.transaction as Transaction;
        this._model = EditTransactionForm.fromTransaction(transaction);
        this._splitTransaction = transaction.isSplit();

        if (transaction.isSplit()) {
          this._splits = transaction.split;
        }

        if (transaction.source.isOwn() && transaction.destination.isOwn()) {
          this._typeOfTransaction = new TransactionType('transfer');
        } else if (this.compareAccount(this._account, transaction.source) && transaction.source.isOwn()) {
          this._typeOfTransaction = new TransactionType('credit');
        } else {
          this._typeOfTransaction = new TransactionType('debit');
        }
      } else {
        const accountRef = new AccountRef(this._account.id, this._account.type, this._account.name);
        this._model.currency = this._account.account.currency;
        if (this._typeOfTransaction.isDebit) {
          this._model.destination = accountRef;
        } else {
          this._model.source = accountRef;
        }
      }
    }));
  }

  ngOnDestroy() {
    this._subscription.unsubscribe();
  }

  startSplit(): void {
    this._splits.push(new SplitRecord(this._model.description, this._model.amount));
    this._splitTransaction = true;
  }

  addSplitRecord(): void {
    this._splits.push(new SplitRecord('', 0));
  }

  removeSplitRecord(index: number) {
    this._splits[index] = null;
    this._splits = this._splits.filter(c => c != null);
  }

  recalculateSum(): void {
    this._model.amount = this._splits.reduce((value, split) => value + split.amount, 0);
  }

  saveTransaction() {
    this._model.validate();

    this._processing = true;
    if (this._transactionId) {
      const updatePromise = this._accountService.updateTransaction(this._account.id, this._transactionId, this._model);
      let splitPromise = new Promise<Transaction>(a => a());
      if (this._splitTransaction) {
        splitPromise = this._accountService.splitTransaction(this._account.id, this._transactionId,
          {splits: this._splits});
      }

      Promise.all([updatePromise, splitPromise])
        .then(() => this._history.previous())
        .finally(() => this._processing = false);
    } else {
      this._accountService.createTransaction(this._account.id, this._model)
        .then(() => this._history.previous())
        .finally(() => this._processing = false);
    }
  }
}
