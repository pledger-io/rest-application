export module PdfMake {
  interface PdfElement {
    margin?: number[] | number
    pageBreak?: 'after' | 'before'
  }

  export interface PdfStyle extends PdfElement {
    fontSize?: number
    font?: string
    fontFeatures?: string[]
    lineHeight?: number
    bold?: boolean
    italics?: boolean
    alignment?: 'left' | 'center' | 'right'
    color?: string
    background?: string
    markerColor?: string
    decoration?: 'underline' | 'lineThrough' | 'overline'
    decorationStyle?: 'dashed' | 'dotted' | 'wavy'
    decorationColor?: string
    margin?: number[]
  }

  export interface PdfStyleDefinition {
    [propName: string]: PdfStyle
  }

  export interface PdfParagraph extends PdfStyle {
    text?: string
    style?: string | string[]
  }

  export interface PdfSvg extends PdfElement {
    svg: string,
    width?: number,
    height?: number,
    fit?: number[]
  }

  /**
   * Column support
   */
  export interface PdfColumn {
    columns: (string | PdfColumnParagraph)[]
    columnGap?: number
    margin?: number | number[]
  }

  export interface PdfColumnParagraph extends PdfParagraph {
    width?: number | string
  }

  export interface PdfTable extends PdfElement {
    layout?: string
    table: PdfTableContent
  }

  export interface PdfTableContent {
    headerRows?: number;
    widths?: (string | number)[];
    body: PdfTableBody
  }

  export interface PdfTableBody extends Array<PdfTableRow> {

  }

  export interface PdfTableRow extends Array<string | PdfParagraph> {
  }

  export interface PdfContent extends Array<string | PdfColumn | PdfParagraph | PdfTable | PdfSvg> {
  }
}
