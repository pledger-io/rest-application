import {Directive, HostBinding, Input} from '@angular/core';
import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from "@angular/forms";

@Directive({
  selector: '[appMinValidator]',
  providers: [{
    provide: NG_VALIDATORS,
    useExisting: MinValidatorDirective,
    multi: true
  }]
})
export class MinValidatorDirective implements Validator {

  @Input('appMinValidator')
  private _minimum: number;

  constructor() { }

  validate(control: AbstractControl): ValidationErrors | null {
    if (control.value && this.below(control.value)) {
      return {
        min: {
          value: control.value,
          min: this._minimum
        }
      }
    }

    return undefined;
  }

  private below(current: string): boolean {
    let number = parseInt(current);

    return !isNaN(number) && number < this._minimum;
  }

  @HostBinding('min')
  get min() {
    return this._minimum;
  }

}
