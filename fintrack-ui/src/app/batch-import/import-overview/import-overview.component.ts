import {Component, OnInit} from '@angular/core';
import {Pagable, Page} from "../../core/core-models";
import {BatchImportService} from "../batch-import.service";
import {BatchImport} from "../batch-import.models";
import {ProcessService} from "../../core/core-services";

@Component({
  selector: 'app-import-overview',
  templateUrl: './import-overview.component.html',
  styleUrls: ['./import-overview.component.scss']
})
export class ImportOverviewComponent implements OnInit {

  private _currentPage: Page<BatchImport>;
  private _pager: Pagable;
  private _loading: boolean;

  constructor(private _service: BatchImportService,
              private _processEngine: ProcessService) { }

  ngOnInit() {
    this._loading = false;
    this._pager = new Pagable(0, 20);
    this.pageChanged();
  }

  get page(): Page<BatchImport> {
    return this._currentPage;
  }

  get info(): Pagable {
    return this._pager;
  }

  get empty() : boolean {
    return !this._currentPage || this._currentPage.info.records == 0;
  }

  get loading(): boolean {
    return this._loading;
  }

  pageChanged() {
    this._loading = true;
    this._service.list(this._pager.page)
      .then(page => this._currentPage = page)
      .finally(() => this._loading = false);
  }

  async importStatus(batch: BatchImport) {
    const style = await this._processEngine.process('BatchTransactionImport', batch.slug)
      .then(process => {
        if (process.length > 0) {
          switch (process[0].state) {
            case 'COMPLETED':
              return 'fa-check';
          }
        }

        return 'fa-times'
      });

    return batch['status'] = style;
  }
}
