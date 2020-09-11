import {Component, Input, OnInit} from '@angular/core';
import {TransactionRuleService} from "../transaction-rule.service";
import {TransactionRule} from "../transaction-rule.models";
import {ConfirmModalComponent} from "../../core/confirm-modal/confirm-modal.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-rule-overview-list',
  templateUrl: './rule-overview-list.component.html',
  styleUrls: ['./rule-overview-list.component.scss']
})
export class RuleOverviewListComponent implements OnInit {

  @Input('group')
  private _group: string;
  private _rules: TransactionRule[];

  constructor(private _service: TransactionRuleService,
              private _modalService: NgbModal) { }

  get rules(): TransactionRule[] {
    return this._rules
  }

  get group(): string {
    return this._group
  }

  ngOnInit() {
    this._service.rules(this._group).then(rules => this._rules = rules);
  }

  down(rule: TransactionRule) {
    this._service.ruleDown(this._group, rule.id)
      .then(() => this.ngOnInit());
  }

  up(rule: TransactionRule) {
    this._service.ruleUp(this._group, rule.id)
      .then(() => this.ngOnInit());
  }

  deleteRule(rule: TransactionRule) {
    let modalRef = this._modalService.open(ConfirmModalComponent);
    modalRef.componentInstance.titleTextKey = 'common.action.delete';
    modalRef.componentInstance.descriptionKey = 'page.settings.rules.delete.confirm';

    modalRef.result
      .then(() => this._service.delete(this.group, rule.id).then(() => this.ngOnInit()));
  }

}
