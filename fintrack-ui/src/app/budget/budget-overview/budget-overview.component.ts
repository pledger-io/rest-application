import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {Budget, BudgetService} from "../budget.service";
import {DateRange} from "../../core/core-models";
import {Subscription} from "rxjs";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CreateExpenseModalComponent} from "../create-expense-modal/create-expense-modal.component";
import {TransactionModalComponent} from "../transaction-modal/transaction-modal.component";

@Component({
  selector: 'app-budget-overview',
  templateUrl: './budget-overview.component.html',
  styleUrls: ['./budget-overview.component.scss']
})
export class BudgetOverviewComponent implements OnInit, OnDestroy {

  year: number;
  month: number;

  activeBudget: Budget;
  cachedComputes = {};

  private _budgetLoaded: boolean;
  private _createModel = {};
  private _dataSubscription: Subscription;

  constructor(private route: ActivatedRoute,
              private budgetService: BudgetService,
              private modelService: NgbModal) {
  }

  ngOnInit() {
    this._dataSubscription = this.route.data.subscribe(data => {
      let dateRange: DateRange = data['dateRange'];

      this.year = dateRange.computeStartYear();
      this.month = dateRange.computeStartMonth() + 1;
      this.loadBudget();
    });
  }

  ngOnDestroy() {
    this._dataSubscription.unsubscribe();
  }

  private loadBudget() {
    this._budgetLoaded = false;
    this.activeBudget = null;

    this.budgetService.getBudget(this.year, this.month).then(budget => {
      this._budgetLoaded = true;
      this.activeBudget = budget;
      if (this.activeBudget) {
        this.activeBudget.expenses.forEach(expense => {
          this.budgetService.computeExpense(expense.id, this.year, this.month)
            .then(computed => this.cachedComputes[expense.id] = computed);
        });
      }
    });
  }

  get loaded(): boolean {
    return this._budgetLoaded;
  }

  get months(): number[] {
    return [...Array(12).keys()].map(x => x + 1);
  }

  get createModel() {
    return this._createModel;
  }

  loadTransactions(expense) {
    let modalRef = this.modelService.open(TransactionModalComponent, {size: 'xl'});
    modalRef.componentInstance.data(expense, this.year, this.month);
  }

  createBudget() {
    this.budgetService.createBudget(this.createModel['year'], this.createModel['month'], this.createModel['expectedIncome'])
      .then(() => this.loadBudget());
  }

  createExpense() {
    this.modelService.open(CreateExpenseModalComponent).result
      .then(() => this.loadBudget())
  }
}
