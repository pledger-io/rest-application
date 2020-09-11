import { Component, OnInit } from '@angular/core';
import {DateRange} from "../core/core-models";
import {BudgetService} from "../budget/budget.service";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  private _range: DateRange;
  private _budgetExpense: number;

  constructor(private budgetService: BudgetService) { }

  get range() : DateRange {
    return this._range
  }

  get budget(): number {
    return this._budgetExpense;
  }

  ngOnInit() {
    this._range = DateRange.previousDays(90);
    this._budgetExpense = 0;

    this.budgetService.getBudget(this.range.computeStartYear(), this.range.computeStartMonth())
      .then(budget => this._budgetExpense += budget.totalExpenses)
  }

}
