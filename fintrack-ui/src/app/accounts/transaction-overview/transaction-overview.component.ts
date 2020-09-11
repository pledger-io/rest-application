import {Component, OnDestroy, OnInit} from '@angular/core';
import {Account, AccountService} from "../account.service";
import {DateRange, Pagable, Page, Transaction} from "../../core/core-models";
import {ActivatedRoute} from "@angular/router";
import {Subscription} from "rxjs";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {GenericTransactionOverviewComponent} from "../../core/component/generic-transaction-overview.component";
import {ToastService} from "../../core/services/toast.service";

@Component({
  selector: 'app-transaction-overview',
  templateUrl: './transaction-overview.component.html',
  styleUrls: ['./transaction-overview.component.scss']
})
export class TransactionOverviewComponent extends GenericTransactionOverviewComponent implements OnInit, OnDestroy {

  private subscription: Subscription;

  account: Account;
  loading: boolean;
  pager: Pagable;
  dateRange: DateRange;
  currentPage: Page<Transaction>;

  constructor(private service: AccountService,
              private route: ActivatedRoute,
              modelService : NgbModal,
              toastService: ToastService) {
    super(
      transaction => this.service.deleteTransaction(this.account.id, transaction.id),
      modelService,
      toastService);

    this.pageChanged = () => {
      this.loading = true;
      this.service.transactions(this.account.id, this.pager.page, this.dateRange)
        .then(page => this.currentPage = page)
        .finally(() => this.loading = false);
    }

    this.loading = false;
  }

  get empty() : boolean {
    return !this.currentPage || this.currentPage.info.records == 0;
  }

  ngOnInit() {
    super.ngOnInit();
    this.pager = new Pagable(1, 20);
    this.subscription = new Subscription();
    this.subscription.add(this.route.data.subscribe(data => {
      this.account = data['account'] as Account;
      this.dateRange = data['dateRange'];
      this.pageChanged();
    }));
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

}
