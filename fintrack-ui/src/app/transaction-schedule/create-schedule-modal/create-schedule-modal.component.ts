import { Component, OnInit } from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {AccountRef, EntityType} from "../../core/core-models";
import {AccountService} from "../../accounts/account.service";
import {ScheduledTransactionService} from "../scheduled-transaction.service";

class ScheduleForm {
  constructor(public periodicity: string = "",
              public interval: number = null) {
  }
}

class CreateForm {
  constructor(public name: string = "",
              public amount: number = null,
              public source: AccountRef = null,
              public destination: AccountRef = null,
              public schedule: ScheduleForm = new ScheduleForm()) {
  }
}

@Component({
  selector: 'app-create-schedule-modal',
  templateUrl: './create-schedule-modal.component.html',
  styleUrls: ['./create-schedule-modal.component.scss']
})
export class CreateScheduleModalComponent implements OnInit {

  EntityType = EntityType;

  private _processing: boolean;
  private _model: CreateForm;
  private _ownAccounts: AccountRef[];

  private _type: string;

  constructor(private _modal: NgbActiveModal,
              private _service: ScheduledTransactionService,
              private _accountService: AccountService) { }

  get processing(): boolean {
    return this._processing
  }

  get model(): CreateForm {
    return this._model
  }

  get ownAccounts(): AccountRef[] {
    return this._ownAccounts
  }

  get type(): string {
    return this._type;
  }

  set type(value: string) {
    this._type = value;
  }

  ngOnInit(): void {
    this._type = 'debit';
    this._model = new CreateForm();
    this._accountService.getOwnAccounts()
      .then(a => this._ownAccounts = a.map(a => new AccountRef(a.id, a.type, a.name)));
  }

  dismiss(): void {
    this._modal.dismiss();
  }

  compareAccount(a1: AccountRef, a2: AccountRef) {
    return a1 != null && a2 != null && a1.id == a2.id;
  }

  create(): void {
    this._service.create(this._model)
      .then(() => this._modal.close());
  }
}
