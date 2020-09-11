import {Component, OnDestroy, OnInit} from '@angular/core';
import {Account, AccountService, TopAccount} from "../../accounts/account.service";
import {ActivatedRoute} from "@angular/router";
import {DateRange} from "../../core/core-models";
import {Subscription} from "rxjs";
import {IncomeExpensePdf} from "./income-expense-pdf";

@Component({
  selector: 'app-income-expense',
  templateUrl: './income-expense.component.html',
  styleUrls: ['./income-expense.component.scss']
})
export class IncomeExpenseComponent implements OnInit, OnDestroy {

  private _range: DateRange;
  private _rangeStart: DateRange;
  private _rangeEnd: DateRange;
  private _ownAccounts: Account[];
  private _topDebtors: TopAccount[];
  private _topCreditors: TopAccount[];
  private _currency: string;

  private _dataSubscription: Subscription;

  constructor(private _accountService: AccountService,
              private _route: ActivatedRoute,
              private _pdfReport: IncomeExpensePdf) {
    this._ownAccounts = [];
  }

  get ownAccounts(): Account[] {
    return this._ownAccounts
  }

  get debtors(): TopAccount[] {
    return this._topDebtors
  }

  get creditors(): TopAccount[] {
    return this._topCreditors
  }

  get range(): DateRange {
    return this._range
  }

  get rangeToStart(): DateRange {
    return this._rangeStart
  }

  get rangeToEnd(): DateRange {
    return this._rangeEnd
  }

  get pdf(): IncomeExpensePdf {
    return this._pdfReport;
  }

  get currency(): string {
    return this._currency;
  }

  download() {
    this._pdfReport.year = this.range.computeStartYear();
    this._pdfReport.accounts = this._ownAccounts;
    this._pdfReport.creditors = this._topCreditors;
    this._pdfReport.debtors = this._topDebtors;

    return this._pdfReport.save();
  }

  ngOnInit() {
    this._accountService.getOwnAccounts().then(acc => {
      this._ownAccounts.push(...acc);
    });

    this._dataSubscription = this._route.data.subscribe(params => {
      let year = params.year;

      this._currency = params.currency.code;
      this._range = DateRange.forYear(year);
      this._rangeStart = DateRange.forRange('1900-01-01', this._range.from);
      this._rangeEnd = DateRange.forRange('1900-01-01', this._range.until);
      this._accountService.getTopCreditors(this._range).then(acc => this._topCreditors = acc);
      this._accountService.getTopDebtors(this._range).then(acc => this._topDebtors = acc);
    })
  }

  ngOnDestroy() {
    this._dataSubscription.unsubscribe();
  }

}
