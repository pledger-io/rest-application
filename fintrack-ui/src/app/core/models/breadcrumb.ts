export class Breadcrumb {
  private _textResolver: (text) => string;
  private _urlResolver: (text) => string;

  constructor(private routerLink: string, private textKey: string) {

  }

  set textResolver(resolver: (text) => string) {
    this._textResolver = resolver;
  }

  set urlResolver(resolver: (text) => string) {
    this._urlResolver = resolver
  }

  hasTextResolver(): boolean {
    return this._textResolver != null;
  }

  hasUrl() : boolean {
    return this.routerLink != null;
  }

  getUrl() : string {
    if (this._urlResolver) {
      return this._urlResolver(this.routerLink);
    }

    return this.routerLink;
  }

  get resolve(): string {
    return this._textResolver(this.textKey);
  }

  getTextKey() : string {
    return this._textResolver == null ? this.textKey : '';
  }

}
