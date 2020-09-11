import {Directive, ElementRef, EventEmitter, forwardRef, HostListener, OnInit, Output, Renderer2} from "@angular/core";
import {ControlValueAccessor, NG_VALUE_ACCESSOR, NgModel} from "@angular/forms";

@Directive({
  selector: '[ngModel][appPercentage]',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(()=> PercentageDirective),
    multi: true
  }]
})
export class PercentageDirective implements ControlValueAccessor  {

  onChangeCallback = (_: any) => {};
  onTouchedCallback = () => {};

  constructor(private _renderer: Renderer2, private _elementRef: ElementRef) {
  }

  @HostListener('input', ['$event.target.value'])
  input(value: any) {
    if (!value) {
      value = "";
    } else {
      value = parseFloat(value) / 100;
    }

    this.onChangeCallback(value);
  }

  @HostListener('blur', [])
  touched(): void {
    this.onTouchedCallback();
  }

  registerOnChange(fn: any): void {
    this.onChangeCallback = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouchedCallback = fn;
  }

  writeValue(obj: any): void {
    if (obj) {
      obj = parseFloat(obj) * 100;
    }

    this._renderer.setProperty(this._elementRef.nativeElement, 'value', obj);
  }

}
