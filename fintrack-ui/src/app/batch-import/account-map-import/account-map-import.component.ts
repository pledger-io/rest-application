import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {BatchImport, ImportConfiguration} from "../batch-import.models";
import {FileService, ProcessService, ToastService} from "../../core/core-services";
import {Account, AccountService} from "../../accounts/account.service";
import {Subscription} from "rxjs";

class ImportMapping {
  constructor(public name: string,
              public accountId: number) {
  }
}

@Component({
  selector: 'app-account-map-import',
  templateUrl: './account-map-import.component.html',
  styleUrls: ['./account-map-import.component.scss']
})
export class AccountMapImportComponent implements OnInit, OnDestroy {

  private _batchImport: BatchImport;
  private _config: ImportConfiguration;
  private _accounts: Account[];

  private _model: ImportMapping[];
  private _processing: boolean;
  private _subscription: Subscription;

  currentPage: number;
  pageSize: number;

  constructor(private _route: ActivatedRoute,
              private _router: Router,
              private _accountService: AccountService,
              private _fileService: FileService,
              private _processService: ProcessService,
              private _toastService: ToastService) {
    this._accountService.getAllAccounts().then(accounts => {
      this._accounts = accounts.sort((a1, a2) => a1.name.localeCompare(a2.name));
    });
  }

  ngOnInit() {
    this._processing = false;
    this.currentPage = 1;
    this.pageSize = 25;
    this._subscription = this._route.data.subscribe(data => {
      this._batchImport = data['batch'];
      this._config = data['config'];

      const mappings = data['mappings'];
      this._model = mappings.map(el => new ImportMapping(el.name, el.account ? el.account : ''));
      if (mappings.length > 250) {
        this._toastService.warning('page.nav.settings.import.mappings.tooMany');
      }

      if (!localStorage.getItem('import.config.' + this._batchImport.slug)) {
        this._router.navigate(['/import/' + this._batchImport.slug + '/analyze']);
      }
    });
  }

  ngOnDestroy() {
    this._subscription.unsubscribe();
  }

  process() {
    this._processing = true;
    let accountMapping = {};
    this._model.forEach(el => accountMapping[el.name] = el.accountId);

    let file: File = new File([new Blob([JSON.stringify(accountMapping)])], 'mappings.data');
    this._fileService.upload(file)
      .then(response => {
        this._processService.start('BatchTransactionImport', {
          slug: this._batchImport.slug,
          businessKey: this._batchImport.slug,
          accountMapping: response.fileCode,
          importConfig: JSON.stringify(ImportConfiguration.toServer(localStorage.getItem('import.config.' + this._batchImport.slug)))
        })
          .then(instanceId => this._router.navigate(['/import']))
          .finally(() => this._processing = false);
      });
  }

  get processing(): boolean {
    return this._processing
  }

  get accounts(): Account[] {
    return this._accounts;
  }

  get config(): ImportConfiguration {
    return this._config;
  }

  get model(): ImportMapping[] {
    return this._model;
  }
}
