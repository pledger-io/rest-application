import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {Router} from "@angular/router";
import {AccountRef} from "../../core/models/account-ref";
import {AccountService} from "../../accounts/account.service";

@Component({
  selector: 'app-account-select',
  templateUrl: './account-select.component.html',
  styleUrls: ['./account-select.component.scss']
})
export class AccountSelectComponent implements OnInit {

  private _type: string;
  private _accounts: AccountRef[];
  private _account: AccountRef;

  constructor(private _modal: NgbActiveModal,
              private _accountService: AccountService) {
  }

  set type(value: string) {
    this._type = value;
  }

  get accounts(): AccountRef[] {
    return this._accounts;
  }

  get account(): AccountRef {
    return this._account;
  }

  set account(value: AccountRef) {
    this._account = value;
  }

  compareAccount(a1: AccountRef, a2: AccountRef) {
    return a1 != null && a2 != null && a1.id == a2.id;
  }

  ngOnInit(): void {
    this._accountService.getOwnAccounts()
      .then(accounts => this._accounts = accounts);
  }

  dismiss(): void {
    this._modal.dismiss();
  }

  process(): void {
    this._modal.close(new AccountRef(this._account.id, this._account.type, this._account.name));
  }
}
