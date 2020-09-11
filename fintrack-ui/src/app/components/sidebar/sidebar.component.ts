import {Component, OnInit} from '@angular/core';
import {AuthorizationService} from "../../core/core-services";
import {SidebarService} from "../../service/sidebar.service";
import {ChangePasswordModalComponent} from "../change-password-modal/change-password-modal.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit {

  private _displayReportSub: boolean;
  private _displayAccounts: boolean;
  private _displayAutomation: boolean;
  private _displayTransactions: boolean;
  private _displayOptions: boolean;
  private _isAdmin: boolean;

  constructor(private _authorizationService : AuthorizationService,
              private _sidebarService: SidebarService,
              private _modalService: NgbModal) { }

  get displayReport(): boolean {
    return this._displayReportSub;
  }

  get displayAccounts(): boolean {
    return this._displayAccounts;
  }

  get displayAutomation(): boolean {
    return this._displayAutomation;
  }

  get displayTransactions(): boolean {
    return this._displayTransactions;
  }

  get displayOptions(): boolean {
    return this._displayOptions;
  }

  get authorizationService(): AuthorizationService {
    return this._authorizationService;
  }

  get service(): SidebarService {
    return this._sidebarService;
  }

  get isAdmin(): boolean {
    return this._isAdmin;
  }

  ngOnInit() {
    this._displayReportSub = false;
    this._authorizationService.userProfile$.subscribe(profile => {
      this._isAdmin = this._authorizationService.token.isAdmin;
    });
  }

  openPasswordDialog() {
    this._modalService.open(ChangePasswordModalComponent);
  }

  toggleReport() {
    this._displayReportSub = !this._displayReportSub;
  }

  toggleAccounts() {
    this._displayAccounts = !this._displayAccounts;
  }

  toggleAutomation() {
    this._displayAutomation = !this._displayAutomation;
  }

  toggleTransactions() {
    this._displayTransactions = !this._displayTransactions;
  }

  toggleOptions() {
    this._displayOptions = !this._displayOptions;
  }

}
