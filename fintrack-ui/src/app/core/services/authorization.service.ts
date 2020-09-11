import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router} from '@angular/router';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {map, switchMap} from 'rxjs/operators';
import {Observable, ReplaySubject, Subject} from 'rxjs';
import {JwtHelperService} from '@auth0/angular-jwt';
import * as moment from 'moment';

interface TokenResponse {
  username: string;
  access_token: string;
}

export class UserAccount {
  constructor(public theme: string,
              public currency: string,
              public profilePicture: string,
              public mfa: boolean) { }
}

class JWTToken {
  constructor(private exp: number, iat: number, private roles: string[], private sub: string) {
  }

  get expiresSoon(): boolean {
    const expires = moment(this.exp * 1000);
    return moment().add(2, 'minutes').isAfter(expires);
  }

  get requiresMFA(): boolean {
    return this.roles.indexOf('PRE_VERIFICATION_USER') > -1;
  }

  get isAdmin(): boolean {
    return this.roles.indexOf('admin') > -1;
  }

  static fromToken(other: JWTToken) {
    return new JWTToken(other.exp, other.exp, other.roles, other.sub);
  }
}

@Injectable({
  providedIn: 'root'
})
export class AuthorizationService implements CanActivate {

  private _userSubject: Subject<UserAccount>;
  private userAccount: UserAccount;
  private _jwtService: JwtHelperService;

  constructor(private router: Router, private http: HttpClient) {
    this._userSubject = new ReplaySubject<UserAccount>();
    this._jwtService = new JwtHelperService();
    this.reloadProfile();
  }

  get userProfile$(): Observable<UserAccount> {
    return this._userSubject.asObservable();
  }

  get authorized(): boolean {
    return sessionStorage.getItem('token') != undefined;
  }

  public get token(): JWTToken {
    if (this.authorized) {
      return JWTToken.fromToken(this._jwtService.decodeToken(sessionStorage.getItem('token')));
    }

    return null;
  }

  canActivate(route: ActivatedRouteSnapshot): Promise<boolean> | boolean {
    if (!this.authorized) {
      return this.router.navigate(['/login'], {
        queryParams: {
          return: route['_routerState']['url']
        }
      });
    }

    if (this.token.requiresMFA) {
      return this.router.navigate(['/verify'], {
        queryParams: {
          return: route['_routerState']['url']
        }
      });
    }

    if (this.token.expiresSoon) {
      return this.http.get<TokenResponse>(environment.backend + 'security/token-refresh')
        .pipe(
          map(result => this.updateJWT(result.access_token)),
          map(() => true)
        ).toPromise();
    }

    return new Promise(resolve => resolve(true));
  }

  register(username: string, password: string): Promise<void> {
      return this.http.put<void>(environment.backend + 'security/create-account', {username, password})
        .toPromise();
  }

  authorize(username: string, password: string): Promise<void> {
    return this.http.post<TokenResponse>(environment.backend + 'security/authenticate', {username, password}).pipe(
        map(result => this.updateJWT(result.access_token)),
        switchMap(() => this.reloadProfile())
      )
      .toPromise();
  }

  verify(token: string): Promise<void> {
    return this.http.post<TokenResponse>(environment.backend + 'security/2-factor', {verificationCode: token}).pipe(
      map(result => this.updateJWT(result.access_token)),
      switchMap(() => this.reloadProfile())
    ).toPromise();
  }

  logout() {
    sessionStorage.clear();
    this.router.navigate(['/login']);
  }

  reloadProfile(): Promise<void> {
    if (this.authorized && this.token.requiresMFA) {
      return new Promise(accept => accept());
    }

    return this.http.get<UserAccount>(environment.backend + 'profile').pipe(
      map(account => this.userAccount = account),
      map(account => sessionStorage.setItem('currency', account.currency || 'EUR')),
      map(() => this._userSubject.next(this.userAccount))
    ).toPromise();
  }

  private updateJWT(token: string): void {
    sessionStorage.setItem('token', token);
  }

}
