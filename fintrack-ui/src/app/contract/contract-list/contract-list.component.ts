import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Contract} from "../contract-models";
import {ConfirmModalComponent} from "../../core/confirm-modal/confirm-modal.component";
import {noop} from "rxjs";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {FileService, ToastService} from "../../core/core-services";
import {ContractService} from "../contract.service";
import * as moment from "moment";
import {environment} from "../../../environments/environment";
import {UploadContractModalComponent} from "../upload-contract-modal/upload-contract-modal.component";

@Component({
  selector: 'app-contract-list',
  templateUrl: './contract-list.component.html',
  styleUrls: ['./contract-list.component.scss']
})
export class ContractListComponent implements OnInit {

  @Input('contracts')
  private _contracts: Contract[];

  @Output('changed')
  private _listChanged: EventEmitter<any>;

  constructor(private _service: ContractService,
              private _fileService: FileService,
              private _modelService : NgbModal,
              private _toastService: ToastService) {
    this._listChanged = new EventEmitter<any>();
  }

  get contracts() : Contract[] {
    return this._contracts;
  }

  get fileService(): FileService {
    return this._fileService
  }

  allowDelete(contract: Contract): boolean {
    let expiresAt = moment(contract.end, environment.isoDateFormat);
    return !contract.terminated && expiresAt.isBefore(moment());
  }

  ngOnInit(): void {
  }

  warnBeforeExpire(contract: Contract) {
    this._service.warnExpiry(contract.id)
      .then(() => this._toastService.success('page.title.budget.contracts.warn.success'))
      .catch(() => this._toastService.warning('page.title.budget.contracts.warn.failed'))
  }

  uploadContract(contract: Contract) {
    this._modelService.open(UploadContractModalComponent).result
      .then(fileCode =>
        this._service.attachment(contract.id, fileCode)
          .then(() => this._toastService.success('page.budget.contracts.upload.success'))
          .then(() => this._listChanged.emit())
      )
  }

  confirmDelete(contract: Contract) {
    let modalRef = this._modelService.open(ConfirmModalComponent);
    modalRef.componentInstance.titleTextKey = 'common.action.delete';
    modalRef.componentInstance.descriptionKey = 'page.budget.contracts.delete.confirm';
    modalRef.result
      .then(() => {
        this._service.delete(contract.id)
          .then(() => this._toastService.success('page.budget.contracts.delete.success'))
          .then(() => this._listChanged.emit())
          .catch(() => this._toastService.warning('page.budget.contracts.delete.failed'));
      })
      .catch(noop)
  }

}
