import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {AuthorizationService} from "../core/core-services";
import {BreadcrumbService} from "../service/breadcrumb.service";
import {TitleService} from "../service/title.service";
import {Breadcrumb} from "../core/core-models";
import {ProfilePageComponent} from "./profile-page/profile-page.component";
import {ProfileModule} from "./profile.module";

const routes: Routes = [
  {
    path: 'profile',
    component: ProfilePageComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    data: {
      title: 'page.title.user.profile',
      breadcrumbs: [
        new Breadcrumb(null, 'page.title.user.profile')
      ]
    }
  }
];

@NgModule({
  imports: [ProfileModule, RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProfileRoutingModule { }
