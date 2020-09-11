import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthorizationService} from "../core/core-services";
import {ActivatedRoute, Router} from "@angular/router";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-verify-token',
  templateUrl: './verify-token.component.html',
  styleUrls: ['./verify-token.component.scss']
})
export class VerifyTokenComponent implements OnInit, OnDestroy {

  private _routeAfterLogin : string;
  private _subscription: Subscription;
  userToken: string;

  constructor(private _service: AuthorizationService,
              private _route: ActivatedRoute,
              private _router: Router) { }

  ngOnInit() {
    this._subscription = this._route.queryParamMap.subscribe(qp => this._routeAfterLogin = qp.getAll('return').join('/'));
  }

  ngOnDestroy() {
    this._subscription.unsubscribe();
  }

  verify() {
    this._service.verify(this.userToken)
      .then(() => this._router.navigate([this._routeAfterLogin]));
  }

}
