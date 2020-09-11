import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from "@angular/router";
import {TransactionRule} from "./transaction-rule.models";
import {TransactionRuleService} from "./transaction-rule.service";

@Injectable({
  providedIn: 'root'
})
export class TransactionRuleResolverService implements Resolve<TransactionRule> {

  constructor(private _service: TransactionRuleService) { }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<TransactionRule> | TransactionRule {
    if (route.paramMap.get('id')) {
      let ruleId: number = parseInt(route.paramMap.get('id'));
      return this._service.rule(route.paramMap.get('group'), ruleId);
    }

    return undefined;
  }

}
