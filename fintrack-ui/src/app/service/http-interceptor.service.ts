import {Injectable} from '@angular/core';
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {Router} from '@angular/router';
import {HttpErrorService} from '../core/core-services';

@Injectable({
  providedIn: 'root'
})
export class HttpInterceptorService implements HttpInterceptor {

  constructor(private _service: HttpErrorService,
              private _router: Router) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // add login credential token
    if (sessionStorage.getItem('token')) {
      req = req.clone({headers: req.headers.set('Authorization', 'Bearer ' + sessionStorage.getItem('token'))});
    }

    if (localStorage.getItem('language')) {
      req = req.clone({headers: req.headers.set('Accept-Language', localStorage.getItem('language'))});
    }

    if (sessionStorage.getItem('CorrelationId')) {
      req = req.clone({headers: req.headers.set('X-Correlation-Id', sessionStorage.getItem('CorrelationId'))});
    }

    // add content type if missing
    if (!(req.body instanceof FormData) && !req.headers.has('Content-Type')) {
      req = req.clone({ headers: req.headers.set('Content-Type', 'application/json') });
    }

    if (!req.headers.has('Accept')) {
      req = req.clone({headers: req.headers.set('Accept', 'application/json')});
    }

    return next.handle(req)
      .pipe(catchError((error: HttpErrorResponse) => {
        this._service.httpException(error);
        if (this.verifyAuthorized(error)) {
          return throwError('Authentication issue');
        }

        return throwError(error);
      }));
  }

  private verifyAuthorized(event: HttpErrorResponse) {
    if (event.status === 401) {
      sessionStorage.clear();
      if (!this._router.isActive('/login', false)) {
        this._router.navigate(['/login']);
      }

      return true;
    }
  }

}

