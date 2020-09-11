import {Injectable, Optional} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from "@angular/router";
import {Observable, ReplaySubject} from "rxjs";
import {DateRange} from "./core-models";

@Injectable({
  providedIn: 'root'
})
export class DateRangeResolverService implements Resolve<DateRange> {

  private subject: ReplaySubject<DateRange>;

  constructor() {
    this.subject = new ReplaySubject<DateRange>();
  }

  get $(): Observable<DateRange> {
    return this.subject.asObservable();
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<DateRange> | Promise<DateRange> | DateRange {
    let range = this.locateRange(route);
    if (!range) {
      range = DateRange.currentMonth();
    }

    this.subject.next(range);
    return range;
  }

  private locateRange(route: ActivatedRouteSnapshot): DateRange | null {
    let from = route.paramMap.get('from');
    let until = route.paramMap.get('until');

    if (from && until) {
      return DateRange.forRange(from, until);
    }

    if (route.children.length > 0) {
      let ranges = route.children
        .map(child => this.locateRange(child))
        .filter(range => range != null);

      if (ranges.length) {
        return ranges[0];
      }
    }

    return null;
  }

}
