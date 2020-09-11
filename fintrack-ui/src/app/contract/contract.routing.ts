import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {AuthorizationService} from "../core/core-services";
import {BreadcrumbService} from "../service/breadcrumb.service";
import {TitleService} from "../service/title.service";
import {Breadcrumb} from "../core/core-models";
import {ContractModule} from "./contract.module";
import {ContractOverviewComponent} from "./contract-overview/contract-overview.component";
import {ContractEditComponent} from "./contract-edit/contract-edit.component";
import {ContractResolverService} from "./contract-resolver.service";
import {ContractDetailsComponent} from "./contract-details/contract-details.component";

const routes: Routes = [
  {
    path: '',
    component: ContractOverviewComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    data: {
      title: 'page.title.import.overview',
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.finances'),
        new Breadcrumb(null, 'page.nav.budget.contracts')
      ]
    }
  },
  {
    path: 'create',
    component: ContractEditComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    data: {
      title: 'page.title.contract.create',
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.finances'),
        new Breadcrumb('/contracts', 'page.nav.budget.contracts'),
        new Breadcrumb(null, 'page.title.contract.create')
      ]
    }
  },
  {
    path: ':id',
    component: ContractDetailsComponent,
    runGuardsAndResolvers: 'always',
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    resolve: {
      contract: ContractResolverService
    },
    data: {
      title: 'page.title.contract.details',
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.finances'),
        new Breadcrumb('/contracts', 'page.nav.budget.contracts'),
        new Breadcrumb(null, 'page.nav.contract.details')
      ]
    }
  },
  {
    path: ':id/edit',
    component: ContractEditComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    resolve: {
      contract: ContractResolverService
    },
    data: {
      title: 'page.title.contract.edit',
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.finances'),
        new Breadcrumb('/contracts', 'page.nav.budget.contracts'),
        new Breadcrumb(null, 'common.action.edit')
      ]
    }
  }
];

@NgModule({
  imports: [ContractModule, RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ContractRoutingModule {
}
