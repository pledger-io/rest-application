import {Injectable} from "@angular/core";
import {LocalizationService} from "../../core/core-services";
import {AccountRef, DateRange, EntityRef, PdfMake, PdfReport} from "../../core/core-models";
import {TopAccount} from "../../accounts/account.service";
import {CustomCurrencyPipe} from "../../core/pipes/custom-currency.pipe";
import {environment} from "../../../environments/environment";
import {map} from "rxjs/operators";
import {HttpClient} from "@angular/common/http";

@Injectable()
export class IncomeExpensePdf extends PdfReport {

  private _year: number;
  private _accounts: AccountRef[];
  private _topDebtors: TopAccount[];
  private _topCreditors: TopAccount[];
  private _balanceSVG: string;

  private _title: string;

  constructor(private _languageService: LocalizationService,
              private _currencyPipe: CustomCurrencyPipe,
              private _http: HttpClient) {
    super();

    this.pageOrientation = 'portrait';
    this._languageService.getText('page.reports.default.title')
      .then(text => this._title = text);
  }

  set year(year: number) {
    this._year = year;
  }

  set accounts(accounts: AccountRef[]) {
    this._accounts = accounts;
  }

  set creditors(creditors: TopAccount[]) {
    this._topCreditors = creditors
  }

  set debtors(debtors: TopAccount[]) {
    this._topDebtors = debtors
  }

  protected get title(): string {
    return this._title + ' ' + this._year
  }

  protected async content(): Promise<PdfMake.PdfContent> {
    return [
      {
        text: this.title,
        style: 'title'
      } as PdfMake.PdfParagraph,

      {
        text: await this._languageService.getText('page.reports.default.title'),
        style: 'header'
      } as PdfMake.PdfParagraph,
      {
        svg: this._balanceSVG,
        width: 550
      } as PdfMake.PdfSvg,

      {
        text: await this._languageService.getText('page.reports.default.title'),
        style: 'header'
      } as PdfMake.PdfParagraph,
      await this.incomeExpense(),

      {
        text: await this._languageService.getText('page.reports.default.balances'),
        style: 'header'
      } as PdfMake.PdfParagraph,
      await this.accountBalances(),

      {
        text: await this._languageService.getText('page.reports.default.top.debit'),
        style: 'header'
      } as PdfMake.PdfParagraph,
      await this.topAccounts(this._topDebtors, 'balancePositive'),

      {
        text: await this._languageService.getText('page.reports.default.top.credit'),
        style: 'header'
      } as PdfMake.PdfParagraph,
      await this.topAccounts(this._topCreditors, 'balanceNegative')
    ]
  }

  balanceSVG(svg: string) {
    this._balanceSVG = svg;
  }

  private async accountBalances(): Promise<PdfMake.PdfTable> {
    let tbody: PdfMake.PdfTableBody = [[
      await this._languageService.getText('Account.name'),
      {text: await this._languageService.getText('page.reports.default.startBalance')},
      {text: await this._languageService.getText('page.reports.default.endBalance')},
      {text: await this._languageService.getText('common.difference')},
    ]];

    for (let account of this._accounts) {
      tbody.push([
        account.name,
        await this.computeBalance(DateRange.forRange('1970-01-01', (this._year - 1) + '-12-31'), [account], null),
        await this.computeBalance(DateRange.forRange('1970-01-01', this._year + '-12-31'), [account], null),
        await this.computeBalance(DateRange.forYear(this._year), [account], null)
      ]);
    }

    return {
      layout: 'lightHorizontalLines',
      table: {
        headerRows: 1,
        body: tbody
      }
    }
  }

  private async incomeExpense(): Promise<PdfMake.PdfTable> {
    return {
      layout: 'noBorders',
      table: {
        widths: [100, 75],
        body: [
          [
            await this._languageService.getText('common.in'),
            await this.computeBalance(DateRange.forYear(this._year), null, true)
          ],
          [
            await this._languageService.getText('common.out'),
            await this.computeBalance(DateRange.forYear(this._year), null, false)
          ],
          [
            {
              text: await this._languageService.getText('common.difference'),
              italics: true
            },
            await this.computeBalance(DateRange.forYear(this._year), null)
          ]
        ]
      }
    }
  }

  private async topAccounts(accounts: TopAccount[], style: string): Promise<PdfMake.PdfTable> {
    let tbody: PdfMake.PdfTableBody = [[
      {text: await this._languageService.getText('Account.name')},
      {text: await this._languageService.getText('common.total')},
      {text: await this._languageService.getText('common.average')}
    ]];

    tbody.push(...accounts.map(account => [
      account.account.name,
      {
        text: this._currencyPipe.transform(account.total, account.account.account.currency),
        style: account.total == 0 ? '' : style
      },
      {
        text: this._currencyPipe.transform(account.average, account.account.account.currency),
        style: account.average == 0 ? '' : style
      }
    ]));

    return {
      layout: 'lightHorizontalLines',
      table: {
        headerRows: 1,
        widths: ['*', 75, 75],
        body: tbody
      }
    }
  }

  private async computeBalance(range: DateRange, accounts: EntityRef[], onlyIncome: boolean = null): Promise<PdfMake.PdfParagraph> {
    let amount = await this._http.post(environment.backend + 'statistics/balance', {
      accounts: accounts,
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
