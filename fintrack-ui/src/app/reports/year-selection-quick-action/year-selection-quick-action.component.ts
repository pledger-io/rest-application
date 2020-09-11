import {Component, OnDestroy, OnInit} from '@angular/core';
import * as moment from "moment";
import {AccountService} from "../../accounts/account.service";
import {YearResolverService} from "../year-resolver.service";
import {Subscription} from "rxjs";
import {CurrencyResolverService} from "../currency-resolver.service";
import {Currency} from "../../core/models/currency";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../../environments/environment";

@Component({
  selector: 'app-year-selection-quick-action',
  templateUrl: './year-selection-quick-action.component.html',
  styleUrls: ['./year-selection-quick-action.component.scss']
})
export class YearSelectionQuickActionComponent implements OnInit, OnDestroy {

  private _firstYear: number;
  private _selectedYear: number;
  private _selectedCurrency: Currency;
  private _currencies: Currency[];
  private _activeReport: string;

  private _subscription: Subscription;

  constructor(private _accountService: AccountService,
              private _yearResolver: YearResolverService,
              private _currencyResolver: CurrencyResolverService,
              private _http: HttpClient) {  }

  get yearRange(): number[] {
    return [...new Array(moment().get('year') - this._firstYear + 1).keys()].map(x => x + this._firstYear);
  }

  get selectedYear(): number {
    return this._selectedYear
  }

  get firstYear(): number {
    return this._firstYear;
  }

  get selectedCurrency(): Currency {
    return this._selectedCurrency;
  }

  get currencies(): Currency[] {
    return this._currencies;
  }

  ngOnInit() {
    this._accountService.calculateFirstYear().then(year => this._firstYear = year);

    this._http.get<Currency[]>(environment.backend + 'settings/currencies')
      .subscribe(currencies => this._currencies = currencies.filter(c => c.enabled));

    this._subscription = new Subscription();
    this._subscription.add(this._yearResolver.$.subscribe(year => this._selectedYear = year));
    this._subscription.add(this._yearResolver.report$.subscribe(report => this._activeReport = report));
    this._subscription.add(this._currencyResolver.$.subscribe(currency => this._selectedCurrency = currency))
  }

  ngOnDestroy() {
    this._subscription.unsubscribe();
  }

  buildYearLink(year: number): string {
    return '/reports/' + this._activeReport + '/' + year;
  }

}
