import {Component, OnInit} from '@angular/core';
import {Budget, BudgetService, Expense} from "../../budget/budget.service";
import {ActivatedRoute} from "@angular/router";
import {DateRange, EntityRef} from "../../core/core-models";
import {environment} from "../../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {distinct, flatMap, map} from "rxjs/operators";
import {of} from "rxjs";
import {BudgetMonthlyPdf} from "./budget-monthly-pdf";

export class StatisticsBudget {
  constructor(public budget: Budget,
              public range: DateRange,
              public actualIncome: number = 0,
              public actualSpent: number = 0) {
  }
}

@Component({
  selector: 'app-budget-monthly',
  templateUrl: './budget-monthly.component.html',
  styleUrls: ['./budget-monthly.component.scss']
})
export class BudgetMonthlyComponent implements OnInit {

  public Math = Math;

  private _budgets: StatisticsBudget[];
  private _range: DateRange;
  private _expenses: Expense[];
  private _currency: string;

  private _computed: any;

  constructor(private _budgetService: BudgetService,
              private _route: ActivatedRoute,
              private _http: HttpClient,
              private _pdfReport: BudgetMonthlyPdf) {
  }

  get budgetsLoaded(): boolean {
    return this._budgets && this._budgets.length > 0;
  }

  get percentageEarned(): number {
    return (this._computed['income'] / this._computed.expectedIncome) * 100
  }

  get percentageSpent(): number {
    return (this._computed['expenses'] / this._computed.expectedExpense) * 100
  }

  get budgets(): StatisticsBudget[] {
    return this._budgets.sort((b1, b2) => b1.range.compare(b2.range));
  }

  get range(): DateRange {
    return this._range
  }

  get currency(): string {
    return this._currency;
  }

  get expenses(): Expense[] {
    return this._expenses
  }

  get pdf(): BudgetMonthlyPdf {
    return this._pdfReport
  }

  ngOnInit() {
    this._route.data.subscribe(data => {
      const year = data.year as number;

      this._currency = data.currency.code;
      this._expenses = [];
      this._budgets = [];
      this._computed = {
        expectedIncome: 0,
        expectedExpense: 0
      };
      this._range = DateRange.forYear(year);

      Promise.all([...new Array(12).keys()].map(month => this._budgetService.getBudget(year, month + 1)))
        .then(budgets => {
          budgets.forEach((budget, idx) => {
            let range = DateRange.forMonth(year, idx + 1);
            let statistic = new StatisticsBudget(budget, range);

            this.computeBalance(range, [], true).then(balance => statistic.actualIncome = balance);
            this.computeBalance(range, budget.expenses, false).then(balance => statistic.actualSpent = balance);

            this._computed.expectedExpense += budget.totalExpenses;
            this._computed.expectedIncome += budget.income;
            this._budgets.push(statistic);
          });

          of(budgets).pipe(
            flatMap(budgets => budgets.map(budget => budget.expenses)),
            flatMap(expenses => expenses),
            distinct(expense => expense.id)
          ).subscribe(expenses => this._expenses.push(expenses));
        });

      this.computeBalance(this._range, [], true).then(balance => this._computed['income'] = balance);
      this.computeBalance(this._range, [], false).then(balance => this._computed['expenses'] = Math.abs(balance));
    });
  }

  download() {
    this._pdfReport.budgets = this._budgets;
    this._pdfReport.expenses = this._expenses;
    this._pdfReport.year = this.range.computeStartYear();

    this._pdfReport.save();
  }

  private async computeBalance(range: DateRange, expenses: EntityRef[], onlyIncome: boolean = null): Promise<number> {
    return this._http.post(environment.backend + 'statistics/balance', {
      expenses: expenses,
      dateRange: {
        start: range.from,
        end: range.until,
      },
      onlyIncome: onlyIncome,
      allMoney: onlyIncome == null,
      currency: this._currency
    }).pipe<number>(map(raw => raw['balance'])).toPromise()
  }

}
