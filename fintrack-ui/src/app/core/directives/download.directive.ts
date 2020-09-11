import {Directive, HostListener, Input} from '@angular/core';

@Directive({
  selector: '[appDownload]'
})
export class DownloadDirective {

  @Input('appDownload')
  private blobFunction: () => Blob;

  @Input()
  private fileName: string;

  constructor() { }

  @HostListener('click')
  async startDownload() {
    let blobFile = await this.blobFunction();;
    let blobUrl = window.URL.createObjectURL(blobFile);

    let secretDownload = document.createElement('a');
    secretDownload.href = blobUrl;
    secretDownload.download = this.fileName || 'unknown.dat';
    secretDownload.dispatchEvent(new MouseEvent('click'));

    setTimeout(() => window.URL.revokeObjectURL(blobUrl), 60);
  }
}
