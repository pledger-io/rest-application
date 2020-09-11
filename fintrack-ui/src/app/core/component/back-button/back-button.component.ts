import {Component, Input, OnInit} from '@angular/core';
import {RouterHistory} from "../../router-history";

@Component({
  selector: 'app-back-button',
  templateUrl: './back-button.component.html',
  styleUrls: ['./back-button.component.scss']
})
export class BackButtonComponent implements OnInit {

  @Input()
  private _textKey: string;

  constructor(private _history: RouterHistory) {
    this._textKey = 'common.action.cancel';
  }

  ngOnInit() {
  }

  history() {
    this._history.previous();
  }

  get textKey(): string {
    return this._textKey;
  }

}
