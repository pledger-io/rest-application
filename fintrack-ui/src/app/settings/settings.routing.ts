import {RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';
import {AuthorizationService} from '../core/core-services';
import {BreadcrumbService} from '../service/breadcrumb.service';
import {TitleService} from '../service/title.service';
import {Breadcrumb} from '../core/core-models';
import {SettingOverviewComponent} from './setting-overview/setting-overview.component';
import {SettingsModule} from './settings.module';
import {CurrencyOverviewComponent} from './currency-overview/currency-overview.component';
import {CurrencyEditComponent} from './currency-edit/currency-edit.component';
import {CurrencyModel} from './settings-models';
import {CurrencyResolverService} from './currency-resolver.service';

const routes: Routes = [
  {
    path: '',
    component: SettingOverviewComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    data: {
      title: 'page.title.category.overview',
      breadcrumbs: [
        new Breadcrumb(null, 'page.header.application.settings')
      ]
    }
  },
  {
    path: 'currencies',
    component: CurrencyOverviewComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    data: {
      title: 'page.settings.currencies.title',
      breadcrumbs: [
        new Breadcrumb(null, 'page.settings.currencies.title')
      ]
    }
  },
  {
    path: 'currency',
    children: [
      {
        path: 'add',
        component: CurrencyEditComponent,
        data: {
          title: 'page.settings.currencies.add',
          breadcrumbs: [
            new Breadcrumb('/settings/currencies', 'page.settings.currencies.title'),
            new Breadcrumb(null, 'page.settings.currencies.add')
          ],
          currency: {} as CurrencyModel
        }
      },
      {
        path: 'edit/:currencyCode',
        component: CurrencyEditComponent,
        resolve: {
          currency: CurrencyResolverService
        },
        data: {
          title: 'page.settings.currencies.edit',
          breadcrumbs: [
            new Breadcrumb('/settings/currencies', 'page.settings.currencies.title'),
            new Breadcrumb(null, 'page.settings.currencies.edit')
          ],
        }
      }
    ]
  }
];

@NgModule({
  imports: [SettingsModule, RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SettingRoutingModule { }
