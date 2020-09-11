import { Component, OnInit } from '@angular/core';
import {Setting} from "../settings-models";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {SettingServiceService} from "../setting-service.service";

@Component({
  selector: 'app-setting-edit-modal',
  templateUrl: './setting-edit-modal.component.html',
  styleUrls: ['./setting-edit-modal.component.scss']
})
export class SettingEditModalComponent implements OnInit {

  private _setting: Setting;
  private _processing: boolean;
  private _value: string;

  constructor(private _modal: NgbActiveModal,
              private _service: SettingServiceService) { }

  set setting(value: Setting) {
    this._setting = value;
    this._value = value.value;
  }

  get setting(): Setting {
    return this._setting;
  }

  get processing(): boolean {
    return this._processing;
  }

  get value(): string {
    return this._value;
  }

  set value(value: string) {
    this._value = value;
  }

  ngOnInit(): void {
  }

  dismiss(): void {
    this._modal.dismiss();
  }

  process(): void {
    this._service.update(this._setting.name, this._value)
      .then(() => this._setting.value = this._value)
      .then(() => this._modal.close());
  }

}
