import {Component, OnInit} from '@angular/core';
import {TransactionRuleGroup} from "../transaction-rule.models";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {TransactionRuleService} from "../transaction-rule.service";

@Component({
  selector: 'app-rule-group-rename-modal',
  templateUrl: './rule-group-rename-modal.component.html',
  styleUrls: ['./rule-group-rename-modal.component.scss']
})
export class RuleGroupRenameModalComponent implements OnInit {
  private _processing: boolean;
  private _name: string;
  private _group: TransactionRuleGroup;

  constructor(private _modal: NgbActiveModal,
              private _service: TransactionRuleService) {
  }

  get processing(): boolean {
    return this._processing;
  }

  get name(): string {
    return this._name;
  }

  set name(value: string) {
    this._name = value;
  }

  set group(value: TransactionRuleGroup) {
    this._group = value;
  }

  ngOnInit(): void {
    this._name = this._group.name
  }

  process() {
    if (this._group) {
      this._service.groupRename(this._group.name, this._name)
        .then(() => this._modal.close());
    } else {
      this._service.createGroup(this._name)
        .then(() => this._modal.close());
    }
  }

  dismiss() {
    this._modal.dismiss();
  }
}
