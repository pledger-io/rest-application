import {Pipe, PipeTransform} from '@angular/core';
import * as moment from "moment";
import {Moment} from "moment";

@Pipe({
  name: 'customDate'
})
export class CustomDatePipe implements PipeTransform {

  transform(value: any, locale?: string): string | null {
    let dateMoment: Moment;

    if (typeof value == 'string' || value instanceof moment) {
      dateMoment = moment(value);
    }

    if (dateMoment) {
      return dateMoment.format(localStorage.getItem('dateFormat'));
    }

    return value;
  }

}
