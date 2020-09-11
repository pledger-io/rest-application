import {Directive, HostBinding, Input, OnInit, Optional, Self} from '@angular/core';
import {NgModel, Validators} from '@angular/forms';
import {Exception} from '../core-models';

export enum Criteria {
  at_least_six_chars,
  at_least_one_lower_case_char,
  at_least_one_upper_case_char,
  at_least_one_digit_char,
  at_least_one_special_char,
}

@Directive({
  selector: '[password-input]',
  exportAs: 'password'
})
export class PasswordInputDirective implements OnInit {

  @Input()
  validators: Criteria[] = Object.keys(Criteria).map(key => Criteria[key]);

  private _criteriaMap: Map<Criteria, RegExp>;
  private _passwordControl: NgModel;
  private _validResult: Exception.ValidationResult[];

  constructor(@Optional() @Self() ngModel: NgModel) {
    this._criteriaMap = new Map();

    this._criteriaMap.set(Criteria.at_least_six_chars, RegExp(/^.{6,63}$/));
    this._criteriaMap.set(Criteria.at_least_one_lower_case_char, RegExp(/^(?=.*?[a-z])/));
    this._criteriaMap.set(Criteria.at_least_one_upper_case_char, RegExp(/^(?=.*?[A-Z])/));
    this._criteriaMap.set(Criteria.at_least_one_digit_char, RegExp(/^(?=.*?[0-9])/));
    this._criteriaMap.set(Criteria.at_least_one_special_char, RegExp(/^(?=.*?[" !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~"])/));

    this._passwordControl = ngModel;
    this._passwordControl.statusChanges
      .subscribe(() => this.updated());
  }

  get validated(): Exception.ValidationResult[] {
    return this._validResult;
  }

  @HostBinding('type')
  get type(): string {
    return 'password';
  }

  ngOnInit(): void {
    this._passwordControl.control.setValidators(
      [...this.validators.map(criteria => {
        if (this._criteriaMap.get(criteria)) {
          return Validators.pattern(this._criteriaMap.get(criteria));
        }
        return null;
      }).filter(val => val)]
    );
  }

  updated() {
    this._validResult = [];

    for (const validator of this.validators) {
      const pattern = this._criteriaMap.get(validator);
      if (pattern) {
        const valid = pattern.test(this._passwordControl.value);
        this._validResult.push({
          validator: Criteria[validator],
          valid,
          style: valid ? 'text-success fa-check' : 'text-danger fa-exclamation-circle'
        });
      }
    }
  }
}
