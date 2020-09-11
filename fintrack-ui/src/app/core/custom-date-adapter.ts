import {Injectable} from "@angular/core";
import {NgbDate, NgbDateAdapter, NgbDateStruct} from "@ng-bootstrap/ng-bootstrap";
import * as moment from "moment";
import {environment} from "../../environments/environment";
import {DecimalPipe} from "@angular/common";

@Injectable()
export class CustomDateAdapter extends NgbDateAdapter<string> {

  constructor(private _numberFormatter: DecimalPipe) {
    super();
  }

  fromModel(value: string): NgbDateStruct {
    let parsed = moment(value, environment.isoDateFormat);
    if (parsed.isValid()) {
      return new NgbDate(parsed.get('y'), parsed.get("month") + 1, parsed.get('D'));
    }
  }

  toModel(date: NgbDateStruct): string {
    if (date) {
      let month = this._numberFormatter.transform(date.month, '2.0');
      let day = this._numberFormatter.transform(date.day, '2.0');

      return date.year + '-' + month + '-' + day;
    }
  }

}
