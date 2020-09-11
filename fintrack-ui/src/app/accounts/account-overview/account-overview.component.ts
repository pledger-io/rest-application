import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Account, AccountService} from '../account.service';
import {ConfirmModalComponent} from '../../core/confirm-modal/confirm-modal.component';
import {Pagable, Page} from '../../core/core-models';
import {Subscription} from 'rxjs';
import {ToastService} from '../../core/core-services';

@Component({
  selector: 'app-account-overview',
  templateUrl: './account-overview.component.html',
  styleUrls: ['./account-overview.component.scss']
})
export class AccountOverviewComponent implements OnInit, OnDestroy {
  public frontEndType: string;

  accountType: string;
  pager: Pagable;
  currentPage: Page<Account>;
  loading: boolean;
  filterVisible: boolean;
  filterName: string;

  private _subscription: Subscription;

  constructor(private route: ActivatedRoute,
              private _router: Router,
              private accountService: AccountService,
              private modelService: NgbModal,
              private _toastyService: ToastService) {
    this.pager = new Pagable(0, 20);
    this._subscription = new Subscription();
  }

  ngOnInit() {
    this.filterName = '';
    this._subscription.add(this.route.paramMap.subscribe(map => {
      this.frontEndType = map.get('type');
      this.accountType = this.frontEndType == 'expense' ? 'creditor' : 'debtor';
      this.load();
    }));
    this._subscription.add(this.route.queryParamMap.subscribe(params => {
      this.pager.page = params.get('page') || 1;
      this.filterName = params.get('filter') || '';
      this.filterVisible = this.filterName.length > 0;

      this.load();
    }));
  }

  ngOnDestroy() {
    this._subscription.unsubscribe();
  }

  private load() {
    if (this.accountType && this.pager.page > 0) {
      this.loading = true;
      const filter = this.filterName !== '' ? this.filterName : null;
      this.accountService.getAccounts([this.accountType], this.pager.page, filter)
        .then(page => this.currentPage = page)
        .finally(() => this.loading = false);
    }
  }

  get empty(): boolean {
    return !this.currentPage || this.currentPage.info.records == 0;
  }

  pageChanged(page: number) {
    if (this.pager.page !== page) {
      this.pager.page = page;
      this.applyChanges();
    }
  }

  applyChanges() {
    this._router.navigate(['/accounts', this.frontEndType], {
      queryParams: {
        page: this.pager.page,
        filter: this.filterName
      }
    });
  }

  filterChange() {
    if (this.filterName.length === 0 || this.filterName.length > 2) {
      this.applyChanges();
    }
  }

  confirmDelete(account: Account) {
    const modalRef = this.modelService.open(ConfirmModalComponent);
    modalRef.componentInstance.titleTextKey = 'common.action.delete';
    modalRef.componentInstance.descriptionKey = 'page.accounts.delete.confirm';
    modalRef.result
      .then(() => this.accountService.delete(account.id)
        .then(() => this.applyChanges())
        .then(() => this._toastyService.success('page.account.delete.success'))
        .catch(() => this._toastyService.warning('page.account.delete.failed'))
      );
  }

}
