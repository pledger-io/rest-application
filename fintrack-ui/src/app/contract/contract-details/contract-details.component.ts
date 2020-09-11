import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {Contract} from "../contract-models";
import {ContractService} from "../contract.service";
import {Page, Transaction} from "../../core/core-models";

@Component({
  selector: 'app-contract-details',
  templateUrl: './contract-details.component.html',
  styleUrls: ['./contract-details.component.scss']
})
export class ContractDetailsComponent implements OnInit {

  currentPage: number;
  contract: Contract;
  transactions: Page<Transaction>;

  constructor(private routerState: ActivatedRoute,
              private contractService: ContractService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.currentPage = 1;
    this.routerState.data.subscribe(data => {
      this.contract = data.contract as Contract;
      this.contractService.transactions(this.contract.id, this.currentPage - 1)
        .then(result => this.transactions = result);
    });
  }

  pageChanged() {
    this.router.navigate(['/contracts', this.contract.id], {
      queryParams: {
        page: this.currentPage
      }
    });
  }
}
