import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EditScheduleComponent } from './edit-schedule/edit-schedule.component';
import { ScheduleOverviewComponent } from './schedule-overview/schedule-overview.component';
import {CoreModule} from "../core/core.module";
import {RouterModule} from "@angular/router";
import { CreateScheduleModalComponent } from './create-schedule-modal/create-schedule-modal.component';
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";



@NgModule({
  declarations: [EditScheduleComponent, ScheduleOverviewComponent, CreateScheduleModalComponent],
  imports: [
    CommonModule,
    CoreModule,
    RouterModule,
    FormsModule,
    NgbModule
  ]
})
export class TransactionScheduleModule { }
