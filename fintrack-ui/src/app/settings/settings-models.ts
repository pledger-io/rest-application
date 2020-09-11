import {Currency} from '../core/core-models';

export interface Setting {
  name: string;
  type: string;
  value: string;
}

export interface CurrencyModel extends Currency {
  name: string;
}
