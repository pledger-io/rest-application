import {Component, EventEmitter, HostListener, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {environment} from "../../../../environments/environment";
import {Chart, stockChart} from "highcharts/highstock"
import {chart} from "highcharts/highcharts";
import {map} from "rxjs/operators";
import {MapEntry, MapLiteral} from "@angular/compiler/src/output/map_util";

@Component({
  selector: 'app-graph-display',
  templateUrl: './graph-display.component.html',
  styleUrls: ['./graph-display.component.scss']
})
export class GraphDisplayComponent implements OnInit, OnDestroy {

  @Input("highstock")
  private highstock: boolean;

  private currency: string;

  private uniqueId: string;
  private graphLoaded: boolean;
  private endPoint: string;

  private _chart: Chart;

  @Output('svg')
  private _svgEmitter: EventEmitter<string>;

  constructor(private http: HttpClient) {
    this._svgEmitter = new EventEmitter<string>();
  }

  ngOnInit() {
    this.uniqueId = [...new Array(5).keys()].fill(Math.random() * 100000).map(x => x << 2).join('-')
  }

  ngOnDestroy(): void {
    if (this._chart) {
      this._chart.destroy();
    }
  }

  @Input('endPoint')
  set setEndpoint(endPoint: string) {
    this.endPoint = endPoint;
    this.redraw();
  }

  @Input("currency")
  set setCurrency(currency: string) {
    this.currency = currency;
    this.redraw();
  }

  get id(): string {
    return this.uniqueId;
  }

  get loading(): boolean {
    return !this.graphLoaded;
  }

  private redraw() {
    this.graphLoaded = false;

    if (this._chart) {
      this._chart.destroy();
    }

    let headers: HttpHeaders = new HttpHeaders();
    if (this.currency) {
      headers = headers.append('X-Accept-Currency', this.currency);
    }

    this.http.get(environment.backend + this.endPoint, {
      headers: headers
    }).pipe(
      map(graph => this.expandHighStock(graph)),
      map(graph => this.highstock ? stockChart(graph) : chart(graph))
    )
      .toPromise()
      .then(chart => this._chart = chart)
      .then(() => setTimeout(() => this._svgEmitter.emit(this._chart.container.innerHTML), 500))
      .finally(() => this.graphLoaded = true);
  }

  private expandHighStock(graph: any) {
    let updated = {
      ...graph, ...{
        rangeSelector: false,
        navigator: true,
        scrollbar: false,
      }
    };

    updated.chart.renderTo = this.uniqueId;
    updated.tooltip.split = false;

    return updated;
  }
}
