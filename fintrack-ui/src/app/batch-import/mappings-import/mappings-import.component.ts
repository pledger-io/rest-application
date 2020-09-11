import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {BatchImport, ImportConfiguration} from "../batch-import.models";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-mappings-import',
  templateUrl: './mappings-import.component.html',
  styleUrls: ['./mappings-import.component.scss']
})
export class MappingsImportComponent implements OnInit, OnDestroy {

  private _batchImport: BatchImport;
  private _model: ImportConfiguration;
  private _subscription: Subscription;

  constructor(private _route: ActivatedRoute,
              private _router: Router) { }

  get model(): ImportConfiguration {
    return this._model;
  }

  ngOnInit() {
    this._subscription = this._route.data.subscribe(data => {
      this._batchImport = data['batch'];
      this._model = data['config'];

      if (!this._model.accountId) {
        this._router.navigate(['/import/' + this._batchImport.slug + '/analyze']);
      }
    });
  }

  ngOnDestroy() {
    this._subscription.unsubscribe()
  }

  remove(index: number) {
    this.model.columnRoles[index] = null;
    this.model.columnRoles = this.model.columnRoles.filter(e => e != null);
  }

  add() {
    this.model.columnRoles.push("");
  }

  process() {
    localStorage.setItem('import.config.' + this._batchImport.slug, JSON.stringify(this._model));
    this._model = null;
    this._router.navigate(['/import/' + this._batchImport.slug + '/accounts']);
  }

}
