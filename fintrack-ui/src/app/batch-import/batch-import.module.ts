import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImportOverviewComponent } from './import-overview/import-overview.component';
import {CoreModule} from "../core/core.module";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {RouterModule} from "@angular/router";
import { StartImportComponent } from './start-import/start-import.component';
import {FormsModule} from "@angular/forms";
import { ConfigurationUploadModalComponent } from './configuration-upload-modal/configuration-upload-modal.component';
import { AnalyzeImportComponent } from './analyze-import/analyze-import.component';
import { MappingsImportComponent } from './mappings-import/mappings-import.component';
import { AccountMapImportComponent } from './account-map-import/account-map-import.component';
import { DetailPageComponent } from './detail-page/detail-page.component';

@NgModule({
  declarations: [ImportOverviewComponent, StartImportComponent, ConfigurationUploadModalComponent, AnalyzeImportComponent, MappingsImportComponent, AccountMapImportComponent, DetailPageComponent],
  imports: [
    CommonModule,
    CoreModule,
    NgbModule,
    FormsModule,
    RouterModule
  ]
})
export class BatchImportModule { }
