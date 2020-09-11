import {Component, OnDestroy, OnInit} from '@angular/core';
import {EntityRef, EntityType} from "../../core/core-models";
import {ContractService} from "../contract.service";
import {RouterHistory} from "../../core/router-history";
import {Contract} from "../contract-models";
import {ActivatedRoute} from "@angular/router";
import {Subscription} from "rxjs";
import {NgbDateParserFormatter} from "@ng-bootstrap/ng-bootstrap";
import {CustomDateParserFormatter} from "../../core/custom-date-parser-formatter";

class EditForm {
  constructor(public name: string = "",
              public description: string = "",
              public company: EntityRef = null,
              public start: string = "",
              public end: string = "") {
  }

  static fromContract(contract: Contract): EditForm {
    return new EditForm(
      contract.name,
      contract.description,
      contract.company,
      contract.start,
      contract.end);
  }
}

@Component({
  selector: 'app-contract-edit',
  templateUrl: './contract-edit.component.html',
  styleUrls: ['./contract-edit.component.scss'],
  providers: [{ provide: NgbDateParserFormatter, useClass: CustomDateParserFormatter }]
})
export class ContractEditComponent implements OnInit, OnDestroy {

  EntityType = EntityType;

  private _id: number;
  private _model: EditForm;
  private _subscriptions: Subscription;

  constructor(private _service: ContractService,
              private _route: ActivatedRoute,
              private _history: RouterHistory) { }

  get model() : EditForm {
    return this._model
  }

  ngOnInit() {
    this._model = new EditForm()
    this._subscriptions = this._route.data.subscribe(data => {
      this._id = data.contract.id;
      this._model = EditForm.fromContract(data.contract);
    });
  }

  ngOnDestroy() {
    this._subscriptions.unsubscribe();
  }

  save() {
    let promise: Promise<void>;

    if (this._id) {
      promise = this._service.update(this._id, this._model);
    } else {
      promise = this._service.create(this._model);
    }

    promise.then(() => this._history.previous());
  }
}
