import {Injectable} from "@angular/core";
import {NgbDate, NgbDateParserFormatter, NgbDateStruct} from "@ng-bootstrap/ng-bootstrap";
import * as moment from "moment";
import {DecimalPipe} from "@angular/common";

@Injectable()
export class CustomDateParserFormatter extends NgbDateParserFormatter {

  constructor(private _numberFormatter: DecimalPipe) {
    super();
  }

  format(date: NgbDateStruct): string {
    if (date) {
      let month = this._numberFormatter.transform(date.month, '2.0');
      let day = this._numberFormatter.transform(date.day, '2.0');

      let isoString = date.year + '-' + month + '-' + day;
      return moment(isoString).format(localStorage.getItem('dateFormat'));
    }

    return ''
  }

  parse(value: string): NgbDateStruct {
    let parsed = moment(value, localStorage.getItem('dateFormat'));
    if (parsed.isValid()) {
      return new NgbDate(parsed.get('y'), parsed.get("month") + 1, parsed.get('D'));
    }
  }

}
