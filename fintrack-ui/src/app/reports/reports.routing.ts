import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {AuthorizationService} from "../core/core-services";
import {BreadcrumbService} from "../service/breadcrumb.service";
import {TitleService} from "../service/title.service";
import {IncomeExpenseComponent} from "./income-expense/income-expense.component";
import {ReportsModule} from "./reports.module";
import {BudgetMonthlyComponent} from "./budget-monthly/budget-monthly.component";
import {YearSelectionQuickActionComponent} from "./year-selection-quick-action/year-selection-quick-action.component";
import {Breadcrumb} from "../core/core-models";
import {YearResolverService} from "./year-resolver.service";
import {CategoryMonthlyComponent} from "./category-monthly/category-monthly.component";
import {CurrencyResolverService} from "./currency-resolver.service";

const routes: Routes = [
  {
    path: 'income-expense',
    runGuardsAndResolvers: 'always',
    component: IncomeExpenseComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    resolve: {
      year: YearResolverService,
      currency: CurrencyResolverService
    },
    data: {
      quickNavigation: YearSelectionQuickActionComponent,
      title: 'page.reports.default.title',
      breadcrumbs: [
        new Breadcrumb(null, 'page.title.reports.default'),
        new Breadcrumb(null, 'page.reports.default.title')
      ]
    }
  },
  {
    path: 'income-expense/:year',
    component: IncomeExpenseComponent,
    runGuardsAndResolvers: 'always',
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    resolve: {
      year: YearResolverService,
      currency: CurrencyResolverService
    },
    data: {
      quickNavigation: YearSelectionQuickActionComponent,
      title: 'page.reports.default.title',
      breadcrumbs: [
        new Breadcrumb(null, 'page.title.reports.default'),
        new Breadcrumb(null, 'page.reports.default.title')
      ]
    }
  },
  {
    path: 'monthly-budget',
    runGuardsAndResolvers: 'always',
    component: BudgetMonthlyComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    resolve: {
      year: YearResolverService,
      currency: CurrencyResolverService

    },
    data: {
      quickNavigation: YearSelectionQuickActionComponent,
      title: 'page.title.reports.budget',
      breadcrumbs: [
        new Breadcrumb(null, 'page.title.reports.default'),
        new Breadcrumb(null, 'page.reports.budget.title')
      ]
    }
  },
  {
    path: 'monthly-budget/:year',
    runGuardsAndResolvers: 'always',
    component: BudgetMonthlyComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    resolve: {
      year: YearResolverService,
      currency: CurrencyResolverService
    },
    data: {
      quickNavigation: YearSelectionQuickActionComponent,
      title: 'page.title.reports.budget',
      breadcrumbs: [
        new Breadcrumb(null, 'page.title.reports.default'),
        new Breadcrumb(null, 'page.reports.budget.title')
      ]
    }
  },
  {
    path: 'monthly-category',
    runGuardsAndResolvers: 'always',
    component: CategoryMonthlyComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    resolve: {
      year: YearResolverService,
      currency: CurrencyResolverService
    },
    data: {
      quickNavigation: YearSelectionQuickActionComponent,
      title: 'page.title.reports.category',
      breadcrumbs: [
        new Breadcrumb(null, 'page.title.reports.default'),
        new Breadcrumb(null, 'page.reports.category.title')
      ]
    }
  },
  {
    path: 'monthly-category/:year',
    runGuardsAndResolvers: 'always',
    component: CategoryMonthlyComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    resolve: {
      year: YearResolverService,
      currency: CurrencyResolverService
    },
    data: {
      quickNavigation: YearSelectionQuickActionComponent,
      title: 'page.title.reports.budget',
      breadcrumbs: [
        new Breadcrumb(null, 'page.title.reports.default'),
        new Breadcrumb(null, 'page.reports.category.title')
      ]
    }
  },
];

@NgModule({
  imports: [ReportsModule, RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReportsRoutingModule { }
