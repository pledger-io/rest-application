export module Exception {
  export interface ValidationResult {
    valid: boolean
    style: string
    validator?: string
  }

  export interface FieldError {
    defaultMessage: string
    field: string
    rejectedValue: any
    code: string
  }

  export class Error {
    constructor(private _status: number,
                private _error: string,
                private _message: string,
                private _path: string,
                private _errors: FieldError[]) {
    }

    get message(): string {
      if (this._message && this._status != 404) {
        return this._message;
      }

      return this._error;
    }

    get fieldErrors(): FieldError[] {
      return this._errors
    }

    get validationFailed(): boolean {
      return this._errors != null && this._errors.length > 0
    }
  }
}
