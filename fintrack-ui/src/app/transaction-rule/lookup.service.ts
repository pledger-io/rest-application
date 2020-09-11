import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {map} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class LookupService {

  constructor(private _http: HttpClient) { }

  async lookup(dataType: string, entityId: number): Promise<string> {
    let promise: Promise<string>;

    switch (dataType) {
      case 'TO_ACCOUNT':
      case 'FROM_ACCOUNT':
      case 'CHANGE_TRANSFER_TO':
      case 'CHANGE_TRANSFER_FROM':
        promise = this._http.get(environment.backend + 'accounts/' + entityId)
          .pipe(map(account => account['name']))
          .toPromise();
        break;
      case 'BUDGET':
        promise = this._http.get(environment.backend + 'budgets/' + new Date().getFullYear() + '/' + (new Date().getMonth() + 1))
          .pipe(map(budget => {
            let expenses = budget['expenses'] as [];
            let expense = expenses.filter(e => e['id'] == entityId)[0];
            return expense['name'];
          }))
          .toPromise();
        break;
      case 'CATEGORY':
        promise = this._http.get(environment.backend + 'categories/' + entityId)
          .pipe(map(category => category['label']))
          .toPromise()
        break
      case 'CONTRACT':
        promise = this._http.get(environment.backend + 'contracts/' + entityId)
          .pipe(map(contract => contract['name']))
          .toPromise();
        break;
      case 'TAGS':
        promise = new Promise(accept => accept(entityId + ''));
        break;
    }

    return promise;
  }

}
