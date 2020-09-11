import { Component, OnInit } from '@angular/core';
import {CurrencyModel} from '../settings-models';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {ToastService} from '../../core/services/toast.service';

@Component({
  selector: 'app-currency-overview',
  templateUrl: './currency-overview.component.html',
  styleUrls: ['./currency-overview.component.scss']
})
export class CurrencyOverviewComponent implements OnInit {

  private _currencies: CurrencyModel[];

  constructor(private _http: HttpClient, private toasty: ToastService) { }

  get currencies(): CurrencyModel[] {
    return this._currencies;
  }

  ngOnInit(): void {
    this._http.get<CurrencyModel[]>(environment.backend + 'settings/currencies')
      .subscribe(data => this._currencies = data);
  }

  changeState(currency: CurrencyModel) {
    this._http.patch<void>(environment.backend + 'settings/currencies/' + currency.code, {enabled: currency.enabled})
      .toPromise()
      .then(() => this.toasty.success('page.settings.currencies.enabled.success'))
      .catch(() => this.toasty.success('page.settings.currencies.enabled.failed'));
  }
}
