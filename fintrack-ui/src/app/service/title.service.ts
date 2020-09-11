import {Injectable} from '@angular/core';
import {Observable, ReplaySubject} from 'rxjs';
import {ActivatedRouteSnapshot, CanActivate, ParamMap, RouterStateSnapshot} from '@angular/router';
import {Title} from '@angular/platform-browser';
import {LocalizationService} from '../core/core-services';

@Injectable({
  providedIn: 'root'
})
export class TitleService implements CanActivate {

  private subject: ReplaySubject<string>;

  constructor(private _pageTitle: Title,
              private _localization: LocalizationService) {
    this.subject = new ReplaySubject<string>();
  }

  get title$(): Observable<string> {
    return this.subject.asObservable();
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | boolean {
    sessionStorage.setItem('CorrelationId', this.uuidv4());

    if (route.data['title']) {
      const title = this.paramReplace(route.paramMap, route.data.title);

      if (title) {
        this._localization.getText(title)
          .then(response => this._pageTitle.setTitle('FinTrack: ' + response));
      } else {
        this._pageTitle.setTitle('FinTrack');
      }

      this.subject.next(title);
    }

    return true;
  }

  uuidv4(a = null) {
    return a?(0|Math.random()*16).toString(16):(""+1e7+-1e3+-4e3+-8e3+-1e11).replace(/1|0/g,v => this.uuidv4(v))
  }

  paramReplace(routeParams: ParamMap, template: string): string {
    let regex = /:[a-zA-Z]+/g
    let result = template;
    let match;

    while ((match = regex.exec(template)) !== null) {
      let paramName = match[0].substr(1)
      if (routeParams.get(paramName)) {
        result = result.replace(match[0], routeParams.get(paramName))
      }
    }

    return result
  }

}
