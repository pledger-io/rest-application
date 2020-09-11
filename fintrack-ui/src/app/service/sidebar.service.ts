import {HostListener, Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SidebarService {

  private visible: boolean;
  private _closeOnClick: boolean;

  constructor() {
    this._closeOnClick = false;
    this._closeOnClick = window.innerWidth < 1024;
  }

  toggle() {
    this.visible = !this.visible;
  }

  isVisible(): boolean {
    return this.visible;
  }

  navigate(): void {
    if (this._closeOnClick) {
      this.visible = false;
    }
  }

  @HostListener('window:resize', ['$event'])
  onResize(event?) {
    this._closeOnClick = window.innerWidth < 1024;
  }
}
