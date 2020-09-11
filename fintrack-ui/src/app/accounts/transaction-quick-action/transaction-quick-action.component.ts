import {Component, OnDestroy, OnInit} from '@angular/core';
import {QuickNavigation} from "../../core/quick-navigation";
import {DateRange} from "../../core/core-models";
import {Account} from "../account.service";
import {AccountResolverService} from "../account-resolver.service";
import * as moment from "moment";
import {DateRangeResolverService} from "../../core/date-range-resolver.service";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-transaction-quick-action',
  templateUrl: './transaction-quick-action.component.html',
  styleUrls: ['./transaction-quick-action.component.scss']
})
export class TransactionQuickActionComponent implements OnInit, OnDestroy, QuickNavigation {

  private account: Account;
  private dateRange: DateRange;
  private accountRange: DateRange;

  private _subscription: Subscription;

  constructor(private service: AccountResolverService, private rangeResolver: DateRangeResolverService) {
  }

  ngOnInit() {
    this._subscription = new Subscription();
    this._subscription.add(this.rangeResolver.$.subscribe(a => this.dateRange = a));
    this._subscription.add(this.service.update$.subscribe(a => {
      this.account = a;
      this.accountRange = DateRange.forRange(this.account.history.firstTransaction, this.account.history.lastTransaction);
    }));
  }

  ngOnDestroy() {
    this._subscription.unsubscribe();
  }

  get loaded(): boolean {
    return this.account != null && this.account.history.firstTransaction != null;
  }

  get selectedMonth(): number {
    return this.dateRange.computeStartMonth();
  }

  get selectedYear(): number {
    return this.dateRange.computeStartYear();
  }

  get firstYear(): number {
    return this.accountRange.computeStartYear()
  }

  get yearRange(): number[] {
    return [...new Array(moment().get('year') - this.firstYear + 1).keys()].map(x => x + this.firstYear);
  }

  monthWithoutTransactions(month: number): boolean {
    if (this.account) {
      let monthRange = DateRange.forMonth(this.dateRange.computeStartYear(), month);
      return !this.accountRange.contains(monthRange) && !monthRange.contains(this.accountRange);
    }

    return false;
  }

  buildMonthLink(month: number): string {
    let monthRange: DateRange = DateRange.forMonth(this.dateRange.computeStartYear(), month);
    return this.account.path + '/transactions/' + monthRange.from + '/' + monthRange.until;
  }

  buildYearLink(year: number): string {
    return this.account.path + '/transactions/' + year + '-01-01/' + year + '-01-31';
  }
}
