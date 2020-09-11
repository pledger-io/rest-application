import {UploadResponse} from "../core-models";
import {FileService} from "../core-services";

export class GenericUpload {

  private _processing: boolean;
  private _fileToUpload: File = null;

  constructor(private _service: FileService) {
  }

  handleFileSelect(files: FileList) {
    this._fileToUpload = files.item(0);
  }

  uploadFile(): Promise<UploadResponse> {
    this._processing = true;
    return this._service.upload(this._fileToUpload)
      .finally(() => this._processing = false)
  }

  get processing(): boolean {
    return this._processing;
  }

  get fileToUpload(): File {
    return this._fileToUpload;
  }
}
