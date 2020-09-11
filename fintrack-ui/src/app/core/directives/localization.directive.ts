import {Directive, ElementRef, Input, OnDestroy, OnInit} from '@angular/core';
import {Subscription} from "rxjs";
import {LocalizationService} from "../core-services";

@Directive({
  selector: '[appLocalization]'
})
export class LocalizationDirective implements OnInit, OnDestroy {

  @Input()
  private location : string;
  private textKey : string;
  private languageSubscription: Subscription;

  constructor(private el: ElementRef, private localizationService : LocalizationService) { }

  ngOnInit() {
    this.languageSubscription = this.localizationService.language$.subscribe(lang => this.reload());
    this.reload();
  }

  ngOnDestroy(): void {
    this.languageSubscription.unsubscribe();
  }

  @Input('appLocalization')
  set setKey(textKey : string) {
    this.textKey = textKey;
    this.reload();
  }

  reload() {
    if (this.textKey) {
      this.localizationService.getText(this.textKey).then(text => {
        if (!this.location) {
          this.el.nativeElement.innerHTML = text;
        } else {
          this.el.nativeElement[this.location] = text;
        }
      });
    }
  }

}
