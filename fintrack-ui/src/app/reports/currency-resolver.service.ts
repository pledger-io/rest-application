import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from "@angular/router";
import {Observable, ReplaySubject} from "rxjs";
import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Currency} from "../core/models/currency";
import {environment} from "../../environments/environment";
import {AuthorizationService} from "../core/services/authorization.service";
import {map} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class CurrencyResolverService implements Resolve<Currency> {

  private defaultCurrency: string;
  private subject: ReplaySubject<Currency>;

  constructor(private http: HttpClient, private authService: AuthorizationService) {
    this.subject = new ReplaySubject();
    authService.userProfile$.subscribe(profile => this.defaultCurrency = profile.currency);
  }

  get $(): Observable<Currency> {
    return this.subject.asObservable();
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Currency> | Promise<Currency> | Currency {
    let code = route.queryParamMap.get('currency') || this.defaultCurrency;

    return this.http.get<Currency>(environment.backend + 'settings/currencies/' + code)
      .pipe(
        map(currency => {
          this.subject.next(currency);
          return currency;
        })
      );
  }

}
