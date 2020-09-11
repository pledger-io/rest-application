import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {AuthorizationService} from "../core/core-services";
import {BreadcrumbService} from "../service/breadcrumb.service";
import {TitleService} from "../service/title.service";
import {Breadcrumb} from "../core/core-models";
import {RuleOverviewComponent} from "./rule-overview/rule-overview.component";
import {TransactionRuleModule} from "./transaction-rule.module";
import {RuleEditComponent} from "./rule-edit/rule-edit.component";
import {TransactionRuleResolverService} from "./transaction-rule-resolver.service";

const routes: Routes = [
  {
    path: '',
    component: RuleOverviewComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    data: {
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.accounting'),
        new Breadcrumb(null, 'page.nav.automation'),
        new Breadcrumb(null, 'page.nav.settings.rules')
      ],
      title: 'page.title.rules.overview',
    }
  },
  {
    path: ':group',
    children: [
      {
        path: 'add',
        component: RuleEditComponent,
        canActivate: [AuthorizationService, BreadcrumbService, TitleService],
        resolve: {
          rule: TransactionRuleResolverService
        },
        data: {
          breadcrumbs: [
            new Breadcrumb(null, 'page.nav.accounting'),
            new Breadcrumb(null, 'page.nav.automation'),
            new Breadcrumb('/rules', 'page.nav.settings.rules'),
            new Breadcrumb(null, 'page.nav.settings.rules.edit'),
          ],
          title: 'page.title.rules.edit',
        }
      },
      {
        path: ':id/edit',
        component: RuleEditComponent,
        canActivate: [AuthorizationService, BreadcrumbService, TitleService],
        resolve: {
          rule: TransactionRuleResolverService
        },
        data: {
          breadcrumbs: [
            new Breadcrumb(null, 'page.nav.accounting'),
            new Breadcrumb(null, 'page.nav.automation'),
            new Breadcrumb('/rules', 'page.nav.settings.rules'),
            new Breadcrumb(null, 'page.nav.settings.rules.edit'),
          ],
          title: 'page.title.rules.edit',
        }
      }
    ]
  }
];

@NgModule({
  imports: [TransactionRuleModule, RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TransactionRuleRoutingModule { }
