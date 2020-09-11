import { Component, OnInit } from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {EntityRef, EntityType} from "../../core/models/entity";
import {TransactionService} from "../transaction.service";

interface EditForm {
  category?: EntityRef
  budget?: EntityRef
  contract?: EntityRef
  tags: string[]
}

@Component({
  selector: 'app-multi-edit-modal',
  templateUrl: './multi-edit-modal.component.html',
  styleUrls: ['./multi-edit-modal.component.scss']
})
export class MultiEditModalComponent implements OnInit {

  EntityType = EntityType;

  private _model: EditForm;
  private _transactions: number[];

  constructor(private _modal: NgbActiveModal,
              private _service: TransactionService) { }

  set transactions(value: number[]) {
    this._transactions = value;
  }

  get model(): EditForm {
    return this._model;
  }

  ngOnInit(): void {
    this._model = {
      tags: []
    };
  }

  dismiss(): void {
    this._modal.dismiss();
  }

  process(): void {
    this._service.patch(this._transactions, {
      category: typeof this._model.category == 'string' ? null : this._model.category,
      contract: typeof this._model.contract == 'string' ? null : this._model.contract,
      budget: typeof this._model.budget == 'string' ? null : this._model.budget,
      tags: this._model.tags.length ? this._model.tags : null
    }).then(() => this._modal.close());
  }

}
