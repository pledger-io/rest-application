import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {AccountRef, DateRange, Page, Transaction} from "../core/core-models";
import {filter, map} from "rxjs/operators";
import * as moment from "moment";

export class AccountHistory {
  constructor(public firstTransaction: string,
              public lastTransaction: string,
              public balance: number) {
  }
}

export class AccountNumber {
  constructor(public iban: string,
              public bic: string,
              public number: string,
              public currency: string) {
  }
}

class InterestPeriod {
  constructor(public value: number, public periodicity: string) {
  }
}

export class Account extends AccountRef {
  constructor(id: number,
              type: string,
              name: string,
              public description: string,
              public account: AccountNumber,
              public history: AccountHistory,
              public interest: InterestPeriod) {
    super(id, type, name);
  }
}

export class TopAccount {
  constructor(public account: Account,
              public total: number,
              public average: number) {
  }
}

export class AccountType {
  constructor(public key: string, public labelKey: string) {
  }
}

export class AccountForm {
  constructor(public name: string,
              public description: string,
              public currency: string,
              public iban: string,
              public bic: string,
              public number: string,
              public type: string,
              public interest: number = 0,
              public interestPeriodicity: string = null) {
  }

  isDebtor(): boolean {
    return this.type == 'debtor'
  }

  isCreditor(): boolean {
    return this.type == 'creditor';
  }

  static fromAccount(account: Account): AccountForm {
    return new AccountForm(
      account.name,
      account.description,
      account.account.currency,
      account.account.iban,
      account.account.bic,
      account.account.number,
      account.type);
  }
}

@Injectable({
  providedIn: 'root'
})
export class AccountService {

  private accountTypeCache = null;

  constructor(private http: HttpClient) {
  }

  getTopCreditors(range: DateRange): Promise<TopAccount[]> {
    return this.http.get<TopAccount[]>(environment.backend + 'accounts/top/creditor/' + range.from + '/' + range.until)
      .toPromise();
  }

  getTopDebtors(range: DateRange): Promise<TopAccount[]> {
    return this.http.get<TopAccount[]>(environment.backend + 'accounts/top/debit/' + range.from + '/' + range.until)
      .toPromise();
  }

  getAccountTypes(): Promise<string[]> {
    if (!this.accountTypeCache) {
      this.accountTypeCache = this.http.get<string[]>(environment.backend + 'account-types').toPromise()
    }

    return this.accountTypeCache;
  }

  getOwnAccounts(): Promise<Account[]> {
    return this.http.get<Account[]>(environment.backend + 'accounts/my-own')
      .pipe(map(accounts => accounts.sort((a1, a2) => a1.name.localeCompare(a2.name))))
      .toPromise()
  }

  calculateFirstYear(): Promise<number> {
    return this.http.get<Account[]>(environment.backend + 'accounts/my-own')
      .pipe(
        map(accounts => accounts
          .filter(account => account.history.firstTransaction)
          .map(account => moment(account.history.firstTransaction, environment.isoDateFormat).get('year'))),
        map((years: number[]) => years.sort((y1, y2) => y1 < y2 ? 1 : y1 > y2 ? -1 : 0)),
        map(years => years.pop())
      ).toPromise();
  }

  getAccount(id: number): Promise<Account> {
    return this.http.get<Account>(environment.backend + 'accounts/' + id)
      .pipe(map(a => new Account(a.id, a.type, a.name, a.description, a.account, a.history, a.interest)))
      .toPromise()
  }

  delete(id: number): Promise<void> {
    return this.http.delete<void>(environment.backend + 'accounts/' + id).toPromise();
  }

  getAllAccounts(): Promise<Account[]> {
    return this.http.get<Account[]>(environment.backend + 'accounts/all').toPromise();
  }

  getAccounts(type: string[], page: number, filter: string): Promise<Page<Account>> {
    return this.http.post<Page<Account>>(environment.backend + 'accounts', {
      accountTypes: type,
      page: page,
      name: filter
    }).toPromise()
  }

  create(account: AccountForm): Promise<Account> {
    return this.http.put<Account>(environment.backend + 'accounts', account).toPromise()
  }

  update(id: number, account: AccountForm): Promise<Account> {
    return this.http.post<Account>(environment.backend + 'accounts/' + id, account).toPromise()
  }

  transaction(accountId: number, transactionId: number): Promise<Transaction> {
    return this.http.get<Transaction>(environment.backend + 'accounts/' + accountId + '/transactions/' + transactionId)
      .pipe(map(t => Transaction.fromRemote(t)))
      .toPromise()
  }

  transactions(id: number, page: number, range: DateRange): Promise<Page<Transaction>> {
    return this.http.post<Page<Transaction>>(environment.backend + 'accounts/' + id + '/transactions', {
      page: page,
      dateRange: {
        start: range.from,
        end: range.until
      }
    }).pipe(
      map(page => {
        page.content = page.content.map(t => Transaction.fromRemote(t));
        return page;
      })
    ).toPromise();
  }

  firstTransaction(id: number, description: string = ""): Promise<Transaction> {
    return this.http.get<Transaction>(environment.backend + 'accounts/' + id + '/transactions/first?description=' + description)
      .pipe(
        map(transaction => Transaction.fromRemote(transaction))
      ).toPromise();
  }

  createTransaction(id: number, transaction: any): Promise<void> {
    return this.http.put<void>(environment.backend + 'accounts/' + id + '/transactions', transaction).toPromise()
  }

  updateTransaction(id: number, transactionId: number, transaction: any): Promise<Transaction> {
    return this.http.post<Transaction>(environment.backend + 'accounts/' + id + '/transactions/' + transactionId, transaction)
      .toPromise();
  }

  splitTransaction(id: number, transactionId: number, split: any): Promise<Transaction> {
    return this.http.patch<Transaction>(environment.backend + 'accounts/' + id + '/transactions/' + transactionId, split)
      .toPromise();
  }

  deleteTransaction(id: number, transactionId: number): Promise<any> {
    return this.http.delete(environment.backend + 'accounts/' + id + '/transactions/' + transactionId).toPromise();
  }

}
