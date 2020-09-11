import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environments/environment';
import {Currency} from '../../core-models';

@Component({
  selector: 'app-currency-selector',
  templateUrl: './currency-selector.component.html',
  styleUrls: ['./currency-selector.component.scss']
})
export class CurrencySelectorComponent implements OnInit {

  selected: Currency;
  class: string;

  @Input('optional')
  private _optional: boolean;
  @Input('readonly')
  private _readOnly: boolean;
  private _currencyCode: string;
  private readonly _selectedChange: EventEmitter<string>;
  private _currencies: Currency[];

  constructor(private http: HttpClient) {
    this._selectedChange = new EventEmitter<string>();
  }

  @Input()
  set value(value: string) {
    this._currencyCode = value;
    this.refresh();
  }

  @Input('class')
  set clazz(value: string) {
    this.class = value;
  }

  @Output()
  get valueChange(): EventEmitter<string> {
    return this._selectedChange;
  }

  get currencies(): Currency[] {
    return this._currencies;
  }

  get optional(): boolean {
    return this._optional;
  }

  get readonly(): boolean {
    return this._readOnly;
  }

  ngOnInit(): void {
    this.http.get<Currency[]>(environment.backend + 'settings/currencies')
      .toPromise()
      .then(currencies => this._currencies = currencies.filter(c => c.enabled))
      .then(() => this.refresh());
  }

  refresh() {
    if (this._currencyCode && this._currencies) {
      this.selected = this._currencies.find(currency => currency.code === this._currencyCode);
    }
  }

  update() {
    this._selectedChange.next(this.selected.code);
  }

  compare(c1: Currency, c2: Currency) {
    return c1 != null && c2 != null && c1.code === c2.code;
  }

}
