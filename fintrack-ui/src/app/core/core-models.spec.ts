import * as moment from "moment";
import {AccountRef, Breadcrumb, DateRange, Exception} from "./core-models";
import {environment} from "../../environments/environment";
import Error = Exception.Error;

describe('DateRange', () => {
  it ('forYear 2019', () => {
    const range = DateRange.forYear(2019);

    expect(range.computeStartMonth()).toBe(0);
    expect(range.computeStartYear()).toBe(2019);
    expect(range.until).toBe('2019-12-31');
    expect(range.from).toBe('2019-01-01');
  });

  it('forMonth 2019-01', () => {
    const range = DateRange.forMonth(2019, 1);

    expect(range.from).toBe('2019-01-01');
    expect(range.until).toBe('2019-01-31');
  });

  it('forRange 2019-2020', () => {
    const range = DateRange.forRange('2019-01-01', '2020-01-01');

    expect(range.from).toBe('2019-01-01');
    expect(range.until).toBe('2020-01-01');
  });

  it('previousDays 30', () => {
    const range = DateRange.previousDays(30);

    expect(range.from).toBe(moment().subtract(30, 'day').format(environment.isoDateFormat));
    expect(range.until).toBe(moment().format(environment.isoDateFormat));
  });

  it('contains 2019 contains january 2019', () => {
    const year = DateRange.forYear(2019);
    const month = DateRange.forMonth(2019, 1);

    expect(year.contains(month)).toBeTruthy();
  });

  it('contains 2019 does not contain january 2018', () => {
    const year = DateRange.forYear(2019);
    const month = DateRange.forMonth(2018, 1);

    expect(year.contains(month)).toBeFalsy();
  });
});

describe('BreadCrumb', () => {

  it('with text resolver', async () => {
    const breadcrumb = new Breadcrumb(null, 'text.key.$test')
    breadcrumb.textResolver = () => 'resolved';

    expect(breadcrumb.getTextKey()).toBe('');
    expect(breadcrumb.hasTextResolver()).toBeTruthy();
    expect(breadcrumb.resolve).toBe('resolved');
  });

  it('with url resolver', () => {
    const breadcrumb = new Breadcrumb(null, 'text.key.$test')
    breadcrumb.urlResolver = () => '/test';

    expect(breadcrumb.hasUrl()).toBeFalsy();
    expect(breadcrumb.getUrl()).toBe('/test')
  });

  it('with text and url', () => {
    const breadcrumb = new Breadcrumb('/test', 'text.key.test')

    expect(breadcrumb.getTextKey()).toBe('text.key.test')
    expect(breadcrumb.getUrl()).toBe('/test')
  });

});

describe('AccountRef', () => {

  it ('ownAccount', () => {
    expect(new AccountRef(1, 'managed', 'test').isOwn()).toBeTruthy();
    expect(new AccountRef(1, 'creditor', 'test').isOwn()).toBeFalsy();
    expect(new AccountRef(1, 'debtor', 'test').isOwn()).toBeFalsy();
    expect(new AccountRef(1, 'reconcile', 'test').isOwn()).toBeFalsy();
  });

  it('isSystem', () => {
    expect(new AccountRef(1, 'reconcile', 'test').isSystem()).toBeTruthy();
    expect(new AccountRef(1, 'creditor', 'test').isSystem()).toBeFalsy();
  });

  it('frontEndType', () => {
    expect(new AccountRef(1, 'managed', 'test').frontEndType).toBe('own');
    expect(new AccountRef(1, 'creditor', 'test').frontEndType).toBe('expense');
    expect(new AccountRef(1, 'debtor', 'test').frontEndType).toBe('revenue');
  });
});

describe('Error', () => {

  it('message', () => {
    expect(new Error(404, 'Page Not Found', 'Sample', '/api/test', []).message).toBe('Page Not Found');
  });

});
