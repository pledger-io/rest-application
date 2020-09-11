import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {AuthorizationService} from "../core/core-services";
import {BreadcrumbService} from "../service/breadcrumb.service";
import {TitleService} from "../service/title.service";
import {Breadcrumb} from "../core/core-models";
import {EditScheduleComponent} from "./edit-schedule/edit-schedule.component";
import {TransactionScheduleModule} from "./transaction-schedule.module";
import {ScheduleOverviewComponent} from "./schedule-overview/schedule-overview.component";

const routes: Routes = [
  {
    path: '',
    component: ScheduleOverviewComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    data: {
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.accounting'),
        new Breadcrumb(null, 'page.nav.automation'),
        new Breadcrumb(null, 'page.nav.budget.recurring')
      ],
      title: 'page.title.schedule.transaction'
    }
  },
  {
    path: ':scheduleId/edit',
    component: EditScheduleComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    data: {
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.accounting'),
        new Breadcrumb(null, 'page.nav.automation'),
        new Breadcrumb(null, 'page.nav.budget.recurring'),
        new Breadcrumb(null, 'common.action.edit')
      ],
      title: 'page.title.schedule.transaction.edit',
    }
  },
];

@NgModule({
  imports: [TransactionScheduleModule, RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TransactionScheduleRoutingModule { }
