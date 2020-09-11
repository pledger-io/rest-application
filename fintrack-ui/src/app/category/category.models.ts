export class Category {
  constructor(public id: number,
              public label: string,
              public description: string,
              public lastUsed: string) {
  }
}

export class CategoryForm {
  constructor(public name: string, public description: string) {
  }
}
