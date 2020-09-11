import {Component, ContentChild, HostBinding, Input, OnDestroy, OnInit} from '@angular/core';
import {NgModel} from "@angular/forms";
import {filter} from "rxjs/operators";
import {Subscription} from "rxjs";
import {HttpErrorService} from "../../services/http-error.service";

@Component({
  selector: 'app-input-field',
  templateUrl: './input-field.component.html',
  styleUrls: ['./input-field.component.scss']
})
export class InputFieldComponent implements OnInit, OnDestroy {

  @Input('control')
  private control: string;

  private inputModel: NgModel;

  private errorSubscription: Subscription;
  private validationSubscription: Subscription;

  constructor(private _service: HttpErrorService) { }

  @ContentChild(NgModel)
  set controlChanged(input: NgModel) {
    if (this.validationSubscription) {
      this.validationSubscription.unsubscribe();
    }

    this.inputModel = input;
    if (input) {
      this.validationSubscription = this.inputModel.valueChanges
        .pipe(filter((e: string) => e == ""))
        .subscribe(() => this.inputModel.viewToModelUpdate(null));
    }
  }

  ngOnInit() {
    this.errorSubscription = this._service.$.subscribe(error => {
      if (error.validationFailed) {
        error.fieldErrors
          .filter(field => field.field == this.inputModel.name)
          .map(field => {
            let error = {};
            error[field.code.toLowerCase()] = {
              value: field.rejectedValue,
            };

            return error;
          })
          .forEach(error => this.inputModel.control.setErrors(error));
      }
    });
  }

  ngOnDestroy() {
    this.errorSubscription.unsubscribe();
    if (this.validationSubscription) {
      this.validationSubscription.unsubscribe();
    }
  }

  @HostBinding('class.invalid')
  get invalid() : boolean {
    return this.inputModel && this.inputModel.invalid && this.dirty;
  }

  @HostBinding('class.valid')
  get valid() : boolean {
    return this.inputModel == null || (this.inputModel.valid && this.dirty);
  }

  private get dirty() : boolean {
    return this.inputModel.dirty || this.inputModel.touched;
  }

  get errors() : any[] {
    return Object.keys(this.inputModel.errors)
      .map(key => this.control + '.' + this.inputModel.name + '.' + key);
  }

}
