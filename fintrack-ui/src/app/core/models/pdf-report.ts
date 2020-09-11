import pdfMake from 'pdfmake/build/pdfmake';
import pdfFonts from 'pdfmake/build/vfs_fonts';
import {PdfMake} from "./pdf-make";

export abstract class PdfReport {

  private _pageOrientation: "portrait" | "landscape";

  protected constructor() {
    pdfMake.vfs = pdfFonts.pdfMake.vfs;
  }

  protected abstract get title(): string;
  protected abstract async content(): Promise<PdfMake.PdfContent>;

  set pageOrientation(value: "portrait" | "landscape") {
    this._pageOrientation = value;
  }

  public async save() {
    let pdfDocument = {
      content: await this.content(),
      pageSize: 'A4',
      pageOrientation: this._pageOrientation,
      pageMargins: [25, 60, 25, 60],
      defaultStyle: {
        fontSize: 10
      },
      styles: this.styles(),
      info: this.info(),
      footer: (currentPage, pageCount): PdfMake.PdfColumn => {
        return {
            columns: [
              {
                text: this.title,
                alignment: 'left',
                fontSize: 8,
                color: 'gray',
                margin: [25, 25, 0, 0]
              },
              {
                text: currentPage + "/" + pageCount,
                alignment: 'right',
                margin: [0, 25, 25, 0],
                fontSize: 8,
                color: 'gray'
              }
            ]
          };
      }
    }

    pdfMake.createPdf(pdfDocument)
      .open();
  }

  private info() {
    return {
      title: this.title,
      creator: 'FinTrack',
      producer: 'FinTrack',
      modDate: new Date()
    }
  }

  private styles(): PdfMake.PdfStyleDefinition {
    return {
      title: {
        fontSize: 18,
        bold: true,
        alignment: 'center',
      },
      header: {
        fontSize: 16,
        bold: true,
        margin: [0, 15, 0, 0]
      },
      balanceNegative: {
        color: '#dc3545'
      },
      balancePositive: {
        color: '#047135'
      },
      link: {
        color: '#007bff'
      },
      smallTable: {
        fontSize: 8
      }
    }
  }
}
