import {NgModule} from '@angular/core';
import {CommonModule, PercentPipe} from '@angular/common';
import {IncomeExpenseComponent} from './income-expense/income-expense.component';
import {BudgetMonthlyComponent} from './budget-monthly/budget-monthly.component';
import {CoreModule} from "../core/core.module";
import {RouterModule} from "@angular/router";
import {YearSelectionQuickActionComponent} from './year-selection-quick-action/year-selection-quick-action.component';
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {CategoryMonthlyComponent} from './category-monthly/category-monthly.component';
import {IncomeExpensePdf} from "./income-expense/income-expense-pdf";
import {CategoryMonthlyPdf} from "./category-monthly/category-monthly-pdf";
import {BudgetMonthlyPdf} from "./budget-monthly/budget-monthly-pdf";

@NgModule({
  declarations: [IncomeExpenseComponent, BudgetMonthlyComponent, YearSelectionQuickActionComponent, CategoryMonthlyComponent],
  imports: [
    CommonModule,
    CoreModule,
    RouterModule,
    NgbModule
  ],
  providers: [
    IncomeExpensePdf,
    CategoryMonthlyPdf,
    BudgetMonthlyPdf,
    PercentPipe
  ]
})
export class ReportsModule {
}
