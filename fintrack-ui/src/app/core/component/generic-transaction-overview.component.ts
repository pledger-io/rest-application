import {Page, Transaction} from "../core-models";
import {OnInit} from "@angular/core";
import {ConfirmModalComponent} from "../confirm-modal/confirm-modal.component";
import {noop} from "rxjs";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {MultiEditModalComponent} from "../../transaction/multi-edit-modal/multi-edit-modal.component";
import {ToastService} from "../services/toast.service";

export class GenericTransactionOverviewComponent implements OnInit {

  transactions: Page<Transaction>;
  pageChanged: () => void = noop;

  private selectedTransactions: number[];
  private deleteOperation: (transaction) => Promise<void>;

  constructor(deleteOperation: (transaction) => Promise<void>,
              protected modalService: NgbModal,
              protected toastService: ToastService) {
    this.deleteOperation = deleteOperation;
  }

  get hasSelection() {
    return this.selectedTransactions.length > 0;
  }

  ngOnInit() {
    this.selectedTransactions = [];
  }

  isSelected(id: number) {
    return this.selectedTransactions.indexOf(id) > -1;
  }

  onTransactionSelect(id: number) {
    let existing = this.selectedTransactions.indexOf(id);
    if (existing > -1) {
      this.selectedTransactions.splice(existing, 1);
    } else {
      this.selectedTransactions.push(id);
    }
  }

  onTransactionSplitClick(transaction: Transaction): void {
    if (transaction.split) {
      transaction['detailed'] = !transaction['detailed'];
    }
  }

  onMultiEditClick(): void {
    let modalRef = this.modalService.open(MultiEditModalComponent);
    modalRef.componentInstance.transactions = this.selectedTransactions;
    modalRef.result
      .then(() => {
        this.pageChanged();
        this.selectedTransactions = [];
      })
  }

  onTransactionDeleteClick(transaction: Transaction) {
    let modalRef = this.modalService.open(ConfirmModalComponent);
    modalRef.componentInstance.titleTextKey = 'common.action.delete';
    modalRef.componentInstance.descriptionKey = 'page.transactions.delete.confirm';
    modalRef.result
      .then(() => this.deleteOperation(transaction)
                    .then(() => this.pageChanged())
                    .then(() => this.toastService.success('page.transactions.delete.success')))
      .catch(noop);
  }


}
