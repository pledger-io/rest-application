import {Directive, HostListener, Input} from '@angular/core';
import {LocalizationService} from "./core/core-services";

@Directive({
  selector: '[appLanguageSelect]'
})
export class LanguageSelectDirective {

  @Input('appLanguageSelect')
  private languageCode: string;

  constructor(private localizationService: LocalizationService) {
  }

  @HostListener('click', ['$event'])
  onClick($event) {
    this.localizationService.setLanguage(this.languageCode);
  }

}
