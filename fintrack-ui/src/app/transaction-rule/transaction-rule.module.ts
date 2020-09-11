import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RuleOverviewComponent } from './rule-overview/rule-overview.component';
import {RouterModule} from "@angular/router";
import {CoreModule} from "../core/core.module";
import { RuleOverviewListComponent } from './rule-overview-list/rule-overview-list.component';
import { RuleEditComponent } from './rule-edit/rule-edit.component';
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {LookupDirective} from "./lookup.directive";
import { RuleGroupRenameModalComponent } from './rule-group-rename-modal/rule-group-rename-modal.component';



@NgModule({
  declarations: [RuleOverviewComponent, RuleOverviewListComponent, RuleEditComponent, LookupDirective, RuleGroupRenameModalComponent],
    imports: [
        CommonModule,
        RouterModule,
        CoreModule,
        FormsModule,
        NgbModule
    ]
})
export class TransactionRuleModule { }
