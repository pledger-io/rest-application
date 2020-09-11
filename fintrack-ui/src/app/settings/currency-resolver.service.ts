import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from '@angular/router';
import {CurrencyModel} from './settings-models';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CurrencyResolverService implements Resolve<CurrencyModel> {

  constructor(private http: HttpClient) { }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<CurrencyModel> | Promise<CurrencyModel> | CurrencyModel {
    const currencyCode: string = route.paramMap.get('currencyCode');

    return this.http.get<CurrencyModel>(environment.backend + 'settings/currencies/' + currencyCode);
  }
}
