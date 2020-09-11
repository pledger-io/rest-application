import { Component, OnInit } from '@angular/core';
import {ContractService} from "../contract.service";
import {ContractOverview} from "../contract-models";

@Component({
  selector: 'app-contract-overview',
  templateUrl: './contract-overview.component.html',
  styleUrls: ['./contract-overview.component.scss']
})
export class ContractOverviewComponent implements OnInit {

  private _overview: ContractOverview;

  constructor(private _service: ContractService) { }

  get overview(): ContractOverview {
    return this._overview;
  }

  ngOnInit() {
    this.load();
  }

  load() {
    this._service.list().then(data => this._overview = data);
  }

}
