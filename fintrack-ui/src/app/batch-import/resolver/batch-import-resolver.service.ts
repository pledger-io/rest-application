import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from "@angular/router";
import {BatchImport} from "../batch-import.models";
import {BatchImportService} from "../batch-import.service";

@Injectable({
  providedIn: 'root'
})
export class BatchImportResolverService implements Resolve<BatchImport> {

  constructor(private batchService: BatchImportService) { }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<BatchImport> {
    return this.batchService.get(route.paramMap.get('batchSlug'));
  }

}
