import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot} from '@angular/router';
import {Observable, Subject} from 'rxjs';
import {Breadcrumb} from '../core/core-models';

@Injectable({
  providedIn: 'root'
})
export class BreadcrumbService implements CanActivate {

  private breadcrumbUpdate: Subject<Breadcrumb[]>;
  private componentSubject: Subject<any>;

  constructor() {
    this.breadcrumbUpdate = new Subject<Breadcrumb[]>();
    this.componentSubject = new Subject<any>();
  }

  get update$(): Observable<Breadcrumb[]> {
    return this.breadcrumbUpdate.asObservable();
  }

  get component$(): Observable<any> {
    return this.componentSubject.asObservable();
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (route.data.breadcrumbs) {
      const breadcrumbs = route.data.breadcrumbs as Breadcrumb[];
      this.breadcrumbUpdate.next(breadcrumbs.map(br => this.enrich(br, route)));
    } else {
      this.breadcrumbUpdate.next([]);
    }

    if (route.data.quickNavigation) {
      this.componentSubject.next(route.data.quickNavigation);
    } else {
      this.componentSubject.next(null);
    }

    return true;
  }

  private enrich(breadcrumb: Breadcrumb, route: ActivatedRouteSnapshot): Breadcrumb {
    const enricher = raw => {
      const dataMatcher = /\$[a-zA-Z\.]+/g;
      let match;

      while ((match = dataMatcher.exec(raw)) !== null) {
        const tokens = match[0].substr(1).split('.') as string[];
        let search = route.data;

        tokens.forEach(token => {
          if (token in search) {
            search = search[token];
          }
        });

        if (search) {
          raw = raw.replace(match[0], '' + search);
        }
      }

      return raw;
    };

    const enriched = new Breadcrumb(breadcrumb.getUrl(), breadcrumb.getTextKey());
    if (breadcrumb.getTextKey().indexOf('$') > -1) {
      enriched.textResolver = enricher;
    }

    if (breadcrumb.hasUrl() && breadcrumb.getUrl().indexOf('$') > -1) {
      enriched.urlResolver = enricher;
    }
    return enriched;
  }
}
