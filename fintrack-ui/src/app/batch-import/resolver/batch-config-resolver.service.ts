import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from "@angular/router";
import {BatchConfig} from "../batch-import.models";
import {BatchImportService} from "../batch-import.service";

@Injectable({
  providedIn: 'root'
})
export class BatchConfigResolverService implements Resolve<BatchConfig[]> {

  constructor(private _service: BatchImportService) { }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<BatchConfig[]> {
    return this._service.config();
  }


}
