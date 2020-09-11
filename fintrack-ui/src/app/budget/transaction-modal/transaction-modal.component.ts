import { Component, OnInit } from '@angular/core';
import {BudgetService, Expense} from "../budget.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {Page, Transaction} from "../../core/core-models";

@Component({
  selector: 'app-transaction-modal',
  templateUrl: './transaction-modal.component.html',
  styleUrls: ['./transaction-modal.component.scss']
})
export class TransactionModalComponent implements OnInit {

  private _id: number;
  private _month: number;
  private _year: number;

  private _loading: boolean;
  private _page: number;
  private _transactions: Page<Transaction>;

  constructor(private _service: BudgetService,
              private _modal: NgbActiveModal) { }

  get page(): number {
    return this._page
  }

  set page(page: number) {
    this._page = page;
  }

  get loading() : boolean {
    return this._loading
  }

  get empty() : boolean {
    return !this._transactions || this._transactions.info.records == 0;
  }

  get transactions(): Page<Transaction> {
    return this._transactions
  }

  data(expense: Expense, year, month) {
    this._page = 1;
    this._id = expense.id;
    this._year = year;
    this._month = month;
    this.pageChanged();
  }

  pageChanged() {
    this._loading = true;
    this._service.transactions(this._id, this._year, this._month, this._page)
      .then(transactions => this._transactions = transactions)
      .finally(() => this._loading = false);
  }

  ngOnInit(): void {
  }

  dismiss(): void {
    this._modal.dismiss();
  }

}
