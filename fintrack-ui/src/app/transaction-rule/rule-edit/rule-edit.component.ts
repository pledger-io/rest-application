import {Component, OnInit} from '@angular/core';
import {TransactionRuleService} from "../transaction-rule.service";
import {RuleCondition, TransactionRule} from "../transaction-rule.models";
import {ActivatedRoute} from "@angular/router";
import {RouterHistory} from "../../core/router-history";
import {EntityRef, EntityType} from "../../core/core-models";
import {LookupService} from "../lookup.service";

class Matcher {
  constructor(public field: string, public operation: string, public appKey: string = "") {
  }
}

class CustomCondition extends RuleCondition {
  private _selectedMatcher: Matcher;

  constructor(public id: number,
              public field: string,
              public operation: string,
              public condition: string) {
    super(id, field, operation, condition);

    this._selectedMatcher = new Matcher(field, operation);
  }

  get matcher(): Matcher {
    return this._selectedMatcher
  }

  set matcher(value) {
    this._selectedMatcher = value;
    this.field = value.field;
    this.operation = value.operation;
  }
}

class CustomChange {
  constructor(public id: number,
              public field: string,
              public change: EntityRef) {
  }

  clearValue() {
    this.change = null;
  }
}

class RuleEditForm {
  constructor(public name: string = "",
              public description: string = "",
              public restrictive: boolean = false,
              public active: boolean = false,
              public changes: CustomChange[] = [],
              public conditions: CustomCondition[] = []) {
  }
}

@Component({
  selector: 'app-rule-edit',
  templateUrl: './rule-edit.component.html',
  styleUrls: ['./rule-edit.component.scss']
})
export class RuleEditComponent implements OnInit {

  private _model: RuleEditForm;
  private _processing: boolean;

  private _group: string;
  private _matchers: Matcher[];
  private _ruleId: number;

  constructor(private _service: TransactionRuleService,
              private _route: ActivatedRoute,
              private _history: RouterHistory,
              private _lookupService: LookupService) {
    this._matchers = [
      new Matcher('AMOUNT', 'LESS_THAN', "amountLessThan"),
      new Matcher('AMOUNT', 'MORE_THAN', "amountMoreThan"),
      new Matcher('SOURCE_ACCOUNT', 'EQUALS', "sourceAccountEquals"),
      new Matcher('SOURCE_ACCOUNT', 'STARTS_WITH', "sourceAccountStartsWith"),
      new Matcher('SOURCE_ACCOUNT', 'CONTAINS', "sourceAccountContains"),
      new Matcher('TO_ACCOUNT', 'EQUALS', "toAccountEquals"),
      new Matcher('TO_ACCOUNT', 'STARTS_WITH', "toAccountStartsWith"),
      new Matcher('TO_ACCOUNT', 'CONTAINS', "toAccountContains"),
      new Matcher('DESCRIPTION', 'EQUALS', "descriptionEquals"),
      new Matcher('DESCRIPTION', 'STARTS_WITH', "descriptionStartsWith"),
      new Matcher('DESCRIPTION', 'CONTAINS', "descriptionContains"),
      new Matcher('CATEGORY', 'EQUALS', "categoryEquals"),
    ];
  }

  get model(): RuleEditForm {
    return this._model
  }

  get processing(): boolean {
    return this._processing
  }

  get matchers(): Matcher[] {
    return this._matchers
  }

  ngOnInit() {
    this._ruleId = null;
    this._route.paramMap.subscribe(map => this._group = map.get('group'));
    this._route.data.subscribe(data => {
      let transactionRule = data.rule as TransactionRule;
      this._model = new RuleEditForm();

      if (transactionRule) {
        this._ruleId = transactionRule.id;
        this._model.name = transactionRule.name;
        this._model.description = transactionRule.description;
        this._model.active = transactionRule.active;
        this._model.restrictive = transactionRule.restrictive;
        this._model.changes = [];
        transactionRule.changes.forEach(change =>
          this._lookupService.lookup(change.field, parseInt(change.change))
              .then(name =>
                this._model.changes.push(new CustomChange(change.id, change.field, new EntityRef(parseInt(change.change), name)))));
        this._model.conditions = transactionRule.conditions
          .map(cond => new CustomCondition(cond.id, cond.field, cond.operation, cond.condition));
      }
    });
  }

  compareSelectCondition(c1: CustomCondition, c2: CustomCondition) {
    return c1 != null && c2 != null &&
      c1.operation == c2.operation && c1.field == c2.field;
  }

  addCondition(): void {
    this._model.conditions.push(new CustomCondition(null, "TO_ACCOUNT", 'EQUALS', null));
  }

  addChange(): void {
    this._model.changes.push(new CustomChange(null, null, null));
  }

  removeCondition(condition: CustomCondition, indice: number): void {
    this._model.conditions[indice] = null;
    this._model.conditions = this._model.conditions.filter(c => c != null);
  }

  removeChange(change: CustomChange, index: number): void {
    this._model.changes[index] = null;
    this._model.changes = this._model.changes.filter(c => c != null);
  }

  toEntity(change: CustomChange) {
    switch (change.field) {
      case 'CHANGE_TRANSFER_TO':
      case 'CHANGE_TRANSFER_FROM':
        return EntityType.OWN_ACCOUNT;
      case 'SOURCE_ACCOUNT':
        return EntityType.DEBIT_ACCOUNT;
      case 'TO_ACCOUNT':
        return EntityType.CREDIT_ACCOUNT;
      case 'BUDGET':
        return EntityType.BUDGET;
      case 'CATEGORY':
        return EntityType.CATEGORY;
      case 'CONTRACT':
        return EntityType.CONTRACT;
      case 'TAGS':
        return EntityType.TAG;
    }
  }

  save() {
    let model = {
      name: this._model.name,
      description: this._model.description,
      restrictive: this._model.restrictive,
      active: this._model.active,
      changes: [],
      conditions: []
    };

    model.changes = this._model.changes.map(c => {
      return {
        id: c.id,
        column: c.field,
        value: "" +c.change.id
      }
    });
    model.conditions = this._model.conditions.map(c => {
      return {
        id: c.id,
        column: c.field,
        operation: c.operation,
        value: c.condition
      }
    });

    this._processing = true;

    let completion: Promise<void>;
    if (this._ruleId) {
      completion = this._service.updateRule(this._group, this._ruleId, model);
    } else {
      completion = this._service.createRule(this._group, model);
    }

    completion.then(() => this._history.previous())
      .finally(() => this._processing = false);
  }

}
