import { Component, OnInit } from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {FileService, ProcessService} from "../../core/core-services";
import {HttpErrorResponse} from "@angular/common/http";
import {GenericUpload} from "../../core/component/generic-upload.component";

@Component({
  selector: 'app-import-profile-modal',
  templateUrl: './import-profile-modal.component.html',
  styleUrls: ['./import-profile-modal.component.scss']
})
export class ImportProfileModalComponent extends GenericUpload implements OnInit {

  private _errorMessage: string;

  constructor(uploadService: FileService,
              public modal: NgbActiveModal,
              private processService: ProcessService) {
    super(uploadService);
  }

  ngOnInit() {
  }

  startImport() {
    super.uploadFile()
      .then(fileResponse => {
        this.processService.start('ImportUserProfile', {storageToken: fileResponse.fileCode})
          .then(() => this.modal.close('Ok click'))
          .catch((e: HttpErrorResponse) => this._errorMessage = e.error.message)
      })
      .catch((e: HttpErrorResponse) => {
        this._errorMessage = e.error.message;
      });
  }

  get errorMessage(): string {
    return this._errorMessage;
  }
}
