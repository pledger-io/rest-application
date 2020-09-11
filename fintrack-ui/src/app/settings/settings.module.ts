import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SettingOverviewComponent } from './setting-overview/setting-overview.component';
import { SettingEditModalComponent } from './setting-edit-modal/setting-edit-modal.component';
import {CoreModule} from "../core/core.module";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {FormsModule} from "@angular/forms";
import { CurrencyOverviewComponent } from './currency-overview/currency-overview.component';
import { CurrencyEditComponent } from './currency-edit/currency-edit.component';
import {RouterModule} from '@angular/router';



@NgModule({
  declarations: [SettingOverviewComponent, SettingEditModalComponent, CurrencyOverviewComponent, CurrencyEditComponent],
    imports: [
        CommonModule,
        CoreModule,
        NgbModule,
        FormsModule,
        RouterModule,
    ]
})
export class SettingsModule { }
