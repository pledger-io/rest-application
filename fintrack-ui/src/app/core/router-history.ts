import {Injectable, OnInit} from "@angular/core";
import {NavigationEnd, Router} from "@angular/router";
import {filter} from "rxjs/operators";

const storageKey: string = 'router.history';

@Injectable()
export class RouterHistory implements OnInit {

  constructor(private router: Router) {
    if (sessionStorage.getItem(storageKey) == null) {
      sessionStorage.setItem(storageKey, '[]');
    }
  }

  ngOnInit(): void {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(({urlAfterRedirects}: NavigationEnd) => {
        let history: string[] = JSON.parse(sessionStorage.getItem(storageKey)) || [];
        if (history.length == 0 || history[history.length - 1] != urlAfterRedirects) {
          history = [...history, urlAfterRedirects];

          // limit the amount of elements in the history
          if (history.length > 10) {
            history = history.slice(history.length - 8, history.length);
          }
          sessionStorage.setItem(storageKey, JSON.stringify(history));
        }
      });
  }

  previous() {
    let navigateTo = '/';
    let history = JSON.parse(sessionStorage.getItem(storageKey));
    if (history.length > 2) {
      navigateTo = history[history.length - 2];
    }

    if (history.length > 1) {
      history.pop();
      sessionStorage.setItem(storageKey, JSON.stringify(history));
    }

    let queryParams = null;
    if (navigateTo.indexOf('?') > -1) {
      queryParams = {}
      new URLSearchParams(navigateTo.substr(navigateTo.indexOf('?')))
        .forEach((value, key) => queryParams[key] = value);

      navigateTo = navigateTo.substr(0, navigateTo.indexOf('?'))
    }

    this.router.navigate([navigateTo], {
      queryParams: queryParams
    });
  }

}
