import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {NgbActiveModal, NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {AuthenticateComponent} from './authenticate/authenticate.component';
import {DashboardComponent} from './dashboard/dashboard.component';
import {FormsModule} from '@angular/forms';
import {FlagDirective} from './flag.directive';
import {LanguageSelectDirective} from './language-select.directive';
import {RegisterComponent} from './authenticate/register.component';
import {HttpInterceptorService} from './service/http-interceptor.service';
import {ProfilePictureDirective} from './profile-picture.directive';
import {CoreModule} from './core/core.module';
import {HeaderComponent} from './components/header/header.component';
import {SidebarComponent} from './components/sidebar/sidebar.component';
import {BreadcrumbComponent} from './components/breadcrumb/breadcrumb.component';
import {CustomCurrencyPipe} from './core/pipes/custom-currency.pipe';
import {CustomDatePipe} from './core/pipes/custom-date.pipe';
import { VerifyTokenComponent } from './authenticate/verify-token.component';
import { ChangePasswordModalComponent } from './components/change-password-modal/change-password-modal.component';
import {ToastyComponent} from './core/component/toasty/toasty.component';
import { ServiceWorkerModule } from '@angular/service-worker';
import { environment } from '../environments/environment';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    SidebarComponent,
    AuthenticateComponent,
    BreadcrumbComponent,
    DashboardComponent,
    FlagDirective,
    LanguageSelectDirective,
    RegisterComponent,
    ProfilePictureDirective,
    ToastyComponent,
    VerifyTokenComponent,
    ChangePasswordModalComponent
  ],
  imports: [
    CoreModule,
    NgbModule,
    BrowserModule,
    HttpClientModule,
    FormsModule,
    AppRoutingModule,
    ServiceWorkerModule.register('ngsw-worker.js', { enabled: environment.production }),
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpInterceptorService,
      multi: true
    },
    NgbActiveModal,
    CustomCurrencyPipe,
    CustomDatePipe
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
