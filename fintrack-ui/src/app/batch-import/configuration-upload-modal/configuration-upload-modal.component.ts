import {Component, OnInit} from '@angular/core';
import {GenericUpload} from "../../core/component/generic-upload.component";
import {FileService} from "../../core/core-services";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {BatchImportService} from "../batch-import.service";
import {CreateBatchConfigRequest} from "../batch-import.models";

@Component({
  selector: 'app-configuration-upload-modal',
  templateUrl: './configuration-upload-modal.component.html',
  styleUrls: ['./configuration-upload-modal.component.scss']
})
export class ConfigurationUploadModalComponent extends GenericUpload implements OnInit {

  constructor(uploadService: FileService,
              private batchService: BatchImportService,
              public modal: NgbActiveModal) {
    super(uploadService);
  }

  ngOnInit() {
  }

  process() {
    super.uploadFile()
      .then(uploadResponse => {
        this.batchService.createConfig(new CreateBatchConfigRequest(this.fileToUpload.name, uploadResponse.fileCode))
          .then(() => this.modal.close('Ok'))
      });
  }

}
