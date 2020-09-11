import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../../environments/environment";
import {Bpmn} from "../core-models";

@Injectable({
  providedIn: 'root'
})
export class ProcessService {

  constructor(private http: HttpClient) { }

  start(definitionKey: string, parameters: any): Promise<Bpmn.Instance> {
    return this.http.put<Bpmn.Instance>(environment.backend + 'runtime-process/' + definitionKey + '/start', parameters)
      .toPromise();
  }

  processList(definitionKey: string): Promise<Bpmn.Instance[]> {
    return this.http.get<Bpmn.Instance[]>(environment.backend + 'runtime-process/' + definitionKey).toPromise();
  }

  process(definitionKey: string, businessKey: string): Promise<Bpmn.Instance[]> {
    return this.http.get<Bpmn.Instance[]>(environment.backend + 'runtime-process/' + definitionKey + '/'
      + businessKey).toPromise();
  }

  delete(definitionKey: string, instanceId: string): Promise<void> {
    return this.http.delete<void>(environment.backend + 'runtime-process/'+ definitionKey +'/dummy/'
      + instanceId).toPromise();
  }

  variable<T>(definitionKey: string, instanceId: string, variable: string): Promise<Bpmn.Variable<T>[]> {
    return this.http.get<Bpmn.Variable<T>[]>(environment.backend + 'runtime-process/' + definitionKey + '/dummy/'
      + instanceId + '/variables/' + variable).toPromise();
  }

  tasks(definitionKey: string, instanceId: string): Promise<Bpmn.Task[]> {
    return this.http.get<Bpmn.Task[]>(environment.backend + 'runtime-process/'+ definitionKey +'/dummy/'
      + instanceId + '/tasks').toPromise();
  }

  closeTask(definitionKey: string, instanceId: string, taskId: string): Promise<Bpmn.Task[]> {
    return this.http.delete<Bpmn.Task[]>(environment.backend + 'runtime-process/'+ definitionKey +'/dummy/'
      + instanceId + '/tasks/' + taskId).toPromise();
  }

}
