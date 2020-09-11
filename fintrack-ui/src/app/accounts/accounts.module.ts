import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AccountOverviewComponent} from './account-overview/account-overview.component';
import {OwnAccountsComponent} from './own-accounts/own-accounts.component';
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {CoreModule} from "../core/core.module";
import {EditAccountComponent} from './edit-account/edit-account.component';
import {FormsModule} from "@angular/forms";
import {RouterModule} from "@angular/router";
import {TransactionOverviewComponent} from './transaction-overview/transaction-overview.component';
import {TransactionQuickActionComponent} from "./transaction-quick-action/transaction-quick-action.component";
import {ReconcileModalComponent} from './reconcile-modal/reconcile-modal.component';
import { LiabilitiesOverviewComponent } from './liabilities-overview/liabilities-overview.component';
import { EditLiabilityComponent } from './edit-liability/edit-liability.component';
import { LiabilityTransactionOverviewComponent } from './liability-transaction-overview/liability-transaction-overview.component';

@NgModule({
  declarations: [
    AccountOverviewComponent,
    OwnAccountsComponent,
    EditAccountComponent,
    TransactionOverviewComponent,
    TransactionQuickActionComponent,
    ReconcileModalComponent,
    LiabilitiesOverviewComponent,
    EditLiabilityComponent,
    LiabilityTransactionOverviewComponent
  ],
  imports: [
    CoreModule,
    CommonModule,
    NgbModule,
    FormsModule,
    RouterModule
  ],
  entryComponents: [TransactionQuickActionComponent]
})
export class AccountsModule {
}
