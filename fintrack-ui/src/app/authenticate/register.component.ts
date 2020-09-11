import {Component, OnInit} from '@angular/core';
import {AuthenticationRequest} from "./authentication-request";
import {Router} from "@angular/router";
import {AuthorizationService} from "../core/core-services";
import {Criteria} from "../core/directives/password-input.directive";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {

  Criteria = Criteria;

  validationFailure : boolean;
  credentials : AuthenticationRequest;

  constructor(private authorizationService: AuthorizationService, private router : Router) {
  }

  ngOnInit() {
    this.credentials = new AuthenticationRequest();
    this.credentials.password = '';
  }

  register() {
    this.authorizationService.register(this.credentials.username, this.credentials.password)
      .then(() => this.router.navigate(['login'], {queryParamsHandling: 'preserve'}))
      .catch(() => this.validationFailure = true);

    return false;
  }
}
