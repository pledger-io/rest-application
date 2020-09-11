import {Component, OnDestroy, OnInit} from '@angular/core';
import {Account, AccountForm, AccountService} from "../account.service";
import {ActivatedRoute} from "@angular/router";
import {Subscription} from "rxjs";
import {EntityRef} from "../../core/models/entity";
import {RouterHistory} from "../../core/router-history";

interface FirstTransaction {
  startDate?: string;
  amount?: number;
}

@Component({
  selector: 'app-edit-liability',
  templateUrl: './edit-liability.component.html',
  styleUrls: ['./edit-liability.component.scss']
})
export class EditLiabilityComponent implements OnInit, OnDestroy {

  model: AccountForm;
  openingBalance: FirstTransaction;

  private reconcileRef: EntityRef;
  private id: number;
  private subscription: Subscription;

  constructor(private service: AccountService,
              private route: ActivatedRoute,
              private history: RouterHistory) { }

  ngOnInit(): void {
    this.model = new AccountForm(null, null, null, null, null, null, "");
    this.openingBalance = {};
    this.subscription = this.route.data.subscribe(data => {
      this.service.getAccounts(['reconcile'], 1, null)
        .then(results => {
          if (results.content.length) {
            this.reconcileRef = new EntityRef(results.content[0].id, null);
          }
        })

      if (data.account) {
        const account = data.account as Account;
        this.id = account.id;
        this.model = new AccountForm(
          account.name,
          account.description,
          account.account.currency,
          null,
          null,
          account.account.number,
          account.type,
          account.interest.value,
          account.interest.periodicity);

        this.service.firstTransaction(account.id, "Opening balance")
          .then(firstTransaction => {
            this.openingBalance = {
              amount: firstTransaction.amount,
              startDate: firstTransaction.dates.transaction
            }
          })
      }
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  editing(): boolean {
    return this.id != null;
  }

  persist(): void {
    if (this.id) {
      this.service.update(this.id, this.model)
        .then(() => this.history.previous());
    } else {
      this.service.create(this.model)
        .then((created) => {
          this.service.createTransaction(created.id, {
            date: this.openingBalance.startDate,
            currency: this.model.currency,
            amount: this.openingBalance.amount,
            source: new EntityRef(created.id, null),
            destination: this.reconcileRef,
            description: "Opening balance"
          }).then(() => this.history.previous());
        })
    }
  }

}
