import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from "@angular/router";
import {Observable, ReplaySubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class YearResolverService implements Resolve<number> {

  private subject: ReplaySubject<number>;
  private reportSubject: ReplaySubject<string>;

  constructor() {
    this.subject = new ReplaySubject();
    this.reportSubject = new ReplaySubject();
  }

  get $(): Observable<number> {
    return this.subject.asObservable();
  }

  get report$(): Observable<string> {
    return this.reportSubject.asObservable();
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<number> | Promise<number> | number {
    let year = this.locate(route);
    if (!year) {
      year = new Date().getFullYear();
    }

    this.reportSubject.next(route.url[0].path);
    this.subject.next(year);
    return year;
  }

  private locate(route: ActivatedRouteSnapshot): number | null {
    let year = route.paramMap.get('year');

    if (year) {
      return parseInt(year);
    }

    if (route.children.length > 0) {
      let ranges = route.children
        .map(child => this.locate(child))
        .filter(range => range != null);

      if (ranges.length) {
        return ranges[0];
      }
    }

    return null;
  }
}
