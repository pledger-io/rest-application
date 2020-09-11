import {Component, Input, OnChanges} from '@angular/core';
import {DateRange, EntityRef} from '../../core-models';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environments/environment';

class BalanceResponse {
  constructor(public balance: number) {
  }
}

@Component({
  selector: 'app-statistical-balance',
  templateUrl: './statistical-balance.component.html',
  styleUrls: ['./statistical-balance.component.scss']
})
export class StatisticalBalanceComponent implements OnChanges {

  private _range: DateRange;

  @Input('account')
  private _accounts: EntityRef[];
  @Input('categories')
  private _categories: EntityRef[];
  @Input('contracts')
  private _contracts: EntityRef[];
  @Input('expenses')
  private _expenses: EntityRef[];
  @Input('currency')
  private _currency: string;
  @Input('importSlug')
  private _importSlug: string;

  @Input('onlyIncome')
  private _onlyIncome: boolean;

  private _balance: number;

  @Input('class')
  private _class: string;

  constructor(private _http: HttpClient) { }

  @Input('range')
  set range(range: DateRange) {
    this._range = range;
  }

  get balance(): number {
    return this._balance;
  }

  get currency(): string {
    return this._currency;
  }

  get class(): string {
    return this._class;
  }

  ngOnChanges() {
    const request = {
      accounts: this._accounts,
      categories: this._categories,
      contracts: this._contracts,
      expenses: this._expenses,
      onlyIncome: this._onlyIncome,
      allMoney: this._onlyIncome == null,
      currency: this._currency,
      dateRange: null,
      importSlug: this._importSlug
    };

    if (this._range) {
      request.dateRange = {
        start: this._range.from,
        end: this._range.until,
      };
    }

    this._balance = null;
    this._http.post<BalanceResponse>(environment.backend + 'statistics/balance', request)
      .toPromise()
      .then(balance => this._balance = balance.balance);
  }

}
