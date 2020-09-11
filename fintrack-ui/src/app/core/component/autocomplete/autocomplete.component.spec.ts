import {AutocompleteComponent} from './autocomplete.component';
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {EntityType} from '../../models/entity';
import {DebugElement} from '@angular/core';
import {By} from '@angular/platform-browser';
import {environment} from '../../../../environments/environment';
import {FormsModule} from '@angular/forms';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {LocalizationDirective} from '../../directives/localization.directive';


describe('Component: AutoComplete', () => {
  let component: AutocompleteComponent;
  let fixture: ComponentFixture<AutocompleteComponent>;
  let httpClient: HttpTestingController;
  let inputEl: DebugElement;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        NgbModule,
        FormsModule
      ],
      declarations: [AutocompleteComponent, LocalizationDirective]
    }).compileComponents();

    fixture = TestBed.createComponent(AutocompleteComponent);
    component = fixture.componentInstance;
    httpClient = TestBed.inject(HttpTestingController, null);

    inputEl = fixture.debugElement.query(By.css('input'));
    fixture.detectChanges();
  }));

  it('should autocomplete on budget', fakeAsync(() => {
    const value = {actual: ''};
    component.model = '';
    component.type = EntityType.BUDGET;
    component.modelChange.subscribe(x => value.actual = x);

    inputEl.nativeElement.value = 'sample';
    inputEl.nativeElement.dispatchEvent(new Event('input'));

    fixture.detectChanges();
    tick(1500);

    expect(value.actual).toBe('sample');
  }));
});
