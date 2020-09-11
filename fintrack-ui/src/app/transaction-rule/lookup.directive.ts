import {Directive, HostBinding, Input, OnInit} from '@angular/core';
import {LookupService} from "./lookup.service";

@Directive({
  selector: '[lookup]'
})
export class LookupDirective implements OnInit {

  @Input('lookup')
  private dataType: string;

  @Input('entityId')
  private entityId: number;

  label: string;

  constructor(private _service: LookupService) { }

  ngOnInit() {
    this._service.lookup(this.dataType, this.entityId).then(value => this.label = value);
  }

  @HostBinding('innerHTML')
  get textValue() {
    return this.label
  }
}
