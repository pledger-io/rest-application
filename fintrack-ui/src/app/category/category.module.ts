import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CategoryOverviewComponent } from './category-overview/category-overview.component';
import {HttpClientModule} from "@angular/common/http";
import {CoreModule} from "../core/core.module";
import {RouterModule} from "@angular/router";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import { CategoryEditComponent } from './category-edit/category-edit.component';
import {FormsModule} from "@angular/forms";

@NgModule({
  declarations: [CategoryOverviewComponent, CategoryEditComponent],
  imports: [
    CommonModule,
    HttpClientModule,
    FormsModule,
    CoreModule,
    RouterModule,
    NgbModule
  ]
})
export class CategoryModule { }
