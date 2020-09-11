import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {UploadResponse} from "../core-models";
import {environment} from "../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class FileService {

  private static HTTPOptions = {
    headers: new HttpHeaders({
      'Accept':'application/pdf'
    }),
    'responseType': 'blob' as 'json'
  }

  constructor(private http: HttpClient) { }

  upload(file: File) : Promise<UploadResponse> {
    let formData: FormData = new FormData();
    formData.append('upload', file, file.name);
    return this.http.post<UploadResponse>(environment.backend + 'attachment', formData).toPromise();
  }

  download(fileToken: string): Promise<Blob> {
    return this.http.get<Blob>(environment.backend + 'attachment/' + fileToken, FileService.HTTPOptions).toPromise();
  }

}
