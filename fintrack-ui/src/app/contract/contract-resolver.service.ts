import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from "@angular/router";
import {Contract} from "./contract-models";
import {ContractService} from "./contract.service";

@Injectable({
  providedIn: 'root'
})
export class ContractResolverService implements Resolve<Contract> {

  constructor(private _service: ContractService) { }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<Contract> | Contract {
    let contractId: number = parseInt(route.paramMap.get('id'));

    return this._service.get(contractId);
  }

}
