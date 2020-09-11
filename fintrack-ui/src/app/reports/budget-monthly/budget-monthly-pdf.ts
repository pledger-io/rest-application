import {Injectable} from "@angular/core";
import {DateRange, EntityRef, PdfMake, PdfReport} from "../../core/core-models";
import {LocalizationService} from "../../core/core-services";
import {CustomCurrencyPipe} from "../../core/pipes/custom-currency.pipe";
import {HttpClient} from "@angular/common/http";
import {StatisticsBudget} from "./budget-monthly.component";
import {PercentPipe} from "@angular/common";
import {environment} from "../../../environments/environment";
import {map} from "rxjs/operators";

interface BudgetActual {
  month: number
  actual: number
  expected: number
  difference: number
  percentage: number
}

@Injectable()
export class BudgetMonthlyPdf extends PdfReport {

  private _expenses: EntityRef[];
  private _budgets: StatisticsBudget[];

  private _incomeSVG: string;
  private _expenseSVG: string;

  private _year: number;
  private _title: string;

  public constructor(private _languageService: LocalizationService,
                     private _currencyPipe: CustomCurrencyPipe,
                     private _percentPipe: PercentPipe,
                     private _http: HttpClient) {
    super();

    this._languageService.getText('page.reports.budget.title')
      .then(text => this._title = text);
  }

  set expenses(value: EntityRef[]) {
    this._expenses = value;
  }

  set budgets(value: StatisticsBudget[]) {
    this._budgets = value;
  }

  set year(value: number) {
    this._year = value;
  }

  expenseSVG(svg: string) {
    this._expenseSVG = svg;
  }

  incomeSVG(svg: string) {
    this._incomeSVG = svg;
  }

  protected async content(): Promise<PdfMake.PdfContent> {
    let content = [
      {
        text: this.title,
        style: 'title'
      } as PdfMake.PdfParagraph,

      {
        text: await this._languageService.getText('page.reports.budget.incomePercent'),
        style: 'header'
      } as PdfMake.PdfParagraph,
      {
        svg: this._incomeSVG,
        width: 550
      } as PdfMake.PdfSvg,
      await this.monthly(this._budgets.map(budget => {
        return {
          month: budget.range.computeStartMonth()  + 1,
          actual: budget.actualIncome,
          expected: budget.budget.income,
          difference: budget.actualIncome - budget.budget.income,
          percentage: (budget.actualIncome / budget.budget.income) - 1
        } as BudgetActual
      })),

      {
        text: await this._languageService.getText('page.reports.budget.expensePercent'),
        style: 'header',
        pageBreak: 'before'
      } as PdfMake.PdfParagraph,
      {
        svg: this._expenseSVG,
        width: 550
      } as PdfMake.PdfSvg,
      await this.monthly(this._budgets.map(budget => {
        return {
          month: budget.range.computeStartMonth() + 1,
          actual: budget.actualSpent,
          expected: budget.budget.totalExpenses,
          difference: budget.actualSpent + budget.budget.totalExpenses,
          percentage: (budget.actualSpent / budget.budget.totalExpenses) + 1
        } as BudgetActual
      })),
    ];

    content.push(...await this.yearly())
    return content;
  }

  protected get title(): string {
    return this._title + ' ' + this._year;
  }

  private async monthly(statistics: BudgetActual[]): Promise<PdfMake.PdfTable> {
    let body: PdfMake.PdfTableBody = [];

    body.push([
      await this._languageService.getText('common.month'),
      await this._languageService.getText('Transaction.budget'),
      await this._languageService.getText('graph.series.budget.actual'),
      await this._languageService.getText('common.difference'),
      await this._languageService.getText('common.percentage'),
    ])

    for (let budget of statistics) {
      let dataRecord = [
        await this._languageService.getText('common.month.' + budget.month),
        this.transform(budget.expected),
        this.transform(budget.actual),
        this.transform(budget.difference),
        {
          text: this._percentPipe.transform(budget.percentage, '1.2'),
          style: budget.percentage > 0 ? 'balancePositive' : 'balanceNegative'
        }
      ]

      body.push(dataRecord);
    }

    return {
      layout: 'lightHorizontalLines',
      table: {
        headerRows: 1,
        widths: ['*', 75, 75, 75, 75],
        body: body
      }
    }
  }

  private async yearly(): Promise<(PdfMake.PdfTable | PdfMake.PdfParagraph)[]> {
    let tableMonths = [];
    tableMonths.push(this._budgets.slice(0, 6));
    tableMonths.push(this._budgets.slice(6, 12));

    let response: (PdfMake.PdfTable | PdfMake.PdfParagraph)[] = [{
      pageBreak: 'before',
      text: ''
    } as PdfMake.PdfParagraph];

    for (let tableSpec of tableMonths) {
      let body: PdfMake.PdfTableBody = []
      let header: PdfMake.PdfTableRow = [
        await this._languageService.getText('Budget.Expense.name')
      ]

      for (let budget of tableSpec) {
        header.push(await this._languageService.getText('common.month.' + (budget.range.computeStartMonth() + 1)))
      }
      body.push(header)

      for (let expense of this._expenses) {
        let record: PdfMake.PdfTableRow = [expense.name]

        for (let budget of tableSpec) {
          record.push(await this.computeBalance(budget.range, [expense]));
        }

        body.push(record)
      }

      response.push({
        layout: 'lightHorizontalLines',
        margin: [0, 25],
        table: {
          widths: ['*', 'auto', 'auto', 'auto', 'auto', 'auto', 'auto'],
          body: body
        }
      })
    }

    return response;
  }

  private transform(actual: number): PdfMake.PdfParagraph {
    return {
      text: this._currencyPipe.transform(actual),
      style: actual > 0 ? 'balancePositive' : actual < 0 ? 'balanceNegative' : ''
    }
  }

  private async computeBalance(range: DateRange, expenses: EntityRef[]): Promise<PdfMake.PdfParagraph> {
    let amount = await this._http.post(environment.backend + 'statistics/balance', {
      expenses: expenses,
      dateRange: {
        start: range.from,
        end: range.until,
      },
      onlyIncome: false,
      allMoney: false
    }).pipe<number>(map(raw => raw['balance'])).toPromise()

    return {
      text: this._currencyPipe.transform(amount),
      style: amount > 0 ? 'balancePositive' : amount < 0 ? 'balanceNegative' : ''
    }
  }

}

