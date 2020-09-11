import {Component, OnDestroy, OnInit} from '@angular/core';
import {BatchImport} from "../batch-import.models";
import {ActivatedRoute, Router} from "@angular/router";
import {Pagable, Page, Transaction} from "../../core/core-models";
import {BatchImportService} from "../batch-import.service";
import {ConfirmModalComponent} from "../../core/confirm-modal/confirm-modal.component";
import {noop, Subscription} from "rxjs";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {MultiEditModalComponent} from "../../transaction/multi-edit-modal/multi-edit-modal.component";

@Component({
  selector: 'app-detail-page',
  templateUrl: './detail-page.component.html',
  styleUrls: ['./detail-page.component.scss']
})
export class DetailPageComponent implements OnInit, OnDestroy {

  private _batchImport: BatchImport;
  private _currentPage: Page<Transaction>;
  private _pager: Pagable;
  private _loading: boolean;
  private _subscription: Subscription;

  private _selected: number[];

  constructor(private _route: ActivatedRoute,
              private _router: Router,
              private _service: BatchImportService,
              private _modelService : NgbModal) { }

  ngOnInit() {
    this._selected = [];
    this._pager = new Pagable(0, 20);
    this._subscription = this._route.data.subscribe(data => {
      this._batchImport = data['batch'];

      this._route.queryParamMap.subscribe(params => {
        this._pager.page = params.get('page') || 0;
        this._loading = true;
        this._service.transactions(this._batchImport.slug, this._pager.page)
          .then(page => this._currentPage = page)
          .finally(() => this._loading = false);
      })
    });
  }

  ngOnDestroy() {
    this._subscription.unsubscribe();
  }

  pageChanged() {
    this._router.navigate(['/import', this._batchImport.slug, 'status'], {
      queryParams: {
        page: this._pager.page
      }
    })
  }

  confirmDelete(transaction: Transaction) {
    let modalRef = this._modelService.open(ConfirmModalComponent);
    modalRef.componentInstance.titleTextKey = 'common.action.delete';
    modalRef.componentInstance.descriptionKey = 'page.transactions.delete.confirm';
    modalRef.result
      .then(() => this._service.deleteTransaction(this._batchImport.slug, transaction.id)
        .then(() => this.pageChanged()))
      .catch(noop)
  }

  transactionSelected(id: number) {
    return this._selected.indexOf(id) > -1;
  }

  openMultiEdit(): void {
    let modalRef = this._modelService.open(MultiEditModalComponent);
    modalRef.componentInstance.transactions = this._selected;
    modalRef.result
      .then(() => {
        this._loading = true;
        this._service.transactions(this._batchImport.slug, this._pager.page)
          .then(page => this._currentPage = page)
          .finally(() => this._loading = false);
        this._selected = [];
      })
  }

  toggleTransaction(id: number) {
    let existing = this._selected.indexOf(id);
    if (existing > -1) {
      this._selected.splice(existing, 1);
    } else {
      this._selected.push(id);
    }
  }

  get batchImport(): BatchImport {
    return this._batchImport
  }

  get page(): Page<Transaction> {
    return this._currentPage
  }

  get pager(): Pagable {
    return this._pager
  }

  get loading(): boolean {
    return this._loading;
  }

  get empty() : boolean {
    return !this._currentPage || this._currentPage.info.records == 0;
  }

  get hasSelection(): boolean {
    return this._selected.length > 0
  }
}
