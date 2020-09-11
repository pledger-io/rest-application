import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from "@angular/router";
import {BatchImport, ImportConfiguration} from "../batch-import.models";
import {FileService} from "../../core/core-services";

@Injectable({
  providedIn: 'root'
})
export class ImportConfigurationResolverService implements Resolve<ImportConfiguration> {

  constructor(private _fileService: FileService) { }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<ImportConfiguration> | ImportConfiguration {
    let batchImportSlug = route.paramMap.get('batchSlug');
    if (localStorage.getItem('import.config.' + batchImportSlug)) {
      return JSON.parse(localStorage.getItem('import.config.' + batchImportSlug));
    }

    let batchImport = route.parent.data.batch as BatchImport;
    return new Promise<ImportConfiguration>((resolve, error) =>
      this._fileService.download(batchImport.config.file)
        .then(blob => this.generateConfiguration(blob, resolve))
        .catch(error));
  }

  private generateConfiguration(raw: Blob, resolve) {
    let reader = new FileReader();
    reader.readAsBinaryString(raw);
    reader.onloadend = (data) => {
      let parsed = JSON.parse(reader.result as string);
      resolve(new ImportConfiguration(
        parsed['has-headers'],
        parsed['apply-rules'],
        parsed['generate-accounts'],
        parsed['date-format'],
        parsed['delimiter'],
        undefined,
        parsed['column-roles'],
        parsed['custom-indicator']));
    };
  }

}
