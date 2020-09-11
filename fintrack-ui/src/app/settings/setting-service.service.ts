import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Setting} from "./settings-models";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class SettingServiceService {

  constructor(private _http: HttpClient) { }

  list(): Promise<Setting[]> {
    return this._http.get<Setting[]>(environment.backend + 'settings').toPromise();
  }

  update(setting: string, value: string): Promise<void> {
    return this._http.post<void>(environment.backend + 'settings/' + setting, {
      value: value
    }).toPromise();
  }
}
