import {Component, OnInit, Renderer2} from '@angular/core';
import {SidebarService} from "./service/sidebar.service";
import {BreadcrumbService} from "./service/breadcrumb.service";
import {AuthorizationService} from "./core/core-services";
import {RouterHistory} from "./core/router-history";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  private _currentStyle = 'dark';

  constructor(private _sidebarService : SidebarService,
              private _breadcrumbService : BreadcrumbService,
              private _authorizationService : AuthorizationService,
              private _renderer: Renderer2,
              routerHistory: RouterHistory) {
    routerHistory.ngOnInit();
  }

  get sidebarService(): SidebarService {
    return this._sidebarService;
  }

  get authorizationService(): AuthorizationService {
    return this._authorizationService;
  }

  ngOnInit(): void {
    this._authorizationService.userProfile$.subscribe(profile => {
      this._renderer.removeClass(document.documentElement, this._currentStyle);
      this._renderer.addClass(document.documentElement, profile.theme);
      this._currentStyle = profile.theme;
    });
  }

}
