import { Directive, ViewContainerRef } from '@angular/core';

@Directive({
  selector: '[quick-navigation]',
})
export class QuickNavigation {
  constructor(public viewContainerRef: ViewContainerRef) { }
}

