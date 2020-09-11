import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ProfilePageComponent} from './profile-page/profile-page.component';
import {CoreModule} from "../core/core.module";
import {ImportProfileModalComponent} from './import-profile-modal/import-profile-modal.component';
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {FormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import { EnableMfaModalComponent } from './enable-mfa-modal/enable-mfa-modal.component';


@NgModule({
  declarations: [ProfilePageComponent, ImportProfileModalComponent, EnableMfaModalComponent],
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    NgbModule,
    CoreModule
  ],
  entryComponents: [ImportProfileModalComponent]
})
export class ProfileModule { }
