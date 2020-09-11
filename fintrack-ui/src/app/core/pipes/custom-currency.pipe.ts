import {Pipe, PipeTransform} from '@angular/core';
import {CurrencyPipe} from "@angular/common";

@Pipe({
  name: 'customCurrency'
})
export class CustomCurrencyPipe extends CurrencyPipe implements PipeTransform {

  transform(value: any, currencyCode?: string, display?: 'code' | 'symbol' | 'symbol-narrow' | string | boolean, digitsInfo?: string, locale?: string): string | null {
    return super.transform(
      value,
      currencyCode || sessionStorage.getItem('currency'),
      display || 'symbol-narrow',
      digitsInfo,
      locale
    );
  }

}
