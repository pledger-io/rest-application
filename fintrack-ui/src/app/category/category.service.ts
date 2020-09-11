import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Category, CategoryForm} from "./category.models";
import {environment} from "../../environments/environment";
import {Page} from "../core/core-models";

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  constructor(private http: HttpClient) { }

  list(): Promise<Category[]> {
    return this.http.get<Category[]>(environment.backend + 'categories').toPromise();
  }

  getCategories(page: number): Promise<Page<Category>> {
    return this.http.post<Page<Category>>(environment.backend + 'categories', {page: page}).toPromise();
  }

  getCategory(id: number) : Promise<Category> {
    return this.http.get<Category>(environment.backend + 'categories/' + id).toPromise();
  }

  create(category: CategoryForm) : Promise<Category> {
    return this.http.put<Category>(environment.backend + 'categories', category).toPromise();
  }

  update(id: number, category: CategoryForm) : Promise<Category> {
    return this.http.post<Category>(environment.backend + 'categories/' + id, category).toPromise();
  }

  delete(id: number): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      this.http.delete(environment.backend + 'categories/' + id).toPromise()
        .then(() => resolve(true))
        .catch(() => reject())
    });
  }

}
