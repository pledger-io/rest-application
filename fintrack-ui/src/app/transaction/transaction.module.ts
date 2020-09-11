import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EditTransactionComponent } from './edit-transaction/edit-transaction.component';
import {CoreModule} from "../core/core.module";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import { GlobalOverviewComponent } from './global-overview/global-overview.component';
import {RouterModule} from "@angular/router";
import {TransactionQuickActionComponent} from "./transaction-quick-action/transaction-quick-action.component";
import { AccountSelectComponent } from './account-select/account-select.component';
import { MultiEditModalComponent } from './multi-edit-modal/multi-edit-modal.component';

@NgModule({
  declarations: [EditTransactionComponent, GlobalOverviewComponent, TransactionQuickActionComponent, AccountSelectComponent, MultiEditModalComponent],
  imports: [
    CommonModule,
    CoreModule,
    FormsModule,
    NgbModule,
    RouterModule
  ]
})
export class TransactionModule { }
