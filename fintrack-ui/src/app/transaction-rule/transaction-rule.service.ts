import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {TransactionRule, TransactionRuleGroup} from "./transaction-rule.models";
import {environment} from "../../environments/environment";
import {map} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class TransactionRuleService {

  constructor(private _http: HttpClient) { }

  groups(): Promise<TransactionRuleGroup[]> {
    return this._http.get<TransactionRuleGroup[]>(environment.backend + 'transaction-rules/groups')
      .pipe(map(raw => raw.map(group => new TransactionRuleGroup(group.name, group.sort))))
      .toPromise();
  }

  groupUp(group: string): Promise<void> {
    return this._http.get<void>(environment.backend + 'transaction-rules/groups/' + group + '/move-up')
      .toPromise()
  }

  groupDown(group: string): Promise<void> {
    return this._http.get<void>(environment.backend + 'transaction-rules/groups/' + group + '/move-down')
      .toPromise()
  }

  createGroup(group: string): Promise<void> {
    return this._http.put<void>(environment.backend + 'transaction-rules/groups/', {
      name: group
    }).toPromise();
  }

  groupRename(group: string, updatedName: string): Promise<void> {
    return this._http.patch<void>(environment.backend + 'transaction-rules/groups/' + group, {
      name: updatedName
    }).toPromise()
  }

  rules(group: string): Promise<TransactionRule[]> {
    return this._http.get<TransactionRule[]>(environment.backend + 'transaction-rules/groups/' + group)
      .pipe(map(raw =>
        raw.map(rule => new TransactionRule(rule.id, rule.name, rule.description, rule.active,
          rule.restrictive, rule.changes, rule.conditions))))
      .toPromise();
  }

  createRule(group: string, ruleForm: any): Promise<void> {
    return this._http.put<void>(environment.backend + 'transaction-rules/groups/' + group, ruleForm)
      .toPromise()
  }

  updateRule(group: string, id: number, ruleForm: any): Promise<void> {
    return this._http.post<void>(environment.backend + 'transaction-rules/groups/' + group + '/' + id, ruleForm)
      .toPromise();
  }

  rule(group: string, id: number): Promise<TransactionRule> {
    return this._http.get<TransactionRule>(environment.backend + 'transaction-rules/groups/' + group + '/' + id)
      .pipe(map(raw => new TransactionRule(raw.id, raw.name, raw.description, raw.active,
        raw.restrictive, raw.changes, raw.conditions)))
      .toPromise()
  }

  ruleUp(group: string, id: number): Promise<void> {
    return this._http.get<void>(environment.backend + 'transaction-rules/groups/' + group + '/' + id + '/move-up')
      .toPromise()
  }

  ruleDown(group: string, id: number): Promise<void> {
    return this._http.get<void>(environment.backend + 'transaction-rules/groups/' + group + '/' + id + '/move-down')
      .toPromise()
  }

  delete(group: string, id: number): Promise<void> {
    return this._http.delete<void>(environment.backend + 'transaction-rules/groups/' + group + '/' + id).toPromise();
  }

}
