import {Breadcrumb} from './models/breadcrumb';
import {DateRange} from './models/date-range';
import {Pagable, Page} from './models/pageable';
import {PdfMake} from './models/pdf-make';
import {PdfReport} from './models/pdf-report';
import {Bpmn} from './models/bpmn-process';
import {EntityType, EntityRef} from './models/entity';
import {Transaction} from './models/transaction';
import {AccountRef} from './models/account-ref';
import {Exception} from './models/exception';
import {Currency} from './models/currency';

export {
  Breadcrumb,
  DateRange,
  Page,
  Pagable,
  PdfMake,
  PdfReport,
  Bpmn,
  EntityType,
  EntityRef,
  Transaction,
  AccountRef,
  Exception,
  Currency
};

export class UploadResponse {
  constructor(public fileCode: string) {
  }
}
