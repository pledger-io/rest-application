import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {BudgetOverviewComponent} from './budget-overview/budget-overview.component';
import {CoreModule} from "../core/core.module";
import {BudgetQuickActionComponent} from './budget-quick-action/budget-quick-action.component';
import {RouterModule} from "@angular/router";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {FormsModule} from "@angular/forms";
import {CreateExpenseModalComponent} from './create-expense-modal/create-expense-modal.component';
import { TransactionModalComponent } from './transaction-modal/transaction-modal.component';

@NgModule({
  declarations: [BudgetOverviewComponent, BudgetQuickActionComponent, CreateExpenseModalComponent, TransactionModalComponent],
  imports: [
    CommonModule,
    CoreModule,
    RouterModule,
    NgbModule,
    FormsModule
  ]
})
export class BudgetModule {
}
