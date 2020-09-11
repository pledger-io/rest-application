import {Component, OnDestroy, OnInit} from '@angular/core';
import {Account, AccountForm, AccountService, AccountType} from '../account.service';
import {ActivatedRoute} from '@angular/router';
import {RouterHistory} from '../../core/router-history';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-edit-account',
  templateUrl: './edit-account.component.html',
  styleUrls: ['./edit-account.component.scss']
})
export class EditAccountComponent implements OnInit, OnDestroy {

  private accountId: number;
  private _subscription: Subscription;

  model: AccountForm;
  accountTypes: string[];

  constructor(private route: ActivatedRoute, private accountService: AccountService, public routerHistory: RouterHistory) {
  }

  ngOnInit() {
    this._subscription = new Subscription();
    this._subscription.add(this.route.paramMap.subscribe(map => {
        const type = map.get('type');
        if (map.get('accountId') == null) {
          const accountType = type == 'expense' ? 'creditor' : (type == 'revenue' ? 'debtor' : '');
          this.model = new AccountForm('', '', '', '', '', '', accountType);
        }
      }));

    this._subscription.add(this.route.data.subscribe(data => {
      if (data.account) {
        this.accountId = data.account.id;
        this.model = AccountForm.fromAccount(data.account);
      }
    }));

    this.accountService.getAccountTypes().then(types => this.accountTypes = types);
  }

  ngOnDestroy() {
    this._subscription.unsubscribe();
  }

  persist() {
    this.model.iban = this.model.iban === '' ? null : this.model.iban;
    this.model.bic = this.model.bic === '' ? null : this.model.bic;
    this.model.number = this.model.number === '' ? null : this.model.number;

    let persistCompletion: Promise<Account>;
    if (this.accountId) {
      persistCompletion = this.accountService.update(this.accountId, this.model);
    } else {
      persistCompletion = this.accountService.create(this.model);
    }

    persistCompletion.then(() => this.routerHistory.previous());
  }

}
