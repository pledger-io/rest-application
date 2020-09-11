import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {map, switchMap} from "rxjs/operators";
import {UserAccount} from "../core/services/authorization.service";

@Injectable({
  providedIn: 'root'
})
export class ProfileService {

  private static HTTPOptions = {
    headers: new HttpHeaders({
      'Accept':'image/png'
    }),
    'responseType': 'blob' as 'json'
  }

  constructor(private http: HttpClient) { }

  async download(): Promise<Blob> {
    return await this.http.get<Blob>(environment.backend + 'profile/export', {responseType: 'blob' as 'json'})
      .toPromise();
  }

  async export(): Promise<string> {
    return await this.http.get<string>(environment.backend + 'transactions/export', {
      headers: new HttpHeaders({
        'Accept': 'text/plain'
      }),
      responseType: 'blob' as 'json'
    }).toPromise();
  }

  applyRules(): Promise<void> {
    return this.http.get<void>(environment.backend + 'transactions/apply-all-rules').toPromise();
  }

  update(patchRequest: any) {
    return this.http.patch<UserAccount>(environment.backend + 'profile', patchRequest).toPromise();
  }

  disableMFA(): Promise<void> {
    return this.http.post<void>(environment.backend + 'profile/multi-factor/disable', {}).toPromise();
  }

  enableMFA(verificationCode: string): Promise<void> {
    return this.http.post<void>(environment.backend + 'profile/multi-factor/enable',
      {verificationCode: verificationCode}).toPromise();
  }

  qrCode(): Promise<string> {
    return this.http.get<Blob>(environment.backend + 'profile/multi-factor/qr-code', ProfileService.HTTPOptions)
      .pipe(
        switchMap(raw => {
          let reader = new FileReader();
          reader.readAsBinaryString(raw);
          return new Promise<string>(resolve => {
            reader.onloadend = (data) => resolve(reader.result as string);
          });
        }),
        map(raw => 'data:image/png;base64,' + btoa(raw))
      ).toPromise();
  }

}
