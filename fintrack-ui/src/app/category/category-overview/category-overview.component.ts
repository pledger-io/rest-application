import { Component, OnInit } from '@angular/core';
import {CategoryService} from "../category.service";
import {Pagable, Page} from "../../core/core-models";
import {Category} from "../category.models";
import {ConfirmModalComponent} from "../../core/confirm-modal/confirm-modal.component";
import {noop} from "rxjs";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ToastService} from "../../core/core-services";

@Component({
  selector: 'app-category-overview',
  templateUrl: './category-overview.component.html',
  styleUrls: ['./category-overview.component.scss']
})
export class CategoryOverviewComponent implements OnInit {

  private _loading: boolean;
  pager: Pagable;
  currentPage: Page<Category>;

  constructor(private service: CategoryService, private modelService : NgbModal, private toastService: ToastService) {
    this.pager = new Pagable(0, 20);
  }

  get loading() : boolean {
    return this._loading
  }

  ngOnInit() {
    this.pageChanged();
  }

  pageChanged() {
    this._loading = true;
    this.service.getCategories(this.pager.page)
      .then(page => this.currentPage = page)
      .finally(() => this._loading = false);
  }

  get empty() : boolean {
    return !this.currentPage || this.currentPage.info.records == 0;
  }

  confirmDelete(category: Category) {
    let modalRef = this.modelService.open(ConfirmModalComponent);
    modalRef.componentInstance.titleTextKey = 'common.action.delete';
    modalRef.componentInstance.descriptionKey = 'page.category.delete.confirm';
    modalRef.result
      .then(() => {
        this.service.delete(category.id)
          .then(() => {
            this.pageChanged();
            this.toastService.show('page.category.delete.success', {classname: 'bg-success text-light', delay: 10000})
          })
          .catch(() => this.toastService.show('page.category.delete.failed', {classname: 'bg-danger text-light', delay: 15000}));
      })
      .catch(noop)
  }

}
