import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {AuthorizationService} from "../core/core-services";
import {BreadcrumbService} from "../service/breadcrumb.service";
import {TitleService} from "../service/title.service";
import {Breadcrumb} from "../core/core-models";
import {BatchImportModule} from "./batch-import.module";
import {ImportOverviewComponent} from "./import-overview/import-overview.component";
import {StartImportComponent} from "./start-import/start-import.component";
import {BatchConfigResolverService} from "./resolver/batch-config-resolver.service";
import {AnalyzeImportComponent} from "./analyze-import/analyze-import.component";
import {BatchImportResolverService} from "./resolver/batch-import-resolver.service";
import {MappingsImportComponent} from "./mappings-import/mappings-import.component";
import {AccountMapImportComponent} from "./account-map-import/account-map-import.component";
import {ImportConfigurationResolverService} from "./resolver/import-configuration-resolver.service";
import {AccountExtractorResolverService} from "./resolver/account-extractor-resolver.service";
import {DetailPageComponent} from "./detail-page/detail-page.component";

const routes: Routes = [
    {
      path: '',
      component: ImportOverviewComponent,
      canActivate: [AuthorizationService, BreadcrumbService, TitleService],
      data: {
        title: 'page.title.import.overview',
        breadcrumbs: [
          new Breadcrumb(null, 'page.nav.settings'),
          new Breadcrumb(null, 'page.nav.settings.import')
        ]
      }
    },
    {
      path: 'start',
      component: StartImportComponent,
      canActivate: [AuthorizationService, BreadcrumbService, TitleService],
      resolve: {
        configs: BatchConfigResolverService
      },
      data: {
        title: 'page.nav.settings.import.start',
        breadcrumbs: [
          new Breadcrumb(null, 'page.nav.settings'),
          new Breadcrumb('/import', 'page.nav.settings.import'),
          new Breadcrumb(null, 'page.nav.settings.import.start')
        ]
      }
    },
    {
      path: ':batchSlug',
      resolve: {
        batch: BatchImportResolverService
      },
      children: [
        {
          path: 'analyze',
          component: AnalyzeImportComponent,
          canActivate: [AuthorizationService, BreadcrumbService, TitleService],
          resolve: {
            config: ImportConfigurationResolverService
          },
          data: {
            title: 'page.nav.settings.import.start',
            breadcrumbs: [
              new Breadcrumb(null, 'page.nav.settings'),
              new Breadcrumb('/import', 'page.nav.settings.import'),
              new Breadcrumb(null, 'page.nav.settings.import.start')
            ]
          }
        },
        {
          path: 'mappings',
          component: MappingsImportComponent,
          canActivate: [AuthorizationService, BreadcrumbService, TitleService],
          resolve: {
            config: ImportConfigurationResolverService
          },
          data: {
            title: 'page.nav.settings.import.start',
            breadcrumbs: [
              new Breadcrumb(null, 'page.nav.settings'),
              new Breadcrumb('/import', 'page.nav.settings.import'),
              new Breadcrumb(null, 'page.nav.settings.import.start')
            ]
          }
        },
        {
          path: 'accounts',
          component: AccountMapImportComponent,
          canActivate: [AuthorizationService, BreadcrumbService, TitleService],
          resolve: {
            config: ImportConfigurationResolverService,
            mappings: AccountExtractorResolverService
          },
          data: {
            title: 'page.nav.settings.import.start',
            breadcrumbs: [
              new Breadcrumb(null, 'page.nav.settings'),
              new Breadcrumb('/import', 'page.nav.settings.import'),
              new Breadcrumb(null, 'page.nav.settings.import.start')
            ]
          }
        },
        {
          path: 'status',
          component: DetailPageComponent,
          canActivate: [AuthorizationService, BreadcrumbService, TitleService],
          data: {
            title: 'page.title.import.status',
            breadcrumbs: [
              new Breadcrumb(null, 'page.nav.settings'),
              new Breadcrumb('/import', 'page.nav.settings.import'),
              new Breadcrumb(null, 'page.nav.settings.import.status')
            ]
          }
        }
      ]
    }
  ]
;

@NgModule({
  imports: [BatchImportModule, RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BatchImportRoutingModule {
}
