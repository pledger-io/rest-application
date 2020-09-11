import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {Criteria} from "../../core/directives/password-input.directive";
import {ProfileService} from "../../profile/profile.service";
import {ToastService} from "../../core/core-services";

@Component({
  selector: 'app-change-password-modal',
  templateUrl: './change-password-modal.component.html',
  styleUrls: ['./change-password-modal.component.scss']
})
export class ChangePasswordModalComponent implements OnInit {

  Criteria = Criteria;

  private _processing: boolean;
  private _updatedPassword: string;

  constructor(private _modal: NgbActiveModal,
              private _service: ProfileService,
              private _toasty: ToastService) {
  }

  get password(): string {
    return this._updatedPassword
  }

  set password(value: string) {
    this._updatedPassword = value
  }

  get processing(): boolean {
    return this._processing;
  }

  ngOnInit(): void {
    this._updatedPassword = '';
  }

  dismiss(): void {
    this._modal.dismiss();
  }

  process(): void {
    this._service.update({
      password: this.password
    })
      .then(() => this._modal.close())
      .then(() => this._toasty.success('page.user.password.changed.success'))
  }
}
