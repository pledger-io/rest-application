import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from "@angular/router";
import {Transaction} from "../core/core-models";
import {AccountService} from "./account.service";

@Injectable({
  providedIn: 'root'
})
export class TransactionResolverService implements Resolve<Transaction> {

  constructor(private _service: AccountService) { }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<Transaction> | Transaction {
    let paramMap = route.children.length > 0 ? route.children[0].paramMap : route.paramMap;
    if (paramMap.get('accountId') && paramMap.get('transactionId')) {
      return this._service.transaction(parseInt(paramMap.get('accountId')), parseInt(paramMap.get('transactionId')))
    }

    return undefined;
  }

}
