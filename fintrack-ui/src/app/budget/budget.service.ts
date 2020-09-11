import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {AccountRef, DateRange, Page, Transaction} from "../core/core-models";
import {map} from "rxjs/operators";
import * as moment from "moment";

export class Expense {
  constructor(public id: number, public name: string, public expected: number) {
  }
}

export class Budget {
  public totalExpenses: number = 0;
  constructor(public income: number, public period: DateRange, public expenses: Expense[]) {
    this.totalExpenses = expenses.reduce((sum, expense) => sum + expense.expected, 0)
  }
}

export class ComputedExpense {
  constructor(public spent: number,
              public left: number,
              public dailySpent: number,
              public dailyLeft: number) {
  }
}

@Injectable({
  providedIn: 'root'
})
export class BudgetService {

  constructor(private http: HttpClient) { }

  firstMonth(): Promise<DateRange> {
    return this.http.get(environment.backend + 'budgets')
      .pipe(map((a: string) => a ? DateRange.forRange(a, moment().toISOString()) : null))
      .toPromise();
  }

  createBudget(year: number, month: number, income: number) : Promise<Budget> {
    return this.http.put<Budget>(environment.backend + 'budgets', {year: year, month: month, income: income})
      .toPromise();
  }

  getBudget(year: number, month: number): Promise<Budget> {
    return this.http.get<Budget>(environment.backend + 'budgets/' + year + '/' + month)
      .pipe(map(budget => budget ? new Budget(budget.income, DateRange.forRange(budget.period.from, budget.period.until), budget.expenses) : null))
      .toPromise();
  }

  createExpense(body: any): Promise<Budget> {
    return this.http.put<Budget>(environment.backend + 'budgets/expenses', body).toPromise();
  }

  computeExpense(id: number, year: number, month: number): Promise<ComputedExpense> {
    return this.http.get<ComputedExpense>(environment.backend + 'budgets/expenses/' + id + '/' + year + '/' + month)
      .toPromise();
  }

  transactions(id: number, year: number, month: number, page: number): Promise<Page<Transaction>> {
    return this.http.get<Page<Transaction>>(environment.backend + 'budgets/expenses/' + id + '/' + year+ '/' + month + '/transactions?page=' + page)
      .pipe(
        map(page => {
          page.content = page.content.map(transaction => {
            transaction.source = new AccountRef(transaction.source.id, transaction.source.type, transaction.source.name);
            transaction.destination = new AccountRef(transaction.destination.id, transaction.destination.type, transaction.destination.name);
            return transaction;
          })
          return page;
        })
      )
      .toPromise()
  }

}
