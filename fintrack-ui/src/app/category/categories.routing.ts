import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {AuthorizationService} from "../core/core-services";
import {BreadcrumbService} from "../service/breadcrumb.service";
import {TitleService} from "../service/title.service";
import {Breadcrumb} from "../core/core-models";
import {CategoryOverviewComponent} from "./category-overview/category-overview.component";
import {CategoryModule} from "./category.module";
import {CategoryEditComponent} from "./category-edit/category-edit.component";

const routes: Routes = [
  {
    path: '',
    component: CategoryOverviewComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    data: {
      title: 'page.title.category.overview',
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.settings'),
        new Breadcrumb(null, 'page.nav.settings.categories')
      ]
    }
  },
  {
    path: 'add',
    component: CategoryEditComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    data: {
      title: 'page.title.category.add',
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.settings'),
        new Breadcrumb('/categories', 'page.nav.settings.categories'),
        new Breadcrumb(null, 'page.nav.settings.categories.add')
      ]
    }
  },
  {
    path: ':id/edit',
    component: CategoryEditComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    data: {
      title: 'page.title.category.edit',
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.settings'),
        new Breadcrumb('/categories', 'page.nav.settings.categories'),
        new Breadcrumb(null, 'page.nav.settings.categories.edit')
      ]
    }
  }
];

@NgModule({
  imports: [CategoryModule, RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CategoryRoutingModule { }
