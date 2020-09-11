import {AccountRef} from "../core/core-models";

export class Schedule {
  constructor(public periodicity: string,
              public interval: number) {
  }
}

class ScheduleRange {
  constructor(public start: string,
              public end: string) {

  }
}

export class ScheduledTransaction {
  constructor(public id: number,
              public name: string,
              public description: string,
              public amount: number,
              public range: ScheduleRange,
              public schedule: Schedule,
              public source: AccountRef,
              public destination: AccountRef) {

  }
}
