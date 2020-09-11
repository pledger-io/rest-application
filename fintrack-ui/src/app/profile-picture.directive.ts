import {Directive, HostBinding, Input, OnDestroy, OnInit} from '@angular/core';
import {Subscription} from "rxjs";
import {AuthorizationService} from "./core/core-services";
import {UserAccount} from "./core/services/authorization.service";

@Directive({
  selector: '[appProfilePicture]'
})
export class ProfilePictureDirective implements OnInit, OnDestroy {

  private user : UserAccount;
  private userSubscription : Subscription;

  constructor(private authorizationService : AuthorizationService) { }

  ngOnInit(): void {
    this.userSubscription = this.authorizationService.userProfile$.subscribe(user => this.user = user);
  }

  ngOnDestroy(): void {
    this.userSubscription.unsubscribe();
  }

  @HostBinding('class.img-avatar')
  get getClass() : boolean {
    return true;
  }

  @HostBinding('src')
  get getBaseCoded() {
    if (this.user && this.user.profilePicture) {
      return 'data:image/png;base64, ' + this.user.profilePicture;
    }

    return 'assets/user60.png';
  }

}
