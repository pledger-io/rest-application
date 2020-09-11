import { TestBed } from '@angular/core/testing';

import { CurrencyResolverService } from './currency-resolver.service';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {RouterTestingModule} from "@angular/router/testing";
import {RouterHistory} from "../core/router-history";

describe('CurrencyResolverService', () => {
  let service: CurrencyResolverService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule, RouterTestingModule.withRoutes([]) ]
    });
    service = TestBed.inject(CurrencyResolverService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
