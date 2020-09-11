import {Component, OnDestroy, OnInit} from '@angular/core';
import {Subscription} from "rxjs";
import {Exception} from "../../core-models";
import {HttpErrorService} from "../../services/http-error.service";

@Component({
  selector: 'app-error-message',
  templateUrl: './error-message.component.html',
  styleUrls: ['./error-message.component.scss']
})
export class ErrorMessageComponent implements OnInit, OnDestroy {

  private _lastError: Exception.Error;
  private _subscription: Subscription;

  constructor(private _service: HttpErrorService) { }

  ngOnInit() {
    this._subscription = this._service.$.subscribe(e => this._lastError = e);
  }

  ngOnDestroy(): void {
    this._subscription.unsubscribe();
  }

  get error(): Exception.Error {
    return this._lastError;
  }

}
