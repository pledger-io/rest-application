import {fakeAsync, TestBed, tick} from '@angular/core/testing';
import {AuthorizationService} from './core-services';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {Router} from '@angular/router';
import {RouterTestingModule} from '@angular/router/testing';
import {Location} from '@angular/common';
import {environment} from '../../environments/environment';

class TestComponent {

}

describe('AuthorizationService', () => {
  let service: AuthorizationService;
  let router: Router;
  let httpClient: HttpTestingController;
  let location: Location;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule.withRoutes([{path: 'login', component: TestComponent}])
      ],
      providers: [AuthorizationService]
    }).compileComponents();

    service = TestBed.inject(AuthorizationService, null);
    router = TestBed.inject(Router, null);
    httpClient = TestBed.inject(HttpTestingController, null);
    location = TestBed.inject(Location, null);
  });

  it('notAuthorized should redirect', () => fakeAsync(() => {
    sessionStorage.removeItem('token');
    let routeMock: any = { _routerState: {url: '/path/to/return'}};

    expect(service.authorized).toBeFalsy();
    service.canActivate(routeMock);
    tick();
    expect(location.path()).toBe('/login');
  }));

  it('notAuthorized everything ok', () => fakeAsync(() => {
    sessionStorage.setItem('token', 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c');
    let routeMock: any = { _routerState: {url: '/path/to/return'}};

    expect(service.authorized).toBeTruthy();
    service.canActivate(routeMock);
    tick();
    expect(location.path()).toBe('');
  }));

  it('register', () => {
    service.register('test-user', 'test-password');

    httpClient.expectOne({
      url: environment.backend + 'profile/create',
      method: 'PUT'
    });
  });
});
