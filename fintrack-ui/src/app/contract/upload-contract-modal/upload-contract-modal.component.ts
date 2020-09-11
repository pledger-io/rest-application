import {Component, OnInit} from '@angular/core';
import {GenericUpload} from "../../core/component/generic-upload.component";
import {FileService} from "../../core/core-services";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-upload-contract-modal',
  templateUrl: './upload-contract-modal.component.html',
  styleUrls: ['./upload-contract-modal.component.scss']
})
export class UploadContractModalComponent extends GenericUpload implements OnInit {

  constructor(uploadService: FileService,
              public modal: NgbActiveModal) {
    super(uploadService)
  }

  ngOnInit(): void {
  }

  startUpload(): void {
    super.uploadFile()
      .then(fileResponse => this.modal.close(fileResponse.fileCode));
  }

}
