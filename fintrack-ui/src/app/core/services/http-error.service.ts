import {Injectable} from '@angular/core';
import {HttpErrorResponse} from "@angular/common/http";
import {Observable, Subject} from "rxjs";
import {Exception} from "../core-models";

@Injectable({
  providedIn: 'root'
})
export class HttpErrorService {

  private _exceptionSubject: Subject<Exception.Error>;

  constructor() {
    this._exceptionSubject = new Subject<Exception.Error>();
  }

  get $(): Observable<Exception.Error> {
    return this._exceptionSubject.asObservable()
  }

  httpException(exception: any) {
    let actualError = exception as HttpErrorResponse;
    this._exceptionSubject.next(new Exception.Error(
      actualError.error['status'],
      actualError.error['error'],
      actualError.error['message'],
      actualError.error['path'],
      actualError.error['errors'],
    ));
  }

}
