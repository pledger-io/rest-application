import {NgModule} from '@angular/core';
import {CommonModule, DecimalPipe} from '@angular/common';
import {ConfirmModalComponent} from './confirm-modal/confirm-modal.component';
import {NgbActiveModal, NgbDateAdapter, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {LocalizationDirective} from './directives/localization.directive';
import {BalanceDirective} from './directives/balance.directive';
import {PatternValidatorDirective} from './directives/pattern-validator.directive';
import {InputFieldComponent} from './component/input-field/input-field.component';
import {FormsModule} from "@angular/forms";
import {RouterHistory} from "./router-history";
import {SpinnerComponent} from './component/spinner/spinner.component';
import {DownloadDirective} from './directives/download.directive';
import {CustomCurrencyPipe} from './pipes/custom-currency.pipe';
import {QuickNavigation} from "./directives/quick-navigation.directive";
import {CustomDatePipe} from "./pipes/custom-date.pipe";
import {GraphDisplayComponent} from './component/graph-display/graph-display.component';
import {BackButtonComponent} from './component/back-button/back-button.component';
import {RouterModule} from "@angular/router";
import {ErrorMessageComponent} from './component/error-message/error-message.component';
import {MinValidatorDirective} from './directives/min-validator.directive';
import {CustomDateAdapter} from "./custom-date-adapter";
import {AutocompleteComponent} from './component/autocomplete/autocomplete.component';
import {StatisticalBalanceComponent} from './component/statistical-balance/statistical-balance.component';
import {ProcessStatusDirective} from './directives/process-status.directive';
import {AbsoluteNumberPipe} from './pipes/absolute-number.pipe';
import {ProcessVariableDirective} from "./directives/process-variable.directive";
import {PasswordInputDirective} from './directives/password-input.directive';
import { TagInputComponent } from './component/tag-input/tag-input.component';
import { CurrencySelectorComponent } from './component/currency-selector/currency-selector.component';
import {PercentageDirective} from "./directives/percentage.directive";

@NgModule({
  declarations: [ConfirmModalComponent, LocalizationDirective, BalanceDirective, PatternValidatorDirective,
    InputFieldComponent, SpinnerComponent, DownloadDirective, CustomCurrencyPipe, QuickNavigation,
    CustomDatePipe, GraphDisplayComponent, BackButtonComponent, ErrorMessageComponent, MinValidatorDirective,
    AutocompleteComponent, StatisticalBalanceComponent, ProcessStatusDirective, AbsoluteNumberPipe,
    ProcessVariableDirective, PasswordInputDirective, TagInputComponent, CurrencySelectorComponent,
    PercentageDirective],
  imports: [
    CommonModule,
    NgbModule,
    FormsModule,
    RouterModule
  ],
  exports: [ConfirmModalComponent, LocalizationDirective, BalanceDirective, PatternValidatorDirective,
    InputFieldComponent, SpinnerComponent, DownloadDirective, CustomCurrencyPipe, QuickNavigation,
    CustomDatePipe, GraphDisplayComponent, BackButtonComponent, ErrorMessageComponent, MinValidatorDirective,
    AutocompleteComponent, StatisticalBalanceComponent, ProcessStatusDirective, AbsoluteNumberPipe,
    ProcessVariableDirective, PasswordInputDirective, TagInputComponent, CurrencySelectorComponent,
    PercentageDirective],
  entryComponents: [ConfirmModalComponent],
  providers: [
    NgbActiveModal,
    RouterHistory,
    DecimalPipe,
    {provide: NgbDateAdapter, useClass: CustomDateAdapter}
  ]
})
export class CoreModule {
}
