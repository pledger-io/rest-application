import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {BudgetOverviewComponent} from "./budget-overview/budget-overview.component";
import {BudgetModule} from "./budget.module";
import {AuthorizationService} from "../core/core-services";
import {BreadcrumbService} from "../service/breadcrumb.service";
import {TitleService} from "../service/title.service";
import {Breadcrumb} from "../core/core-models";
import {BudgetQuickActionComponent} from "./budget-quick-action/budget-quick-action.component";
import {DateRangeResolverService} from "../core/date-range-resolver.service";

const routes: Routes = [
  {
    path: '',
    component: BudgetOverviewComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    resolve: {
      dateRange: DateRangeResolverService
    },
    data: {
      title: 'page.title.budget.group',
      quickNavigation: BudgetQuickActionComponent,
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.finances'),
        new Breadcrumb(null, 'page.nav.budget.groups')
      ]
    }
  },
  {
    path: ':from/:until',
    component: BudgetOverviewComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    resolve: {
      dateRange: DateRangeResolverService
    },
    data: {
      title: 'page.title.budget.group',
      quickNavigation: BudgetQuickActionComponent,
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.finances'),
        new Breadcrumb(null, 'page.nav.budget.groups')
      ]
    }
  }
];

@NgModule({
  imports: [BudgetModule, RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BudgetRoutingModule { }
