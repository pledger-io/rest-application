import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from "@angular/router";
import {ProcessService} from "../../core/core-services";
import {Bpmn} from "../../core/core-models";
import {ImportConfiguration} from "../batch-import.models";

class Pair {
  constructor(public first: any, public second: any) {
  }
}

@Injectable({
  providedIn: 'root'
})
export class AccountExtractorResolverService implements Resolve<any> {

  constructor(private _service: ProcessService) { }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<any> | any {
    let batchImportSlug = route.paramMap.get('batchSlug');

    if (localStorage.getItem('import.extract.' + batchImportSlug)) {
      return JSON.parse(localStorage.getItem('import.extract.' + batchImportSlug));
    }

    return new Promise<any>((resolve, error) => {
      this._service.process('ImportExtractAccounts', batchImportSlug)
        .then(processInstance => {
          if (processInstance && processInstance.length > 0) {
            this.resolveVariable(processInstance[0], resolve, error, 0);
          } else {
            this._service.start('ImportExtractAccounts', {
              businessKey: batchImportSlug,
              slug: batchImportSlug,
              importConfig: JSON.stringify(ImportConfiguration.toServer(localStorage.getItem('import.config.' + batchImportSlug)))
            }).then(processInstance =>this.resolveVariable(processInstance, resolve, error, 0))
              .catch(error);
          }
        })
        .catch(error);
    });
  }

  private resolveVariable(processInstance: Bpmn.Instance, resolve: (value?: (PromiseLike<any> | any)) => void, error: (reason?: any) => void, attempt?: number) {
    if (attempt > 5) {
      return error('Failed to resolve.');
    }

    this._service.variable<Pair[]>('ImportExtractAccounts', processInstance.id, 'extractionResult')
      .then(data => {
        if (data.length == 0) {
          setTimeout(() => this.resolveVariable(processInstance, resolve, error, ++attempt), 100);
        } else {
          resolve(data[0].value.map(e => {
            return {name: e.first, account: e.second}
          }));
        }
      })
      .catch(error);
  }
}
