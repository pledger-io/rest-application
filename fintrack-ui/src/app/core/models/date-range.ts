import {Moment} from "moment";
import * as moment from "moment";
import {environment} from "../../../environments/environment";

export class DateRange {

  private readonly _from: Moment;
  private readonly _until: Moment;

  constructor(from: string, until: string) {
    this._from = moment(from, environment.isoDateFormat);
    this._until = until ? moment(until, environment.isoDateFormat) : null;
  }

  get from(): string {
    return this._from.format(environment.isoDateFormat);
  }

  get until(): string {
    return this._until ? this._until.format(environment.isoDateFormat) : null;
  }

  computeStartMonth(): number {
    return this._from.get('month');
  }

  computeStartYear(): number {
    return this._from.get('year');
  }

  contains(range: DateRange): boolean {
    return this.containsStartOrEnd(range) || this.within(range);
  }

  compare(range: DateRange): number {
    return this._from.isBefore(range._from) ? -1 : 1;
  }

  private containsStartOrEnd(range: DateRange) {
    return (range._from.isBetween(this._from, this._until) || range._from.isSame(this._from) || range._from.isSame(this._until))
      || (range._until.isBetween(this._from, this._until) || range._until.isSame(this._from) || range._until.isSame(this._until));
  }

  private within(range: DateRange) {
    return this._from.isBefore(range._from) && this._until.isAfter(range._until);
  }

  static previousDays(days: number): DateRange {
    let end = moment().startOf('day');
    let begin = moment().startOf('day').add(-days, "days");

    return new DateRange(begin.format(environment.isoDateFormat), end.format(environment.isoDateFormat));
  }

  static currentMonth(): DateRange {
    return new DateRange(moment().startOf('M').format(environment.isoDateFormat), moment().endOf('M').format(environment.isoDateFormat));
  }

  static forYear(year: number) {
    return new DateRange(year +'-01-01', year + '-12-31');
  }

  static forMonth(year: number, month: number) {
    let strMonth :string = month < 10 ? '0' + month : '' + month;
    let monthFirstDay = moment(year + '-' + strMonth + '-01');
    return new DateRange(monthFirstDay.startOf('M').format(environment.isoDateFormat), monthFirstDay.endOf('M').format(environment.isoDateFormat));
  }

  static forRange(from: string, until: string) {
    let actualStart = from;
    let actualEnd = until;
    if (from) {
      actualStart = moment(from, environment.isoDateFormat).startOf('D')
        .format(environment.isoDateFormat);
    }
    if (until) {
      actualEnd =  moment(until, environment.isoDateFormat).startOf('D')
        .format(environment.isoDateFormat);
    }

    return new DateRange(actualStart, actualEnd);
  }

}
