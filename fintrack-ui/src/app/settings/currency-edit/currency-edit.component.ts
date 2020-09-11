import { Component, OnInit } from '@angular/core';
import {CurrencyModel} from '../settings-models';
import {ActivatedRoute} from '@angular/router';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {RouterHistory} from '../../core/router-history';

@Component({
  selector: 'app-currency-edit',
  templateUrl: './currency-edit.component.html',
  styleUrls: ['./currency-edit.component.scss']
})
export class CurrencyEditComponent implements OnInit {

  private _currencyCode: string;
  private _model: CurrencyModel;

  constructor(private route: ActivatedRoute,
              private http: HttpClient,
              private history: RouterHistory) { }

  get model(): CurrencyModel {
    return this._model;
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this._currencyCode = params.get('currencyCode');
    })
    this.route.data.subscribe(data => {
      this._model = data.currency as CurrencyModel;
    });
  }

  persist() {
    let completion: Promise<void>;

    if (this._currencyCode) {
      completion = this.http.post<void>(environment.backend + 'settings/currencies/' + this._currencyCode, this._model)
        .toPromise();
    } else {
      completion = this.http.put<void>(environment.backend + 'settings/currencies', this._model)
        .toPromise();
    }

    completion.then(() => this.history.previous());
  }

}
