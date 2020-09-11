import {Component, OnInit} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Account, AccountService} from '../account.service';
import {ConfirmModalComponent} from '../../core/confirm-modal/confirm-modal.component';
import {ProcessService, ToastService} from '../../core/core-services';
import {AccountRef, Bpmn} from '../../core/core-models';
import * as moment from 'moment';
import {ReconcileModalComponent} from '../reconcile-modal/reconcile-modal.component';

class Reconciliation {
  constructor(public process: Bpmn.Instance,
              public account: AccountRef = null,
              public variables: Bpmn.Variable<any>[] = [],
              public tasks: Bpmn.Task[] = []) {
  }

  get year(): number {
    return moment(this.value('startDate')).year();
  }

  get loaded(): boolean {
    return this.variables.length > 0;
  }

  value<T>(variable: string): T {
    const pVar = this.variables
      .filter(v => v.name == variable);

    if (pVar.length) {
      return pVar.pop().value;
    }

    return null;
  }
}

@Component({
  selector: 'app-own-accounts',
  templateUrl: './own-accounts.component.html',
  styleUrls: ['./own-accounts.component.scss']
})
export class OwnAccountsComponent implements OnInit {

  accounts: Account[];
  loading: boolean;

  private _activeReconcile: Reconciliation[];

  constructor(private _service: AccountService,
              private _processService: ProcessService,
              private _toastyService: ToastService,
              private modelService: NgbModal) { }

  get reconciles(): Reconciliation[] {
    return this._activeReconcile;
  }

  ngOnInit() {
    this.loading = true;
    this._service.getOwnAccounts()
      .then(accounts => this.accounts = accounts.sort((a1, a2) => a1.name.localeCompare(a2.name)))
      .finally(() => this.loading = false);

    this._processService.processList('AccountReconcile')
      .then(processes => {
        this._activeReconcile = processes.filter(p => p.state == 'ACTIVE')
          .map(p => {
            const reconciliation = new Reconciliation(p);

            Promise.all([
              this._processService.variable('AccountReconcile', p.id, 'accountId'),
              this._processService.variable('AccountReconcile', p.id, 'startDate'),
              this._processService.variable('AccountReconcile', p.id, 'openBalance'),
              this._processService.variable('AccountReconcile', p.id, 'computedStartBalance'),
              this._processService.variable('AccountReconcile', p.id, 'endBalance'),
              this._processService.variable('AccountReconcile', p.id, 'computedEndBalance')
            ]).then(variables => {
              reconciliation.variables = variables
                .map(v => v.length ? v[0] : null)
                .filter(v => v != null);

              const accountId = reconciliation.value('accountId');
              reconciliation.account = this.accounts.filter(a => a.id == accountId).pop();
            });

            this._processService.tasks('AccountReconcile', p.id)
              .then(tasks => reconciliation.tasks = tasks);

            return reconciliation;
          });
      });
  }

  confirmDelete(account: Account) {
    const modalRef = this.modelService.open(ConfirmModalComponent);
    modalRef.componentInstance.titleTextKey = 'common.action.delete';
    modalRef.componentInstance.descriptionKey = 'page.accounts.delete.confirm';
    modalRef.result
      .then(() => this._service.delete(account.id)
        .then(() => this.ngOnInit())
        .then(() => this._toastyService.success('page.account.delete.success'))
        .catch(() => this._toastyService.warning('page.account.delete.failed'))
      );
  }

  onReconcileClick(account: AccountRef, year: number = null, endBalance: number = null) {
    const modalRef = this.modelService.open(ReconcileModalComponent, {size: 'lg'});
    modalRef.componentInstance.accounts = this.accounts;
    modalRef.componentInstance.model.account = account;
    modalRef.componentInstance.model.year = year;
    modalRef.componentInstance.model.closeBalance = endBalance;

    modalRef.result.then(() => this.ngOnInit());
  }

  confirmReconcileDelete(reconciliation: Reconciliation) {
    const modalRef = this.modelService.open(ConfirmModalComponent);
    modalRef.componentInstance.titleTextKey = 'common.action.delete';
    modalRef.componentInstance.descriptionKey = 'page.accounts.reconcile.delete.confirm';
    modalRef.result
      .then(() => this._processService.delete('AccountReconcile', reconciliation.process.id)
        .then(() => this.ngOnInit())
        .then(() => this._toastyService.success('page.account.reconcile.delete.success'))
        .catch(() => this._toastyService.warning('page.account.reconcile.delete.failed'))
      );
  }

  closeReconcileTask(reconciliation: Reconciliation): void {
    this._processService.closeTask('AccountReconcile', reconciliation.process.id, reconciliation.tasks.pop().id)
      .then(() => this.ngOnInit());
  }
}
