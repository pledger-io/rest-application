import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthorizationService, ToastService} from "../../core/core-services";
import {Subscription} from "rxjs";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ImportProfileModalComponent} from "../import-profile-modal/import-profile-modal.component";
import {ProfileService} from "../profile.service";
import {EnableMfaModalComponent} from "../enable-mfa-modal/enable-mfa-modal.component";
import {UserAccount} from "../../core/services/authorization.service";
import * as moment from "moment";

@Component({
  selector: 'app-profile-page',
  templateUrl: './profile-page.component.html',
  styleUrls: ['./profile-page.component.scss']
})
export class ProfilePageComponent implements OnInit, OnDestroy {

  private _profile: UserAccount;
  private subscription: Subscription;

  constructor(private _service: ProfileService,
              private _toastService: ToastService,
              private authorizationService: AuthorizationService,
              private modelService : NgbModal) { }

  get profile(): UserAccount {
    return this._profile;
  }

  get service(): ProfileService {
    return this._service;
  }

  get currencies() : string[] {
    return ['EUR', 'GBP', 'USD'];
  }

  ngOnInit() {
    this.subscription = this.authorizationService.userProfile$.subscribe(profile => this._profile = profile);
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  importProfile() {
    this.modelService.open(ImportProfileModalComponent);
  }

  openMultiFactor() {
    this.modelService.open(EnableMfaModalComponent).result
      .then(() => this._toastService.success('page.user.profile.twofactor.enable.success'));
  }

  exportDateString(): string {
    return moment().format('YYYYMMDD');
  }

  applyAllRules() {
    this._service.applyRules();
  }

  disableMultiFactor() {
    this._service.disableMFA()
      .then(() => this.authorizationService.reloadProfile())
      .then(() => this._toastService.success('page.user.profile.twofactor.disable.success'))
      .catch(() => this._toastService.warning('page.user.profile.twofactor.disable.failed'));
  }

  patchProfile(field: string, value: any) {
    let patchRequest = {};
    patchRequest[field] = value;
    this._service.update(patchRequest)
      .then(() => this.authorizationService.reloadProfile());
  }
}
