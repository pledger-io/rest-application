import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CurrencyEditComponent} from './currency-edit.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {RouterTestingModule} from "@angular/router/testing";
import {RouterHistory} from "../../core/router-history";
import {FormsModule} from "@angular/forms";
import {ActivatedRoute} from "@angular/router";
import {Currency} from "../../core/models/currency";
import {ReplaySubject} from "rxjs";

describe('CurrencyEditComponent', () => {
  let component: CurrencyEditComponent;
  let fixture: ComponentFixture<CurrencyEditComponent>;
  class MockRouterHistory extends RouterHistory {};

  beforeEach(async(() => {
    let dataSubject = new ReplaySubject();
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule, RouterTestingModule.withRoutes([]), FormsModule ],
      declarations: [ CurrencyEditComponent ],
      providers: [
        { provide: RouterHistory, useClass: MockRouterHistory},
        {
          provide: ActivatedRoute,
          useValue: {
            data: dataSubject,
            paramMap: new ReplaySubject()
          }
        }
      ],
    })
    .compileComponents();

    dataSubject.next({
      currency: {} as Currency
    })
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CurrencyEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
