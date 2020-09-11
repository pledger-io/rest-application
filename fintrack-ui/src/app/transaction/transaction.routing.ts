import {Route, RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {AuthorizationService} from "../core/core-services";
import {BreadcrumbService} from "../service/breadcrumb.service";
import {TitleService} from "../service/title.service";
import {EditTransactionComponent} from "./edit-transaction/edit-transaction.component";
import {TransactionModule} from "./transaction.module";
import {Breadcrumb} from "../core/core-models";
import {GlobalOverviewComponent} from "./global-overview/global-overview.component";
import {DateRangeResolverService} from "../core/date-range-resolver.service";
import {TransactionQuickActionComponent} from "./transaction-quick-action/transaction-quick-action.component";
import {FirstDateResolverService} from "./first-date-resolver.service";

const routes: Routes = [
  {
    path: 'add/:type',
    component: EditTransactionComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    data: {
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.accounts'),
        new Breadcrumb('$account.path/transactions', '$account.name'),
        new Breadcrumb(null, 'page.nav.transactions'),
        new Breadcrumb(null, 'common.action.edit'),
      ],
      title: 'page.title.transactions.:type.add',
    }
  },
  {
    path: ':transactionId/edit',
    component: EditTransactionComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    data: {
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.accounts'),
        new Breadcrumb('$account.path/transactions', '$account.name'),
        new Breadcrumb(null, 'page.nav.transactions'),
        new Breadcrumb(null, 'common.action.edit'),
      ],
      title: 'page.title.transaction.edit',
    }
  },
];

const globalRoutes: Route[] = [
  {
    path: 'income-expense',
    runGuardsAndResolvers: 'always',
    component: GlobalOverviewComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    resolve: {
      dateRange: DateRangeResolverService,
      transactionRange: FirstDateResolverService
    },
    data: {
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.accounting'),
        new Breadcrumb(null, 'page.nav.transactions'),
      ],
      quickNavigation: TransactionQuickActionComponent,
      title: 'page.title.transactions.overview',
      transfers: false,
    }
  },
  {
    path: 'income-expense/:from/:until',
    runGuardsAndResolvers: 'always',
    component: GlobalOverviewComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    resolve: {
      dateRange: DateRangeResolverService,
      transactionRange: FirstDateResolverService
    },
    data: {
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.accounting'),
        new Breadcrumb(null, 'page.nav.transactions'),
      ],
      quickNavigation: TransactionQuickActionComponent,
      title: 'page.title.transactions.overview',
      transfers: false
    }
  },
  {
    path: 'transfers',
    runGuardsAndResolvers: 'always',
    component: GlobalOverviewComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    resolve: {
      dateRange: DateRangeResolverService,
      transactionRange: FirstDateResolverService
    },
    data: {
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.accounting'),
        new Breadcrumb(null, 'page.nav.transactions'),
      ],
      quickNavigation: TransactionQuickActionComponent,
      title: 'page.title.transactions.overview',
      transfers: true
    }
  },
  {
    path: 'transfers/:from/:until',
    runGuardsAndResolvers: 'always',
    component: GlobalOverviewComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    resolve: {
      dateRange: DateRangeResolverService,
      transactionRange: FirstDateResolverService
    },
    data: {
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.accounting'),
        new Breadcrumb(null, 'page.nav.transactions'),
      ],
      quickNavigation: TransactionQuickActionComponent,
      title: 'page.title.transactions.overview',
      transfers: true
    }
  },
]

@NgModule({
  imports: [TransactionModule, RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TransactionRoutingModule { }

@NgModule({
  imports: [TransactionModule, RouterModule.forChild(globalRoutes)],
  exports: [RouterModule]
})export class GlobalTransactionRoutingModule {

}
