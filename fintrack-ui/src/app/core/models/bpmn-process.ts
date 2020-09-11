
export module Bpmn {
  export interface Instance {
    id: string,
    process: string,
    businessKey: string
    state: string
    suspended: boolean
  }

  export interface Variable<T> {
    id: string
    name: string
    value: T
  }

  export interface Task {
    id: string
    definition: string
    created: string
    formKey: string
    name: string
  }
}
