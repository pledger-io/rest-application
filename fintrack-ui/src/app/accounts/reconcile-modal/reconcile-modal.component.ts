import { Component, OnInit } from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {AccountRef, DateRange} from '../../core/core-models';
import {ProcessService} from '../../core/core-services';

class ReconcileForm {
  constructor(public account: AccountRef,
              public year: number = null,
              public openBalance: number = null,
              public closeBalance: number = null) {
  }
}

@Component({
  selector: 'app-reconcile-modal',
  templateUrl: './reconcile-modal.component.html',
  styleUrls: ['./reconcile-modal.component.scss']
})
export class ReconcileModalComponent implements OnInit {

  private _processing: boolean;
  private _accounts: AccountRef[];
  private readonly _model: ReconcileForm;

  constructor(private _modal: NgbActiveModal,
              private _processService: ProcessService) {
    this._model = new ReconcileForm(null, null);
  }

  get processing(): boolean {
    return this._processing;
  }

  get accounts(): AccountRef[] {
    return this._accounts;
  }

  get model(): ReconcileForm {
    return this._model;
  }

  set accounts(value) {
    this._accounts = value;
  }

  ngOnInit(): void {

  }

  dismiss(): void {
    this._modal.dismiss();
  }

  process(): void {
    const range = DateRange.forYear(this._model.year);

    this._processService.start('AccountReconcile', {
      accountId: this._model.account.id,
      openBalance: this._model.openBalance,
      endBalance: this._model.closeBalance,
      startDate: range.from,
      endDate: range.until
    }).then(() => this._modal.close());
  }

  compareAccount(a1: AccountRef, a2: AccountRef) {
    return a1 != null && a2 != null && a1.id == a2.id;
  }

}
