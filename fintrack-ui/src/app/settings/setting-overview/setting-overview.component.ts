import {Component, OnInit} from '@angular/core';
import {SettingServiceService} from "../setting-service.service";
import {Setting} from "../settings-models";
import {ToastService} from "../../core/core-services";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {SettingEditModalComponent} from "../setting-edit-modal/setting-edit-modal.component";

@Component({
  selector: 'app-setting-overview',
  templateUrl: './setting-overview.component.html',
  styleUrls: ['./setting-overview.component.scss']
})
export class SettingOverviewComponent implements OnInit {

  private _settings: Setting[];

  constructor(private _service: SettingServiceService,
              private _toasty: ToastService,
              private _modalService: NgbModal) {
  }

  get settings(): Setting[] {
    return this._settings
  }

  ngOnInit(): void {
    this._service.list()
      .then(settings => this._settings = settings)
      .catch(() => this._toasty.warning('page.application.settings.fetch.failed'));
  }

  edit(setting: Setting) {
    let modalRef = this._modalService.open(SettingEditModalComponent);
    modalRef.componentInstance.setting = setting;
    modalRef.result
      .then(() => this._toasty.success('page.application.setting.update.success'));
  }

}
