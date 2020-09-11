import {Injectable} from '@angular/core';
import {Page} from "../core/models/pageable";
import {Transaction} from "../core/models/transaction";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {map} from "rxjs/operators";
import {DateRange} from "../core/models/date-range";
import {Moment} from "moment";
import * as moment from "moment";
import {EntityRef} from "../core/models/entity";


@Injectable({
  providedIn: 'root'
})
export class TransactionService {

  constructor(private _http: HttpClient) { }

  search(range: DateRange, transfers: boolean, page: number, filters: any = {}): Promise<Page<Transaction>> {
    return this._http.post<Page<Transaction>>(environment.backend + 'transactions', Object.assign(filters, {
      dateRange: {
        start: range.from,
        end: range.until
      },
      transfers: transfers,
      page: page
    })).pipe(
        map(page => {
          page.content = page.content.map(t => Transaction.fromRemote(t));
          return page;
        })
      ).toPromise()
  }

  delete(id: number, transactionId: number): Promise<any> {
    return this._http.delete(environment.backend + 'accounts/' + id + '/transactions/' + transactionId).toPromise();
  }

  patch(ids: number[], body = {}) {
    return this._http.patch<void>(environment.backend + 'transactions', Object.assign({transactions: ids}, body))
      .toPromise();
  }

}
