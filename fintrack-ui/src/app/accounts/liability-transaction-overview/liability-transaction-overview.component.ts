import {Component} from '@angular/core';
import {Subscription} from "rxjs";
import {Account, AccountService} from "../account.service";
import {ActivatedRoute} from "@angular/router";
import {DateRange, Pagable, Transaction} from "../../core/core-models";
import {GenericTransactionOverviewComponent} from "../../core/component/generic-transaction-overview.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {environment} from "../../../environments/environment";
import * as moment from "moment";
import {ToastService} from "../../core/services/toast.service";

@Component({
  selector: 'app-liability-transaction-overview',
  templateUrl: './liability-transaction-overview.component.html',
  styleUrls: ['./liability-transaction-overview.component.scss']
})
export class LiabilityTransactionOverviewComponent extends GenericTransactionOverviewComponent {

  loading: boolean;
  account: Account;
  pageable: Pagable;
  dateRange: DateRange;
  openingTransaction: Transaction;

  private subscription: Subscription;

  constructor(private service: AccountService,
              private route: ActivatedRoute,
              modalService: NgbModal,
              toastService: ToastService) {
    super(
      transaction => service.deleteTransaction(this.account.id, transaction.id),
      modalService,
      toastService);

    this.pageChanged = () => {
      this.service.transactions(this.account.id, this.pageable.page, this.dateRange)
        .then(page => this.transactions = page)
        .finally(() => this.loading = false);
    }
  }

  ngOnInit(): void {
    super.ngOnInit();

    this.loading = true;
    this.pageable = new Pagable(1, 20);
    this.subscription = this.route.data.subscribe(params => {
      this.account = params.account as Account;
      this.dateRange = params.dateRange as DateRange;

      if (this.account.history.firstTransaction) {
        let correctedLastDate = moment(this.account.history.lastTransaction).add(1, 'day');
        this.dateRange = DateRange.forRange(
          this.account.history.firstTransaction,
          correctedLastDate.format(environment.isoDateFormat));
      }

      this.service.firstTransaction(this.account.id, 'Opening balance')
        .then(transaction => this.openingTransaction = transaction);
      this.pageChanged();
    });
  }

}
