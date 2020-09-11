import {Directive, HostBinding, Input} from '@angular/core';
import {CustomCurrencyPipe} from "../pipes/custom-currency.pipe";

@Directive({
  selector: '[appBalance]'
})
export class BalanceDirective {

  private balance: number;
  private currency: string;

  constructor(private currencyPipe : CustomCurrencyPipe) { }

  @Input('appBalance')
  set setBalance(balance: number) {
    this.balance = balance;
  }

  @Input('currency')
  set setCurrency(currency: string) {
    this.currency = currency;
  }

  @HostBinding('class.text-green')
  get greenClass(): boolean {
    return this.balance > 0;
  }

  @HostBinding('class.text-danger')
  get redClass(): boolean {
    return this.balance < 0;
  }

  @HostBinding('innerHTML')
  get content() : string {
    return this.currencyPipe.transform(this.balance, this.currency);
  }

}
