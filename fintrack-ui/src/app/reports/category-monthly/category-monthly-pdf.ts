import {Injectable} from "@angular/core";
import {LocalizationService} from "../../core/core-services";
import {CustomCurrencyPipe} from "../../core/pipes/custom-currency.pipe";
import {HttpClient} from "@angular/common/http";
import {Category} from "../../category/category.models";
import {DateRange, EntityRef, PdfMake, PdfReport} from "../../core/core-models";
import {environment} from "../../../environments/environment";
import {map} from "rxjs/operators";

@Injectable()
export class CategoryMonthlyPdf extends PdfReport {

  private _title: string;
  private _year: number;

  private _categories: EntityRef[];
  private _categorySVG: string;

  constructor(private _languageService: LocalizationService,
              private _currencyPipe: CustomCurrencyPipe,
              private _http: HttpClient) {
    super();

    super.pageOrientation = 'landscape';
    this._languageService.getText('page.reports.category.title')
      .then(text => this._title = text);
  }

  set year(year: number) {
    this._year = year
  }

  set categories(categories: Category[]) {
    this._categories = categories.map(c => new EntityRef(c.id, c.label));
  }

  categorySVG(svg: string) {
    this._categorySVG = svg
  }

  protected get title(): string {
    return this._title + ' ' + this._year;
  }

  protected async content(): Promise<PdfMake.PdfContent> {
    return [
      {
        text: this.title,
        style: 'title'
      } as PdfMake.PdfParagraph,

      {
        columnGap: 15,
        columns: [
          [
            {
              text: await this._languageService.getText('page.reports.category.title'),
              style: 'header'
            } as PdfMake.PdfParagraph,
            {
              svg: this._categorySVG,
              width: 500
            } as PdfMake.PdfSvg
          ],
          [
            {
              text: await this._languageService.getText('page.reports.category.monthly'),
              style: 'header'
            } as PdfMake.PdfParagraph,
            await this.monthly()
          ]
        ]
      } as PdfMake.PdfColumn,

      await this.monthlyCategories()

    ];
  }

  private async monthly(): Promise<PdfMake.PdfTable> {
    let tableBody: PdfMake.PdfTableBody = [
      [
        await this._languageService.getText('common.month'),
        await this._languageService.getText('page.reports.category.income'),
        await this._languageService.getText('page.reports.category.expense'),
      ]
    ];

    for (let month of this.generateMonths()) {
      tableBody.push([
        await this._languageService.getText('common.month.' + month.month),
        await this.computeBalance(month.range, this._categories, true),
        await this.computeBalance(month.range, this._categories, false)
      ]);
    }

    return {
      layout: 'lightHorizontalLines',
      table: {
        headerRows: 1,
        widths: [150, 50, 50],
        body: tableBody
      }
    }
  }

  private async monthlyCategories(): Promise<PdfMake.PdfTable> {
    let months = this.generateMonths();
    let tableBody: PdfMake.PdfTableBody = [];

    let headerRow: PdfMake.PdfTableRow = [{
      text: await this._languageService.getText('Category.label'),
    }]
    for (let month of months) {
      headerRow.push({
        text: await this._languageService.getText('common.month.' + month.month),
      })
    }

    tableBody.push(headerRow);

    for (let category of this._categories) {
      let dataRow: PdfMake.PdfTableRow = [{
        text: category.name,
        style: 'smallTable'
      }];

      for (let month of months) {
        let paragraph = await this.computeBalance(month.range, [category], false);
        paragraph.style = typeof paragraph.style == 'string'
          ? [paragraph.style, 'smallTable']
          : [...paragraph.style, 'smallTable'];
        dataRow.push(paragraph);
      }

      tableBody.push(dataRow);
    }

    let widths = ['*'];
    widths.push(...[...new Array(12).keys()].map(() => 'auto'));
    return {
      layout: 'lightHorizontalLines',
      margin: [0, 25],
      table: {
        headerRows: 1,
        widths: widths,
        body: tableBody
      }
    }
  }

  private generateMonths() {
    return [...new Array(12).keys()].map((x, idx) => {
      return {
        month: idx + 1,
        range: DateRange.forMonth(this._year, idx + 1)
      }
    });
  }

  private async computeBalance(range: DateRange, categories: EntityRef[], onlyIncome: boolean): Promise<PdfMake.PdfParagraph> {
    let amount = await this._http.post(environment.backend + 'statistics/balance', {
      categories: categories,
      dateRange: {
        start: range.from,
        end: range.until,
      },
      onlyIncome: onlyIncome,
      allMoney: onlyIncome == null
    }).pipe<number>(map(raw => raw['balance'])).toPromise();

    return {
      text: this._currencyPipe.transform(amount),
      style: amount > 0 ? 'balancePositive' : amount < 0 ? 'balanceNegative' : ''
    }
  }

}
