import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Contract, ContractOverview} from "./contract-models";
import {AccountRef, Page, Transaction} from "../core/core-models";
import {map} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class ContractService {

  constructor(private _http: HttpClient) { }

  list() : Promise<ContractOverview> {
    return this._http.get<ContractOverview>(environment.backend + 'contracts').toPromise();
  }

  create(request: any) : Promise<void> {
    return this._http.put<void>(environment.backend + 'contracts', request).toPromise();
  }

  get(id: number) : Promise<Contract> {
    return this._http.get<Contract>(environment.backend + 'contracts/' + id).toPromise();
  }

  update(id: number, request: any) : Promise<void> {
    return this._http.post<void>(environment.backend + 'contracts/' + id, request).toPromise();
  }

  delete(id: number) : Promise<void> {
    return this._http.delete<void>(environment.backend + 'contracts/' + id).toPromise();
  }

  transactions(id: number, page: number): Promise<Page<Transaction>> {
    return this._http.get<Page<Transaction>>(environment.backend + 'contracts/' + id + '/transactions?page=' + page)
      .pipe(
        map(page => {
          page.content = page.content.map(transaction => {
            transaction.source = new AccountRef(transaction.source.id, transaction.source.type, transaction.source.name);
            transaction.destination = new AccountRef(transaction.destination.id, transaction.destination.type, transaction.destination.name);
            return transaction;
          })
          return page;
        })
      )
      .toPromise();
  }

  attachment(id: number, fileToken: string) : Promise<void> {
    return this._http.post<void>(environment.backend + 'contracts/'+ id +'/attachment', {
      fileCode: fileToken
    }).toPromise();
  }

  warnExpiry(id: number): Promise<void> {
    return this._http.get<void>(environment.backend + 'contracts/' + id + '/expire-warning').toPromise();
  }

}
