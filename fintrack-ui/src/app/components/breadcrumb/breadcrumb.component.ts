import {Component, ComponentFactoryResolver, OnDestroy, OnInit, Type, ViewChild} from '@angular/core';
import {Subscription} from "rxjs";
import {BreadcrumbService} from "../../service/breadcrumb.service";
import {Breadcrumb} from "../../core/core-models";
import {QuickNavigation} from "../../core/directives/quick-navigation.directive";

@Component({
  selector: 'app-breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrls: ['./breadcrumb.component.scss']
})
export class BreadcrumbComponent implements OnInit, OnDestroy {

  breadcrumbs : Breadcrumb[];

  private updateSubscription : Subscription;
  private componentSubscription: Subscription;
  private quickNavigation: QuickNavigation;

  constructor(private breadcrumbService : BreadcrumbService,
              private componentResolver: ComponentFactoryResolver) {
  }

  ngOnInit() {
    this.updateSubscription = this.breadcrumbService.update$.subscribe(br => this.breadcrumbs = br);
    this.componentSubscription = this.breadcrumbService.component$.subscribe(component => {
        setTimeout(() => this.activateComponent(component), 1);
    });
  }

  ngOnDestroy() {
    this.updateSubscription.unsubscribe();
    this.componentSubscription.unsubscribe();
  }

  @ViewChild(QuickNavigation) set navigationChild(content: QuickNavigation) {
    this.quickNavigation = content;
  }

  private activateComponent(component: Type<any>) {
    if (component) {
      const componentFactory = this.componentResolver.resolveComponentFactory(component);

      if (this.quickNavigation.viewContainerRef) {
        this.quickNavigation.viewContainerRef.clear();
        this.quickNavigation.viewContainerRef.createComponent(componentFactory);
      }
    } else if (this.quickNavigation) {
      this.quickNavigation.viewContainerRef.clear();
    }
  }
}
