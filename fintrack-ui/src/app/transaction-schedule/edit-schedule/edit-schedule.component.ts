import {Component, OnInit} from '@angular/core';
import {NgbDateParserFormatter} from "@ng-bootstrap/ng-bootstrap";
import {CustomDateParserFormatter} from "../../core/custom-date-parser-formatter";
import {ScheduledTransactionService} from "../scheduled-transaction.service";
import {ActivatedRoute} from "@angular/router";
import {Subscription} from "rxjs";
import {RouterHistory} from "../../core/router-history";

class EditSchedule {
  private original;

  constructor(public periodicity: string = null,
              public interval: number = null) {
    this.original = {
      periodicity: periodicity,
      interval: interval
    }
  }

  hasChanged(): boolean {
    return this.periodicity != this.original.periodicity
      || this.interval != this.original.interval;
  }
}

class RangeSchedule {
  private original: any;

  constructor(public start: string = null,
              public end: string = null) {
    this.original = {
      start: start,
      end: end
    }
  }

  hasChanged(): boolean {
    return this.original.start != this.start
      || this.original.end != this.end;
  }
}

class EditForm {
  constructor(public name: string = null,
              public description: string = null,
              public schedule: EditSchedule = null,
              public range: RangeSchedule = null) {
  }
}

@Component({
  selector: 'app-edit-schedule',
  templateUrl: './edit-schedule.component.html',
  styleUrls: ['./edit-schedule.component.scss'],
  providers: [{provide: NgbDateParserFormatter, useClass: CustomDateParserFormatter}]
})
export class EditScheduleComponent implements OnInit {

  private _scheduleId: number;
  private _model: EditForm;
  private _subscription: Subscription;

  constructor(private _service: ScheduledTransactionService,
              private _route: ActivatedRoute,
              private _history: RouterHistory) {
  }

  get model(): EditForm {
    return this._model
  }

  ngOnInit(): void {
    this._subscription = this._route.paramMap.subscribe(params => {
      this._scheduleId = parseInt(params.get('scheduleId'));
      this._service.get(this._scheduleId)
        .then(scheduledTransaction => {
          this._model = new EditForm(
            scheduledTransaction.name,
            scheduledTransaction.description,
            new EditSchedule(scheduledTransaction.schedule.periodicity, scheduledTransaction.schedule.interval),
            new RangeSchedule(scheduledTransaction.range.start, scheduledTransaction.range.end)
          );
        });
    });
  }

  persist(): void {
    let body = {
      name: this._model.name,
      description: this._model.description,
      schedule: this._model.schedule,
      range: this._model.range
    }

    if (!this._model.range.hasChanged()) {
      delete body['range'];
    }

    if (!this._model.schedule.hasChanged()) {
      delete body['schedule'];
    }

    this._service.update(this._scheduleId, body)
      .then(() => this._history.previous());
  }

}
