import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from "@angular/router";
import {DateRange} from "../core/models/date-range";
import {Observable, ReplaySubject, Subject} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {map} from "rxjs/operators";
import * as moment from "moment";

@Injectable({
  providedIn: 'root'
})
export class FirstDateResolverService implements Resolve<DateRange> {

  private _typePublisher: Subject<string>;
  private _rangePublisher: Subject<DateRange>;

  constructor(private _http: HttpClient) {
    this._typePublisher = new ReplaySubject<string>();
    this._rangePublisher = new ReplaySubject<DateRange>()
  }

  get type$(): Observable<string> {
    return this._typePublisher.asObservable();
  }

  get $(): Observable<DateRange> {
    return this._rangePublisher.asObservable();
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<DateRange> | Promise<DateRange> | DateRange {
    let transfers: boolean = route.data['transfers'];

    this._typePublisher.next(transfers ? 'transfers' : 'income-expense');

    return this._http.post<string>(environment.backend + 'transactions/locate-first', {
      transfers: transfers
    }).pipe(
      map(raw => {
        let dateRange = DateRange.forRange(raw, moment().format(environment.isoDateFormat));
        this._rangePublisher.next(dateRange);
        return dateRange;
      })
    );
  }

}
