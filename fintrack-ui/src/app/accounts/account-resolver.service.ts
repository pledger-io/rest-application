import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from "@angular/router";
import {Observable, ReplaySubject} from "rxjs";
import {Account, AccountService} from "./account.service";

@Injectable({
  providedIn: 'root'
})
export class AccountResolverService implements Resolve<Account> {

  private accountSubject: ReplaySubject<Account>;

  constructor(private service: AccountService) {
    this.accountSubject = new ReplaySubject<Account>();
  }

  get update$(): Observable<Account> {
    return this.accountSubject.asObservable();
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Account> | Promise<Account> | any {
    let accountId: number = parseInt(route.paramMap.get('accountId'));

    return new Promise<Account>((resolver, reject) => {
      this.service.getAccount(accountId)
        .then(a => {
          this.accountSubject.next(a);
          resolver(a);
        })
        .catch(reject);
    });
  }

}
