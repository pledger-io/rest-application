import {AfterContentInit, Directive, Input} from '@angular/core';
import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';

@Directive({
  selector: '[appPatternValidator]',
  providers: [{
    provide: NG_VALIDATORS,
    useExisting: PatternValidatorDirective,
    multi: true
  }]
})
export class PatternValidatorDirective implements Validator,  AfterContentInit {

  @Input()
  private pattern: string;
  private regex: RegExp;

  constructor() { }

  ngAfterContentInit() {
    if (this.pattern) {
      this.regex = new RegExp(this.pattern, '\i');
    }
  }

  validate(control: AbstractControl): ValidationErrors | null {
    if (control.value && this.regex && !this.regex.test(control.value)) {
      return {
        pattern: {
          value: control.value,
          pattern: this.pattern
        }
      };
    }

    return null;
  }

}
