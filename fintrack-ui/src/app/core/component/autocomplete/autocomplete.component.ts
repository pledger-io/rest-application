import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {EntityType} from '../../core-models';
import {Observable, of} from 'rxjs';
import {catchError, debounceTime, distinctUntilChanged, map, switchMap} from 'rxjs/operators';
import {environment} from '../../../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Category} from '../../../category/category.models';

@Component({
  selector: 'app-autocomplete',
  templateUrl: './autocomplete.component.html',
  styleUrls: ['./autocomplete.component.scss']
})
export class AutocompleteComponent implements OnInit {

  @Input('name')
  private _name: string;

  @Input('placeholder')
  private _placeholderKey: string;

  private _model: any;
  private _modelChange: EventEmitter<any>;
  private _lookupType: EntityType;

  constructor(private _http: HttpClient) {
    this._modelChange = new EventEmitter<any>();
  }

  get name(): string {
    return this._name;
  }

  @Input()
  set model(model: any) {
    this._model = model;
  }

  get model(): any {
    return this._model;
  }

  @Output()
  get modelChange() {
    return this._modelChange;
  }

  get formatter(): (any) => string {
    return m => m.name;
  }

  @Input()
  set type(type: EntityType) {
    this._lookupType = type;
  }

  get type(): EntityType {
    return this._lookupType;
  }

  get placeholder(): string {
    return this._placeholderKey;
  }

  get typeahead(): (text: Observable<string>) => Observable<any[]> {
    return (text$) => text$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(token => {
        let serviceObservable: Observable<any[]>;
        switch (this._lookupType) {
          case EntityType.BUDGET:
            serviceObservable = this._http.get<any>(environment.backend + 'budgets/auto-complete?token=' + token);
            break;
          case EntityType.CATEGORY:
            serviceObservable = this._http.get<Category[]>(environment.backend + 'categories/auto-complete?token=' + token)
              .pipe(map(c => c.map(category => {
                return {
                  id: category.id,
                  name: category.label,
                  description: category.description
                };
              })));
            break;
          case EntityType.CONTRACT:
            serviceObservable = this._http.get<any>(environment.backend + 'contracts/auto-complete?token=' + token);
            break;
          case EntityType.CREDIT_ACCOUNT:
            serviceObservable = this._http.get<any>(environment.backend + 'accounts/auto-complete?type=creditor&token=' + token);
            break;
          case EntityType.DEBIT_ACCOUNT:
            serviceObservable = this._http.get<any>(environment.backend + 'accounts/auto-complete?type=debtor&token=' + token);
            break;
          case EntityType.OWN_ACCOUNT:
            serviceObservable = this._http.get<any>(environment.backend + 'accounts/auto-complete?type=own&token=' + token);
            break;
          case EntityType.TAG:
            serviceObservable = this._http.get<string[]>(environment.backend + 'transactions/tags/auto-complete?token=' + token)
              .pipe(map(tags => tags.map(tag => {
                return {
                  id: tag,
                  name: tag
                };
              })));
            break;
        }

        return serviceObservable.pipe(catchError(() => of([])));
      })
    );
  }

  ngOnInit() {
  }

}
