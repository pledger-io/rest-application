import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {AccountRef, Page, Transaction} from "../core/core-models";
import {BatchConfig, BatchImport, CreateBatchConfigRequest, CreateBatchImportRequest} from "./batch-import.models";
import {map} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class BatchImportService {

  constructor(private _http: HttpClient) { }

  create(request: CreateBatchImportRequest): Promise<BatchImport> {
    return this._http.put<BatchImport>(environment.backend + 'import', request).toPromise();
  }

  list(page: number): Promise<Page<BatchImport>> {
    return this._http.post<Page<BatchImport>>(environment.backend + 'import', {page: page}).toPromise();
  }

  get(batchSlug: string): Promise<BatchImport> {
    return this._http.get<BatchImport>(environment.backend + 'import/' + batchSlug).toPromise();
  }

  config(): Promise<BatchConfig[]> {
    return this._http.get<BatchConfig[]>(environment.backend + 'import/config').toPromise();
  }

  createConfig(request: CreateBatchConfigRequest): Promise<BatchConfig> {
    return this._http.put<BatchConfig>(environment.backend + 'import/config', request).toPromise()
  }

  transactions(batchSlug: string, page: number) {
    return this._http.post<Page<Transaction>>(environment.backend + 'import/' + batchSlug + '/transactions', {
      page: page
    }).pipe(
      map(page => {
        page.content = page.content.map(transaction => {
          transaction.source = new AccountRef(transaction.source.id, transaction.source.type, transaction.source.name);
          transaction.destination = new AccountRef(transaction.destination.id, transaction.destination.type, transaction.destination.name);
          return transaction;
        })
        return page;
      })
    ).toPromise();
  }

  deleteTransaction(batchSlug: string, transactionId: number): Promise<any> {
    return this._http.delete(environment.backend + 'import/' + batchSlug + '/transactions/' + transactionId).toPromise();
  }

}
