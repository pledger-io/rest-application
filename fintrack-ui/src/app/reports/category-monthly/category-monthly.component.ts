import { Component, OnInit } from '@angular/core';
import {DateRange} from "../../core/core-models";
import {Subscription} from "rxjs";
import {ActivatedRoute} from "@angular/router";
import {CategoryService} from "../../category/category.service";
import {Category} from "../../category/category.models";
import {CategoryMonthlyPdf} from "./category-monthly-pdf";

@Component({
  selector: 'app-category-monthly',
  templateUrl: './category-monthly.component.html',
  styleUrls: ['./category-monthly.component.scss']
})
export class CategoryMonthlyComponent implements OnInit {

  private _range: DateRange;
  private _subscription: Subscription;
  private _months: any[];
  private _categories: Category[];
  private _currency: string;

  constructor(private _route: ActivatedRoute,
              private _service: CategoryService,
              private _pdfReport: CategoryMonthlyPdf) {
  }

  get range(): DateRange {
    return this._range
  }

  get currency(): string {
    return this._currency;
  }

  get months(): any[] {
    return this._months;
  }

  get categories(): Category[] {
    return this._categories
  }

  get pdf(): CategoryMonthlyPdf {
    return this._pdfReport
  }

  ngOnInit(): void {
    this._subscription = this._route.data.subscribe(params => {
      let year = params.year;

      this._currency = params.currency.code;
      this._months = [...new Array(12).keys()].map((x, idx) => {
        return {
          month: idx + 1,
          range: DateRange.forMonth(year, idx + 1)
        }
      });

      this._range = DateRange.forYear(year);
    });

    this._service.list()
      .then(result => this._categories = result
        .sort((c1, c2) => c1.label.localeCompare(c2.label)));
  }

  download() {
    this._pdfReport.year = this._range.computeStartYear();
    this._pdfReport.categories = this._categories;

    return this.pdf.save();
  }

}
