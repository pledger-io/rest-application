import {Component, OnDestroy, OnInit} from '@angular/core';
import {QuickNavigation} from "../../core/quick-navigation";
import {DateRange} from "../../core/core-models";
import * as moment from "moment";
import {DateRangeResolverService} from "../../core/date-range-resolver.service";
import {Subscription} from "rxjs";
import {FirstDateResolverService} from "../first-date-resolver.service";

@Component({
  selector: 'app-transaction-quick-action',
  templateUrl: './transaction-quick-action.component.html',
  styleUrls: ['./transaction-quick-action.component.scss']
})
export class TransactionQuickActionComponent implements OnInit, OnDestroy, QuickNavigation {

  private dateRange: DateRange;
  private accountRange: DateRange;
  private type: string;

  private _subscription: Subscription;

  constructor(private _rangeResolver: DateRangeResolverService,
              private _firstResolver: FirstDateResolverService) {
  }

  ngOnInit() {
    this._subscription = new Subscription();
    this._subscription.add(this._rangeResolver.$.subscribe(a => this.dateRange = a));
    this._subscription.add(this._firstResolver.$.subscribe(range => this.accountRange = range));
    this._subscription.add(this._firstResolver.type$.subscribe(type => this.type = type));
  }

  ngOnDestroy() {
    this._subscription.unsubscribe();
  }

  get loaded(): boolean {
    return this.accountRange != null && this.accountRange.from != 'Invalid date';
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
    let fromYear = this.firstYear;
    let untilYear = moment().get('year');
    if (untilYear && fromYear) {
      return [...new Array(moment().get('year') - this.firstYear + 1).keys()].map(x => x + this.firstYear);
    }

    return [];
  }

  monthWithoutTransactions(month: number): boolean {
    if (this.accountRange != null) {
      let monthRange = DateRange.forMonth(this.dateRange.computeStartYear(), month);
      return !this.accountRange.contains(monthRange) && !monthRange.contains(this.accountRange);
    }

    return false;
  }

  buildMonthLink(month: number): string {
    let monthRange: DateRange = DateRange.forMonth(this.dateRange.computeStartYear(), month);
    return '/transactions/' + this.type + '/' + monthRange.from + '/' + monthRange.until;
  }

  buildYearLink(year: number): string {
    return '/transactions/' + this.type + '/' + year + '-01-01/' + year + '-01-31';
  }
}
