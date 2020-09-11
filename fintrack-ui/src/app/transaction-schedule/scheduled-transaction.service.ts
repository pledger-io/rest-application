import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {ScheduledTransaction} from "./scheduled-transaction.models";

@Injectable({
  providedIn: 'root'
})
export class ScheduledTransactionService {

  constructor(private _http: HttpClient) { }

  list(): Promise<ScheduledTransaction[]> {
    return this._http.get<ScheduledTransaction[]>(environment.backend + 'schedule/transaction')
      .toPromise();
  }

  create(body: any): Promise<void> {
    return this._http.put<void>(environment.backend + 'schedule/transaction', body).toPromise();
  }

  get(scheduleId: number): Promise<ScheduledTransaction> {
    return this._http.get<ScheduledTransaction>(environment.backend + 'schedule/transaction/' + scheduleId)
      .toPromise();
  }

  update(scheduleId: number, body: any): Promise<void> {
    return this._http.patch<void>(environment.backend + 'schedule/transaction/'+ scheduleId, body).toPromise();
  }

  delete(scheduleId: number): Promise<void> {
    return this._http.delete<void>(environment.backend + 'schedule/transaction/'+ scheduleId).toPromise();
  }

}
