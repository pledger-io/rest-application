import {Component, OnDestroy, OnInit} from '@angular/core';
import {Pagable, Page} from '../../core/models/pageable';
import {Transaction} from '../../core/models/transaction';
import {ActivatedRoute, Router} from '@angular/router';
import {TransactionService} from '../transaction.service';
import {DateRange} from '../../core/models/date-range';
import {Subscription} from 'rxjs';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {ToastService} from '../../core/core-services';
import {AccountSelectComponent} from '../account-select/account-select.component';
import {EntityRef, EntityType} from '../../core/core-models';
import {GenericTransactionOverviewComponent} from "../../core/component/generic-transaction-overview.component";

interface Filters {
  show?: boolean;
  description?: string;
  account?: string;
  onlyIncome?: boolean;
  onlyExpense?: boolean;
  category?: EntityRef;
  budget?: EntityRef;
  tag?: string;
  currency?: string;
  forServer(): Filters;
}

@Component({
  selector: 'app-global-overview',
  templateUrl: './global-overview.component.html',
  styleUrls: ['./global-overview.component.scss']
})
export class GlobalOverviewComponent extends GenericTransactionOverviewComponent implements OnInit, OnDestroy {
  EntityType = EntityType;

  private _initDone: any;
  private _loading: boolean;
  private _pager: Pagable;
  private _dateRange: DateRange;
  private _currentPage: Page<Transaction>;
  private _subscription: Subscription;
  private _transfers: boolean;
  private _type: string;
  private _filterSettings: Filters;

  constructor(private _route: ActivatedRoute,
              private _service: TransactionService,
              private _router: Router,
              toastService: ToastService,
              modelService: NgbModal) {
    super(
      transaction => this._service.delete(transaction.source.id, transaction.id),
      modelService,
      toastService);

    this.pageChanged = () => {
      let category = null;
      let budget = null;

      if (this._filterSettings.category) {
        category = JSON.stringify(new EntityRef(this._filterSettings.category.id, this._filterSettings.category.name));
      }
      if (this._filterSettings.budget) {
        budget = JSON.stringify(new EntityRef(this._filterSettings.budget.id, this._filterSettings.budget.name));
      }

      this._router.navigate(['/transactions', this._type, this._dateRange.from, this._dateRange.until], {
        queryParams: {
          page: this._pager.page,
          account: this._filterSettings.account,
          description: this._filterSettings.description,
          revenue: this._filterSettings.onlyIncome,
          expenses: this._filterSettings.onlyExpense,
          currency: this._filterSettings.currency,
          category,
          budget
        }
      });
    }
  }

  get dateRange(): DateRange {
    return this._dateRange;
  }

  get loading(): boolean {
    return this._loading;
  }

  get pager(): Pagable {
    return this._pager;
  }

  get currentPage(): Page<Transaction> {
    return this._currentPage;
  }

  get filter(): Filters {
    return this._filterSettings;
  }

  ngOnInit(): void {
    super.ngOnInit();
    this._filterSettings = {
      show: true,
      forServer(): Filters {
        return {
          onlyExpense: this.onlyExpense,
          onlyIncome: this.onlyIncome,
          account: this.account,
          description: this.description,
          category: typeof this.category === 'string' ? null : this.category,
          budget: typeof this.budget === 'string' ? null : this.budget,
          currency: this.currency,
          forServer(): Filters {
            return null;
          }
        } as Filters;
      }
    } as Filters;
    this._pager = new Pagable(1, 20);

    this._initDone = {
      params: false,
      resolver: false
    };

    this._subscription = this._route.queryParamMap.subscribe(params => {
      this._pager.page = params.get('page') || 1;

      this._filterSettings.account = params.get('account');
      this._filterSettings.description = params.get('description');
      this._filterSettings.onlyExpense = params.get('expenses') === 'true';
      this._filterSettings.onlyIncome = params.get('revenue') === 'true';
      this._filterSettings.currency = params.get('currency');

      if (params.get('category')) {
        this._filterSettings.category = JSON.parse(params.get('category'));
      }
      if (params.get('budget')) {
        this._filterSettings.budget = JSON.parse(params.get('budget'));
      }

      this._initDone.params = true;
      this.load();
    }).add(this._route.data.subscribe(data => {
      this._dateRange = data.dateRange;
      this._transfers = data.transfers;

      this._filterSettings.show = !this._transfers;

      this._type = this._transfers ? 'transfers' : 'income-expense';

      this._initDone.resolver = true;
      this.load();
    }));
  }

  ngOnDestroy(): void {
    this._subscription.unsubscribe();
  }

  private load() {
    if (this._initDone.resolver && this._initDone.params) {
      this._loading = true;
      this._service.search(this._dateRange, this._transfers, this._pager.page, this._filterSettings.forServer())
        .then(transactions => this._currentPage = transactions)
        .finally(() => this._loading = false);
    }
  }

  pagerUpdated(page: number) {
    if (page !== this.pager.page) {
      this.pager.page = page;
      this.pageChanged();
    }
  }

  computeAmount(transaction: Transaction) {
    if (transaction.source.isOwn() && !transaction.destination.isOwn()) {
      return -transaction.amount;
    }

    return transaction.amount;
  }

  openCreateTransaction(type: string) {
    const modalRef = this.modalService.open(AccountSelectComponent);
    modalRef.componentInstance.type = type;
    modalRef.result
      .then(account => this._router.navigate([
        '/accounts',
        account.frontEndType,
        account.id,
        'transaction',
        'add',
        type
      ]));
  }

  checkboxChange(property: string) {
    if (this._filterSettings[property]) {
      this._filterSettings.onlyExpense = false;
      this._filterSettings.onlyIncome = false;
      this._filterSettings[property] = true;
    }
  }

}
