import {AccountRef} from "../core/core-models";

export class Contract {
  constructor(public id: number,
              public name: string,
              public description: string,
              public contractAvailable: boolean,
              public fileToken: string,
              public start: string,
              public end: string,
              public terminated: boolean,
              public notification: boolean,
              public company: AccountRef) {
  }

}

export class ContractOverview {
  constructor(public active: Contract[],
              public terminated: Contract[]) {
  }
}
