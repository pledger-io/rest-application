import {Component, OnDestroy, OnInit} from '@angular/core';
import {QuickNavigation} from "../../core/quick-navigation";
import {DateRangeResolverService} from "../../core/date-range-resolver.service";
import {DateRange} from "../../core/core-models";
import {BudgetService} from "../budget.service";
import * as moment from "moment";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-budget-quick-action',
  templateUrl: './budget-quick-action.component.html',
  styleUrls: ['./budget-quick-action.component.scss']
})
export class BudgetQuickActionComponent implements OnInit, OnDestroy, QuickNavigation {

  private _dateRange: DateRange;
  private _fullRange: DateRange;

  private _subscription: Subscription;

  constructor(private _dateRangeResolver: DateRangeResolverService, private _service: BudgetService) { }

  ngOnInit() {
    this._subscription = new Subscription();
    this._subscription.add(this._dateRangeResolver.$.subscribe(e => this._dateRange = e));
    this._service.firstMonth().then(e => this._fullRange = e);
  }

  ngOnDestroy() {
    this._subscription.unsubscribe();
  }

  get loaded(): boolean {
    return this._dateRange != null && this._fullRange != null;
  }

  get currentRange(): DateRange {
    return this._dateRange
  }

  get yearRange(): number[] {
    return [...new Array(moment().year() - this._fullRange.computeStartYear() + 1).keys()].map(x => x + this._fullRange.computeStartYear());
  }

  buildMonthLink(month: number): string {
    let monthRange: DateRange = DateRange.forMonth(this._dateRange.computeStartYear(), month);
    return '/budgets/' + monthRange.from + '/' + monthRange.until;
  }


}
