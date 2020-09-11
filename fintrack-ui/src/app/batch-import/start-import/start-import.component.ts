import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {BatchConfig, CreateBatchImportRequest} from "../batch-import.models";
import {GenericUpload} from "../../core/component/generic-upload.component";
import {FileService} from "../../core/core-services";
import {BatchImportService} from "../batch-import.service";
import {ConfigurationUploadModalComponent} from "../configuration-upload-modal/configuration-upload-modal.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {BatchConfigResolverService} from "../resolver/batch-config-resolver.service";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-start-import',
  templateUrl: './start-import.component.html',
  styleUrls: ['./start-import.component.scss']
})
export class StartImportComponent extends GenericUpload implements OnInit, OnDestroy  {

  private _availableConfig: BatchConfig[];
  private _model: CreateBatchImportRequest;
  private _subscription: Subscription;

  constructor(private _route: ActivatedRoute,
              private _batchService: BatchImportService,
              private _router: Router,
              private _configResolver: BatchConfigResolverService,
              private modelService : NgbModal,
              uploadService: FileService) {
    super(uploadService);
  }

  ngOnInit() {
    this._model = new CreateBatchImportRequest(null, null);

    this._subscription = this._route.data.subscribe(data => {
      this._availableConfig = data['configs'];
    });
  }

  ngOnDestroy() {
    this._subscription.unsubscribe()
  }

  process() {
    super.uploadFile()
      .then(fileResponse => {
        this._model.uploadToken = fileResponse.fileCode;
        this._batchService.create(this._model)
          .then(batchImport => this._router.navigate(['/import/' + batchImport.slug + '/analyze']))
      });
  }

  showImportConfig() {
    this.modelService.open(ConfigurationUploadModalComponent)
      .result.then(() => this._configResolver.resolve(null, null).then(config => this._availableConfig = config));
  }

  get configs(): BatchConfig[] {
    return this._availableConfig;
  }

  get model(): CreateBatchImportRequest {
    return this._model;
  }

}
