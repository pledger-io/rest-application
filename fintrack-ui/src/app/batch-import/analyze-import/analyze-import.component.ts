import {Component, OnDestroy, OnInit} from '@angular/core';
import {Account, AccountService} from "../../accounts/account.service";
import {ActivatedRoute, Router} from "@angular/router";
import {BatchImport, ImportConfiguration} from "../batch-import.models";
import {FileService} from "../../core/core-services";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-analyze-import',
  templateUrl: './analyze-import.component.html',
  styleUrls: ['./analyze-import.component.scss']
})
export class AnalyzeImportComponent implements OnInit, OnDestroy {

  private _model: ImportConfiguration;
  private _accounts: Account[];
  private _batchImport: BatchImport;
  private _subscription: Subscription;

  constructor(private _route: ActivatedRoute,
              private _router: Router,
              private _fileService: FileService,
              private accountService: AccountService) {
  }

  ngOnInit() {
    this._model = null;
    this.accountService.getOwnAccounts()
      .then(accounts => this._accounts = accounts);

    this._subscription = this._route.data.subscribe(data => {
      this._batchImport = data['batch'];
      this._model = data['config'];

      console.log(this._model)
    });
  }

  ngOnDestroy() {
    this._subscription.unsubscribe();
  }

  process() {
    localStorage.setItem('import.config.' + this._batchImport.slug, JSON.stringify(this._model));
    this._router.navigate(['/import/' + this._batchImport.slug + '/mappings']);
  }

  get model(): ImportConfiguration {
    return this._model
  }

  get accounts(): Account[] {
    return this._accounts
  }
}
