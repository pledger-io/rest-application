import {Component, OnDestroy, OnInit} from '@angular/core';
import {CategoryService} from "../category.service";
import {ActivatedRoute} from "@angular/router";
import {Category, CategoryForm} from "../category.models";
import {HttpErrorResponse} from "@angular/common/http";
import {RouterHistory} from "../../core/router-history";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-category-edit',
  templateUrl: './category-edit.component.html',
  styleUrls: ['./category-edit.component.scss']
})
export class CategoryEditComponent implements OnInit, OnDestroy {

  private id: number;

  model: CategoryForm;
  errorMessage: string;

  private _paramSubscription: Subscription;

  constructor(private service: CategoryService, private route: ActivatedRoute, public routerHistory: RouterHistory) {
    this.model = new CategoryForm('', '');
  }

  ngOnInit() {
    this._paramSubscription = this.route.paramMap.subscribe(map => {
      if (map.get('id')) {
        this.id = parseInt(map.get('id'));
        this.service.getCategory(this.id)
          .then(category => this.model = new CategoryForm(category.label, category.description))
          .catch(() => console.log('Category not found'));
      }
    });
  }

  ngOnDestroy() {
    this._paramSubscription.unsubscribe();
  }

  persist() {
    let persistCompletion : Promise<Category>;
    if (this.id) {
      persistCompletion = this.service.update(this.id, this.model);
    } else {
      persistCompletion = this.service.create(this.model);
    }

    persistCompletion.then(() => this.routerHistory.previous())
      .catch((e: HttpErrorResponse) => this.errorMessage = e.error.message);
  }

}
