import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'abs'
})
export class AbsoluteNumberPipe implements PipeTransform {

  transform(value: any, ...args: any[]): any {
    let casted: number;
    if (typeof value == 'number') {
      casted = value;
    } else if (typeof value == 'string') {
      casted = parseInt(value);
    }

    return casted != null ? Math.abs(casted) : null;
  }

}
