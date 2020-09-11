import {Directive, HostBinding, Input, OnInit} from "@angular/core";
import {ProcessService} from "../core-services";

@Directive({
  selector: '[appProcessVariable]'
})
export class ProcessVariableDirective implements OnInit {

  @Input("variable")
  private _variable: string;

  @Input("definition-key")
  private _definitionKey: string;

  @Input("instance-id")
  private _id: string;

  private _value;

  constructor(private _service: ProcessService) {
  }

  @HostBinding('innerHTML')
  get value(): string {
    return this._value;
  }

  ngOnInit(): void {
    this._service.variable(this._definitionKey, this._id, this._variable)
      .then(variables => {
        if (variables.length > 0) {
          this._value = variables[0].value
        }
      });
  }

}
