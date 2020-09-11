import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {of, Subject, Subscription} from "rxjs";
import {catchError, debounceTime, distinctUntilChanged, switchMap} from "rxjs/operators";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../../../environments/environment";

@Component({
  selector: 'app-tag-input',
  templateUrl: './tag-input.component.html',
  styleUrls: ['./tag-input.component.scss']
})
export class TagInputComponent implements OnInit {

  private _editing: boolean;
  private _tags: string[];
  private _selectable: string[];
  private _tagsChange: EventEmitter<string[]>;

  private $: Subject<string>;

  constructor(private _http: HttpClient) {
    this.$ = new Subject<string>();
    this._editing = false
    this._tagsChange = new EventEmitter<string[]>();
    this._selectable = [];
  }

  @Input()
  set tags(model: any) {
    this._tags = model || []
  }

  get tags(): any {
    return this._tags
  }

  @Output()
  get tagsChange() {
    return this._tagsChange;
  }

  get editing(): boolean {
    return this._editing;
  }

  get selectable(): string[] {
    return this._selectable;
  }

  ngOnInit(): void {
    this.$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(lookup =>
        this._http.get<string[]>(environment.backend + 'transactions/tags/auto-complete?token=' + lookup)
          .pipe(
            catchError(() => of([]))
          ))
    ).subscribe(tags => this._selectable = tags);
  }

  add() {
    this._editing = true;
  }

  cancel() {
    this._editing = false;
  }

  create(value: string) {
    this._http.post(environment.backend + 'transactions/tags', value).toPromise()
      .then(() => this.select(value))
  }

  select(value: string) {
    this._tags.push(value);
    this._editing = false;
    this._selectable = []
    this._tagsChange.emit(this._tags);
  }

  remove(index : number) {
    this._tags[index] = null;
    this._tags = this._tags.filter(c => c != null);
    this._tagsChange.emit(this._tags);
  }

  typeahead(value: KeyboardEvent) {
    let dom = value.target as HTMLElement;
    this.$.next(dom.innerText);
  }

}
