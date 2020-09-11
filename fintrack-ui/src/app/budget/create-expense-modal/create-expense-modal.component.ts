import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {BudgetService} from "../budget.service";

class CreateExpenseRequest {
  constructor(public name: string,
              public lowerBound: number,
              public upperBound: number) {
  }
}

@Component({
  selector: 'app-create-expense-modal',
  templateUrl: './create-expense-modal.component.html',
  styleUrls: ['./create-expense-modal.component.scss']
})
export class CreateExpenseModalComponent implements OnInit, AfterViewInit {

  @ViewChild("name")
  private _nameField: ElementRef;

  private _model: CreateExpenseRequest;
  private _processing: boolean;

  constructor(public _modal: NgbActiveModal, private _service: BudgetService) {
    this._processing = false;
  }

  get model(): CreateExpenseRequest {
    return this._model;
  }

  get processing(): boolean {
    return this._processing
  }

  ngOnInit() {
    this._model = new CreateExpenseRequest(null, null, null);
  }

  ngAfterViewInit() {
    this._nameField.nativeElement.focus();
  }

  dismiss() {
    this._modal.dismiss('close');
  }

  create() {
    this._processing = true;
    this._service.createExpense(this._model)
      .then(budget => this._modal.close(budget))
      .finally(() => this._processing = false);
  }
}
