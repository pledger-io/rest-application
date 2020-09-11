import { Component, OnInit } from '@angular/core';
import {ScheduledTransaction} from "../scheduled-transaction.models";
import {ScheduledTransactionService} from "../scheduled-transaction.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CreateScheduleModalComponent} from "../create-schedule-modal/create-schedule-modal.component";
import {ConfirmModalComponent} from "../../core/confirm-modal/confirm-modal.component";
import {ToastService} from "../../core/core-services";

@Component({
  selector: 'app-schedule-overview',
  templateUrl: './schedule-overview.component.html',
  styleUrls: ['./schedule-overview.component.scss']
})
export class ScheduleOverviewComponent implements OnInit {

  private _schedules: ScheduledTransaction[];

  constructor(private _service: ScheduledTransactionService,
              private _toastyService: ToastService,
              private _modalService: NgbModal) { }

  get schedules(): ScheduledTransaction[] {
    return this._schedules
  }

  ngOnInit(): void {
    this._service.list()
      .then(schedules => this._schedules = schedules);
  }

  confirmDelete(schedule: ScheduledTransaction) {
    let modalRef = this._modalService.open(ConfirmModalComponent);
    modalRef.componentInstance.titleTextKey = 'common.action.delete';
    modalRef.componentInstance.descriptionKey = 'page.budget.schedule.delete.confirm';
    modalRef.result.then(() => this._service.delete(schedule.id)
      .then(() => this.ngOnInit()))
      .then(() => this._toastyService.success('page.budget.schedule.delete.success'));
  }

  openCreateModal(): void {
    this._modalService.open(CreateScheduleModalComponent, {size: 'lg'})
      .result.then(() => this.ngOnInit())
  }

}
