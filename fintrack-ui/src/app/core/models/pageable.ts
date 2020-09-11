class Pagable {
  constructor(public page, public pageSize) {

  }
}

class PageInfo {
  constructor(public current: number,
              public pages: number,
              public records: number) {
  }
}

class Page<T> {
  constructor(public content: T[],
              public info: PageInfo) {
  }
}

export {
  Pagable,
  Page
}
