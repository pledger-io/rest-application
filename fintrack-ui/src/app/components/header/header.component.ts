import {Component, OnDestroy, OnInit} from '@angular/core';
import {Subscription} from "rxjs";
import {SidebarService} from "../../service/sidebar.service";
import {AuthorizationService} from "../../core/core-services";
import {TitleService} from "../../service/title.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ChangePasswordModalComponent} from "../change-password-modal/change-password-modal.component";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit, OnDestroy {

  titleTextKey : string;
  private _isAdmin: boolean;

  private titleSubscription : Subscription;

  constructor(private _sidebarService: SidebarService,
              private _authorizationService : AuthorizationService,
              private _modalService: NgbModal,
              private titleService : TitleService) { }

  get sidebarService(): SidebarService {
    return this._sidebarService;
  }

  get authorizationService(): AuthorizationService {
    return this._authorizationService;
  }

  get isAdmin(): boolean {
    return this._isAdmin
  }

  ngOnInit() {
    this.titleSubscription = this.titleService.title$.subscribe(title => this.titleTextKey = title);
    this.titleSubscription.add(this._authorizationService.userProfile$.subscribe(
      profile => {
        this._isAdmin = this._authorizationService.token.isAdmin
      }
    ))
  }

  ngOnDestroy() {
    this.titleSubscription.unsubscribe();
  }

  openPasswordDialog() {
    this._modalService.open(ChangePasswordModalComponent);
  }
}
