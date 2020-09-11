import {Directive, HostBinding, Input, OnDestroy, OnInit} from '@angular/core';
import {Subscription} from "rxjs";
import {LocalizationService} from "./core/core-services";

@Directive({
  selector: '[appFlag]'
})
export class FlagDirective implements OnInit, OnDestroy {

  @Input()
  private noText: boolean;
  private localeString: string;
  private flagClass : string;
  private languageSubscription : Subscription;

  constructor(private localizationService : LocalizationService) { }

  ngOnInit(): void {
    this.languageSubscription = this.localizationService.language$.subscribe(lang => {
      if (lang) {
        this.flagClass = lang;
        this.localizationService.getText('common.language.' + lang).then(text => this.localeString = text);
      }
    });
  }

  ngOnDestroy(): void {
    this.languageSubscription.unsubscribe();
  }

  @HostBinding('class')
  get getClass() : string {
    return 'flag-icon flag-icon-' + this.flagClass;
  }

  @HostBinding('innerHTML')
  get html(): string {
    return this.noText ? '' : '<span>' + this.localeString + '</span>';
  }
}
