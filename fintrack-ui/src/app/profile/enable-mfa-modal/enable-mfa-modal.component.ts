import {Component, OnInit} from '@angular/core';
import {ProfileService} from "../profile.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-enable-mfa-modal',
  templateUrl: './enable-mfa-modal.component.html',
  styleUrls: ['./enable-mfa-modal.component.scss']
})
export class EnableMfaModalComponent implements OnInit {

  private _verificationCode: string;
  private _image: string;
  private _processing: boolean;

  constructor(private _service: ProfileService,
              private _modal: NgbActiveModal) {
  }

  get processing(): boolean {
    return this._processing
  }

  get verificationCode(): string {
    return this._verificationCode;
  }

  set verificationCode(value: string) {
    this._verificationCode = value;
  }

  ngOnInit(): void {
    this._service.qrCode().then(image => this._image = image);
  }

  buildQRCode(): string {
    return this._image;
  }

  dismiss(): void {
    this._modal.dismiss();
  }

  verify(): void {
    this._service.enableMFA(this.verificationCode)
      .then(() => this._modal.close());
  }
}
