import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ContractOverviewComponent } from './contract-overview/contract-overview.component';
import { ContractEditComponent } from './contract-edit/contract-edit.component';
import {FormsModule} from "@angular/forms";
import {CoreModule} from "../core/core.module";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import { ContractListComponent } from './contract-list/contract-list.component';
import {RouterModule} from "@angular/router";
import { UploadContractModalComponent } from './upload-contract-modal/upload-contract-modal.component';
import { ContractDetailsComponent } from './contract-details/contract-details.component';

@NgModule({
  declarations: [ContractOverviewComponent, ContractEditComponent, ContractListComponent, UploadContractModalComponent,  ContractDetailsComponent],
    imports: [
        CommonModule,
        FormsModule,
        CoreModule,
        NgbModule,
        RouterModule
    ]
})
export class ContractModule { }
