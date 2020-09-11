import {Component, OnDestroy, OnInit} from '@angular/core';
import {Pagable, Page} from "../../core/models/pageable";
import {Account, AccountService} from "../account.service";
import {ActivatedRoute, Router} from "@angular/router";
import {Subscription} from "rxjs";
import {ConfirmModalComponent} from "../../core/confirm-modal/confirm-modal.component";
import {ToastService} from "../../core/services/toast.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

const LIABILITY_TYPES: string[] = [
  'loan',
  'mortgage',
  'debt'
];


@Component({
  selector: 'app-liabilities-overview',
  templateUrl: './liabilities-overview.component.html',
  styleUrls: ['./liabilities-overview.component.scss']
})
export class LiabilitiesOverviewComponent implements OnInit, OnDestroy {

  private pager: Pagable;
  private subscription: Subscription;

  private currentPage: Page<Account>;

  constructor(private service: AccountService,
              private toastService: ToastService,
              private modalService: NgbModal,
              private route: ActivatedRoute,
              private router: Router) { }

  get results(): Page<Account> {
    return this.currentPage;
  }

  get info(): Pagable {
    return this.pager;
  }

  ngOnInit(): void {
    this.pager = new Pagable(1, 20);

    this.subscription = this.route.queryParamMap.subscribe(params => {
      this.pager.page = params.get('page') || 1;
      this.service.getAccounts(LIABILITY_TYPES, this.pager.page, null)
        .then(results => this.currentPage = results);
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  pageChanged(): void {
    this.router.navigate(
      ['/accounts/liability'],
      {
        queryParams: {
          page: this.pager.page
        }
      }
    )
  }

  confirmDelete(account: Account) {
    const modalRef = this.modalService.open(ConfirmModalComponent);
    modalRef.componentInstance.titleTextKey = 'common.action.delete';
    modalRef.componentInstance.descriptionKey = 'page.accounts.delete.confirm';
    modalRef.result
      .then(() => this.service.delete(account.id)
        .then(() => this.toastService.success('page.account.delete.success'))
        .then(() => this.pageChanged())
        .catch(() => this.toastService.warning('page.account.delete.failed'))
      );
  }

}
