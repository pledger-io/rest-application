import { Component, OnInit } from '@angular/core';
import {TransactionRuleService} from "../transaction-rule.service";
import {TransactionRuleGroup} from "../transaction-rule.models";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {RuleGroupRenameModalComponent} from "../rule-group-rename-modal/rule-group-rename-modal.component";

@Component({
  selector: 'app-rule-overview',
  templateUrl: './rule-overview.component.html',
  styleUrls: ['./rule-overview.component.scss']
})
export class RuleOverviewComponent implements OnInit {

  private _ruleGroups: TransactionRuleGroup[];

  constructor(private _service: TransactionRuleService,
              private _modalService: NgbModal) { }

  get groups(): TransactionRuleGroup[] {
    return this._ruleGroups;
  }

  ngOnInit() {
    this._service.groups()
      .then(groups => this._ruleGroups = groups);
  }

  addGroup() {
    let modalRef = this._modalService.open(RuleGroupRenameModalComponent);
    modalRef.result
      .then(() => this.ngOnInit())
  }

  up(group : string) {
    this._service.groupUp(group)
      .then(() => this.ngOnInit());
  }

  down(group : string) {
    this._service.groupDown(group)
      .then(() => this.ngOnInit());
  }

  rename(group: TransactionRuleGroup) {
    let modalRef = this._modalService.open(RuleGroupRenameModalComponent);
    modalRef.componentInstance.group = group;
    modalRef.result
      .then(() => this.ngOnInit())
  }

}
