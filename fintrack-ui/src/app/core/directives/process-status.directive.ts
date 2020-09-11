import {Directive, HostBinding, Input, OnInit} from '@angular/core';
import {ProcessService} from '../core-services';

@Directive({
  selector: '[appProcessStatus]'
})
export class ProcessStatusDirective implements OnInit {

  @Input('definition-key')
  private _definitionKey: string;
  @Input('business-key')
  private _businessKey: string;

  private _class: string;

  constructor(private _service: ProcessService) { }

  @HostBinding('class')
  get class(): string {
    return this._class;
  }

  ngOnInit() {
    this._class = 'fa fa-question';
    this._service.process(this._definitionKey, this._businessKey).then(processes => {
      if (processes.length > 0) {
        const process = processes[0];

        if (process.state == 'COMPLETED') {
          this._class = 'fa fa-check';
        } else if (process.state == 'ACTIVE') {
          this._class = 'fa fa-process';
        }
      }
    });
  }
}
