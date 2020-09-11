import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthenticationRequest} from "./authentication-request";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthorizationService} from "../core/core-services";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-authenticate',
  templateUrl: './authenticate.component.html',
  styleUrls: ['./authenticate.component.scss']
})
export class AuthenticateComponent implements OnInit, OnDestroy {

  validationFailure : boolean;
  credentials : AuthenticationRequest;
  routeAfterLogin : string;

  private _subscription: Subscription;

  constructor(private authorizationService : AuthorizationService, private router : Router, private route: ActivatedRoute) {
    this.validationFailure = false;
    this.credentials = new AuthenticationRequest();
  }

  ngOnInit() {
    this._subscription = new Subscription();
    this._subscription.add(this.route.queryParamMap.subscribe(qp => this.routeAfterLogin = qp.getAll('return').join('/')));

    if (this.authorizationService.authorized) {
      this.router.navigate(['/']);
    }
  }

  ngOnDestroy() {
    this._subscription.unsubscribe();
  }

  login() : boolean {
    this.authorizationService.authorize(this.credentials.username, this.credentials.password)
      .then(() => {
        this.router.navigate([this.routeAfterLogin]);
      }).catch(() => this.validationFailure = true);

    return false;
  }
}
