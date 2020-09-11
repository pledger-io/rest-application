import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EMPTY, Observable, ReplaySubject} from "rxjs";
import {catchError, map} from "rxjs/operators";
import {environment} from "../../../environments/environment";

const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type':  'application/json'
  })
};

@Injectable({
  providedIn: 'root'
})
export class LocalizationService {

  private rootEndPoint : string;
  private language : string;
  private languageSubject: ReplaySubject<string>;
  private cache = {};

  constructor(private http: HttpClient) {
    this.rootEndPoint = environment.backend + 'localization/';
    this.languageSubject = new ReplaySubject<string>();

    if (localStorage.getItem('language')) {
      this.language = localStorage.getItem('language');
      this.languageSubject.next(this.language);
    } else {
      this.setLanguage('en');
    }
  }

  get language$() : Observable<string> {
    return this.languageSubject.asObservable();
  }

  setLanguage(languageCode: string) {
    localStorage.setItem('language', languageCode);
    this.language = languageCode;
    this.cache = {};
    this.languageSubject.next(this.language);
    this.getText('common.date.format')
      .then(text => localStorage.setItem('dateFormat', text));
  }

  getText(textKey: string): Promise<string> {
    if (this.cache[textKey]) {
      return this.cache[textKey];
    }

    this.cache[textKey] = this.http.get(this.rootEndPoint + 'lang/' + this.language + '/' + textKey).pipe(
      map(r => r['text']),
      catchError(() => {
        delete this.cache[textKey];
        return EMPTY;
      })).toPromise();

    return this.cache[textKey];
  }

}
